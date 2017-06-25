package tgBot.actors

import akka.actor.Actor
import com.typesafe.scalalogging.StrictLogging
import tgBot.{SessionInfo, Texts}
import tgBot.akClient.{AkClient, Answer}
import tgBot.tgClient.{ChatId, KeyboardButton, KeyboardMarkup, KeyboardRemove, ResponseMessage, TelegramApi}

import scala.util.{Failure, Success}

class BotActor(
  akClient: AkClient,
  telegramApi: TelegramApi,
  chatId: ChatId
) extends Actor with StrictLogging {

  implicit val ec = context.system.dispatcher

  override def receive: Receive = {
    case x: String if x.toLowerCase.contains("start") =>
      context.become(starting)
      self ! x
    case _ =>
      sendStartButton(Texts.startMessage)
      context.become(starting)
  }

  def starting: Receive = {
    case x: String if x.toLowerCase.contains("start") =>
      akClient.startSession().onComplete {
        case Success(r) =>
          val sessionInfo = SessionInfo(r.parameters.identification.session, r.parameters.identification.signature, r.parameters.step_information.step)
          telegramApi.sendMessage(
            ResponseMessage(
              chatId,
              r.parameters.step_information.question,
              Some(TelegramApi.createKeyboard(r.parameters.step_information.answers))))
          context.become(answering(sessionInfo))
        case Failure(ex) =>
          logger.error("Error when starting new ak session", ex)
          telegramApi.sendMessage(chatId, Texts.errorMessage)
          context.stop(self)
      }
  }

  def answering(sessionInfo: SessionInfo): Receive = {
    case x: String =>
      BotActor.readUserResponse(Answer(x)) match {
        case Some(code) =>
          val nextStepSession = sessionInfo.copy(step = sessionInfo.step.next)
          akClient.sendResponse(code, sessionInfo).onComplete {
            case Success(r) =>
              if (r.parameters.progression > 97.0) { // todo to config
                akClient.getPossibleCharacters(nextStepSession).onComplete {
                  case Success(characters) =>
                    // todo ak can return 0 characters. Need support for it
                    logger.debug(s"Got possible characters: ${characters.parameters.elements}")
                    val character = characters.parameters.elements.head
                    val text = s"Загаданный персонаж - ${character.element.name}, ${character.element.description}"
                    telegramApi.sendMessage(ResponseMessage(chatId, text, Some(KeyboardRemove(true)))).andThen {
                      case Success(_) =>
                        telegramApi.sendPicture(chatId, character.element.absolute_picture_path)
                    }.andThen {
                      case _ => sendStartButton(Texts.restartMessage)
                    }.andThen {
                      case _ =>
                        context.stop(self)
                    }
                  case Failure(ex) => logger.error(s"Unable to load characters list from ak for session $sessionInfo", ex)
                }
              }
              else {
                telegramApi.sendMessage(
                  ResponseMessage(
                    chatId,
                    r.parameters.question,
                    Some(TelegramApi.createKeyboard(r.parameters.answers))))
                context.become(answering(nextStepSession))
              }
            case Failure(ex) =>
              logger.error("Error when sending response to ak api", ex)
              for {
                _ <- telegramApi.sendMessage(chatId, Texts.errorMessage)
                _ <- sendStartButton(Texts.restartMessage)
              } yield context.stop(self)
          }
        case None =>
          telegramApi.sendMessage(chatId, Texts.unableToParseResponse)
      }
  }

  private def sendStartButton(text: String) = {
    val startKeyboardMarkup = KeyboardMarkup(Seq(Seq(KeyboardButton("/start"))))
    telegramApi.sendMessage(ResponseMessage(chatId, text, Some(startKeyboardMarkup)))
  }
}

object BotActor {
  def readUserResponse(answer: Answer): Option[Int] = {
    // todo stupid but fast temporary solution
    answer.answer match {
      case "Да" => Some(0)
      case "Нет" => Some(1)
      case "Я не знаю" => Some(2)
      case "Возможно Частично" => Some(3)
      case "Скорее нет Не совсем" => Some(4)
      case _ => None
    }
  }
}

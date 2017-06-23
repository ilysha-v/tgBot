package tgBot.actors

import akka.actor.Actor
import com.typesafe.scalalogging.StrictLogging
import tgBot.{SessionInfo, Texts}
import tgBot.akClient.{AkClient, Answer, SessionId, Signature, Step}
import tgBot.tgClient.{ChatId, KeyboardRemove, ResponseMessage, TelegramApi}

import scala.util.{Failure, Success}

class BotActor(
  akClient: AkClient,
  telegramApi: TelegramApi,
  chatId: ChatId
) extends Actor with StrictLogging {

  implicit val ec = context.system.dispatcher

  // todo remove var usage
  var sessionInfo = SessionInfo(SessionId(0), Signature(0), Step(0))

  override def receive: Receive = {
    case _ =>
      telegramApi.sendMessage(ResponseMessage(chatId, Texts.startMessage))
      context.become(starting)
  }

  def starting: Receive = {
    case x: String if x.toLowerCase == "start" =>
      akClient.startSession().onComplete {
        case Success(r) =>
          sessionInfo = SessionInfo(r.parameters.identification.session, r.parameters.identification.signature, r.parameters.step_information.step)
          telegramApi.sendMessage(
            ResponseMessage(
              chatId,
              r.parameters.step_information.question,
              Some(TelegramApi.createKeyboard(r.parameters.step_information.answers))))
              context.become(answering)
        case Failure(ex) =>
          logger.error("Error when starting new ak session", ex)
          telegramApi.sendMessage(chatId, Texts.errorMessage)
          context.stop(self)
      }
  }

  def answering: Receive = {
    case x: String =>
      BotActor.readUserResponse(Answer(x)) match {
        case Some(code) =>
          akClient.sendResponse(code, sessionInfo).onComplete {
            case Success(r) =>
              sessionInfo = sessionInfo.copy(step = r.parameters.step)
              if (r.parameters.progression > 97.0) { // todo to config
                akClient.getPossibleCharacters(sessionInfo).onComplete {
                  case Success(characters) =>
                    // todo ak can return 0 characters. Need support for it
                    logger.debug(s"Got possible characters: ${characters.parameters.elements}")
                    val character = characters.parameters.elements.head
                    val text = s"Загаданный персонаж - ${character.element.name}, ${character.element.description}"
                    telegramApi.sendMessage(ResponseMessage(chatId, text, Some(KeyboardRemove(true))))
                    context.stop(self)
                  case Failure(ex) => logger.error(s"Unable to load characters list from ak for session $sessionInfo", ex)
                }
              }
              else {
                telegramApi.sendMessage(
                  ResponseMessage(
                    chatId,
                    r.parameters.question,
                    Some(TelegramApi.createKeyboard(r.parameters.answers))))
              }
            case Failure(ex) =>
              logger.error("Error when sending response to ak api", ex)
              telegramApi.sendMessage(chatId, Texts.errorMessage)
              context.stop(self)
          }
        case None =>
          telegramApi.sendMessage(chatId, Texts.unableToParseResponse)
      }
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

package tgBot.actors

import tgBot.akClient.{AkClient, Answer, SessionId, Step}
import akka.actor.Actor
import com.typesafe.scalalogging.StrictLogging
import tgBot.actors.AkActor.{NewSession, ProcessAnswer}
import tgBot.storage.{SessionInfo, Storage}
import tgBot.tgClient.ChatId

import scala.util.{Failure, Success}

class AkActor(akClient: AkClient, storage: Storage) extends Actor with StrictLogging {

  implicit val ec = context.system.dispatcher
  val telegramActor = context.system.actorSelection("/user/" + TelegramActor.name) // todo find some good solution for it

  override def receive: Receive = {
    case NewSession(chatId) =>
      val response = akClient.startSession()
      response.onComplete {
        case Success(r) =>
          val session = SessionInfo(r.parameters.identification.session, Step(0), r.parameters.identification.signature)
          storage.saveSession(chatId, session)
          telegramActor ! SendQuestion(chatId, r.parameters.step_information.question, r.parameters.step_information.answers)
        case Failure(ex) => logger.error("Error happened when requesting new ak session", ex)
      }
    case ProcessAnswer(chatId, answer, sessionInfo) =>
      AkActor.readUserResponse(answer) match { // todo fold or getOrElse
        case Some(code) =>
          akClient.sendResponse(code, sessionInfo).onComplete {
            case Success(r) =>
              val newSession = sessionInfo.copy(step = sessionInfo.step.next)
              storage.saveSession(chatId, newSession)
              if (r.parameters.progression > 97.0) { // todo 97.0 to config
                akClient.getPossibleCharacters(newSession).onComplete {
                  case Success(characters) =>
                    // todo ak can return 0 characters. Need support for it
                    logger.debug(s"Got possible characters: ${characters.parameters.elements}")
                    storage.removeSession(chatId)
                    telegramActor ! SendPossibleCharacter(chatId, characters.parameters.elements.head.element)
                  case Failure(ex) => logger.error(s"Unable to load characters list from ak for session $newSession", ex)
                }
              }
              else {
                logger.info(s"Got next question for session $newSession. Progress: ${r.parameters.progression}")
                telegramActor ! SendQuestion(chatId, r.parameters.question, r.parameters.answers)
              }
            case Failure(ex) =>
              logger.error(s"Error happened when requesting next question from ak for session: $sessionInfo", ex)
          }
        case None => ??? // todo
      }

  }
}

object AkActor {
  case class NewSession(chatId: ChatId)
  case class ProcessAnswer(chatId: ChatId, answer: Answer, sessionInfo: SessionInfo)

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


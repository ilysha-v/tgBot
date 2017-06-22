package tgBot.actors

import tgBot.akClient.{AkClient, Answer, SessionId, Step}
import akka.actor.Actor
import com.typesafe.scalalogging.StrictLogging
import tgBot.actors.AkActor.{NewSession, ProcessAnswer}
import tgBot.storage.{SessionInfo, Storage}
import tgBot.tgClient.ChatId

import scala.util.{Failure, Success}

class AkActor(akClient: AkClient, storage: Storage) extends Actor with StrictLogging {

  implicit val ex = context.system.dispatcher
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
    case ProcessAnswer(chatId, sessionId, answer) =>
  }
}

object AkActor {
  case class NewSession(chatId: ChatId)
  case class ProcessAnswer(chatId: ChatId, sessionId: SessionId, answer: Answer)

  def responseToCode(answer: Answer): Option[Int] = {
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


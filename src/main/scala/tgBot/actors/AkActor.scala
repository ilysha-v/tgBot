package tgBot.actors

import tgBot.akClient.{AkClient, Answer}
import akka.actor.Actor
import com.typesafe.scalalogging.StrictLogging
import tgBot.actors.AkActor.NewSession
import tgBot.storage.Storage
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
          storage.saveSessionId(chatId, r.parameters.identification.session)
          telegramActor ! SendQuestion(chatId, r.parameters.step_information.question, r.parameters.step_information.answers)
        case Failure(ex) => logger.error("Error happened when requesting new ak session", ex)
      }
  }
}

object AkActor {
  case class NewSession(chatId: ChatId)
  case class ProcessAnswer(chatId: ChatId, answer: Answer)

  def responseToCode(answer: Answer): Int = {
    // todo just stupic but fast temporary solution
    answer.answer match {
      case "Да" => 0
      case "Нет" => 1
      case "Я не знаю" => 2
      case "Возможно Частично" => 3
      case "Скорее нет Не совсем" => 4
    }
  }
}


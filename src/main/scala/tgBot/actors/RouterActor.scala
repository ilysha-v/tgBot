package tgBot.actors

import tgBot.akClient.{AkClient, Answer}
import tgBot.tgClient.{TelegramApi, TelegramUpdate}
import akka.actor.{Actor, Props}
import tgBot.actors.AkActor.{NewSession, ProcessAnswer}
import tgBot.storage.Storage
class RouterActor(storage: Storage, akClient: AkClient, telegramApi: TelegramApi) extends Actor {

  val akActor = context.system.actorOf(Props[AkActor](new AkActor(akClient, storage)))
  val telegramActor = context.system.actorOf(Props[TelegramActor](new TelegramActor(telegramApi)), TelegramActor.name)

  override def receive: Receive = {
    case update: TelegramUpdate =>
      val session = storage.getSession(update.message.chat.id) match {
        case Some(s) =>
          akActor ! ProcessAnswer(update.message.chat.id, s.id, Answer(update.message.text))
        case None => akActor ! NewSession(update.message.chat.id)
      }
  }
}

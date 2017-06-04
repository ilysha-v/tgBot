package tgBot.actors

import tgBot.akClient.AkClient
import tgBot.tgClient.{TelegramApi, TelegramUpdate}
import akka.actor.{Actor, Props}
import tgBot.actors.AkActor.NewSession
import tgBot.storage.Storage
class RouterActor(storage: Storage, akClient: AkClient, telegramApi: TelegramApi) extends Actor {

  val akActor = context.system.actorOf(Props[AkActor](new AkActor(akClient, storage)))
  val telegramActor = context.system.actorOf(Props[TelegramActor](new TelegramActor(telegramApi)), TelegramActor.name)

  override def receive: Receive = {
    case update: TelegramUpdate =>
      val sessionId = storage.getSessionId(update.message.chat.id) match {
        case Some(sessionId) => ???
        case None => akActor ! NewSession(update.message.chat.id)
      }

  }
}

package tgBot.actors

import tgBot.akClient.AkClient
import tgBot.tgClient.{ChatId, TelegramApi, TelegramUpdate}
import akka.actor.{Actor, ActorRef, Props, Terminated}
import com.typesafe.scalalogging.StrictLogging

class RouterActor(akClient: AkClient, telegramApi: TelegramApi) extends Actor with StrictLogging {
  val actorsStorage = new scala.collection.concurrent.TrieMap[ChatId, ActorRef]

  override def receive: Receive = {
    case update: TelegramUpdate =>
      actorsStorage.getOrElse(update.message.chat.id, {
        val actorRef = context.system.actorOf(Props[BotActor](new BotActor(akClient, telegramApi, update.message.chat.id)))
        actorsStorage.put(update.message.chat.id, actorRef)
        context.watch(actorRef)
        actorRef
      }) ! update.message.text
    case Terminated(terminated) =>
      actorsStorage.find(x => x._2 == terminated).map(x => actorsStorage.remove(x._1)) // todo do not use tuples
  }
}


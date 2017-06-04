package tgBot.actors

import akka.actor.Actor
import tgBot.akClient.Answer
import tgBot.tgClient.{ChatId, KeyboardButton, KeyboardMarkup, ResponseMessage, TelegramApi}

class TelegramActor(api: TelegramApi) extends Actor {
  override def receive: Receive = {
    case SendQuestion(chatId, question, answers) =>
      api.sendMessage(ResponseMessage(chatId, question, Some(TelegramActor.createKeyboard(answers))))
  }
}

case object TelegramActor {
  def name = "telegram_actor"

  def createKeyboard(andswers: Seq[Answer]): KeyboardMarkup = {
    KeyboardMarkup(andswers.map(x => List(KeyboardButton(text = x.answer))))
  }
}

case class SendQuestion(chatId: ChatId, question: String, answers: Seq[Answer])

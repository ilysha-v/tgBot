package tgBot.actors

import akka.actor.Actor
import tgBot.akClient.{AkCharacter, Answer}
import tgBot.tgClient.{ChatId, KeyboardButton, KeyboardMarkup, KeyboardRemove, ResponseMessage, TelegramApi}

class TelegramActor(api: TelegramApi) extends Actor {
  override def receive: Receive = {
    case SendQuestion(chatId, question, answers) =>
      api.sendMessage(ResponseMessage(chatId, question, Some(TelegramActor.createKeyboard(answers))))
    case SendPossibleCharacter(chatId, character) =>
      // todo make good text and remove character class usage - in comes from another library
      // todo remove keyboard also
      val text = s"Ваш персонаж - ${character.name}, ${character.description}"
      api.sendMessage(ResponseMessage(chatId, text, Some(KeyboardRemove(true))))
  }
}

case object TelegramActor {
  def name = "telegram_actor"

  def createKeyboard(answers: Seq[Answer]): KeyboardMarkup = {
    KeyboardMarkup(answers.map(x => List(KeyboardButton(text = x.answer))))
  }
}

case class SendQuestion(chatId: ChatId, question: String, answers: Seq[Answer])
case class SendPossibleCharacter(chatId: ChatId, character: AkCharacter)

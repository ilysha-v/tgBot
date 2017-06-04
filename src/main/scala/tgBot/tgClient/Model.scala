package tgBot.tgClient

case class ChatId(value: Int) extends AnyVal

case class KeyboardButton(
  text: String
)

case class KeyboardMarkup(
  keyboard: Seq[Seq[KeyboardButton]]
)

case class TelegramApiResponse[T](
  ok: Boolean,
  result: T
)

case class WebHookInfo(
  url: Option[String],
  pendingUpdates: Int
)

case class SetWebHookPayload(
  url: String
)

case class Sender(
  first_name: String,
  last_name: Option[String],
  username: Option[String],
  language_code: String
)

case class Message(
  message_id: Int,
  from: Sender,
  chat: ChatInfo,
  date: Int, // todo timestamp
  text: String
  // todo entities
)

case class ChatInfo(
  id: ChatId,
  first_name: String,
  last_name: Option[String]
  //todo type
)

case class TelegramUpdate(
  update_id: Int,
  message: Message
)

case class ResponseMessage(
  chat_id: ChatId,
  text: String,
  reply_markup: Option[KeyboardMarkup] = None
)

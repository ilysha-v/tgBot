package TgClient

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
  last_name: String,
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
  id: Int,
  first_name: String,
  last_name: String
  //todo type
)

case class TelegramUpdate(
  update_id: Int,
  message: Message
)

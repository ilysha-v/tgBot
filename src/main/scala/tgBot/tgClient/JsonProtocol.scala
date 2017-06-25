package tgBot.tgClient

import spray.json.DefaultJsonProtocol

trait TelegramJsonProtocol extends DefaultJsonProtocol {
  import spray.json._

  implicit val webHookInfoFormat = new RootJsonFormat[WebHookInfo] {
    override def read(json: JsValue): WebHookInfo = json match {
      case JsObject(map) =>
        val url = map("url") match {
          case JsString(x) if x != "" => Some(x)
          case x => None
        }
        val pendingChanges = map("pending_update_count") match {
          case JsNumber(num) => num.toInt
          case x => ???
        }
        WebHookInfo(url, pendingChanges)
      case x => ???
    }

    override def write(obj: WebHookInfo): JsValue = ???
  }
  implicit def telegramResponse[T: JsonFormat] = jsonFormat2(TelegramApiResponse[T])

  implicit val chatIdFormat = new RootJsonFormat[ChatId] {
    override def write(obj: ChatId): JsValue = obj.value.toJson

    override def read(json: JsValue): ChatId = json match {
      case JsNumber(int) => ChatId(int.toInt)
      case x => ???
    }
  }
  implicit val setWebHookPayloadFormat = jsonFormat1(SetWebHookPayload)
  implicit val senderFormat = jsonFormat4(Sender)
  implicit val chatInfoFormat = jsonFormat3(ChatInfo)
  implicit val messageFormat = jsonFormat5(Message)
  implicit val telegramUpdateFormat = jsonFormat2(TelegramUpdate)

  implicit val keyboardButtonFormat = jsonFormat1(KeyboardButton)
  implicit val keyboardMarkupFormat = jsonFormat1(KeyboardMarkup)
  implicit val keyboardRemoveFormat = jsonFormat1(KeyboardRemove)

  implicit val keyboardPayloadFormat = new RootJsonFormat[KeyboardPayload] {
    override def read(json: JsValue): KeyboardPayload = ???

    override def write(obj: KeyboardPayload): JsValue = obj match {
      case x @ KeyboardMarkup(_) => keyboardMarkupFormat.write(x)
      case x @ KeyboardRemove(_) => keyboardRemoveFormat.write(x)
    }
  }
  implicit val responseMessageFormat = jsonFormat3(ResponseMessage)
  implicit val imageMessageFormat = jsonFormat2(ImageMessage)
}

object TelegramJsonProtocol extends TelegramJsonProtocol
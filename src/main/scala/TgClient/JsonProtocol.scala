package TgClient

import spray.json.{DefaultJsonProtocol, JsNumber, JsObject, JsString, JsValue, JsonFormat, RootJsonFormat}

trait TelegramJsonProtocol extends DefaultJsonProtocol {
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

  implicit val setWebHookPayloadFormat = jsonFormat1(SetWebHookPayload)
  implicit val senderFormat = jsonFormat4(Sender)
  implicit val chatInfoFormat = jsonFormat3(ChatInfo)
  implicit val messageFormat = jsonFormat5(Message)
  implicit val telegramUpdateFormat = jsonFormat2(TelegramUpdate)

  implicit val responseMessageFormat = jsonFormat2(ResponseMessage)
}

object TelegramJsonProtocol extends TelegramJsonProtocol
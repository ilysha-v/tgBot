package AkClient

import spray.json.{DefaultJsonProtocol, JsObject, JsValue, RootJsonFormat, deserializationError}

abstract class JsonProtocol extends DefaultJsonProtocol {
  implicit val identificationFormat = new RootJsonFormat[Identification] {
    override def write(obj: Identification): JsValue = ???

    override def read(json: JsValue): Identification = json match {
      case JsObject(map) =>
        (for {
          channel <- map.get("channel").map(_.convertTo[Int])
          session <- map.get("session").map(_.convertTo[String].toInt)
          signature <- map.get("signature").map(_.convertTo[String].toInt)
        } yield Identification(channel, session, signature)
          ).getOrElse(deserializationError("identification"))
      case _ => deserializationError("identification should be object")
    }
  }
  implicit val answerFormat = jsonFormat1(Answer) // todo looks like they always the same. Maybe enum?
  implicit val stepInformationFormat = jsonFormat6(StepInformation)
  implicit val parametersFormat = jsonFormat2(Parameters)
  implicit val apiResponseFormat = new RootJsonFormat[Response] {
    override def write(obj: Response): JsValue = ???

    override def read(json: JsValue): Response = json match {
      case JsObject(map) =>
        val completionOpt = map.get("completion").map { stringValue =>
          stringValue.convertTo[String] match {
            case "OK" => true
            case _ => false
          }
        }

        (for {
          completion <- completionOpt
          parameters <- map.get("parameters").map(parametersFormat.read)
        } yield Response(completion, parameters)
          ).getOrElse(deserializationError("response"))
      case _ => deserializationError("Response should be object")
    }
  }
}

object AkJsonProtocol extends JsonProtocol
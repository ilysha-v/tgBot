package tgBot.akClient

import spray.json.{DefaultJsonProtocol, JsArray, JsObject, JsString, JsValue, RootJsonFormat, deserializationError}

abstract class JsonProtocol extends DefaultJsonProtocol {
  implicit val sessionIdFormat = jsonFormat1(SessionId)

  implicit val identificationFormat = new RootJsonFormat[Identification] {
    override def write(obj: Identification): JsValue = ???

    override def read(json: JsValue): Identification = json match {
      case JsObject(map) =>
        (for {
          channel <- map.get("channel").map(_.convertTo[Int])
          session <- map.get("session").map(_.convertTo[String].toInt) // todo use format instead
          signature <- map.get("signature").map(_.convertTo[String].toInt)
        } yield Identification(channel, SessionId(session), signature)
          ).getOrElse(deserializationError("identification"))
      case _ => deserializationError("identification should be object")
    }
  }
  implicit val answerFormat = jsonFormat1(Answer) // todo looks like they are always the same. Maybe enum?
  implicit val stepInformationFormat = new RootJsonFormat[StepInformation] {
    override def write(obj: StepInformation): JsValue = ???

    override def read(json: JsValue): StepInformation = json match {
      case JsObject(map) =>
        (for {
          question <- map.get("question").map(_.convertTo[String])
          step <- map.get("step").map {
            case JsString(str) => str.toInt
            case x => deserializationError("step should be a int")
          }
          progress <- map.get("progression").map {
            case JsString(str) => str.toDouble
            case x => deserializationError("progression should be a int")
          }
          questionId <- map.get("questionid").map {
            case JsString(str) => str.toInt
            case x => deserializationError("question id should be a int")
          }
          answers <- map.get("answers").map {
            case x @ JsArray(_) => x.convertTo[Seq[Answer]]
            case x => deserializationError("answers should be an array")
          }
        } yield StepInformation(question, step, progress, questionId, answers)
          ).getOrElse(deserializationError("response"))
      case x => deserializationError("step information should be object")
    }
  }
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
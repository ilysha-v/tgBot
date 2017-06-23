package tgBot.akClient

import spray.json.{DefaultJsonProtocol, JsArray, JsObject, JsString, JsValue, RootJsonFormat, deserializationError}

abstract class JsonProtocol extends DefaultJsonProtocol {
  implicit val sessionIdFormat = jsonFormat1(SessionId)
  implicit val signatureFormat = jsonFormat1(Signature)

  implicit val identificationFormat = new RootJsonFormat[Identification] {
    override def write(obj: Identification): JsValue = ???

    override def read(json: JsValue): Identification = json match {
      case JsObject(map) =>
        (for {
          channel <- map.get("channel").map(_.convertTo[Int])
          session <- map.get("session").map(_.convertTo[String].toInt) // todo use format instead
          signature <- map.get("signature").map(_.convertTo[String].toInt)
        } yield Identification(channel, SessionId(session), Signature(signature))
          ).getOrElse(deserializationError("identification"))
      case _ => deserializationError("identification should be object")
    }
  }
  implicit val answerFormat = jsonFormat1(Answer)
  // todo looks like they are always the same. Maybe enum?
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
            case x@JsArray(_) => x.convertTo[Seq[Answer]]
            case x => deserializationError("answers should be an array")
          }
        } yield StepInformation(question, Step(step), progress, questionId, answers)
          ).getOrElse(deserializationError("response"))
      case x => deserializationError("step information should be object")
    }
  }
  implicit val initialParametersFormat = jsonFormat2(InitialParameters)
  implicit val apiInitialResponseFormat = new RootJsonFormat[InitialResponse] {
    override def write(obj: InitialResponse): JsValue = ???

    override def read(json: JsValue): InitialResponse = json match {
      case JsObject(map) =>
        val completionOpt = map.get("completion").map { stringValue =>
          stringValue.convertTo[String] match {
            case "OK" => true
            case _ => false
          }
        }

        (for {
          completion <- completionOpt
          parameters <- map.get("parameters").map(initialParametersFormat.read)
        } yield InitialResponse(completion, parameters)
          ).getOrElse(deserializationError("response"))
      case _ => deserializationError("Response should be object")
    }
  }

  implicit val akBooleanFormat = new RootJsonFormat[AkBoolean] {
    override def read(json: JsValue): AkBoolean = json match {
      case JsString(strValue) =>
        AkBoolean(strValue == "OK")
      case _ => deserializationError("Unable to read ak boolean (wrapper for completion string values)")
    }

    override def write(obj: AkBoolean): JsValue = ???
  }
  implicit val responseJsonProtocol = jsonFormat2(Response)

  implicit val characterFormat = new RootJsonFormat[AkCharacter] {
    override def read(json: JsValue): AkCharacter = json match {
      case JsObject(dict) =>
        (for {
          // todo should rename properties because we use manual deserialization
          id <- dict.get("id").map(_.convertTo[String].toInt)
          name <- dict.get("name").map(_.convertTo[String])
          id_base <- dict.get("id_base").map(_.convertTo[String].toInt)
          proba <- dict.get("proba").map(_.convertTo[String].toDouble)
          description <- dict.get("description").map(_.convertTo[String])
          ranking <- dict.get("ranking").map(_.convertTo[String].toInt)
          pseudo = dict.get("pseudo").flatMap { x =>
            val stringVal = x.convertTo[String]
            if (stringVal == "none") None
            else Some(stringVal)
          }
          picture <- dict.get("absolute_picture_path").map(_.convertTo[String])
        } yield AkCharacter(id, name, id_base, proba, description, ranking, pseudo, picture)
          ).getOrElse(deserializationError("Unable to read character info"))
      case _ => deserializationError("Character should be an object")
    }

    override def write(obj: AkCharacter): JsValue = ???
  }

  implicit val elementFormat = jsonFormat1(Element)
  implicit val charactersParametersFormat = jsonFormat1(CharactersParameters)
  implicit val charactersResponseProtocol = jsonFormat2(CharactersResponse)
}

object AkJsonProtocol extends JsonProtocol
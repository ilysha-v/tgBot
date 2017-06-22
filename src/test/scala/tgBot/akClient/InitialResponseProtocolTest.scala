package tgBot.akClient

import org.scalatest.FreeSpec

/**
  * Created by ilysha on 30/05/2017.
  */
class InitialResponseProtocolTest extends FreeSpec {
  val resp = """{"completion":"OK","parameters":{"identification":{"channel":0,"session":"1702","signature":"125046769"},"step_information":{"question":"\u0412\u0430\u0448 \u043f\u0435\u0440\u0441\u043e\u043d\u0430\u0436 \u0441\u0443\u0449\u0435\u0441\u0442\u0432\u043e\u0432\u0430\u043b \u0432 \u0440\u0435\u0430\u043b\u044c\u043d\u043e\u0441\u0442\u0438?","answers":[{"answer":"\u0414\u0430"},{"answer":"\u041d\u0435\u0442"},{"answer":"\u042f \u043d\u0435 \u0437\u043d\u0430\u044e"},{"answer":"\u0412\u043e\u0437\u043c\u043e\u0436\u043d\u043e \u0427\u0430\u0441\u0442\u0438\u0447\u043d\u043e"},{"answer":"\u0421\u043a\u043e\u0440\u0435\u0435 \u043d\u0435\u0442 \u041d\u0435 \u0441\u043e\u0432\u0441\u0435\u043c"}],"step":"0","progression":"0.00000","questionid":"800","infogain":"8.96831e-44"}}}"""

  "Test response should be deserialized" in {
    import AkJsonProtocol._
    import spray.json._

    val response = resp.parseJson.convertTo[InitialResponse]
  }
}

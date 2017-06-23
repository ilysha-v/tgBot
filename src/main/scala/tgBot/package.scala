import tgBot.akClient.{SessionId, Signature, Step}

/**
  * Created by ilysha on 23/06/2017.
  */
package object tgBot {
  case class SessionInfo(id: SessionId, signature: Signature, step: Step)
}

package tgBot.akClient

case class SessionId(value: Int)
case class Step(value: Int) {
  def next: Step = {
    Step(value + 1)
  }
}
case class Signature(value: Int)

case class Identification(channel: Int, session: SessionId, signature: Signature)
case class Answer(answer: String) // todo anyval, and add shapeless format for anyvals

case class StepInformation(
  question: String,
  step: Step,
  progression: Double,
  questionid: Int,
  answers: Seq[Answer]
)

case class InitialResponse(completion: Boolean, parameters: InitialParameters)
case class InitialParameters(identification: Identification, step_information: StepInformation)

case class AkBoolean(value: Boolean)
case class Response(completion: AkBoolean, parameters: StepInformation)

case class Character(id: Int)
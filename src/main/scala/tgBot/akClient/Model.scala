package tgBot.akClient

case class SessionId(value: Int)
case class Step(value: Int)
case class Signature(value: Int)

case class Identification(channel: Int, session: SessionId, signature: Signature)
case class Answer(answer: String) // todo anyval, and add shapeless format for anyvals

case class StepInformation(
  question: String,
  step: Int,
  progression: Double,
  questionid: Int,
  answers: Seq[Answer]
)

case class InitialResponse(completion: Boolean, parameters: InitialParameters)
case class InitialParameters(identification: Identification, step_information: StepInformation)

case class Response(completion: Boolean, parameters: Parameters)
case class Parameters(step_information: StepInformation)
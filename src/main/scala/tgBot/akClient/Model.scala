package tgBot.akClient

case class SessionId(value: Int)

case class Response(completion: Boolean, parameters: Parameters)
case class Identification(channel: Int, session: SessionId, signature: Int)
case class Answer(answer: String) // todo anyval, and add shapeless format for anyvals

case class StepInformation(
  question: String,
  step: Int,
  progression: Double,
  questionid: Int,
  answers: Seq[Answer]
)

case class Parameters(identification: Identification, step_information: StepInformation)
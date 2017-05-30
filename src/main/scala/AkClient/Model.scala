package AkClient

case class Response(completion: Boolean, parameters: Parameters)

case class Identification(channel: Int, session: Int, signature: Int)
case class Answer(answer: String)

case class StepInformation(
  question: String,
  step: Int,
  progression: Double,
  questionid: Int,
  infogain: Double,
  answers: Seq[Answer]
)

case class Parameters(identification: Identification, step_information: StepInformation)
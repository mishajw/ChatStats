package chatstats

import scala.collection.mutable.ArrayBuffer

object ChatStats {

  val seekingGroup = "nicholas artrayo alex hoagy connie tayo charlie sam sophie emma jasmine wilfrid" split " "

  val allMessages = ArrayBuffer[Message]()

  def main(args: Array[String]) {
    val messageCollector = new MessageCollector("src/main/resources/messages_xml.htm", messageHandler)

    messageCollector.run()

    val counts =
      allMessages
        .groupBy({ m => m.header.name })
        .map( { case (sender, ms) => (sender, ms.length) }).toSeq
        .sortBy(-_._2)

    println(counts.mkString("\n"))
  }

  def messageHandler(message: Message): Unit = {
    if (seekingGroup forall message.threadMembers.toSeq.contains) {
      allMessages += message
    }
  }
}

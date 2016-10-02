package chatstats

import scala.collection.mutable.ArrayBuffer

object ChatStats {

  val seekingGroup = "nicholas artrayo alex hoagy connie tayo charlie sam sophie emma jasmine wilfrid" split " "
  val path: String = "src/main/resources/messages_xml.htm"

  def main(args: Array[String]): Unit = {
    val messages = loadMessages()

    perMemberCounts(messages)
//    mostPopularWords(messages)
    mostCapitals(messages)
  }

  def loadMessages() = {
    val allMessages = ArrayBuffer[Message]()

    def messageHandler(message: Message): Unit = {
      if (seekingGroup forall message.threadMembers.toSeq.contains) {
        allMessages += message
      }
    }

    val messageCollector = new MessageCollector(path, messageHandler)

    messageCollector.run()

    allMessages
  }

  def perMemberCounts(messages: Seq[Message]) {
    println("Messages per person:")

    messages
        .groupBy({ m => m.header.name })
        .map( { case (sender, ms) => (sender, ms.length) }).toSeq
        .sortBy(-_._2)
        .foreach { case (sender, count) =>
            val percentage: Double = (count.toDouble / messages.size.toDouble) * 100
          println(f"$sender: $count ($percentage%.2f%%)")
        }
  }

  def mostPopularWords(messages: ArrayBuffer[Message]): Unit = {
    messages.flatMap(_.contents.toLowerCase().split("[^A-Za-z0-9']+"))
        .groupBy(identity)
        .map({ case (w, ws) => (w, ws.length) }).toSeq
        .sortBy(-_._2)
        .take(20)
        .foreach(println)
  }

  def mostCapitals(messages: Seq[Message]): Unit = {
    println("Percentage of capitals in messages:")
    messages.groupBy(_.header.name)
        .map( { case (name, ms) =>
          val allText: Seq[Char] = ms.flatMap(_.contents)
          (name, allText.count(Character.isUpperCase).toDouble / allText.size.toDouble)
        }).toSeq
        .sortBy(-_._2)
        .foreach { case (name, perc) =>
          println(f"$name: ${perc * 100}%.2f%%")
        }
  }
}

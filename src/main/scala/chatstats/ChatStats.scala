package chatstats

import java.io.PrintWriter
import java.util.Calendar

import scala.collection.mutable.ArrayBuffer

object ChatStats {

  val seekingGroup = "nicholas artrayo alex hoagy connie tayo charlie sam sophie emma jasmine wilfrid" split " "
  val path: String = "src/main/resources/messages_xml.htm"

  def main(args: Array[String]): Unit = {
    val messages = loadMessages()

    perMemberCounts(messages)
    mostCapitals(messages)
    wordOutliers(messages)
    dayFrequencies(messages)
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
      .reverse
      .sortBy(_.header.time)
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

  def dayFrequencies(messages: Seq[Message]): Unit = {
    messages
      .map(_.header.time.get(Calendar.DAY_OF_WEEK))
      .groupBy(identity)
      .map({ case (d, ds) => (d, ds.length) })
      .foreach(println)
  }

  def wordOutliers(messages: Seq[Message]): Unit = {
    val allFreqs = getWordFrequencies(messages)

    def getOutliers(memberMessages: Seq[Message]) = {
      getWordFrequencies(memberMessages)
          .map({ case (w, p) =>
            (w, { if (allFreqs contains w) Math.abs(allFreqs(w) - p) else p})
          }).toSeq
          .sortBy(-_._2)
          .map(_._1)
          .take(5)
    }

    messages
        .groupBy(_.header.name)
        .map({ case (sender, ms) => (sender, getOutliers(ms)) })
        .foreach(println)
  }

  def getWordFrequencies(messages: Seq[Message]): Map[String, Double] = {
    val words = getAllWords(messages)

    words
      .groupBy(identity)
      .flatMap({ case (w, ws) =>
        if (ws.length > 10)
          Some(w, ws.length.toDouble / words.length.toDouble)
        else
          None
      })
  }

  def getAllWords(messages: Seq[Message]): Seq[String] = {
    messages.flatMap(_.contents.toLowerCase().split("[^A-Za-z0-9']+"))
  }

  def prettyPrint(messages: Seq[Message]): Unit = {
    val messagesHtml = messages.map( m => {
      val formattedName = m.header.name match {
        case "Sam Judd" => "SJu"
        case "Sam James Johnstone" => "SJo"
        case other =>
          new String(other.split(" ").map(_.head))
      }
      s"""
         |<span class="sender">$formattedName</span><span class="colon">:</span>
         |<span class="message">
         |  ${m.contents}
         |</span>
       """.stripMargin
    })
        .mkString("\n")

    val fullHtml =
      s"""
         |<html>
         |  <head>
         |    <link rel="stylesheet" type="text/css" href="style.css">
         |  </head>
         |  <body>
         |    <div id="container">
         |      $messagesHtml
         |    </div>
         |  </body>
         |</html>
       """.stripMargin

    new PrintWriter("poster/index.html") { write(fullHtml) ; close() }
  }
}

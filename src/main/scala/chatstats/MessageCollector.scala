package chatstats

import java.util.Calendar
import javax.xml.parsers.SAXParserFactory

import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler

class MessageCollector(
    messagesFilePath: String,
    messageHandler: (Message) => Unit) {

  sealed abstract class State
  case class Starting() extends State

  case class StartingThread() extends State
  case class InsideThread(members: Iterable[String]) extends State

  case class InsideMessage() extends State
  case class InsideMessageText(header: InsideMessageHeader, var text: String = "") extends State

  case class InsideMessageHeader(var sender: String = "", var time: String = "") extends State
  case class InsideMessageHeaderSender(header: InsideMessageHeader) extends State
  case class InsideMessageHeaderTime(header: InsideMessageHeader) extends State

  var currentState: State = Starting()
  var currentThread: Option[InsideThread] = None

  val handler = new DefaultHandler {
    override def startElement(uri: String, localName: String, elem: String, attributes: Attributes): Unit = {
      val clazz: String = attributes.getValue("class")

      (clazz, elem, currentState) match {
        case ("thread", _, _) =>
          currentState = StartingThread()
        case ("message", _, thread: InsideThread) =>
          currentState = InsideMessage()
        case ("message", _, text: InsideMessageText) =>
          currentState = InsideMessage()
        case ("message_header", _, message: InsideMessage) =>
          currentState = InsideMessageHeader()
        case ("user", _, header: InsideMessageHeader) =>
          currentState = InsideMessageHeaderSender(header)
        case ("meta", _, sender: InsideMessageHeaderSender) =>
          currentState = InsideMessageHeaderTime(sender.header)
        case (_, "p", time: InsideMessageHeaderTime) =>
          currentState = InsideMessageText(time.header)
        case (c, e, s) =>
          println(s"Unrecognised combination: class $c, elem $e, state $s")
      }
    }

    override def characters(ch: Array[Char], start: Int, length: Int): Unit = {
      val currentText = new String(ch, start, length)

      currentState match {
        case StartingThread() => handleThread(currentText)
        case sender: InsideMessageHeaderSender =>
          sender.header.sender = currentText
        case time: InsideMessageHeaderTime =>
          time.header.time = currentText
        case text: InsideMessageText =>
          text.text = currentText
          handleMessage(text)
        case _ =>
      }
    }
  }

  def handleThread(currentText: String) {
    val members = currentText.split("[\n,]")
      .map(_.trim)
      .flatMap(_.split(" ").headOption)
      .map(_.toLowerCase())

    currentState = InsideThread(members)
    currentThread = Some(InsideThread(members))
  }

  def handleMessage(text: InsideMessageText): Unit = {
    messageHandler(
      Message(
        currentThread.get.members,
        text.text,
        Header(
          text.header.sender,
          parseDate(text.header.time))))
  }

  def run() {
    val factory = SAXParserFactory.newInstance()
    val parser = factory.newSAXParser()
    parser.parse(messagesFilePath, handler)
  }

  def parseDate(dateString: String): Calendar = {
    val cal = Calendar.getInstance()

    dateString.split(" ") match {
      case Array(dayOfWeek, date, month, year, _, time, _) =>
        cal.set(Calendar.DATE, date.toInt)
        cal.set(Calendar.MONTH, months.indexOf(month))
        cal.set(Calendar.YEAR, year.toInt)
        cal.set(Calendar.HOUR, time.split(":")(0).toInt)
        cal.set(Calendar.MINUTE, time.split(":")(1).toInt)
      case _ => println(s"Couldn't recognise date: $dateString")
    }

    cal
  }
}

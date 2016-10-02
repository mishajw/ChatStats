package chatstats

import javax.xml.parsers.SAXParserFactory

import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler

object ChatStats {
  val messagesFilePath = "src/main/resources/messages.htm"

  val factory = SAXParserFactory.newInstance()
  val parser = factory.newSAXParser()

  val handler = new DefaultHandler {
    override def startElement(uri: String, localName: String, qName: String, attributes: Attributes): Unit = {
      println(uri)
      println(localName)
      println(qName)
      println(attributes)
    }
  }

  def main(args: Array[String]) {
    parser.parse(messagesFilePath, handler)
  }
}

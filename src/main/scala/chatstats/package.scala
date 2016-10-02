package object chatstats {
  case class Header(name: String, time: String)
  case class Message(threadMembers: Iterable[String], contents: String, header: Header)
}

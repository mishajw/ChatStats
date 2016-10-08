import java.util.Calendar

package object chatstats {
  case class Header(name: String, time: Calendar)
  case class Message(threadMembers: Iterable[String], contents: String, header: Header)

  val daysOfWeek = Seq("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
  val months = Seq(
    "January", "February", "March",
    "April", "May", "June", "July",
    "August", "September", "October",
    "November", "December")
}

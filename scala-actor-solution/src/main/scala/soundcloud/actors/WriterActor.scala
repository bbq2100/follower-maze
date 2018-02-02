package soundcloud.actors

import java.io.PrintWriter

import soundcloud.actors.MessageDispatcherActor.RawMessage
import soundcloud.actors.WriterActor.{NewLine, Write}

case class WriterActor(out: PrintWriter) extends Actor {

  override protected[this] def handleMessage = {
    case Write(message) =>
      out.print(message)
      out.print(NewLine)
      out.flush()
  }

  override protected[this] def onShutdown() = {
    out.close()
    super.onShutdown()
  }
}

object WriterActor {
  val NewLine = "\r\n"
  case class Write(rawMessage: RawMessage)

}
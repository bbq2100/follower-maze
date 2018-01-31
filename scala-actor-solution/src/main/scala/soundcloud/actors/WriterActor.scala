package soundcloud.actors

import java.io.PrintWriter

import soundcloud.actors.MessageDispatcherActor.RawMessage
import soundcloud.actors.WriterActor.Write

case class WriterActor(out: PrintWriter) extends Actor {
  val NewLine = "\r\n"

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

  case class Write(rawMessage: RawMessage)

}
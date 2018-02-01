package soundcloud.actors

import java.util.concurrent.LinkedBlockingQueue

import soundcloud.actors.Actor.`R.I.P`

import scala.annotation.tailrec

trait Actor extends Runnable {

  private[this] val messageBox = new LinkedBlockingQueue[Any]

  protected def handleMessage: PartialFunction[Any, Any]

  final def ! : Any => Unit = {
    case `R.I.P` => onShutdown() /* Preemptively checking whether to stop the processing because otherwise the Actor could be stuck in an endless-loop */
    case other => messageBox.add(other)
  }

  protected def onShutdown(): Unit = println(s"Actor $this stopped")

  override def run(): Unit = {
    @tailrec
    def loop(): Unit = messageBox.take() match {
      case msg if handleMessage.isDefinedAt(msg) =>
        handleMessage(msg)
        loop();
      case other =>
        println(s"Unhandled message $other @$this")
        loop()
    }

    loop()
  }
}

object Actor {

  case object `R.I.P`

}

package soundcloud.actors

import java.util.concurrent.LinkedBlockingQueue

import soundcloud.actors.Actor.`R.I.P`

import scala.annotation.tailrec

trait Actor extends Runnable {

  private val messageBox = new LinkedBlockingQueue[Any]

  protected def handleMessage: PartialFunction[Any, Any]

  final def ! : Any => Unit = messageBox.add

  protected def onShutdown(): Unit = println(s"Actor $this stopped")

  final def self = this

  override def run(): Unit = {
    @tailrec
    def loop(): Unit = messageBox.take() match {
      case msg if handleMessage.isDefinedAt(msg) =>
        handleMessage(msg)
        loop();
      case `R.I.P` =>
        onShutdown()
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

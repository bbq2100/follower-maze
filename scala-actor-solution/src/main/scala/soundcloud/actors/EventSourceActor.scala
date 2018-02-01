package soundcloud.actors

import java.net.ServerSocket

import soundcloud.actors.CommonMessages.StartSocketServer
import soundcloud.actors.MessageDispatcherActor._

import scala.collection.mutable
import scala.io.BufferedSource
import scala.language.postfixOps

case class EventSourceActor(dispatcherActor: Actor, port: Int) extends Actor {
  var eventSocket: ServerSocket = _

  override protected def handleMessage = {
    case StartSocketServer =>
      println(s"Waiting for event messages on port $port")

      eventSocket = new ServerSocket(port)
      var messageCounter = 1
      val eventQueue = mutable.PriorityQueue()(EventQueueOrdering).reverse
      val messages = new BufferedSource(eventSocket.accept().getInputStream).getLines()
      val hasAdjacentMessages = () => eventQueue.nonEmpty && eventQueue.head.seqId == messageCounter

      messages
        .flatMap(EventMessage.apply)
        .foreach { event =>
          eventQueue += event
          while (hasAdjacentMessages()) {
            dispatcherActor ! eventQueue.dequeue()
            messageCounter += 1
          }
        }
  }

  override protected[this] def onShutdown() = {
    eventSocket.close()
    super.onShutdown()
  }
}

package soundcloud.actors

import java.net.ServerSocket

import soundcloud.actors.Common.StartSocketServer
import soundcloud.actors.MessageDispatcherActor._

import scala.collection.mutable
import scala.io.BufferedSource
import scala.language.postfixOps

case class EventSourceActor(dispatcherActor: Actor, port: Int)(implicit actorSystem: ActorSystem) extends Actor {
  var eventSocket: ServerSocket = _

  override protected def handleMessage = {
    case StartSocketServer =>
      println(s"Waiting for event messages on port $port")

      val receiveMessages = new Runnable {
        override def run() = {
          var MessageCounter = 1
          eventSocket = new ServerSocket(port)
          val messageQueue = mutable.PriorityQueue()(EventQueueOrdering).reverse
          val messages = new BufferedSource(eventSocket.accept().getInputStream).getLines()
          messages
            .flatMap(EventMessage.apply)
            .foreach { event =>
              messageQueue += event
              messageQueue.headOption
                .filter(_.seqId == MessageCounter)
                .foreach(_ => {
                  val message = messageQueue.dequeue()
                  dispatcherActor ! message
                  MessageCounter += 1
                })
            }
        }
      }

      actorSystem.execute(receiveMessages)
  }

  override protected[this] def onShutdown() = {
    eventSocket.close()
    super.onShutdown()
  }
}

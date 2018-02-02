package soundcloud

import java.io.PrintWriter
import java.net.Socket

import org.scalatest.concurrent.Eventually
import soundcloud.EventMockActor.Start
import soundcloud.actors.CommonMessages.StartSocketServer
import soundcloud.actors.MessageDispatcherActor.NewClientConnection
import soundcloud.actors.WriterActor.NewLine
import soundcloud.actors.{ActorSystem, ClientsActor}

class ClientsActorTest extends UnitSpec {
  implicit val actorSystem = new ActorSystem

  "ClientsActor" should "receive messages and emit NewClientConnection events" in {
    val serverPort = 1337
    val clientId = 42
    val dispatcherMockActor = TestActor()
    actorSystem.materialize(dispatcherMockActor)
    val clientsActor = actorSystem.materialize(ClientsActor(dispatcherMockActor, serverPort))
    val eventMockActor = actorSystem.materialize(EventMockActor(serverPort))

    clientsActor ! StartSocketServer
    eventMockActor ! Start
    eventMockActor ! clientId.toString

    Eventually.eventually {
      assert(dispatcherMockActor.MessageBox.poll().asInstanceOf[NewClientConnection].id == clientId)
    }
  }
}

case class EventMockActor(serverPort: Int) extends TestActor {
  var socket: Socket = _
  var writer: PrintWriter = _

  override def testHandleMessage = {
    case Start =>
      socket = new Socket("localhost", serverPort)
      val out = socket.getOutputStream
      writer = ClientsActor.printWriter(out)
    case msg: String =>
      writer.write(msg)
      writer.write(NewLine)
      writer.flush()
  }

  override def onTestShutdown() = socket.close()
}

object EventMockActor {

  case object Start

}
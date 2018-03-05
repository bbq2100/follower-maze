package soundcloud.actors

import java.io.PrintWriter

import org.scalatest.concurrent.Eventually
import soundcloud.UnitSpec
import soundcloud.actors.MessageDispatcherActor.NewClientConnection

class MessageDispatcherActorTest extends UnitSpec {
  implicit val actorSystem = new ActorSystem

  "MessageDispatcher" should "populate clientConnections" in {
    val clientId = 42
    val messageDispatcher = new MessageDispatcherActor
    actorSystem.materialize(messageDispatcher)

    val printWriter = new PrintWriter(System.out)
    messageDispatcher ! NewClientConnection(clientId, printWriter)

    Eventually.eventually {
      assert(messageDispatcher.clientConnections.get(clientId).isDefined)
    }
  }

}


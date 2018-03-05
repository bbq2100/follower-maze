package soundcloud.actors

import java.io.PrintWriter

import org.scalatest.concurrent.Eventually
import org.scalatest.mockito.MockitoSugar.mock
import soundcloud.UnitSpec
import soundcloud.actors.MessageDispatcherActor.{Follow, NewClientConnection}

class MessageDispatcherActorTest extends UnitSpec {
  implicit val actorSystem = new ActorSystem

  "MessageDispatcher" should "populate the clientConnections map" in {
    val clientId = 42
    val messageDispatcher = new MessageDispatcherActor
    actorSystem.materialize(messageDispatcher)

    val printWriter = new PrintWriter(System.out)
    messageDispatcher ! NewClientConnection(clientId, printWriter)

    Eventually.eventually {
      assert(messageDispatcher.clientConnections.get(clientId).isDefined)
    }

  }

  it should "allow to follow clients when a Follow request is made" in {
    val clientFrom = 42
    val clientTo = 142
    val messageDispatcher = new MessageDispatcherActor
    actorSystem.materialize(messageDispatcher)

    val printWriter = mock[PrintWriter]
    val expectedMessage = "lol"

    messageDispatcher ! NewClientConnection(clientFrom, printWriter)
    messageDispatcher ! NewClientConnection(clientTo, printWriter)
    messageDispatcher ! Follow(999, expectedMessage, clientFrom, clientTo)

    Eventually.eventually {
      // TODO: fix the mockito mock setup
//      verify(printWriter).write(expectedMessage)
      assert(messageDispatcher.followers(clientTo).contains(clientFrom))
    }

  }

}


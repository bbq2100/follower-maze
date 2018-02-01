package soundcloud

import java.util.concurrent.LinkedBlockingQueue

import soundcloud.actors.Actor.`R.I.P`
import soundcloud.actors.{Actor, ActorSystem}

class ActorTest extends UnitSpec {
  implicit val actorSystem = new ActorSystem

  "An actor" should "live" in {
    val actor = TestActor()
    actor ! "Hello there!"
    assert(actor.MessageBox.take() == "Hello there!")
  }

  it should "terminate when R.I.P message is sent" in {
    var isInvoked = false
    val actor = TestActor(_ => isInvoked = true)
    actor ! `R.I.P`
    assert(isInvoked)
  }

}

class TestActor(shutdownCallBack: (Actor) => Unit = TestActor.emptyCallback) extends Actor {
  val MessageBox = new LinkedBlockingQueue[Any]

  override protected def handleMessage = {
    case msg => MessageBox.put(msg)
  }

  override protected def onShutdown() = {
    shutdownCallBack(this)
    super.onShutdown()
  }
}

object TestActor {
  val emptyCallback: Actor => Unit = _ => ()

  def apply(shutdownCallBack: => (Actor) => Unit = TestActor.emptyCallback)
           (implicit actorSystem: ActorSystem): TestActor = {
    val actor = new TestActor(shutdownCallBack)
    Function.const(actor)(actorSystem.materialize(actor))
  }
}
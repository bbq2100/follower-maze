package soundcloud

import java.util.concurrent.LinkedBlockingQueue

import soundcloud.actors.Actor.`R.I.P`
import soundcloud.actors.{Actor, ActorSystem}

class ActorTest extends UnitSpec {
  implicit val actorSystem = new ActorSystem

  "An actor" should "live" in {
    val actor = TestActor()
    actorSystem.materialize(actor)
    actor ! "Hello there!"
    assert(actor.MessageBox.take() == "Hello there!")
  }

  it should "terminate when a R.I.P message is sent" in {
    var isInvoked = false
    val actor = TestActor(_ => isInvoked = true)
    actor ! `R.I.P`
    assert(isInvoked)
  }

}

abstract class TestActor(shutdownCallBack: (Actor) => Unit = TestActor.emptyCallback) extends Actor {
  val MessageBox = new LinkedBlockingQueue[Any]

  final override protected def handleMessage = {
    case msg => MessageBox.put(msg); testHandleMessage(msg)
  }

  def testHandleMessage: Any => Any

  final override protected def onShutdown() = {
    shutdownCallBack(this)
    onTestShutdown()
    super.onShutdown()
  }

  def onTestShutdown(): Unit = ()
}

object TestActor {
  val emptyCallback: Actor => Unit = _ => ()

  def apply(shutdownCallBack: (Actor) => Unit = TestActor.emptyCallback): TestActor = new TestActor(shutdownCallBack) {
    override def testHandleMessage = identity
  }
}
package soundcloud

import soundcloud.actors.Actor.`R.I.P`
import soundcloud.actors.Common.StartSocketServer
import soundcloud.actors._

object Application {
  def main(args: Array[String]): Unit = {
    println("follower-maze server started \\O/")

    implicit val actorSystem = new ActorSystem
    val dispatcherActor = actorSystem.materialize(new MessageDispatcherActor)
    val clientsActor = actorSystem.materialize(ClientsActor(dispatcherActor, 9099))
    val eventSourceActor = actorSystem.materialize(EventSourceActor(dispatcherActor, 9090))

    clientsActor ! StartSocketServer
    eventSourceActor ! StartSocketServer

    println("Press enter to terminate")
    System.in.read()

    eventSourceActor ! `R.I.P`
    clientsActor ! `R.I.P`
    dispatcherActor ! `R.I.P`
    actorSystem.shutdown()

    println("follower-maze server exited \\O/")
  }
}

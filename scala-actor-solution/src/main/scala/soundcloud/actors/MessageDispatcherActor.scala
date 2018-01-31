package soundcloud.actors

import java.io.PrintWriter

import soundcloud.actors.Actor.`R.I.P`
import soundcloud.actors.MessageDispatcherActor._
import soundcloud.actors.WriterActor.Write

import scala.collection.mutable

case class MessageDispatcherActor()(implicit system: ActorSystem) extends Actor {
  val clientConnections = mutable.Map[UserId, Actor]()
  val followers = mutable.Map[UserId, mutable.Set[UserId]]()
  val NewLine = "\r\n"

  override protected def handleMessage = {
    case NewClientConnection(id, out) =>
      clientConnections.put(id, system.materialize(WriterActor(out)))

    case Follow(_, rawMessage, from, to) =>
      followers.getOrElseUpdate(to, mutable.Set()) += from
      clientConnections.get(to).foreach(_ ! Write(rawMessage))

    case Unfollow(_, _, from, to) =>
      followers.get(to).map(f => f -= from)

    case Broadcast(_, rawMessage) =>
      clientConnections.values.foreach(_ ! Write(rawMessage))

    case PrivateMessage(_, rawMessage, _, to) =>
      clientConnections.get(to).foreach(_ ! Write(rawMessage))

    case StatusUpdate(_, rawMessage, from) =>
      followers.get(from).foreach(_.foreach(clientConnections.get(_).foreach(_ ! Write(rawMessage))))
  }

  override protected def onShutdown() = {
    clientConnections.values.foreach(_ ! `R.I.P`)
    super.onShutdown()
  }

}

object MessageDispatcherActor {
  val EventQueueOrdering: Ordering[EventMessage] = Ordering.by(_.seqId)

  case object StartRouter

  type UserId = Int

  type SeqID = Int

  type RawMessage = String

  sealed trait EventMessage {
    val seqId: SeqID
    val originalMessage: RawMessage
  }

  case class Follow(seqId: SeqID, originalMessage: RawMessage, from: UserId, to: UserId) extends EventMessage

  case class Unfollow(seqId: SeqID, originalMessage: RawMessage, from: UserId, to: UserId) extends EventMessage

  case class Broadcast(seqId: SeqID, originalMessage: RawMessage) extends EventMessage

  case class PrivateMessage(seqId: SeqID, originalMessage: RawMessage, from: UserId, to: UserId) extends EventMessage

  case class StatusUpdate(seqId: SeqID, originalMessage: RawMessage, from: UserId) extends EventMessage

  case class NewClientConnection(id: Int, outputStream: PrintWriter)

  object EventMessage {

    def apply(msg: String): Option[EventMessage] = msg.split('|') match {
      case Array(AsInt(seq), "F", AsInt(from), AsInt(to)) => Some(Follow(seq, msg, from, to))
      case Array(AsInt(seq), "U", AsInt(from), AsInt(to)) => Some(Unfollow(seq, msg, from, to))
      case Array(AsInt(seq), "B") => Some(Broadcast(seq, msg))
      case Array(AsInt(seq), "P", AsInt(from), AsInt(to)) => Some(PrivateMessage(seq, msg, from, to))
      case Array(AsInt(seq), "S", AsInt(from)) => Some(StatusUpdate(seq, msg, from))
      case _ => None
    }

    object AsInt {
      def unapply(str: String): Option[Int] = try {
        Some(str.toInt)
      } catch {
        case _: NumberFormatException => None
      }
    }

  }

}
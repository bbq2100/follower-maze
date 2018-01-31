package soundcloud.actors

import java.io._
import java.net.ServerSocket
import java.nio.charset.Charset

import soundcloud.actors.Common.StartSocketServer
import soundcloud.actors.MessageDispatcherActor.NewClientConnection

case class ClientsActor(dispatcherActor: Actor, port: Int) extends Actor {
  var clientSocket: ServerSocket = _

  override protected[this] def handleMessage = {
    case StartSocketServer =>
      println(s"Waiting for connecting clients on port $port")
      clientSocket = new ServerSocket(port)

      while (!Thread.interrupted()) {
        val socket = clientSocket.accept()
        val userId = new BufferedReader(new InputStreamReader(socket.getInputStream))
          .readLine()
          .toInt
        dispatcherActor ! NewClientConnection(userId, printWriter(socket.getOutputStream))
      }
  }

  private def printWriter: OutputStream => PrintWriter = out =>
    new PrintWriter(new BufferedWriter(
      new OutputStreamWriter(out, Charset.forName("UTF-8"))))

  override protected[this] def onShutdown() = {
    clientSocket.close()
    super.onShutdown()
  }
}

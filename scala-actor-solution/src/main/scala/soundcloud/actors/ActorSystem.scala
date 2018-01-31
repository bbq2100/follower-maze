package soundcloud.actors

import java.util.concurrent.Executors

class ActorSystem {
  private val threadPool = Executors.newCachedThreadPool()

  def execute(runnable: Runnable) = threadPool.execute(runnable)

  def materialize(a: Actor): Actor = Function.const(a)(threadPool.execute(a))

  def shutdown(): Unit = threadPool.shutdownNow()
}

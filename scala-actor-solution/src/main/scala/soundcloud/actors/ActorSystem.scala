package soundcloud.actors

import java.util.concurrent.Executors

class ActorSystem {
  def execute(runnable: Runnable) = threadPool.execute(runnable)

  private val threadPool = Executors.newCachedThreadPool()

  def materialize(a: Actor): Actor = {
    threadPool.execute(a)
    a
  }

  def shutdown(): Unit = threadPool.shutdownNow()
}

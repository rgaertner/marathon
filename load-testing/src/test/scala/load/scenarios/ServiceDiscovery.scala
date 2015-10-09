package load.scenarios

import io.gatling.core.Predef._
import load.actions.{ Tasks, Queue, Deployments, Apps }
import scala.concurrent.duration._

/**
  * Actions performed by service discovery software.
  */
object ServiceDiscovery {
  def viaTasks = {
    scenario("Query Tasks")
      .forever {
        pace(10.seconds)
          .exec(Tasks.index)
      }
  }

}

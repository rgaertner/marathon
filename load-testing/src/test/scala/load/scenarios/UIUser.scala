package load.scenarios

import io.gatling.core.Predef._
import load.actions.{ Queue, Deployments, Apps }

import scala.concurrent.duration._

/**
  * Requests that correspond to actions in the UI.
  */
object UIUser {
  /**
    * Perform the requests equivalent to a user opening the UIUser index.
    *
    * (static resources excluded)
    */
  def index = {
    scenario("UIUser Index")
      .forever {
        pace(5.seconds)
          .exec(Apps.index)
          .exec(Deployments.index)
          .exec(Queue.index)
      }
  }
}

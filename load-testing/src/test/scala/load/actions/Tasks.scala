package load.actions

import io.gatling.core.Predef._
import io.gatling.http.Predef._
//import scala.concurrent.duration._

object Tasks {
  private[this] val baseUrl = "/v2/tasks"

  def index = exec {
    http(baseUrl)
      .get(baseUrl)
      .check(status.is(200))
  }
}

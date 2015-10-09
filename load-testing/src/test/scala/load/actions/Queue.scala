package load.actions

import io.gatling.core.Predef._
import io.gatling.http.Predef._
//import scala.concurrent.duration._

object Queue {
  private[this] val baseUrl = "/v2/queue"

  def index = exec {
    http(baseUrl)
      .get(baseUrl)
      .check(status.is(200))
  }
}

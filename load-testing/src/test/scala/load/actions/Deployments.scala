package load.actions

import io.gatling.core.Predef._
import io.gatling.http.Predef._
//import scala.concurrent.duration._

object Deployments {
  private[this] val baseUrl = "/v2/deployments"

  def index = exec {
    http(baseUrl)
      .get(baseUrl)
      .check(status.is(200))
  }
}

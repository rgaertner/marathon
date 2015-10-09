package load.actions

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import org.slf4j.LoggerFactory

//import scala.concurrent.duration._

object Deployments {
  val LastDeploymentId = "deploymentId"

  private[this] val baseUrl = "/v2/deployments"
  private[this] val log = LoggerFactory.getLogger(getClass)

  def index = exec {
    http(baseUrl)
      .get(baseUrl)
      .check(status.is(200))
  }

  def revertLastDeployment = exec {
    http("revert last deployment")
      .delete(lastDeploymentUrl)
      .check(status.is(200))
  }

  private[this] def lastDeploymentUrl(session: Session) = {
    val url = s"$baseUrl/${session(LastDeploymentId).as[String]}"
    log.warn(s"revert url: $url")
    url
  }

}

package load

import io.gatling.core.Predef._
import load.scenarios.{ AppConfigurator, ServiceDiscovery, UIUser }

// 2
import io.gatling.http.Predef._
import scala.concurrent.duration._

class MarathonSimulation extends Simulation {
  val httpConf = http
    .baseURL("http://localhost:8080")
    .acceptHeader("application/json")
    .doNotTrackHeader("1")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .acceptEncodingHeader("gzip, deflate")
    .userAgentHeader("Mozilla/5.0 (Windows NT 5.1; rv:31.0) Gecko/20100101 Firefox/31.0")

  val totalDuration = 60.seconds

  setUp(
    UIUser.index.inject(rampUsers(20).over(10.seconds)),
    ServiceDiscovery.viaTasks.inject(rampUsers(3).over(10.seconds)),
    AppConfigurator.runApp.inject(constantUsersPerSec(1).during(totalDuration-10.seconds)),
    AppConfigurator.deployAndRevert.inject(constantUsersPerSec(1).during(totalDuration-10.seconds))
  ).protocols(httpConf).maxDuration(totalDuration)
}

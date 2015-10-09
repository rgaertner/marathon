package load.scenarios

import io.gatling.core.Predef._
import load.actions.{ Queue, Deployments, Apps }

import scala.concurrent.duration._
import scala.util.Random

/**
  * Actions performed by a person deploying apps or stopping apps.
  */
object AppConfigurator {
  val appNameFeeder = Iterator.continually(Map("appName" -> ("/test/" + Random.alphanumeric.take(20).mkString.toLowerCase())))

  def runApp = scenario("run app")
    .exec {
      feed(appNameFeeder)
        .exec(Apps.createApp())
        .forever(pause(1.second))
    }
}

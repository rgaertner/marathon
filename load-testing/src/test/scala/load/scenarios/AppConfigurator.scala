package load.scenarios

import io.gatling.core.Predef._
import load.actions.{ Queue, Deployments, Apps }

import scala.concurrent.duration._
import scala.util.Random

/**
  * Actions performed by a person deploying apps or stopping apps.
  */
object AppConfigurator {
  def appNameFeeder(prefix: String) =
    Iterator.continually(Map("appName" -> (prefix + Random.alphanumeric.take(20).mkString.toLowerCase())))

  def runApp = scenario("run app forever")
    .exec {
      feed(appNameFeeder("/test"))
        .exec(Apps.createApp())
        .forever(pause(1.second))
    }

  def deployAndRevert = scenario("deploy and revert")
    .exec {
      feed(appNameFeeder("/failing"))
        .exec(
          Apps.createUndeployableApp(),
          pause(5.second),
          Deployments.revertLastDeployment
        )
    }
}

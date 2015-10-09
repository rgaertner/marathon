package load.actions

import io.gatling.core.Predef._
import io.gatling.core.json.JSON
import io.gatling.core.structure.ChainBuilder
import io.gatling.core.validation.{ Validation, Success }
import io.gatling.http.Predef._
import org.slf4j.LoggerFactory

//import scala.concurrent.duration._

object Apps {
  private[this] val log = LoggerFactory.getLogger(getClass)
  private[this] val baseUrl = "/v2/apps"

  def index = exec {
    http(baseUrl)
      .get(baseUrl)
      .check(status.is(200))
  }

  def createApp(): ChainBuilder = exec {
    def appDefinition(session: Session): Validation[String] = Success {
      val ret =
        s"""
        |{
        |       "id": "${session("appName").as[String]}",
        |       "env": {
        |         "ELASTICSEARCH_SERVICE_PORT": "9200",
        |         "MON_GROUP": "ssre-sd-de",
        |         "MON_APP": "ssre-sd-de-logstash-rabbitmq:17",
        |         "ELASTICSEARCH_IP": "elasticsearch-ssre-sd-de.mon-marathon-service.mesos",
        |         "RABBITMQ_SERVICE_PORT": "5672",
        |         "MON_CONTACT": "SDN-Dev-Team@one.verizon.com",
        |         "ELASTICSEARCH_HOST": "elasticsearch-ssre-sd-de.marathon.mesos",
        |         "OPSPORTAL_WORKFLOW_STATUS_CALLBACK_AUTH": "",
        |         "RABBITMQ_IP": "rabbitmq-ssre-sd-de.mon-marathon-service.mesos",
        |         "RABBITMQ_HOST": "rabbitmq-ssre-sd-de.marathon.mesos",
        |         "DOMAIN_SUFFIX": "-ssre-sd-de.mon-marathon-service.mesos",
        |         "ETCD_HOST": "10.100.33.1",
        |         "OPSPORTAL_WORKFLOW_STATUS_CALLBACK_URL": ""
        |       },
        |       "instances": 0,
        |       "cpus": 0.01,
        |       "mem": 1,
        |       "disk": 0,
        |       "uris": [
        |         "file:///.dockercfg"
        |       ],
        |       "ports": [ 0, 0 ],
        |       "container": {
        |         "type": "DOCKER",
        |         "docker": {
        |           "image": "thomasr/sleep",
        |           "network": "BRIDGE",
        |           "portMappings": [
        |             {
        |               "containerPort": 22,
        |               "hostPort": 0,
        |               "servicePort": 0,
        |               "protocol": "tcp"
        |             },
        |             {
        |               "containerPort": 12346,
        |               "hostPort": 0,
        |               "servicePort": 0,
        |               "protocol": "tcp"
        |             }
        |           ],
        |           "privileged": false,
        |           "parameters": [],
        |           "forcePullImage": false
        |         }
        |       },
        |       "dependencies": ${JSON.stringify(session("appDependencies").asOption[Seq[String]].getOrElse(Seq.empty))},
        |       "upgradeStrategy": {
        |         "minimumHealthCapacity": 1,
        |         "maximumOverCapacity": 1
        |       }
        |}
      """.stripMargin
//      log.warn("app:\n{}", ret)
      ret
    }

    http("create app")
      .put(appUrl)
      .body(StringBody(appDefinition(_)))
      .check(status.is(201))
  }

  def deleteApp(name: String) = exec {
    http("delete app")
      .delete(appUrl)
      .check(status.is(200))
  }

  private[this] def appUrl(session: Session) = s"$baseUrl${session("appName").as[String]}"
}
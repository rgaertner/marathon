package mesosphere.marathon.core.task.bus

import scala.concurrent.Future

trait TaskStatusEmitter {
  def publish(status: TaskStatusObservables.TaskStatusUpdate): Future[Unit]
}

package mesosphere.marathon.core.task.bus.impl

import mesosphere.marathon.core.task.bus.TaskStatusEmitter
import mesosphere.marathon.core.task.bus.TaskStatusObservables.TaskStatusUpdate

import scala.concurrent.Future

object DummyTaskStatusEmitter {
  def apply(): TaskStatusEmitter = {
    new TaskStatusEmitter {
      override def publish(status: TaskStatusUpdate): Future[Unit] = Future.successful(())
    }
  }
}

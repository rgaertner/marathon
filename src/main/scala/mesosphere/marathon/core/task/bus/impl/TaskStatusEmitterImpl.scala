package mesosphere.marathon.core.task.bus.impl

import mesosphere.marathon.core.task.bus.TaskStatusEmitter
import mesosphere.marathon.core.task.bus.TaskStatusObservables.TaskStatusUpdate
import org.slf4j.LoggerFactory

import scala.concurrent.Future

private[bus] class TaskStatusEmitterImpl(
  taskStatusLegacyEmitter: TaskStatusEmitter,
  internalTaskStatusEventStream: InternalTaskStatusEventStream)
    extends TaskStatusEmitter {
  private[this] val log = LoggerFactory.getLogger(getClass)

  import scala.concurrent.ExecutionContext.Implicits.global

  override def publish(status: TaskStatusUpdate): Future[Unit] = {
    taskStatusLegacyEmitter.publish(status).map { _ =>
      log.debug("publishing update {}", status)
      internalTaskStatusEventStream.publish(status)
    }
  }
}

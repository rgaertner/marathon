package mesosphere.marathon.core.task.bus.impl

import javax.inject.{ Inject, Named }

import akka.event.EventStream
import mesosphere.marathon.MarathonSchedulerDriverHolder
import mesosphere.marathon.Protos.MarathonTask
import mesosphere.marathon.core.task.bus.MarathonTaskStatus.WithMesosStatus
import mesosphere.marathon.core.task.bus.TaskStatusObservables.TaskStatusUpdate
import mesosphere.marathon.core.task.bus.{ TaskStatusEmitter, TaskStatusObservables }
import mesosphere.marathon.event.{ EventModule, MesosStatusUpdateEvent }
import mesosphere.marathon.health.HealthCheckManager
import mesosphere.marathon.state.{ PathId, Timestamp }
import mesosphere.marathon.tasks.{ TaskIdUtil, TaskTracker }
import org.apache.mesos.Protos.{ TaskID, TaskStatus }
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.util.control.NonFatal

/**
  * Processes task status update events, mostly to update the task tracker.
  *
  * The task tracker has to be updated BEFORE the event is published!
  */
class TaskStatusLegacyEmitter @Inject() (
  @Named(EventModule.busName) eventBus: EventStream,
  taskIdUtil: TaskIdUtil,
  healthCheckManager: HealthCheckManager,
  taskTracker: TaskTracker,
  marathonSchedulerDriverHolder: MarathonSchedulerDriverHolder)
    extends TaskStatusEmitter {

  private[this] val log = LoggerFactory.getLogger(getClass)

  import scala.concurrent.ExecutionContext.Implicits.global

  def publish(status: TaskStatusObservables.TaskStatusUpdate): Future[Unit] = status match {
    case TaskStatusUpdate(timestamp, taskId, WithMesosStatus(status)) =>
      val appId = taskIdUtil.appId(taskId)

      val maybeTask = taskTracker.fetchTask(appId, taskId.getValue)

      val eventuallyProcessed: Future[Unit] = updateTaskTrackerAndEmitEvent(status, appId, taskId, maybeTask)

      eventuallyProcessed.map { _ =>
        // forward health changes to the health check manager
        for (marathonTask <- maybeTask)
          healthCheckManager.update(status, Timestamp(marathonTask.getVersion))

        driverOpt.foreach(_.acknowledgeStatusUpdate(status))
      }
  }

  //scalastyle:off cyclomatic.complexity
  private[this] def updateTaskTrackerAndEmitEvent(
    status: TaskStatus, appId: PathId, taskId: TaskID, maybeTask: Option[MarathonTask]): Future[Unit] =
    {
      import org.apache.mesos.Protos.TaskState._
      status.getState match {
        case TASK_ERROR | TASK_FAILED | TASK_FINISHED | TASK_KILLED | TASK_LOST =>
          // Remove from our internal list
          taskTracker.terminated(appId, taskId.getValue).map {
            case Some(task) => postEvent(status, task)
            case None       => log.warn(s"Task not found. Do not post event for '{}'", taskId.getValue)
          }

        case TASK_RUNNING if !maybeTask.exists(_.hasStartedAt) => // staged, not running
          taskTracker.running(appId, status)
            .map { task => postEvent(status, task) }
            .recover {
              case NonFatal(t) =>
                log.warn(s"Task could not be saved. Do not post event for '${taskId.getValue}'", t)
                driverOpt.foreach(_.killTask(status.getTaskId))
            }

        case TASK_STAGING if !taskTracker.contains(appId) =>
          log.warn(s"Received status update for unknown app $appId, killing task ${status.getTaskId}")
          driverOpt.foreach(_.killTask(status.getTaskId))
          Future.successful(())

        case _ =>
          taskTracker.statusUpdate(appId, status).map {
            case None =>
              log.warn(s"Killing task ${status.getTaskId}")
              driverOpt.foreach(_.killTask(status.getTaskId))
            case _ =>
          }
      }
    }

  private[this] def driverOpt = marathonSchedulerDriverHolder.driver

  private[this] def postEvent(status: TaskStatus, task: MarathonTask): Unit = {
    log.info("Sending event notification.")
    import scala.collection.JavaConverters._
    eventBus.publish(
      MesosStatusUpdateEvent(
        status.getSlaveId.getValue,
        status.getTaskId.getValue,
        status.getState.name,
        if (status.hasMessage) status.getMessage else "",
        taskIdUtil.appId(task.getId),
        task.getHost,
        task.getPortsList.asScala,
        task.getVersion
      )
    )
  }

}

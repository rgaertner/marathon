package mesosphere.marathon.core.task.tracker.impl

import akka.event.EventStream
import mesosphere.marathon.Protos.MarathonTask
import mesosphere.marathon.core.task.bus.TaskStatusUpdateTestHelper
import mesosphere.marathon.core.task.bus.impl.TaskStatusLegacyEmitter
import mesosphere.marathon.event.MesosStatusUpdateEvent
import mesosphere.marathon.health.HealthCheckManager
import mesosphere.marathon.state.{ PathId, Timestamp }
import mesosphere.marathon.tasks.{ TaskIdUtil, TaskTracker }
import mesosphere.marathon.{ MarathonSchedulerDriverHolder, MarathonSpec, MarathonTestHelper }
import org.apache.mesos.Protos.TaskStatus
import org.apache.mesos.SchedulerDriver
import org.mockito.{ ArgumentCaptor, Mockito }

import scala.concurrent.Future

class TaskStatusLegacyEmitterTest extends MarathonSpec {

  for (
    update <- Seq(
      TaskStatusUpdateTestHelper.finished,
      TaskStatusUpdateTestHelper.lost,
      TaskStatusUpdateTestHelper.killed,
      TaskStatusUpdateTestHelper.error
    ).map(_.withAppId(appId.toString))
  ) {
    test(s"Remove terminated task (${update.wrapped.status.getClass.getSimpleName})") {

      Mockito.when(taskTracker.fetchTask(appId, update.wrapped.taskId.getValue))
        .thenReturn(Some(marathonTask))
      Mockito.when(taskTracker.terminated(appId, update.wrapped.taskId.getValue))
        .thenReturn(Future.successful(Some(marathonTask)))

      taskStatusLegacyEmitter.publish(update.wrapped)

      Mockito.verify(taskTracker).fetchTask(appId, update.wrapped.taskId.getValue)
      val status: TaskStatus = update.wrapped.status.mesosStatus.get
      Mockito.verify(healthCheckManager).update(status, version)
      Mockito.verify(taskTracker).terminated(appId, update.wrapped.taskId.getValue)
      Mockito.verify(schedulerDriver).acknowledgeStatusUpdate(status)

      val eventCaptor = ArgumentCaptor.forClass(classOf[MesosStatusUpdateEvent])
      Mockito.verify(eventBus).publish(eventCaptor.capture())
      assert(eventCaptor.getValue != null)
      assert(eventCaptor.getValue.appId == appId)
    }
  }

  private[this] lazy val appId = PathId("/app")
  private[this] lazy val version = Timestamp.now()
  private[this] lazy val task = MarathonTestHelper.makeOneCPUTask(TaskIdUtil.newTaskId(appId).getValue).build()
  private[this] lazy val marathonTask =
    MarathonTask.newBuilder().setId(task.getTaskId.getValue).setVersion(version.toString).build()

  private[this] var eventBus: EventStream = _
  private[this] var taskIdUtil: TaskIdUtil = _
  private[this] var healthCheckManager: HealthCheckManager = _
  private[this] var taskTracker: TaskTracker = _
  private[this] var schedulerDriver: SchedulerDriver = _
  private[this] var marathonSchedulerDriverHolder: MarathonSchedulerDriverHolder = _
  private[this] var taskStatusLegacyEmitter: TaskStatusLegacyEmitter = _

  before {
    eventBus = mock[EventStream]
    taskIdUtil = TaskIdUtil
    healthCheckManager = mock[HealthCheckManager]
    taskTracker = mock[TaskTracker]
    schedulerDriver = mock[SchedulerDriver]
    marathonSchedulerDriverHolder = new MarathonSchedulerDriverHolder
    marathonSchedulerDriverHolder.driver = Some(schedulerDriver)

    taskStatusLegacyEmitter = new TaskStatusLegacyEmitter(
      eventBus,
      taskIdUtil,
      healthCheckManager,
      taskTracker,
      marathonSchedulerDriverHolder
    )
  }

  after {
    Mockito.verifyNoMoreInteractions(eventBus)
    Mockito.verifyNoMoreInteractions(healthCheckManager)
    Mockito.verifyNoMoreInteractions(taskTracker)
    Mockito.verifyNoMoreInteractions(schedulerDriver)
  }
}

package mesosphere.marathon.core

import com.google.inject.name.Names
import com.google.inject.{ AbstractModule, Provides, Scopes, Singleton }
import mesosphere.marathon.core.appinfo.{ AppInfoModule, AppInfoService }
import mesosphere.marathon.core.base.Clock
import mesosphere.marathon.core.launcher.OfferProcessor
import mesosphere.marathon.core.launchqueue.LaunchQueue
import mesosphere.marathon.core.leadership.{ LeadershipCoordinator, LeadershipModule }
import mesosphere.marathon.core.task.bus.impl.TaskStatusLegacyEmitter
import mesosphere.marathon.core.task.bus.{ TaskStatusEmitter, TaskStatusObservables }
import mesosphere.marathon.core.task.tracker.TaskTrackerModule

/**
  * Provides the glue between guice and the core modules.
  */
class CoreGuiceModule extends AbstractModule {

  // Export classes used outside of core to guice
  @Provides @Singleton
  def leadershipModule(coreModule: CoreModule): LeadershipModule = coreModule.leadershipModule

  @Provides @Singleton
  def leadershipCoordinator(
    leadershipModule: LeadershipModule,
    launchQueue: LaunchQueue): LeadershipCoordinator =
    leadershipModule.coordinator()

  @Provides @Singleton
  def offerProcessor(coreModule: CoreModule): OfferProcessor = coreModule.launcherModule.offerProcessor

  @Provides @Singleton
  def taskStatusEmitter(coreModule: CoreModule): TaskStatusEmitter = coreModule.taskBusModule.taskStatusEmitter

  @Provides @Singleton
  def taskStatusObservable(coreModule: CoreModule): TaskStatusObservables =
    coreModule.taskBusModule.taskStatusObservables

  @Provides @Singleton
  def taskTrackerModule(coreModule: CoreModule): TaskTrackerModule =
    coreModule.taskTrackerModule

  @Provides @Singleton
  final def taskQueue(coreModule: CoreModule): LaunchQueue = coreModule.appOfferMatcherModule.taskQueue

  @Provides @Singleton
  final def appInfoService(appInfoModule: AppInfoModule): AppInfoService = appInfoModule.appInfoService

  override def configure(): Unit = {
    bind(classOf[Clock]).toInstance(Clock())
    bind(classOf[CoreModule]).to(classOf[CoreModuleImpl]).in(Scopes.SINGLETON)
    bind(classOf[TaskStatusEmitter])
      .annotatedWith(Names.named(CoreGuiceModule.NAMED_TASK_STATUS_LEGACY_EMITTER))
      .to(classOf[TaskStatusLegacyEmitter])
      .asEagerSingleton()

    bind(classOf[AppInfoModule]).asEagerSingleton()
  }
}

object CoreGuiceModule {
  final val NAMED_TASK_STATUS_LEGACY_EMITTER = "taskStatusLegacyEmitter"
}

package helpers

import javax.inject._

import models.{ProjectDAO, TaskDAO, UserMetricsDAO}
import play.api.i18n.Messages

import scala.concurrent.ExecutionContext

@Singleton
class UserRegistration @Inject() (projectDAO: ProjectDAO, taskDAO: TaskDAO, userMetricsDAO: UserMetricsDAO)
                                 (implicit ec: ExecutionContext) {
  def performRegistrationSetup(userId: Int)(implicit messages: Messages) = {
    projectDAO.createDefaultProject(userId).map(taskDAO.createGreetingTasks(userId, _))
    userMetricsDAO.setSuretyCardsCount(userId, 3)
  }
}

package controllers

import json.implicits.formats.DateJsonFormat
import org.joda.time.DateTime
import play.api.libs.json.{JsArray, Json}
import play.api.test.Helpers._
import test.{BaseSpec, FutureTest}

class TaskControllerSpec  extends BaseSpec with AuthActionBehaviors with FutureTest {
  "Task controller" should {
    "GET by id" should {
      val controller = app.injector.instanceOf[TaskController]
      behave like authAction(controller.get(1))
      behave like filterOnlyObjectOwnerAllowed(controller.get(1), controller.get(2))

      "Response with json task" in {
        val getMethod = controller.get(1).apply(fakeRequestWithRightAuthHeaders)
        status(getMethod) mustBe OK
        val jsonResponse = contentAsJson(getMethod)
        (jsonResponse \ "id").as[Long] mustBe 1
        (jsonResponse \ "title").as[String] mustBe "First test task"
      }
    }

    "GET the latest tasks" should {
      val controller = app.injector.instanceOf[TaskController]
      behave like authAction(controller.getLatestTasks())

      "Response with array json of tasks if start_with query parameter is omitted" in {
        val getLatestTasksMethod = controller.getLatestTasks().apply(fakeRequestWithRightAuthHeaders)

        status(getLatestTasksMethod) mustBe OK
        val jsonContent = contentAsJson(getLatestTasksMethod)
        jsonContent mustBe a [JsArray]
      }

      "Response with array json of tasks if there is start_with query parameter " in {
        val getLatestTasksMethod = controller.getLatestTasks(3).apply(fakeRequestWithRightAuthHeaders)

        status(getLatestTasksMethod) mustBe OK
        val jsonContent = contentAsJson(getLatestTasksMethod)
        jsonContent mustBe a [JsArray]
        val tasksJsArray = jsonContent.asInstanceOf[JsArray]
        tasksJsArray.value.size mustBe 1
        tasksJsArray.value.head.\("id").as[Int] mustBe 1
      }
    }


    "POST new task" should {
      val controller = app.injector.instanceOf[TaskController]
      behave like authAction(controller.create())

      "Create new task" in {
        val newTaskJson = Json.obj(
          "title" -> "NewTask",
          "projectId" -> 1,
          "description" -> "new Desc",
          "deadline" -> DateTime.now().toString(DateJsonFormat.format),
          "importance" -> 1,
          "complexity" -> 6,
          "data" -> Json.obj("tags" -> Json.arr(Seq("tag1", "tag2")))
        )

        val createMethod = controller.create().apply(fakeRequestWithRightAuthHeaders.withJsonBody(newTaskJson))

        status(createMethod) mustBe OK

        val jsonResponse = contentAsJson(createMethod)
        (jsonResponse \ "id").asOpt[Long].isDefined mustBe true
        (jsonResponse \ "title") mustBe (newTaskJson \ "title")
        (jsonResponse \ "projectId") mustBe (newTaskJson \ "projectId")
        (jsonResponse \ "description") mustBe (newTaskJson \ "description")
        (jsonResponse \ "deadline") mustBe (newTaskJson \ "deadline")
        (jsonResponse \ "importance") mustBe (newTaskJson \ "importance")
        (jsonResponse \ "complexity") mustBe (newTaskJson \ "complexity")
        (jsonResponse \ "data") mustBe (newTaskJson \ "data")
      }

      "Deny creation if projectId nonOnwer" in {
        val newTaskJson = Json.obj(
          "title" -> "NewTask",
          "projectId" -> 2,
          "description" -> "new Desc",
          "deadline" -> DateTime.now().toString(DateJsonFormat.format),
          "importance" -> 1,
          "complexity" -> 6,
          "data" -> Json.obj("tags" -> Json.arr(Seq("tag1", "tag2")))
        )

        val createMethod = controller.create().apply(fakeRequestWithRightAuthHeaders.withJsonBody(newTaskJson))

        status(createMethod) mustBe FORBIDDEN

        val jsonResponse = contentAsJson(createMethod)
        (jsonResponse \ "data" \ "projectId").as[String] mustBe "nonOwner"
      }
    }


    "PUT existing task" should {
      val controller = app.injector.instanceOf[TaskController]

      behave like authAction(controller.update(3))
      behave like filterOnlyObjectOwnerAllowed(controller.update(1), controller.update(2))

      "Update task" in {
        val newTaskJson = Json.obj(
          "title" -> "Updated NewTask",
          "projectId" -> 1,
          "description" -> "Updated new Desc",
          "deadline" -> DateTime.now().toString(DateJsonFormat.format),
          "importance" -> 3,
          "complexity" -> 7,
          "data" -> Json.obj("tags" -> Json.arr(Seq("tag3", "tag4")))
        )

        val updateMethod = controller.update(3).apply(fakeRequestWithRightAuthHeaders.withJsonBody(newTaskJson))

        status(updateMethod) mustBe OK

        val jsonResponse = contentAsJson(updateMethod)
        (jsonResponse \ "id").as[Long] mustBe 3
        (jsonResponse \ "title") mustBe (newTaskJson \ "title")
        (jsonResponse \ "projectId") mustBe (newTaskJson \ "projectId")
        (jsonResponse \ "description") mustBe (newTaskJson \ "description")
        (jsonResponse \ "deadline") mustBe (newTaskJson \ "deadline")
        (jsonResponse \ "importance") mustBe (newTaskJson \ "importance")
        (jsonResponse \ "complexity") mustBe (newTaskJson \ "complexity")
        (jsonResponse \ "data") mustBe (newTaskJson \ "data")
      }

      "Deny updating task with nonOwn projectId" in {
        val newTaskJson = Json.obj(
          "title" -> "Updated NewTask",
          "projectId" -> 2,
          "description" -> "Updated new Desc",
          "deadline" -> DateTime.now().toString(DateJsonFormat.format),
          "importance" -> 3,
          "complexity" -> 7,
          "data" -> Json.obj("tags" -> Json.arr(Seq("tag3", "tag4")))
        )

        val updateMethod = controller.update(3).apply(fakeRequestWithRightAuthHeaders.withJsonBody(newTaskJson))

        status(updateMethod) mustBe FORBIDDEN

        (contentAsJson(updateMethod) \ "data" \ "projectId").as[String] mustBe "nonOwner"
      }
    }

    "DELETE existing task" should {
      val controller = app.injector.instanceOf[TaskController]

      behave like authAction(controller.delete(1))
      behave like filterOnlyObjectOwnerAllowed(controller.delete(3), controller.delete(2))

      "Delete Task" in {
        val newTaskJson = Json.obj(
          "title" -> "NewTask",
          "projectId" -> 1,
          "description" -> "new Desc",
          "deadline" -> DateTime.now().toString(DateJsonFormat.format),
          "importance" -> 1,
          "complexity" -> 6,
          "data" -> Json.obj("tags" -> Json.arr(Seq("tag1", "tag2")))
        )

        val createMethod = controller.create().apply(fakeRequestWithRightAuthHeaders.withJsonBody(newTaskJson))

        status(createMethod) mustBe OK

        val jsonResponse = contentAsJson(createMethod)
        val newTaskId = (jsonResponse \ "id").as[Long]

        val deleteMethod = controller.delete(newTaskId).apply(fakeRequestWithRightAuthHeaders)
        status(deleteMethod) mustBe NO_CONTENT
        contentAsString(deleteMethod) mustBe ""
      }
    }
  }
}

package controllers

import org.joda.time.DateTime
import play.api.libs.json.{JsArray, Json}
import play.api.test.Helpers._
import test._
import json.implicits.formats.DateJsonFormat._

class ProjectControllerSpec extends BaseSpec with AuthActionBehaviors {

  "ProjectController" should {

    val controller = app.injector.instanceOf[ProjectController]

    "GET project" should {
      behave like authAction(controller.get(1))
      behave like filterOnlyObjectOwnerAllowed(controller.get(1), controller.get(2))

      "Response with project details" in {
        val getMethod = controller.get(1).apply(fakeRequestWithRightAuthHeaders)

        status(getMethod) mustBe OK
        val jsonResponse = contentAsJson(getMethod)
        (jsonResponse \ "id").as[Long] mustBe 1
        (jsonResponse \ "title").asOpt[String].isDefined mustBe true
      }
    }

    "GET latest projects" should {
      behave like authAction(controller.getLatestProjects())

      "Response with array of projects" in {
        val getLatestProjectsMethod = controller.getLatestProjects().apply(fakeRequestWithRightAuthHeaders)
        status(getLatestProjectsMethod) mustBe OK
        contentAsJson(getLatestProjectsMethod) mustBe a [JsArray]
      }
    }

    "POST" should {
      behave like authAction(controller.create())

      "Create a new project" in {
        val jsonRequest = Json.obj(
          "title" -> "A new project",
          "description" -> "Proj desc",
          "isArchived" -> false,
          "color" -> "ff0000"
        )

        val createMethod = controller.create().apply(fakeRequestWithRightAuthHeaders.withJsonBody(jsonRequest))
        status(createMethod) mustBe OK

        val jsonResponse = contentAsJson(createMethod)

        (jsonResponse \ "id").asOpt[Long].isDefined mustBe true
        (jsonResponse \ "createdAt").asOpt[DateTime].isDefined mustBe true
        (jsonResponse \ "updatedAt").asOpt[DateTime].isDefined mustBe true
        (jsonResponse \ "title") mustBe (jsonRequest \ "title")
        (jsonResponse \ "description") mustBe (jsonRequest \ "description")
        (jsonResponse \ "isArchived") mustBe (jsonRequest \ "isArchived")
        (jsonResponse \ "color") mustBe (jsonRequest \ "color")
      }
    }

    "PUT" should {
      val jsonRequest = Json.obj(
        "title" -> "An updated  project",
        "description" -> "Project update desc",
        "isArchived" -> false,
        "color" -> "ffff00"
      )

      behave like authAction(controller.update(1))
      behave like filterOnlyObjectOwnerAllowed(controller.update(1), controller.update(2), Some(jsonRequest))

      "Update a project" in {

        val createMethod = controller.update(1).apply(fakeRequestWithRightAuthHeaders.withJsonBody(jsonRequest))
        status(createMethod) mustBe OK

        val jsonResponse = contentAsJson(createMethod)

        (jsonResponse \ "id").asOpt[Long].isDefined mustBe true
        (jsonResponse \ "createdAt").asOpt[DateTime].isDefined mustBe true
        (jsonResponse \ "updatedAt").asOpt[DateTime].isDefined mustBe true
        (jsonResponse \ "title") mustBe (jsonRequest \ "title")
        (jsonResponse \ "description") mustBe (jsonRequest \ "description")
        (jsonResponse \ "isArchived") mustBe (jsonRequest \ "isArchived")
        (jsonResponse \ "color") mustBe (jsonRequest \ "color")
      }
    }

    "DELETE" should {
      behave like authAction(controller.delete(1))
      behave like filterOnlyObjectOwnerAllowed(controller.delete(3), controller.delete(2))

      "Delete own project" in {
        val jsonRequest = Json.obj(
          "title" -> "A new project",
          "description" -> "Proj desc",
          "isArchived" -> false,
          "color" -> "ff0000"
        )

        val createMethod = controller.create().apply(fakeRequestWithRightAuthHeaders.withJsonBody(jsonRequest))
        val deleteMethod = controller.delete((contentAsJson(createMethod) \ "id").as[Long]).apply(fakeRequestWithRightAuthHeaders)

        status(deleteMethod) mustBe NO_CONTENT
      }
    }

  }

}

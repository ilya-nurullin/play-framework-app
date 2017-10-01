package helpers

import play.api.data.{FieldMapping, FormError, Mapping}
import play.api.data.format.Formatter

object CustomFormMapping {
  val requiredBoolean: Mapping[Boolean] = of[Boolean]

  private implicit def requiredBooleanFormat: Formatter[Boolean] = new Formatter[Boolean] {

    override val format = Some(("format.boolean", Nil))

    def bind(key: String, data: Map[String, String]) = {
      Right(data.getOrElse(key, "required")).right.flatMap {
        case "true" => Right(true)
        case "false" => Right(false)
        case "required" => Left(Seq(FormError(key, "error.required", Nil)))
        case _ => Left(Seq(FormError(key, "error.boolean", Nil)))
      }
    }

    def unbind(key: String, value: Boolean) = Map(key -> value.toString)
  }

  private def of[T](implicit binder: Formatter[T]): FieldMapping[T] = FieldMapping[T]()(binder)
}

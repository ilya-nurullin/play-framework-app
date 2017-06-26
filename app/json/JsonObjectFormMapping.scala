package json

object JsonObjectFormMapping {
  /*val jsonMapping: Mapping[JsObject] = FieldMapping[JsObject]()(
    new Formatter[JsObject] {
      def bind(key: String, data: Map[String, String]) = {
        def parsing(flatJson: Map[String, String]): JsObject = {
          jsObject: JsObject = JsObject(Map.empty[String, JsValue]
        }

        println(data.filter({ case (k, _) => k.startsWith(key+".") }).toString)
        println("TEST: "+data.get(key).map(Json.parse(_).asInstanceOf[JsObject]).toString)
        data.get(key).map(Json.parse(_).asInstanceOf[JsObject]).toRight(Seq(FormError(key, "error.json", Nil)))
      }

      def unbind(key: String, value: JsObject) = Map(key -> value.toString())
    }
  )*/
}

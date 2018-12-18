import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.softwaremill.sttp._

import scala.collection.immutable.Seq

object Bootstrap {

  // Application entry point
  def main(args: Array[String]): Unit = {

    implicit val backend = HttpURLConnectionBackend()
    val api = sys.env("AWS_LAMBDA_RUNTIME_API")
    val nextEventUrl = s"http://$api/2018-06-01/runtime/invocation/next"

    while (true) {
      val response = sttp.get((uri"$nextEventUrl")).send()
      val requestId = response.header("Lambda-Runtime-Aws-Request-Id")
      requestId.map { rqId =>
        val errorResponseUrl = s"http://$api/2018-06-01/runtime/invocation/$rqId/error"
        val successResponseUrl = s"http://$api/2018-06-01/runtime/invocation/$rqId/response"
        try {
          response.body match {
            case Left(errorMessage) => sttp.post(uri"$errorResponseUrl").body(errorMessage).send()
            case Right(rawStringBody) => {
              val out = invokeFunction(rqId, response.headers, rawStringBody)
              sttp.post(uri"$successResponseUrl").body(out).send()
            }
          }
        } catch {
          case e: Throwable => sttp.post(uri"$errorResponseUrl").body(e.getMessage).send()
        }
      }
    }
  }

  // It's just a demo, use inline functions
  def invokeFunction(requestId: String, headers: Seq[(String, String)], body: String): String = {
    sys.env("_HANDLER") match {
      case "echo" => JsonUtil.toJson(Map("statusCode" -> 200, "headers" -> headers.toMap, "body" -> body))
      case "reverse" => JsonUtil.toJson(Map("statusCode" -> 200, "headers" -> headers.toMap, "body" -> body.reverse))
      case _ => JsonUtil.toJson(Map("statusCode" -> 404, "headers" -> headers.toMap, "body" -> "Unknown Function"))
    }
  }
}


// Json Utility
object JsonUtil {
  val mapper = new ObjectMapper() with ScalaObjectMapper
  mapper.registerModule(DefaultScalaModule)

  def toJson(value: Any): String = {
    mapper.writeValueAsString(value)
  }
}
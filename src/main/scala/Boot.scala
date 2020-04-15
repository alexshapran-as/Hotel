import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import configurations.Conf.{confApiServiceInterface, confApiServicePort}
import service.MainApiService

import scala.concurrent.ExecutionContextExecutor

object Boot {
  def main(args: Array[String]) = {
    implicit val system: ActorSystem = ActorSystem("routing-system")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher
    Http().bindAndHandle(MainApiService.routingStart, confApiServiceInterface, confApiServicePort)
    println(s"Hotel admin page: http://$confApiServiceInterface:$confApiServicePort/hotel/admin/start_page")
  }
}

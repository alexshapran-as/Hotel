package service

import akka.http.scaladsl.server.{Directives, Route}
import api.web.{HttpRouteUtils, UserSessionData}
import authenticator.{MongoAuthApi, Roles}
import org.slf4j.LoggerFactory

object MongoApiService extends HttpRouteUtils with Directives {
  protected val logger = LoggerFactory.getLogger(getClass)

  def getRoute(pathPrefix: String): Route =
    respondWithJsonContentType {
      post("login") {
        formFields(
          'username.as[String], 'password.as[String],
          'rememberMe.?
        ) {
          case (userName, password, rememberMe) =>
            MongoAuthApi.authorize(userName, password) match {
              case Some(roles) =>
                if (MongoAuthApi.hasAccessToWithRoles(pathPrefix, roles)) {
                  setCsrfToken {
                    rememberMe match {
                      case Some("on") =>
                        setRefreshableSession(UserSessionData(userName, roles)) {
                          complete(getOkResponse)
                        }
                      case _ =>
                        println("Authorization for 30 sec")
                        logger.info("Authorization for 30 sec")
                        setOneLogInSession(UserSessionData(userName, roles)) {
                          complete(getOkResponse)
                        }
                    }
                  }
                } else {
                  complete(getErrorResponse(401, "Unauthorized"))
                }

              case None =>
                complete(getErrorResponse(401, "Unauthorized"))
            }
        }
      } ~
        post("logout") {
          validateRequiredSession { session =>
            invalidateRequiredSession { ctx =>
              println(s"Logging out $session")
              logger.info(s"Logging out $session")
              ctx.complete(getOkResponse)
            }
          }
        }
    }
}

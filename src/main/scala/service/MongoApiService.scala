package service

import akka.http.scaladsl.server.{AuthorizationFailedRejection, Directives, Route}
import api.web.{HttpRouteUtils, UserSessionData}
import authenticator.{MongoAuthApi, Roles}
//import org.slf4j.LoggerFactory

object MongoApiService extends HttpRouteUtils with Directives {
//  protected val logger = LoggerFactory.getLogger(getClass)

  def getRoute(pathPrefix: String): Route =
    respondWithJsonContentType {
      post("login") {
        extractPostRequest { case (postStr, postMsa) =>
          val userName = postMsa.getOrElse("userName", throw new IllegalArgumentException("Username was not sent")).toString
          val password = postMsa.getOrElse("loginPassword", throw new IllegalArgumentException("Password was not sent")).toString
          val rememberMe = postMsa.getOrElse("rememberMe", throw new IllegalArgumentException("rememberMe was not sent")).toString.toBoolean
            MongoAuthApi.authorize(userName, password) match {
              case Some(roles) =>
                if (MongoAuthApi.hasAccessToWithRoles(pathPrefix, roles)) {
                  setCsrfToken {
                    rememberMe match {
                      case true =>
                        setRefreshableSession(UserSessionData(userName, roles)) {
                          complete(getOkResponse)
                        }
                      case _ =>
                        println("Authorization for 30 sec")
//                        logger.info("Authorization for 30 sec")
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
          validateRequiredSession { session =>
            post("logout") {
              invalidateRequiredSession { ctx =>
                println(s"Logging out $session")
                //              logger.info(s"Logging out $session")
                ctx.complete(getOkResponse)
              }
            } ~
                post("check") {
                  if (MongoAuthApi.hasAccessToWithRoles(pathPrefix, session.groups))
                    complete(getOkResponse(Map("user" -> session.groups.mkString(""))))
                  else
                    reject(AuthorizationFailedRejection)
                }
          }
    }
}

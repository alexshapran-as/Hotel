package service

import java.time.LocalDate

import akka.http.scaladsl.server.{Directives, Route}
import api.web.HttpRouteUtils
import authenticator.{Roles, UserAuthData}
import org.slf4j.LoggerFactory
import staff.Employee

object AdminApiService extends HttpRouteUtils with Directives {
  protected val logger = LoggerFactory.getLogger(getClass)

  def getRoute: Route =
      get("start_page") {
        getFromResource("web/admin_page.html")
      } ~
      validateRequiredSession { session =>
        respondWithJsonContentType {
          get("check_auth") {
            complete(getOkResponse)
          } ~
          post("employees_list") {
            complete(getOkResponse(UserAuthData.findAllEmployees))
          } ~
          post("delete_employee") {
            formField('username.as[String]) {
              case userName =>
                UserAuthData.delete(userName)
                complete(getOkResponse)
            }
          } ~
          post("add_employee") {
            formFields(
              'username.as[String], 'password.as[String], 'roles.as[String],
              'lastName.as[String], 'firstName.as[String], 'surName.as[String],
              'birthDate.as[String], 'seriesNumberPassport.as[String], 'address.as[String], 'salary.as[String]
            ) {
              case (userName, password, rolesStr,
                    lastName, firstName, surName, birthDateStr, seriesNumberPassport, address, salary) =>
                if (UserAuthData.create(userName, password, rolesStr.split(",").toList.map(UserAuthData.toRole),
                  Employee(lastName, firstName, surName, LocalDate.parse(birthDateStr), seriesNumberPassport, address, salary)).save) {
                  complete(getOkResponse)
                } else {
                  complete(getErrorResponse(400, "User with this username already exists"))
                }

            }
          }
      }
    }
}

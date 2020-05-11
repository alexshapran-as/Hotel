package service

import akka.http.scaladsl.server.Directives
import api.web.{CORSSupport, HttpRouteUtils}

case object MainApiService extends HttpRouteUtils with Directives {

    var pathPrefixRole = new String()

    def routingStart =
        CORSSupport.corsHandler {
            extractRequest { req =>
                get("js" / Segment) { sourceName =>
                    getFromResource(s"web/js/$sourceName")
                } ~
                    get("css" / Segment) { sourceName =>
                        getFromResource(s"web/css/$sourceName")
                    } ~
                    get("images" / Segment) { sourceName =>
                        getFromResource(s"web/images/$sourceName")
                    } ~
                    pathPrefix("hotel_auth") {
                        MongoApiService.getRoute(pathPrefixRole)
                    } ~
                    pathPrefix("hotel") {
                        pathPrefix("visitor") {
                            pathPrefixRole = "visitor"
                            VisitorApiService.getRoute
                        } ~
                            tokenCsrfProtectionDirective {
                                pathPrefix("admin") {
                                    pathPrefixRole = "admin"
                                    AdminApiService.getRoute
                                } ~
                                    pathPrefix("manager") {
                                        pathPrefixRole = "manager"
                                        ManagerApiService.getRoute
                                    } ~
                                    pathPrefix("staff") {
                                        pathPrefixRole = "staff"
                                        StaffApiService.getRoute
                                    }
                            }
                    }
            }

        }

}

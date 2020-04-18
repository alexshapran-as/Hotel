package service

import akka.http.scaladsl.server.Directives
import api.web.{CORSSupport, HttpRouteUtils}

case object MainApiService extends HttpRouteUtils with Directives {

    var pathPrefixRole = new String()

    def routingStart =
        CORSSupport.corsHandler {
            get("js" / Segment) { sourceName =>
                getFromResource(s"web/js/$sourceName")
            } ~
                get("css" / Segment) { sourceName =>
                    getFromResource(s"web/css/$sourceName")
                } ~
                tokenCsrfProtectionDirective {
                    pathPrefix("hotel_auth") {
                        MongoApiService.getRoute(pathPrefixRole)
                    } ~
                        pathPrefix("hotel") {
                            pathPrefix("admin") {
                                pathPrefixRole = "admin"
                                AdminApiService.getRoute
                            } ~
                                pathPrefix("manager") {
                                    pathPrefixRole = "manager"
                                    get("start_page") {
                                        getFromResource("")
                                    }
                                } ~
                                pathPrefix("staff") {
                                    pathPrefixRole = "staff"
                                    get("start_page") {
                                        getFromResource("")
                                    }
                                } ~
                                pathPrefix("visitor") {
                                    get("start_page") {
                                        getFromResource("")
                                    }
                                }
                        }
                }
        }

}

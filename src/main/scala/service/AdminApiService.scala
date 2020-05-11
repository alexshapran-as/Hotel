package service

import java.time.LocalDate

import Hotel.MSA
import akka.http.scaladsl.server.{Directives, Route}
import api.web.HttpRouteUtils
import authenticator.{Roles, UserAuthData}
import client.{Client, GroupOfClients}
import room.{Room, RoomClass, RoomExtraOptions, RoomReservation}
import util.Utils

import scala.util.{Failure, Success}
//import org.slf4j.LoggerFactory
import staff.Employee
import util.Utils._

object AdminApiService extends HttpRouteUtils with Directives {
    //  protected val logger = LoggerFactory.getLogger(getClass)

    def fillRommDescription(msa: MSA): MSA = (RoomClass.withName(msa("class").toString) match {
        case RoomClass.ROH =>
            Map("class" -> "Любая сторона здания") ++
                Map("description" ->
                    s"""${msa("description").toString}
                       |Размещение в отеле без уточнения типа номера и вида из окна.""".stripMargin)
        case RoomClass.STD =>
            Map("class" -> "Стандартный") ++
                Map("description" ->
                    s"""${msa("description").toString}
                       |Однокомнатный номер с одной или двумя кроватями, расчитан на проживание одного или двух человек.
                       | Размер комнаты, как правило, составляет: 16 м² - 22 м².В санузле: ванна или душ, умывальник, унитаз.""".stripMargin)
        case RoomClass.SuperiorRoom =>
            Map("class" -> "Улучшенный") ++
                Map("description" ->
                    s"""${msa("description").toString}
                       |Аналогичен Стандартному номеру.
                       | Может отличаться размером, лучшим видом из окна, улучшенной отделкой и обстановкой (дополнительный диван).""".stripMargin)
        case RoomClass.BDR =>
            Map("class" -> "Номер со спальней") ++
                Map("description" ->
                    s"""${msa("description").toString}
                       |Двухкомнатный номер. В одной из комнат стоит кровать. Интерьер номера, как правило, на уровне Стандартного номера.""".stripMargin)
        case RoomClass.APT =>
            Map("class" -> "Квартира (апартаменты)") ++
                Map("description" ->
                    s"""${msa("description").toString}
                       |Номер состоит из одной или нескольких комнат.
                       |Интерьер номера, как правило, на уровне Стандартного номера или Семейного.
                       |Апартаменты обязательно оборудованы кухней или кухонным уголком (с плитой, набором посуды, чайником и др. кухонными принадлежностями).
                       |Как правило, апартаменты расчитаын на проживание семей или компаний от 2 до 8 человек.""".stripMargin)
        case RoomClass.BGLW =>
            Map("class" -> "Бунгало") ++
                Map("description" ->
                    s"""${msa("description").toString}
                       |Отдельные постройки на территории отеля, находящиеся в стороне от Главного здания, но имеющие удобный доступ ко всей инфраструктуре отеля.
                       |Бунгало расчитаны на один или несколько номеров.
                       |Интерьер номера в бунгало, как правило, на уровне Стандартного номера или Улучшенного, иногда - на уровне номера ПолуЛюкс или Люкс.""".stripMargin)
        case RoomClass.VILLA =>
            Map("class" -> "Вилла") ++
                Map("description" ->
                    s"""${msa("description").toString}
                       |Отдельностоящий одно- двухэтажный дом на территории отеля с роскошной обстановкой и отделкой.
                       |Вилла обязательно состоит из нескольких комнат и имеет большую площадь.
                       |Как правило, Вилла стоит в привилегированном и уединенном месте, а также имеет свой сад, бассейн и др. эксклюзивные преимущества.
                       |Вилла может и не являться частью какого-либо отеля.""".stripMargin)
        case RoomClass.FamilyRoom =>
            Map("class" -> "Семейный") ++
                Map("description" ->
                    s"""${msa("description").toString}
                       |Двухкомнатный номер (спальня и гостиная) или Трехкомнатный номер ( 2 спальни и гостиная). Очень редко - Однокомнатный номер с несколькими кроватями в одной большой комнате.
                       |Номер расчитан на проживание семьи с 2 - 3 детьми.
                       |Интерьер номера, как правило, на уровне Стандартного номера или Улучшенного, иногда - на уровне номера Превосходный.""".stripMargin)
        case RoomClass.Deluxe =>
            Map("class" -> "Превосходный (Deluxe)") ++
                Map("description" ->
                    s"""${msa("description").toString}
                       |Аналогичен Стандартному номеру.
                       |Отличается от Стандартного номера или Улучшенного большим размером (размер комнаты, как правило, составляет: 22 м² - 28 м²), лучшим видом из окна, улучшенной отделкой и обстановкой.
                       |Иногда включены дополнительные удобства (тапочки, халаты и др.)""".stripMargin)
        case RoomClass.JuniorSuite =>
            Map("class" -> "ПолуЛюкс") ++
                Map("description" ->
                    s"""${msa("description").toString}
                       |Однокомнатный номер с отгороженным спальным местом, редко двухкомнатный номер (спальня и гостиная).
                       |Отличается от Превосходного номера большим размером и роскошной обстановкой.""".stripMargin)
        case RoomClass.Suite =>
            Map("class" -> "Люкс") ++
                Map("description" ->
                    s"""${msa("description").toString}
                       |Двухкомнатный номер (спальня и гостиная) или Трехкомнатный номер (спальня, гостиная и кабинет или 2 спальни и гостиная).
                       |Размер номера, как правило, составляет более 35 м².
                       |Интерьер номера имеет роскошную обстановку и отделку. Номер отличается живописным видом из окна и удобным расположением.
                       |Включены дополнительные удобства (тапочки, халаты, чайные и кофейные принадлежности и др.).
                       |Часто номера оборудованы дополнительным туалетом, джакузи и др. удобствами, создающими домашний уют.""".stripMargin)
        case RoomClass.SeniorSuite =>
            Map("class" -> "Улучшенный Люкс") ++
                Map("description" ->
                    s"""${msa("description").toString}
                       |Аналогичен Люксу.
                       |Как правило, в номере имеется холл и две спальни.
                       |Номер может иметь большую площадь, дополнительные услуги и оборудование.""".stripMargin)
        case RoomClass.RoyalSuite =>
            Map("class" -> "Королевский Люкс") ++
                Map("description" ->
                    s"""${msa("description").toString}
                       |Аналогичен Президентскому Люксу.
                       |Отличается от Президентского Люкса менее роскошной обстановкой.""".stripMargin)
        case RoomClass.PresidentialSuite =>
            Map("class" -> "Президентский Люкс") ++
                Map("description" ->
                    s"""${msa("description").toString}
                       |Номер состоит из нескольких больших комнат (спальня, гостиная, кабинет, столовая, холл, несколько ванных комнат и туалетов, большой балкон).
                       |Обычно, находится на верхнем этаже (Пентхаус) и имеет отдельный лифт.
                       |Интерьер номера имеет самую роскошную обстановку и отделку.""".stripMargin)
        case RoomClass.HoneymoonSuite =>
            Map("class" -> "Люкс для Молодоженов") ++
                Map("description" ->
                    s"""${msa("description").toString}
                       |Номер с большой кроватью KING SIZE.
                       |Интерьер номера имеет романтическую обстановку и дизайн.
                       |Включены дополнительные удобства и оборудование (тапочки, халаты, джакузи и др.).
                       |Подарок от отеля при въезде и специальный сервис для молодоженов (шампанское и фрукты, ужин при свечах, свадебная церемония и др.)
                       |""".stripMargin)
        case RoomClass.BusinessRoom =>
            Map("class" -> "Номер для бизнесменов") ++
                Map("description" ->
                    s"""${msa("description").toString}
                       |Номер с местом для рабочего стола бизнесмена с компьютером.""".stripMargin)
    }).asInstanceOf[MSA]

    def getRoute: Route =
        get("start_page") {
            getFromResource("web/admin_page.html")
        } ~
            validateRequiredSession { session =>
                respondWithJsonContentType {
                    post("employees_list") {
                        complete(getOkResponse(UserAuthData.findAllEmployees))
                    } ~
                        post("delete_employee") {
                            extractPostRequest { case (postStr, postMsa) =>
                                UserAuthData.delete(postMsa("userName").toString)
                                complete(getOkResponse)
                            }
                        } ~
                        post("add_employee") {
                            extractPostRequest { case (postStr, postMsa) =>
                                val userName = postMsa.getOrError("userName").toString
                                val password = postMsa.getOrError("password").toString
                                val rolesStr = postMsa.getOrError("roles").toString
                                val lastName = postMsa.getOrError("lastName").toString
                                val firstName = postMsa.getOrError("firstName").toString
                                val surName = postMsa.getOrError("surName").toString
                                val birthDate = postMsa.getOrError("birthDate").toString
                                val seriesNumberPassport = postMsa.getOrError("seriesNumberPassport").toString
                                val address = postMsa.getOrError("address").toString
                                val salary = postMsa.getOrError("salary").toString
                                if (UserAuthData.create(userName, password, rolesStr.split(",").toList.map(Roles.withName),
                                    Employee(lastName, firstName, surName, birthDate, seriesNumberPassport, address, salary)).save) {
                                    complete(getOkResponse)
                                } else {
                                    complete(getErrorResponse(400, "User with this username already exists"))
                                }

                            }
                        } ~
                        post("rooms_list") {
                            val roomsList = Room.findAllRoomsMSA.map { msa =>
                                msa ++ fillRommDescription(msa)
                            }
                            complete(getOkResponse(roomsList))
                        } ~
                        post("reservations_list") {
                            val roomsList: List[MSA] = Room.findAllRoomsMSA.filter(msa => msa("reservations").asInstanceOf[List[MSA]].nonEmpty).foldLeft(List.empty[MSA]) { (newRoomsList, msa) =>
                                newRoomsList ++ msa("reservations").asInstanceOf[List[MSA]].foldLeft(List.empty[MSA]) { (newListMsa, reservationMsa) =>
                                    val reservation = RoomReservation.fromMSA(reservationMsa)
                                    val (checkInDate, checkOutDate) = (reservation.checkInDate, reservation.checkOutDate)
                                    val fio = reservation.client match {
                                        case Client(_, lastName, firstName, surName, _, _, _) =>
                                            (lastName.nonEmpty, firstName.nonEmpty, surName.nonEmpty) match {
                                                case (true, true, true) => lastName + " " + firstName + " " + surName
                                                case _ => ""
                                            }
                                        case GroupOfClients(_, client, _, _) =>
                                            (client.lastName.nonEmpty, client.firstName.nonEmpty, client.surName.nonEmpty) match {
                                                case (true, true, true) => client.lastName + " " + client.firstName + " " + client.surName
                                                case _ => ""
                                            }
                                    }
                                    val countOfDays = Utils.getAllPartsOfDateAsMap(Utils.formattedDateToMillis(checkOutDate), "checkOutDate")("checkOutDate.day").toLong -
                                        Utils.getAllPartsOfDateAsMap(Utils.formattedDateToMillis(checkInDate), "checkInDate")("checkInDate.day").toLong
                                    val totalCost = Room.fromMSA(msa).calculateRoomCost(reservation.extraOptions, reservation.client)
                                    newListMsa :+ msa ++ fillRommDescription(msa) ++ Map("checkInDate" -> checkInDate, "checkOutDate" -> checkOutDate, "totalCost" -> totalCost * (countOfDays + 1), "fio" -> fio)
                                }
                            }.filter(msa => msa("fio").toString.nonEmpty)
                            complete(getOkResponse(roomsList))
                        } ~
                        post("accommodation_info_list") {
                            val currentDateMillis = Utils.getDayStartInMillis(System.currentTimeMillis())
                            val roomsList: List[MSA] = Room.findAllRoomsMSA.filter { msa =>
                                msa("reservations").asInstanceOf[List[MSA]] match {
                                    case List() =>
                                        false
                                    case reservations =>
                                        reservations.foldLeft(false) { (busyRoom, reservation) =>
                                            busyRoom match {
                                                case true =>
                                                    busyRoom
                                                case false =>
                                                    val reservedCheckInDate = Utils.formattedDateToMillis(reservation("checkInDate").toString)
                                                    val reservedCheckOutDate = Utils.formattedDateToMillis(reservation("checkOutDate").toString)
                                                    if (currentDateMillis >= reservedCheckInDate && currentDateMillis <= reservedCheckOutDate) {
                                                        true
                                                    } else {
                                                        false
                                                    }
                                            }
                                        }
                                }
                            }.foldLeft(List.empty[MSA]) { (newRoomsList, msa) =>
                                newRoomsList ++ msa("reservations").asInstanceOf[List[MSA]].filter { reservation =>
                                    val reservedCheckInDate = Utils.formattedDateToMillis(reservation("checkInDate").toString)
                                    val reservedCheckOutDate = Utils.formattedDateToMillis(reservation("checkOutDate").toString)
                                    if (currentDateMillis >= reservedCheckInDate && currentDateMillis <= reservedCheckOutDate) {
                                        true
                                    } else {
                                        false
                                    }
                                }.foldLeft(List.empty[MSA]) { (newListMsa, reservationMsa) =>
                                    val reservation = RoomReservation.fromMSA(reservationMsa)
                                    val (checkInDate, checkOutDate) = (reservation.checkInDate, reservation.checkOutDate)
                                    val (fio, birthDate, seriesNumberPassport, address) = reservation.client match {
                                        case Client(_, lastName, firstName, surName, birthDate, seriesNumberPassport, address) =>
                                            (lastName.nonEmpty, firstName.nonEmpty, surName.nonEmpty) match {
                                                case (true, true, true) =>
                                                    (lastName + " " + firstName + " " + surName, birthDate, seriesNumberPassport, address)
                                                case _ =>
                                                    ("", "", "", "")
                                            }
                                        case GroupOfClients(_, client, _, _) =>
                                            (client.lastName.nonEmpty, client.firstName.nonEmpty, client.surName.nonEmpty) match {
                                                case (true, true, true) =>
                                                    (client.lastName + " " + client.firstName + " " + client.surName, client.birthDate, client.seriesNumberPassport, client.address)
                                                case _ =>
                                                    ("", "", "", "")
                                            }
                                    }
                                    var extraOptions = List.empty[String]
                                    if (reservation.extraOptions.extraBed) {
                                        extraOptions = extraOptions :+ "дополнительная кровать"
                                    }
                                    if (reservation.extraOptions.child) {
                                        extraOptions = extraOptions :+ "ребенок"
                                    }
                                    if (reservation.extraOptions.bedBreakfast) {
                                        extraOptions = extraOptions :+ "завтрак"
                                    }
                                    if (reservation.extraOptions.allInclusive) {
                                        extraOptions = extraOptions :+ "все включено"
                                    }
                                    val countOfDays = Utils.getAllPartsOfDateAsMap(Utils.formattedDateToMillis(checkOutDate), "checkOutDate")("checkOutDate.day").toLong -
                                        Utils.getAllPartsOfDateAsMap(Utils.formattedDateToMillis(checkInDate), "checkInDate")("checkInDate.day").toLong
                                    val totalCost = Room.fromMSA(msa).calculateRoomCost(reservation.extraOptions, reservation.client)
                                    newListMsa :+ msa ++ fillRommDescription(msa) ++
                                        Map("checkInDate" -> checkInDate, "checkOutDate" -> checkOutDate,
                                            "totalCost" -> totalCost * (countOfDays + 1), "fio" -> fio,
                                            "seriesNumberPassport" -> seriesNumberPassport, "birthDate" -> birthDate, "address" -> address,
                                            "extraOptions" -> extraOptions.mkString(","))
                                }
                            }.filter(msa => msa("fio").toString.nonEmpty)
                            complete(getOkResponse(roomsList))
                        } ~
                        post("busy_rooms_list") {
                            val currentDateMillis = Utils.getDayStartInMillis(System.currentTimeMillis())
                            val roomsList = Room.findAllRoomsMSA.filter { msa =>
                                msa("reservations").asInstanceOf[List[MSA]] match {
                                    case List() =>
                                        false
                                    case reservations =>
                                        reservations.foldLeft(false) { (busyRoom, reservation) =>
                                            busyRoom match {
                                                case true =>
                                                    busyRoom
                                                case false =>
                                                    val reservedCheckInDate = Utils.formattedDateToMillis(reservation("checkInDate").toString)
                                                    val reservedCheckOutDate = Utils.formattedDateToMillis(reservation("checkOutDate").toString)
                                                    if (currentDateMillis >= reservedCheckInDate && currentDateMillis <= reservedCheckOutDate) {
                                                        true
                                                    } else {
                                                        false
                                                    }
                                            }
                                        }
                                }
                            } map { msa =>
                                msa ++ fillRommDescription(msa)
                            }
                            complete(getOkResponse(roomsList))
                        } ~
                        post("free_rooms_list") {
                            val currentDateMillis = Utils.getDayStartInMillis(System.currentTimeMillis())
                            val roomsList = Room.findAllRoomsMSA.filter { msa =>
                                msa("reservations").asInstanceOf[List[MSA]].foldLeft(true) { (freeRoom, reservation) =>
                                    freeRoom match {
                                        case false =>
                                            freeRoom
                                        case true =>
                                            val reservedCheckInDate = Utils.formattedDateToMillis(reservation("checkInDate").toString)
                                            val reservedCheckOutDate = Utils.formattedDateToMillis(reservation("checkOutDate").toString)
                                            if (currentDateMillis >= reservedCheckInDate && currentDateMillis <= reservedCheckOutDate) {
                                                false
                                            } else {
                                                true
                                            }
                                    }
                                }
                            } map { msa =>
                                msa ++ fillRommDescription(msa)
                            }
                            complete(getOkResponse(roomsList))
                        } ~
                        post("filtered_rooms_list") {
                            extractPostRequest { case (postStr, postMsa) =>
                                val checkInDate = Utils.formattedDateToMillis(postMsa("checkInDate").toString)
                                val checkOutDate = Utils.formattedDateToMillis(postMsa("checkOutDate").toString)
                                val roomsList = Room.findAllRoomsMSA.filter { msa =>
                                    msa.getOrElse("reservations", List()).asInstanceOf[List[MSA]].foldLeft(true) { (freeRoom, reservation) =>
                                        freeRoom match {
                                            case false =>
                                                freeRoom
                                            case true =>
                                                val reservedCheckInDate = Utils.formattedDateToMillis(reservation("checkInDate").toString)
                                                val reservedCheckOutDate = Utils.formattedDateToMillis(reservation("checkOutDate").toString)
                                                if (checkInDate >= reservedCheckInDate && checkInDate <= reservedCheckOutDate || checkOutDate >= reservedCheckInDate && checkOutDate <= reservedCheckOutDate) {
                                                    false
                                                } else {
                                                    true
                                                }
                                        }
                                    }
                                } map { msa =>
                                    msa ++ fillRommDescription(msa)
                                }
                                complete(getOkResponse(roomsList))
                            }
                        } ~
                        post("reserve_room") {
                            extractPostRequest { case (postStr, postMsa) =>
                                scala.util.Try {
                                    val groupId = postMsa.getOrError("groupId").toString
                                    val room = Room.findRoomById(postMsa.getOrError("roomId").toString)
                                    val (checkInDate, checkOutDate) = (postMsa.getOrError("checkInDate").toString, postMsa.getOrError("checkOutDate").toString)
                                    val reservations = postMsa.getOrError("reservations").asInstanceOf[List[MSA]]
                                    if (groupId.isEmpty) {
                                        reservations.foreach { reservation =>
                                            val birthDate = if (reservation.getOrError("birthDate").toString.isEmpty) {
                                                "1970-01-01"
                                            } else {
                                                reservation.getOrError("birthDate").toString
                                            }
                                            room.addReservation(RoomReservation(
                                                client = Client(
                                                    lastName = reservation.getOrError("lastName").toString,
                                                    firstName = reservation.getOrError("firstName").toString,
                                                    surName = reservation.getOrError("surName").toString,
                                                    birthDate = birthDate,
                                                    seriesNumberPassport = reservation.getOrError("seriesNumberPassport").toString,
                                                    address = reservation.getOrError("address").toString
                                                ),
                                                checkInDate = checkInDate,
                                                checkOutDate = checkOutDate,
                                                extraOptions = RoomExtraOptions(
                                                    extraBed = reservation.getOrError("extraBed").toString.toBoolean,
                                                    child = Utils.getAllPartsOfDateAsMap(System.currentTimeMillis(), "currentDate")("currentDate.year").toLong -
                                                        Utils.getAllPartsOfDateAsMap(Utils.formattedDateToMillis(birthDate), "clientDate")("clientDate.year").toLong < 7,
                                                    bedBreakfast = reservation.getOrError("bedBreakfast").toString.toBoolean,
                                                    allInclusive = reservation.getOrError("allInclusive").toString.toBoolean
                                                )
                                            ))
                                        }
                                    } else {
                                        reservations.foreach { reservation =>
                                            val birthDate = if (reservation.getOrError("birthDate").toString.isEmpty) {
                                                "1970-01-01"
                                            } else {
                                                reservation.getOrError("birthDate").toString
                                            }
                                            room.addReservation(RoomReservation(
                                                client = GroupOfClients(
                                                    id = groupId,
                                                    client = Client(
                                                        lastName = reservation.getOrError("lastName").toString,
                                                        firstName = reservation.getOrError("firstName").toString,
                                                        surName = reservation.getOrError("surName").toString,
                                                        birthDate = birthDate,
                                                        seriesNumberPassport = reservation.getOrError("seriesNumberPassport").toString,
                                                        address = reservation.getOrError("address").toString
                                                    ),
                                                    totalCountOfClients = postMsa.getOrError("groupCountOfClients").toString.toInt,
                                                    description = if (postMsa.getOrError("groupDescription").toString.isEmpty) None else Some(postMsa.getOrError("groupDescription").toString)
                                                ),
                                                checkInDate = checkInDate,
                                                checkOutDate = checkOutDate,
                                                extraOptions = RoomExtraOptions(
                                                    extraBed = reservation.getOrError("extraBed").toString.toBoolean,
                                                    child = Utils.getAllPartsOfDateAsMap(System.currentTimeMillis(), "currentDate")("currentDate.year").toLong -
                                                        Utils.getAllPartsOfDateAsMap(Utils.formattedDateToMillis(birthDate), "clientDate")("clientDate.year").toLong < 7,
                                                    bedBreakfast = reservation.getOrError("bedBreakfast").toString.toBoolean,
                                                    allInclusive = reservation.getOrError("allInclusive").toString.toBoolean
                                                )
                                            ))
                                        }
                                    }
                                    room.save
                                    getOkResponse()
                                } match {
                                    case Success(response) =>
                                        complete(response)
                                    case Failure(exception) =>
                                        complete(getErrorResponse(500, exception.getMessage))
                                }
                            }
                        } ~
                        post("new_group_id") {
                            complete(getOkResponse(Map("groupId" -> Utils.getId)))
                        } ~
                        post("calculate_room_cost") {
                            extractPostRequest { case (postStr, postMsa) =>
                                scala.util.Try {
                                    val groupId = postMsa.getOrError("groupId").toString
                                    val room = Room.findRoomById(postMsa.getOrError("roomId").toString)
                                    val (checkInDate, checkOutDate) = (postMsa.getOrError("checkInDate").toString, postMsa.getOrError("checkOutDate").toString)
                                    val reservations = postMsa.getOrError("reservations").asInstanceOf[List[MSA]]
                                    val totalCost = if (groupId.isEmpty) {
                                        reservations.foldLeft(0.0) { case (totalCost, reservation) =>
                                            val birthDate = if (reservation.getOrError("birthDate").toString.isEmpty) {
                                                "1970-01-01"
                                            } else {
                                                reservation.getOrError("birthDate").toString
                                            }
                                            val roomClient = Client(
                                                lastName = reservation.getOrError("lastName").toString,
                                                firstName = reservation.getOrError("firstName").toString,
                                                surName = reservation.getOrError("surName").toString,
                                                birthDate = birthDate,
                                                seriesNumberPassport = reservation.getOrError("seriesNumberPassport").toString,
                                                address = reservation.getOrError("address").toString
                                            )
                                            val roomExtraOptions = RoomExtraOptions(
                                                extraBed = reservation.getOrError("extraBed").toString.toBoolean,
                                                child = Utils.getAllPartsOfDateAsMap(System.currentTimeMillis(), "currentDate")("currentDate.year").toLong -
                                                    Utils.getAllPartsOfDateAsMap(Utils.formattedDateToMillis(birthDate), "clientDate")("clientDate.year").toLong < 7,
                                                bedBreakfast = reservation.getOrError("bedBreakfast").toString.toBoolean,
                                                allInclusive = reservation.getOrError("allInclusive").toString.toBoolean
                                            )
                                            totalCost + room.calculateRoomCost(roomExtraOptions, roomClient)
                                        }
                                    } else {
                                        reservations.foldLeft(0.0) { case (totalCost, reservation) =>
                                            val birthDate = if (reservation.getOrError("birthDate").toString.isEmpty) {
                                                "1970-01-01"
                                            } else {
                                                reservation.getOrError("birthDate").toString
                                            }
                                            val roomGroup = GroupOfClients(
                                                id = groupId,
                                                client = Client(
                                                    lastName = reservation.getOrError("lastName").toString,
                                                    firstName = reservation.getOrError("firstName").toString,
                                                    surName = reservation.getOrError("surName").toString,
                                                    birthDate = birthDate,
                                                    seriesNumberPassport = reservation.getOrError("seriesNumberPassport").toString,
                                                    address = reservation.getOrError("address").toString
                                                ),
                                                totalCountOfClients = postMsa.getOrError("groupCountOfClients").toString.toInt,
                                                description = if (postMsa.getOrError("groupDescription").toString.isEmpty) None else Some(postMsa.getOrError("groupDescription").toString)
                                            )
                                            val roomExtraOptions = RoomExtraOptions(
                                                extraBed = reservation.getOrError("extraBed").toString.toBoolean,
                                                child = Utils.getAllPartsOfDateAsMap(System.currentTimeMillis(), "currentDate")("currentDate.year").toLong -
                                                    Utils.getAllPartsOfDateAsMap(Utils.formattedDateToMillis(birthDate), "clientDate")("clientDate.year").toLong < 7,
                                                bedBreakfast = reservation.getOrError("bedBreakfast").toString.toBoolean,
                                                allInclusive = reservation.getOrError("allInclusive").toString.toBoolean
                                            )
                                            totalCost + room.calculateRoomCost(roomExtraOptions, roomGroup)
                                        }
                                    }
                                    val countOfDays = Utils.getAllPartsOfDateAsMap(Utils.formattedDateToMillis(checkOutDate), "checkOutDate")("checkOutDate.day").toLong -
                                        Utils.getAllPartsOfDateAsMap(Utils.formattedDateToMillis(checkInDate), "checkInDate")("checkInDate.day").toLong
                                    getOkResponse(Map("totalCost" -> totalCost * (countOfDays + 1)))
                                } match {
                                    case Success(response) =>
                                        complete(response)
                                    case Failure(exception) =>
                                        complete(getErrorResponse(500, exception.getMessage))
                                }
                            }
                        }
                }
            }
}

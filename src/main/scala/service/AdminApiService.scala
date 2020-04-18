package service

import java.time.LocalDate

import Hotel.MSA
import akka.http.scaladsl.server.{Directives, Route}
import api.web.HttpRouteUtils
import authenticator.{Roles, UserAuthData}
import room.{Room, RoomClass}
//import org.slf4j.LoggerFactory
import staff.Employee

object AdminApiService extends HttpRouteUtils with Directives {
//  protected val logger = LoggerFactory.getLogger(getClass)

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
                    lastName, firstName, surName, birthDate, seriesNumberPassport, address, salary) =>
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
              msa ++ (RoomClass.withName(msa("class").toString) match {
                case RoomClass.ROH =>
                  Map("class"-> "Любая сторона здания") ++
                      Map("description" ->
                          s"""${msa("description").toString}
                             |Размещение в отеле без уточнения типа номера и вида из окна.""".stripMargin)
                case RoomClass.STD =>
                  Map("class"-> "Стандартный") ++
                      Map("description" ->
                          s"""${msa("description").toString}
                             |Однокомнатный номер с одной или двумя кроватями, расчитан на проживание одного или двух человек.
                             | Размер комнаты, как правило, составляет: 16 м² - 22 м².В санузле: ванна или душ, умывальник, унитаз.""".stripMargin)
                case RoomClass.SuperiorRoom =>
                  Map("class"-> "Улучшенный") ++
                      Map("description" ->
                          s"""${msa("description").toString}
                             |Аналогичен Стандартному номеру.
                             | Может отличаться размером, лучшим видом из окна, улучшенной отделкой и обстановкой (дополнительный диван).""".stripMargin)
                case RoomClass.BDR =>
                  Map("class"-> "Номер со спальней") ++
                      Map("description" ->
                          s"""${msa("description").toString}
                             |Двухкомнатный номер. В одной из комнат стоит кровать. Интерьер номера, как правило, на уровне Стандартного номера.""".stripMargin)
                case RoomClass.APT =>
                  Map("class"-> "Квартира (апартаменты)") ++
                      Map("description" ->
                          s"""${msa("description").toString}
                             |Номер состоит из одной или нескольких комнат.
                             |Интерьер номера, как правило, на уровне Стандартного номера или Семейного.
                             |Апартаменты обязательно оборудованы кухней или кухонным уголком (с плитой, набором посуды, чайником и др. кухонными принадлежностями).
                             |Как правило, апартаменты расчитаын на проживание семей или компаний от 2 до 8 человек.""".stripMargin)
                case RoomClass.BGLW =>
                  Map("class"-> "Бунгало") ++
                      Map("description" ->
                          s"""${msa("description").toString}
                             |Отдельные постройки на территории отеля, находящиеся в стороне от Главного здания, но имеющие удобный доступ ко всей инфраструктуре отеля.
                             |Бунгало расчитаны на один или несколько номеров.
                             |Интерьер номера в бунгало, как правило, на уровне Стандартного номера или Улучшенного, иногда - на уровне номера ПолуЛюкс или Люкс.""".stripMargin)
                case RoomClass.VILLA =>
                  Map("class"-> "Вилла") ++
                      Map("description" ->
                          s"""${msa("description").toString}
                             |Отдельностоящий одно- двухэтажный дом на территории отеля с роскошной обстановкой и отделкой.
                             |Вилла обязательно состоит из нескольких комнат и имеет большую площадь.
                             |Как правило, Вилла стоит в привилегированном и уединенном месте, а также имеет свой сад, бассейн и др. эксклюзивные преимущества.
                             |Вилла может и не являться частью какого-либо отеля.""".stripMargin)
                case RoomClass.FamilyRoom =>
                  Map("class"-> "Семейный") ++
                      Map("description" ->
                          s"""${msa("description").toString}
                             |Двухкомнатный номер (спальня и гостиная) или Трехкомнатный номер ( 2 спальни и гостиная). Очень редко - Однокомнатный номер с несколькими кроватями в одной большой комнате.
                             |Номер расчитан на проживание семьи с 2 - 3 детьми.
                             |Интерьер номера, как правило, на уровне Стандартного номера или Улучшенного, иногда - на уровне номера Превосходный.""".stripMargin)
                case RoomClass.Deluxe =>
                  Map("class"-> "Превосходный (Deluxe)") ++
                      Map("description" ->
                          s"""${msa("description").toString}
                             |Аналогичен Стандартному номеру.
                             |Отличается от Стандартного номера или Улучшенного большим размером (размер комнаты, как правило, составляет: 22 м² - 28 м²), лучшим видом из окна, улучшенной отделкой и обстановкой.
                             |Иногда включены дополнительные удобства (тапочки, халаты и др.)""".stripMargin)
                case RoomClass.JuniorSuite =>
                  Map("class"-> "ПолуЛюкс") ++
                      Map("description" ->
                          s"""${msa("description").toString}
                             |Однокомнатный номер с отгороженным спальным местом, редко двухкомнатный номер (спальня и гостиная).
                             |Отличается от Превосходного номера большим размером и роскошной обстановкой.""".stripMargin)
                case RoomClass.Suite =>
                  Map("class"-> "Люкс") ++
                      Map("description" ->
                          s"""${msa("description").toString}
                             |Двухкомнатный номер (спальня и гостиная) или Трехкомнатный номер (спальня, гостиная и кабинет или 2 спальни и гостиная).
                             |Размер номера, как правило, составляет более 35 м².
                             |Интерьер номера имеет роскошную обстановку и отделку. Номер отличается живописным видом из окна и удобным расположением.
                             |Включены дополнительные удобства (тапочки, халаты, чайные и кофейные принадлежности и др.).
                             |Часто номера оборудованы дополнительным туалетом, джакузи и др. удобствами, создающими домашний уют.""".stripMargin)
                case RoomClass.SeniorSuite =>
                  Map("class"-> "Улучшенный Люкс") ++
                      Map("description" ->
                          s"""${msa("description").toString}
                             |Аналогичен Люксу.
                             |Как правило, в номере имеется холл и две спальни.
                             |Номер может иметь большую площадь, дополнительные услуги и оборудование.""".stripMargin)
                case RoomClass.RoyalSuite =>
                  Map("class"-> "Королевский Люкс") ++
                      Map("description" ->
                          s"""${msa("description").toString}
                             |Аналогичен Президентскому Люксу.
                             |Отличается от Президентского Люкса менее роскошной обстановкой.""".stripMargin)
                case RoomClass.PresidentialSuite =>
                  Map("class"-> "Президентский Люкс") ++
                      Map("description" ->
                          s"""${msa("description").toString}
                             |Номер состоит из нескольких больших комнат (спальня, гостиная, кабинет, столовая, холл, несколько ванных комнат и туалетов, большой балкон).
                             |Обычно, находится на верхнем этаже (Пентхаус) и имеет отдельный лифт.
                             |Интерьер номера имеет самую роскошную обстановку и отделку.""".stripMargin)
                case RoomClass.HoneymoonSuite =>
                  Map("class"-> "Люкс для Молодоженов") ++
                      Map("description" ->
                          s"""${msa("description").toString}
                             |Номер с большой кроватью KING SIZE.
                             |Интерьер номера имеет романтическую обстановку и дизайн.
                             |Включены дополнительные удобства и оборудование (тапочки, халаты, джакузи и др.).
                             |Подарок от отеля при въезде и специальный сервис для молодоженов (шампанское и фрукты, ужин при свечах, свадебная церемония и др.)
                             |""".stripMargin)
                case RoomClass.BusinessRoom =>
                  Map("class"-> "Номер для бизнесменов") ++
                      Map("description" ->
                          s"""${msa("description").toString}
                             |Номер с местом для рабочего стола бизнесмена с компьютером.""".stripMargin)
              }).asInstanceOf[MSA]
            }
            complete(getOkResponse(roomsList))
          }
      }
    }
}

import authenticator.{Roles, UserAuthData}
import client.Client
import room.{Room, RoomClass, RoomExtraOptions, RoomOptions, RoomReservation}
import staff.Employee
import util.Utils

object CreateAdmin {
  def main(args: Array[String]): Unit = {
    UserAuthData.create(
      "admin",
      "1@m@dm1n",
      List(Roles.ADMIN),
      Employee("Иванов", "Иван", "Иванович",
        "1998-05-13" ,
        "1234567890", "Москва, Ленинский проспект, 13", "50000")).save
  }
}

object CreateRooms {
  def main(args: Array[String]): Unit = {
//    Room(
//      description = Some("Тестовый номер 1"),
//      roomClass = RoomClass.Deluxe,
//      seatsNumber = 6,
//      options = RoomOptions(
//        fridge = true,
//        miniBar = true,
//        tv = true,
//        seaView = true
//      ),
//      defaultCostPerNight = 10000.0
//    ).save
//    Room(
//      description = Some("Тестовый номер 2"),
//      roomClass = RoomClass.STD,
//      seatsNumber = 2,
//      options = RoomOptions(
//        fridge = true,
//        tv = true
//      ),
//      defaultCostPerNight = 2000.0
//    ).save
//    Room(
//      description = Some("Тестовый номер 3"),
//      roomClass = RoomClass.STD,
//      seatsNumber = 2,
//      options = RoomOptions(
//        fridge = true,
//        tv = true
//      ),
//      defaultCostPerNight = 2000.0,
//      reservations = List(RoomReservation(
//        client = Client(lastName = "Шепелев", firstName = "Дмитрий", surName = "Васильевич",
//          birthDate = "1997-04-01", seriesNumberPassport = "1435597190", address = "Химки, Ленинский проспект, 11"),
//        checkInDate = Utils.millisToFormattedDate(format = Utils.defaultDateFormat),
//        checkOutDate = Utils.millisToFormattedDate(t = System.currentTimeMillis() + 432000000, format = Utils.defaultDateFormat),
//        extraOptions = RoomExtraOptions(child = true, allInclusive = true)
//      ))
//    ).save
//    Room(
//      description = Some("Тестовый номер 4"),
//      roomClass = RoomClass.BGLW,
//      seatsNumber = 4,
//      options = RoomOptions(
//        fridge = true,
//        tv = true,
//        miniBar = true
//      ),
//      defaultCostPerNight = 4000.0
//    ).save
  }
}

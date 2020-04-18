import authenticator.{Roles, UserAuthData}
import room.{Room, RoomClass, RoomOptions}
import staff.Employee

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
    Room(
      description = Some("Тестовый номер 1"),
      roomClass = RoomClass.Deluxe,
      seatsNumber = 6,
      options = RoomOptions(
        fridge = true,
        miniBar = true,
        tv = true,
        seaView = true
      ),
      defaultCostPerNight = 10000.0
    ).save
    Room(
      description = Some("Тестовый номер 2"),
      roomClass = RoomClass.STD,
      seatsNumber = 2,
      options = RoomOptions(
        fridge = true,
        tv = true
      ),
      defaultCostPerNight = 2000.0
    ).save
  }
}

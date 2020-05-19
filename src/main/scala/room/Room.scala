package room

import Hotel.MSA
import client.{Client, GroupOfClients, HotelClient}
import dao.MainDAO
import util.Utils

object RoomClass extends Enumeration {
    val
    // Любая сторона здания
    ROH,
    // Стандартный
    STD,
    // Улучшенный
    SuperiorRoom,
    // Номер со спальней
    BDR,
    // Квартира (апартаменты)
    APT,
    // Бунгало
    BGLW,
    // Вилла
    VILLA,
    // Семейный
    FamilyRoom,
    // Превосходный
    Deluxe,
    // ПолуЛюкс
    JuniorSuite,
    // Люкс
    Suite,
    // Улучшенный Люкс
    SeniorSuite,
    // Королевский Люкс
    RoyalSuite,
    // Президентский Люкс
    PresidentialSuite,
    // Люкс для Молодоженов
    HoneymoonSuite,
    // Номер для бизнесменов
    BusinessRoom = Value
}

case class RoomOptions(
                          fridge: Boolean = false,
                          miniBar: Boolean = false,
                          tv: Boolean = false,
                          gardenView: Boolean = false,
                          mountainView: Boolean = false,
                          seaView: Boolean = false,
                      ) {
    def toMSA: MSA = Map (
        "fridge" -> fridge,
        "miniBar" -> miniBar,
        "tv" -> tv,
        "gardenView" -> gardenView,
        "mountainView" -> mountainView,
        "seaView" -> seaView,
    )
}

case object RoomOptions {
    def fromMSA(msa: MSA): RoomOptions = RoomOptions(
        msa("fridge").toString.toBoolean,
        msa("miniBar").toString.toBoolean,
        msa("tv").toString.toBoolean,
        msa("gardenView").toString.toBoolean,
        msa("mountainView").toString.toBoolean,
        msa("seaView").toString.toBoolean
    )
}

case class Room(
                   id: String = Utils.getId,
                   description: Option[String] = None,
                   roomClass: RoomClass.Value,
                   seatsNumber: Int,
                   options: RoomOptions,
                   defaultCostPerNight: Double,
                   var reservations: List[RoomReservation] = List()
               ) {
    private val breakfastCost = 500.0
    private val allInclusiveCost = 1500.0

    def calculateRoomCost(roomExtraOptions: RoomExtraOptions, roomClient: HotelClient): Double = {
        val preTotalSum: Double = defaultCostPerNight +
            (if (roomExtraOptions.extraBed && roomExtraOptions.child) defaultCostPerNight / 3.0 else if (roomExtraOptions.extraBed) defaultCostPerNight else 0.0) +
            (if (options.fridge) defaultCostPerNight / 10.0 else 0.0) +
            (if (options.miniBar) defaultCostPerNight / 5.0 else 0.0) +
            (if (options.tv) defaultCostPerNight / 10.0 else 0.0) +
            (if (roomExtraOptions.child && !roomExtraOptions.extraBed) - (defaultCostPerNight / 2.0) else 0.0) +
            (if (options.gardenView || options.mountainView || options.seaView) defaultCostPerNight / 4.0 else 0.0) +
            (if (roomExtraOptions.bedBreakfast) breakfastCost else 0.0) +
            (if (roomExtraOptions.allInclusive) allInclusiveCost else 0.0)
        val sale = roomClient match {
            case _: Client =>
                0.0
            case group: GroupOfClients if group.totalCountOfClients > 5 && group.totalCountOfClients < 10 =>
                preTotalSum * 0.1
            case group: GroupOfClients if group.totalCountOfClients > 10 =>
                preTotalSum * 0.3
            case _ =>
                0.0
        }
        val totalSum = preTotalSum - sale
        totalSum
    }

    def toMSA: MSA = Map(
        "_id" -> id,
        "description" -> description.getOrElse(""),
        "class" -> roomClass.toString,
        "seatsNumber" -> seatsNumber,
        "options" -> options.toMSA,
        "defaultCostPerNight" -> defaultCostPerNight,
        "reservations" -> reservations.map { reservation =>
            val countOfDays = Utils.getAllPartsOfDateAsMap(Utils.formattedDateToMillis(reservation.checkOutDate), "checkOutDate")("checkOutDate.day").toLong -
                Utils.getAllPartsOfDateAsMap(Utils.formattedDateToMillis(reservation.checkInDate), "checkInDate")("checkInDate.day").toLong
            reservation.toMSA ++ Map("totalCost" -> calculateRoomCost(reservation.extraOptions, reservation.client) * (countOfDays + 1))
        }
    )

    def save = MainDAO.saveRoom(id, this.toMSA)

    def addReservation(reservation: RoomReservation) = this.reservations = this.reservations :+ reservation
}

case object Room {
    def fromMSA(msa: MSA) = Room(
        id = msa("_id").toString,
        description = if (msa.getOrElse("description", "").toString.nonEmpty) Some(msa("description").toString) else None,
        roomClass =  RoomClass.withName(msa("class").toString),
        seatsNumber = msa("seatsNumber").toString.toInt,
        options = RoomOptions.fromMSA(msa("options").asInstanceOf[MSA]),
        defaultCostPerNight = msa("defaultCostPerNight").toString.toDouble,
        reservations = msa.getOrElse("reservations", List()).asInstanceOf[List[MSA]].map(RoomReservation.fromMSA)
    )

    def findAllRooms: List[Room] = MainDAO.getAllRooms

    def findAllRoomsMSA: List[MSA] = MainDAO.getAllRoomsMSA

    def findAllRoomsWithNotNullReservationsMSA: List[MSA] = MainDAO.getAllReservationsMSA

    def findRoomById(roomId: String): Room = MainDAO.getRoom(roomId)
}

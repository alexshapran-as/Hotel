package room

import Hotel.MSA
import client.{Client, GroupOfClients, HotelClient}
import util.Utils

case class RoomExtraOptions(
                               extraBed: Boolean = false,
                               child: Boolean = false,
                               bedBreakfast: Boolean = false,
                               allInclusive: Boolean = false
                           ) {
    def toMSA: MSA = Map(
        "extraBed" -> extraBed,
        "child" -> child,
        "bedBreakfast" -> bedBreakfast,
        "allInclusive" -> allInclusive
    )
}

case object RoomExtraOptions {
    def fromMSA(msa: MSA): RoomExtraOptions = RoomExtraOptions(
        msa("extraBed").toString.toBoolean,
        msa("child").toString.toBoolean,
        msa("bedBreakfast").toString.toBoolean,
        msa("allInclusive").toString.toBoolean
    )
}

case class RoomReservation(
                              id: String = Utils.getId,
                              client: HotelClient,
                              checkInDate: String,
                              checkOutDate: String,
                              extraOptions: RoomExtraOptions
                          ) {
    def toMSA: MSA = {
        val msa: Map[String, Any] = Map(
            "id" -> id,
            "checkInDate" -> checkInDate,
            "checkOutDate" -> checkOutDate,
            "extraOptions" -> extraOptions.toMSA
        )
        val clientMSA: Map[String, MSA] = client match {
            case client: Client => Map("client" -> client.toMsa)
            case group: GroupOfClients => Map("group" -> group.toMsa)
        }
        msa ++ clientMSA
    }
}

case object RoomReservation {
    def fromMSA(msa: MSA): RoomReservation = {
        val client: HotelClient = if (msa.contains("client")) {
            Client.fromMSA(msa("client").asInstanceOf[MSA])
        } else if (msa.contains("group")) {
            GroupOfClients.fromMSA(msa("group").asInstanceOf[MSA])
        } else {
            sys.error("Client was not found")
        }
        RoomReservation(
            msa("id").toString,
            client,
            msa("checkInDate").toString,
            msa("checkOutDate").toString,
            RoomExtraOptions.fromMSA(msa("extraOptions").asInstanceOf[MSA])
        )
    }
}

package client

import Hotel.MSA
import util.Utils

trait HotelClient {
    def toMSA: MSA = this match {
        case client: Client => client.toMsa
        case group: GroupOfClients => group.toMsa
    }
}

case class Client(id: String = Utils.getId, lastName: String, firstName: String, surName: String,
                  birthDate: String, seriesNumberPassport: String, address: String) extends HotelClient{
    def toMsa: MSA = Map(
        "id" -> id,
        "lastName" -> lastName,
        "firstName" -> firstName,
        "surName" -> surName,
        "birthDate" -> birthDate,
        "seriesNumberPassport" -> seriesNumberPassport,
        "address" -> address
    )
}

case object Client extends HotelClient {
    def fromMSA(msa: MSA): Client = Client(
        msa("id").toString,
        msa.getOrElse("lastName", sys.error("Last Name was not found in db")).toString,
        msa.getOrElse("firstName", sys.error("First Name was not found in db")).toString,
        msa.getOrElse("surName", sys.error("Surname was not found in db")).toString,
        msa.getOrElse("birthDate", sys.error("Birth Date was not found in db")).asInstanceOf[String],
        msa.getOrElse("seriesNumberPassport", sys.error("Series and Number Passport were not found in db")).toString,
        msa.getOrElse("address", sys.error("Address were not found in db")).toString
    )
}

case class GroupOfClients(id: String = Utils.getId, client: Client, totalCountOfClients: Int, description: Option[String] = None) extends HotelClient {
    def toMsa: MSA = Map(
        "id" -> id,
        "client" -> client.toMSA,
        "description" -> description.getOrElse(""),
        "totalCountOfClients" -> totalCountOfClients
    )
}

case object  GroupOfClients extends HotelClient {
    def fromMSA(msa: MSA): GroupOfClients = GroupOfClients(
        msa("id").toString,
        Client.fromMSA(msa("client").asInstanceOf[MSA]),
        msa("totalCountOfClients").toString.toInt,
        if (msa("description").toString.isEmpty) None else Some(msa("description").toString)
    )
}

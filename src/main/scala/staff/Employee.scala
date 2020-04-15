package staff

import Hotel.MSA
import java.time.LocalDate

case class Employee(lastName: String, firstName: String, surName: String,
                    birthDate: LocalDate, seriesNumberPassport: String, address: String, salary: String) {
  def toMSA = Map(
    "lastName" -> lastName,
    "firstName" -> firstName,
    "surName" -> surName,
    "birthDate" -> birthDate.toString,
    "seriesNumberPassport" -> seriesNumberPassport,
    "address" -> address,
    "salary" -> salary
  )
}

case object Employee {
  def fromMSA(msa: MSA): Employee = Employee(
    msa.getOrElse("lastName", sys.error("Last Name was not found in db")).toString,
    msa.getOrElse("firstName", sys.error("First Name was not found in db")).toString,
    msa.getOrElse("surName", sys.error("Surname was not found in db")).toString,
    LocalDate.parse(msa.getOrElse("birthDate", sys.error("Birth Date was not found in db")).asInstanceOf[String]),
    msa.getOrElse("seriesNumberPassport", sys.error("Series and Number Passport were not found in db")).toString,
    msa.getOrElse("address", sys.error("Address were not found in db")).toString,
    msa.getOrElse("salary", sys.error("Salary was not found in db")).toString
  )
}

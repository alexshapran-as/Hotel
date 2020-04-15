package authenticator

import Hotel.MSA
import dao.MainDAO
import staff.Employee

object Roles extends Enumeration {
  val ADMIN, MANAGER, STAFF = Value
}

case class UserAuthData(username: String, passwordHash: String, roles: List[Roles.Value], employee: Employee) {
  def toMSA = Map("username" -> username, "passwordHash" -> passwordHash, "roles" -> roles.map(_.toString), "employee" -> employee.toMSA)

  def save = MainDAO.tryToGetUserAuthData(username) match {
    case None =>
      MainDAO.saveUserAuthData(username, this.toMSA)
      true
    case Some(_) =>
      false
  }
}

case object UserAuthData {
  def toRole(role: String) = role.toUpperCase match {
    case "ADMIN" => Roles.ADMIN
    case "MANAGER" => Roles.MANAGER
    case "STAFF" => Roles.STAFF
  }

  def fromMSA(msa: MSA): UserAuthData = UserAuthData(
    msa.getOrElse("username", sys.error("Username was not found in db")).toString,
    msa.getOrElse("passwordHash", sys.error("Password was not found in db")).toString,
    msa.getOrElse("roles", sys.error("Role was not found in db")).asInstanceOf[List[String]].map(role => toRole(role)),
    staff.Employee.fromMSA(msa.getOrElse("employee", sys.error("Employee was not found in db")).asInstanceOf[MSA])
  )

  def find(username: String) = MainDAO.tryToGetUserAuthData(username)

  def findAll = MainDAO.tryToGetAllUserAuthData

  def findAllEmployees = MainDAO.tryToGetAllUserAuthData.map(msa => msa("employee").asInstanceOf[MSA] ++ Map("username" -> msa("username")))

  def delete(username: String) = MainDAO.removeUserAuthData(username)

  def create(username: String, password: String, roles: List[Roles.Value], employee: Employee) =
    UserAuthData(username, HMAC.generateHMAC(password), roles, employee: Employee)
}

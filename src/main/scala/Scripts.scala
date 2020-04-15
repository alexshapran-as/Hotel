import java.time.LocalDate
import java.util.Date

import authenticator.{Roles, UserAuthData}
import staff.Employee
import org.joda.time.DateTime

object Scripts {
  def main(args: Array[String]) = {
    UserAuthData.create("admin", "pass", List(Roles.ADMIN), Employee("Иванов", "Иван", "Иванович", LocalDate.of(1998, 5, 13), "1234567890", "Москва, Ленинский проспект, 13", "50000")).save
  }
}

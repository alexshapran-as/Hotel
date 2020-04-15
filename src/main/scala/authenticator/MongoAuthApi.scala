package authenticator

object MongoAuthApi {
  def authorize(username: String, password: String): Option[List[String]] = UserAuthData.find(username) match {
    case None =>
      None
    case Some(UserAuthData(_, passwordHash, roles, _)) =>
      if (HMAC.hashIsValid(password, passwordHash))
        Some(roles.map(_.toString))
      else
        None
  }

  def hasAccessToWithRoles(pathPrefix: String, roles: List[String]): Boolean =
    if (roles.contains(Roles.ADMIN.toString)) true else roles.contains(pathPrefix.toUpperCase)
}

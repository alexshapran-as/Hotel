package api.web

import java.util.Date

import Hotel.{MSA, MSS}
import com.softwaremill.session.{MultiValueSessionSerializer, RefreshTokenData}
import org.slf4j.LoggerFactory
import dao.MainDAO

import scala.util.{Failure, Success, Try}

case class UserSessionData(username: String, groups: List[String]) {
  def toMSA(data: RefreshTokenData[UserSessionData], iat: Long) = Map(
    "_id" -> data.selector,
    "username" -> data.forSession.username,
    "groups" -> data.forSession.groups,
    "tokenHash" -> data.tokenHash,
    "exp" -> new Date(data.expires),
    "iat" -> new Date(iat)
  )
}

case object UserSessionData {
  protected val logger = LoggerFactory.getLogger(getClass)

  implicit def serializer: MultiValueSessionSerializer[UserSessionData] =
    new MultiValueSessionSerializer(
      toMap = (userSessionData: UserSessionData) =>
        Map (
          "username" -> userSessionData.username,
          "groups" -> userSessionData.groups.mkString(";")
        ),
      fromMap = (mss: MSS) =>
        Try {
          UserSessionData(
            username = mss("username"),
            groups = mss("groups").split(";").toList
          )
        }
    )

  def fromMSAToRefTokenData(msa: MSA) = Try {
    RefreshTokenData[UserSessionData](
      forSession = UserSessionData(msa("username").toString, msa("groups").asInstanceOf[List[String]]),
      selector = msa("_id").toString,
      tokenHash = msa("tokenHash").toString,
      expires = msa("exp").asInstanceOf[Date].getTime
    )
  } match {
    case Failure(e) =>
      logger.error(s"Error mapping user session data with selector = ${msa("_id").toString} (${e.getMessage})")
      throw e
    case Success(value) =>
      value
  }

  def fromMSAToUserSessionData(msa: MSA) = Try {
    UserSessionData(
      msa("username").toString,
      msa("groups").asInstanceOf[List[String]]
    )
  } match {
    case Failure(e) =>
      logger.error(s"Error mapping user session data with selector = ${msa("_id").toString} (${e.getMessage})")
      throw e
    case Success(value) =>
      value
  }

  def save(data: RefreshTokenData[UserSessionData], iat: Long): Unit = {
    MainDAO.saveUserSessionData(
      selector = data.selector,
      userSessionDataMSA = data.forSession.toMSA(data, iat)
    )
    MainDAO.createIndexesIfNotExists()
  }

  def remove(selector: String): Unit = MainDAO.removeUserSessionData(selector)

  def findBySelector(selector: String): Option[RefreshTokenData[UserSessionData]] = MainDAO.tryToGetRefTokenData(selector)
}
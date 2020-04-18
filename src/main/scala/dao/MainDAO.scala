package dao

import java.io.File

import Hotel.MSA
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.gridfs.GridFS
import MongoUtils._
import com.softwaremill.session.RefreshTokenData
import room.Room
//import org.slf4j.LoggerFactory
import api.web.UserSessionData
import authenticator.UserAuthData

object MainDAO {
//  protected val logger = LoggerFactory.getLogger(getClass)

  private[dao] val mainDb = getMongoDbConnection("main")

  protected val sessionDataColl = mainDb("session_data")
  protected val authDataColl = mainDb("auth_data")
  protected val roomsColl = mainDb("rooms")

  /*
   * gridFS
   */
//  private val commonDB = getMongoDbConnection("common")
//
//  private val gridFS = GridFS(commonDB)
//
//  def writeGridFSFile2File(gridFSFileId: String, filePath: String): Unit = writeGridFSFile2File(gridFSFileId, new File(filePath))
//
//  def writeGridFSFile2File(gridFSFileId: String, file: File): Unit = MainDAO.gridFS.findOne(gridFSFileId).map(x => x.writeTo(file)).getOrElse(throw new RuntimeException(s"file with id $gridFSFileId not found in CommonDB"))
//
//  def writeFileWithId(id: String, file: File) = MainDAO.gridFS(file) { f => f.filename = id }
//
//  def writeFileAsBytesWithId(id: String, bytes: Array[Byte]) = MainDAO.gridFS(bytes) { f => f.filename = id }

  /*
   * Admin methods
   */


  /*
   * User session data methods
   */

  def saveUserSessionData(selector: String, userSessionDataMSA: MSA): Unit =
    sessionDataColl.update("_id" $eq selector, map2dbo(userSessionDataMSA), upsert = true)

  def tryToGetUserSessionData(selector: String): Option[UserSessionData] =
    sessionDataColl.findOne("_id" $eq selector).map(x => UserSessionData.fromMSAToUserSessionData(dbo2map(x)))

  def tryToGetRefTokenData(selector: String): Option[RefreshTokenData[UserSessionData]] =
    sessionDataColl.findOne("_id" $eq selector).map(x => UserSessionData.fromMSAToRefTokenData(dbo2map(x)))

  def removeUserSessionData(selector: String): Unit =
    sessionDataColl.remove(MongoDBObject("_id" -> selector))

  /*
   * User auth data methods
   */

  def saveUserAuthData(username: String, userAuthDataMSA: MSA): Unit =
    authDataColl.update("username" $eq username, map2dbo(userAuthDataMSA), upsert = true)

  def tryToGetUserAuthData(username: String): Option[UserAuthData] =
    authDataColl.findOne("username" $eq username).map(x => UserAuthData.fromMSA(dbo2map(x)))

  def tryToGetAllUserAuthData: List[MSA] =
    authDataColl.find().map(x => dbo2map(x)).toList

  def removeUserAuthData(username: String): Unit =
    authDataColl.remove(MongoDBObject("username" -> username))

  /*
   * Rooms data methods
   */

  def saveRoom(id: String, roomMSA: MSA): Unit =
    roomsColl.update("_id" $eq id, map2dbo(roomMSA), upsert = true)

  private def findRooms(search: DBObject): List[Room] = {
    roomsColl.find(search)
        .map(roomMSA => Room.fromMSA(dbo2map(roomMSA)))
        .toList
  }

  def getAllRooms: List[Room] = findRooms(DBObject.empty)

  def getAllRoomsMSA: List[MSA] =
    roomsColl.find().map(x => dbo2map(x)).toList

  def tryToGetRoom(roomId: String): Option[Room] = {
    roomsColl.findOne("_id" $eq roomId).map(roomMSA => Room.fromMSA(dbo2map(roomMSA)))
  }

  def getRoom(roomId: String): Room = {
    tryToGetRoom(roomId).getOrElse(sys.error(s"Room with id = $roomId not found in db"))
  }


  // --------------------------------------------------------------------------------
  // create indexes
  // --------------------------------------------------------------------------------

  def createIndexesIfNotExists(): Unit = {
    createTTLIndex(sessionDataColl, Map("exp" -> 1))
  }

}
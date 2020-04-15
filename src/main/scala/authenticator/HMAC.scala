package authenticator

import configurations.Conf
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object HMAC {

  def generateHMAC(preHashString: String): String = {
    val secret: SecretKeySpec = new javax.crypto.spec.SecretKeySpec(Conf.confSecretKey.getBytes("UTF-8"), "HmacSHA256")
    val mac: Mac = javax.crypto.Mac.getInstance("HmacSHA256")
    mac.init(secret)
    val result: Array[Byte] = mac.doFinal(preHashString.replaceAll("\n", "").replaceAll("\\s", "").getBytes("UTF-8"))
    new sun.misc.BASE64Encoder().encode(result)
  }

  def hashIsValid(preHashString: String, hash: String): Boolean = generateHMAC(preHashString) == hash

}

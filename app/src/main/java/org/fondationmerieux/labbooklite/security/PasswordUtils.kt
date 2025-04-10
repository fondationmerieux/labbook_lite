package org.fondationmerieux.labbooklite.security

/**
 * Created by AlC on 01/04/2025.
 */
fun getPasswordDB(pwd: String, storedHash: String): String {
    val saltStart = storedHash.indexOf(":")
    if (saltStart == -1) return ""
    val salt = storedHash.substring(saltStart + 1)

    val lenPwd = pwd.length
    val lenStart = lenPwd - 1
    val lenEnd = 40 - lenPwd + lenStart
    val partSalt = salt.substring(lenStart, lenEnd)

    val md5 = java.security.MessageDigest.getInstance("MD5")
    val sha1 = java.security.MessageDigest.getInstance("SHA-1")

    val pwdMd5 = md5.digest((pwd + partSalt).toByteArray()).joinToString("") { "%02x".format(it) }
    val pwdSha1 = sha1.digest(pwdMd5.toByteArray()).joinToString("") { "%02x".format(it) }

    return "$pwdSha1:$salt"
}

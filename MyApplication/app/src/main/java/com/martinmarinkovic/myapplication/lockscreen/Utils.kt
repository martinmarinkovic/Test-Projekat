package com.martinmarinkovic.myapplication.lockscreen

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class Utils {

    companion object {
        private fun bytesToHexString(bytes: ByteArray): String {
            val sb = StringBuffer()
            for (i in bytes) {
                val hex = Integer.toHexString(0xFF and i.toInt())
                if (hex.length == 1) {
                    sb.append('0')
                }
                sb.append(hex)
            }
            return sb.toString()
        }

        fun sha256(s: String): String {
            val digest: MessageDigest
            val hash: String
            return try {
                digest = MessageDigest.getInstance("SHA-256")
                digest.update(s.toByteArray())
                hash = bytesToHexString(digest.digest())
                hash
            } catch (e1: NoSuchAlgorithmException) {
                s
            }
        }
    }
}
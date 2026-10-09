package com.example.crypto

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import org.json.JSONObject

data class EncryptedBundle(
    val cipherTextBase64: String,
    val ivBase64: String,
    val saltBase64: String
)

object CryptoManager {
    private const val PBKDF2_ITERATIONS = 65536
    private const val KEY_LENGTH = 256
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128
    private const val CLIENT_AADHAAR_SALT = "DHRITI_SECURE_GOV_INDIA_SALT_2026"
    private const val JWT_SECRET = "DHRITI_DEMO_JWT_SIGNING_KEY_XYZ_987654"

    private val secureRandom = SecureRandom()

    /**
     * Client-side SHA-256 hashing of Aadhaar number.
     * Raw Aadhaar is never saved in plaintext.
     */
    fun hashAadhaar(rawAadhaar: String): String {
        val cleanNumber = rawAadhaar.replace(" ", "").replace("-", "")
        val salted = cleanNumber + CLIENT_AADHAAR_SALT
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(salted.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    /**
     * Formats and masks Aadhaar number to XXXX-XXXX-1234.
     */
    fun maskAadhaar(rawAadhaar: String): String {
        val digits = rawAadhaar.filter { it.isDigit() }
        if (digits.length < 4) return "XXXX-XXXX-XXXX"
        val last4 = digits.takeLast(4)
        return "XXXX-XXXX-$last4"
    }

    /**
     * Sanitizes user input to prevent injection / malicious scripting.
     */
    fun sanitizeInput(input: String): String {
        return input
            .replace("<script", "&lt;script")
            .replace("</script>", "&lt;/script&gt;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .trim()
    }

    /**
     * Derives an AES-256 key using PBKDF2 with high iteration count.
     */
    fun deriveKey(password: String, salt: ByteArray): SecretKey {
        val spec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val secretBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(secretBytes, "AES")
    }

    /**
     * Encrypts plaintext using AES-256-GCM.
     */
    fun encryptAesGcm(plainText: String, secretKey: SecretKey): EncryptedBundle {
        val iv = ByteArray(GCM_IV_LENGTH).also { secureRandom.nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)

        val cipherBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        return EncryptedBundle(
            cipherTextBase64 = Base64.encodeToString(cipherBytes, Base64.NO_WRAP),
            ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP),
            saltBase64 = ""
        )
    }

    /**
     * Decrypts AES-256-GCM cipher text.
     */
    fun decryptAesGcm(cipherTextBase64: String, ivBase64: String, secretKey: SecretKey): String {
        val cipherBytes = Base64.decode(cipherTextBase64, Base64.NO_WRAP)
        val iv = Base64.decode(ivBase64, Base64.NO_WRAP)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

        val plainBytes = cipher.doFinal(cipherBytes)
        return String(plainBytes, Charsets.UTF_8)
    }

    /**
     * Generates a mock JWT token held strictly in memory.
     */
    fun generateMockJwt(email: String, fullName: String): String {
        val headerJson = JSONObject().apply {
            put("alg", "HS256")
            put("typ", "JWT")
        }
        val now = System.currentTimeMillis() / 1000
        val payloadJson = JSONObject().apply {
            put("sub", email)
            put("name", fullName)
            put("iss", "dhriti-safety-gov-in")
            put("iat", now)
            put("exp", now + 3600) // 1 hour validity
            put("scope", "citizen:safety")
        }

        val headerBase64 = Base64.encodeToString(headerJson.toString().toByteArray(), Base64.NO_WRAP or Base64.URL_SAFE)
        val payloadBase64 = Base64.encodeToString(payloadJson.toString().toByteArray(), Base64.NO_WRAP or Base64.URL_SAFE)

        val rawSignature = "$headerBase64.$payloadBase64"
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(JWT_SECRET.toByteArray(), "HmacSHA256"))
        val signatureBytes = mac.doFinal(rawSignature.toByteArray())
        val signatureBase64 = Base64.encodeToString(signatureBytes, Base64.NO_WRAP or Base64.URL_SAFE)

        return "$headerBase64.$payloadBase64.$signatureBase64"
    }

    fun generateComplaintHash(complaintId: String, narrative: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest("$complaintId:$narrative:${System.currentTimeMillis()}".toByteArray())
        return digest.joinToString("") { "%02x".format(it) }.take(16).uppercase()
    }
}

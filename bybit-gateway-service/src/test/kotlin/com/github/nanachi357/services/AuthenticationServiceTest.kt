package com.github.nanachi357.services

import com.github.nanachi357.models.BybitCredentials
import com.github.nanachi357.models.BybitAuthHeaders
import com.github.nanachi357.models.AuthRequestContext
import com.github.nanachi357.models.AuthErrorResponse
import com.github.nanachi357.models.AuthErrorCodes
import com.github.nanachi357.utils.HmacSignatureGenerator
import kotlin.test.*

class AuthenticationServiceTest {
    
    private lateinit var authService: AuthenticationService
    private lateinit var signatureGenerator: HmacSignatureGenerator
    
    @BeforeTest
    fun setup() {
        authService = AuthenticationService()
        signatureGenerator = HmacSignatureGenerator()
    }
    
    @Test
    fun `test HMAC signature generation`() {
        val message = "api_key=test_key&timestamp=1234567890&recv_window=5000"
        
        // Create separate CharArrays for generation and validation
        val secretKey1 = "test_secret_key".toCharArray()
        val secretKey2 = "test_secret_key".toCharArray()
        
        try {
            val signature = signatureGenerator.generateSignature(secretKey1, message)
            
            assertNotNull(signature)
            assertTrue(signature.isNotBlank())
            assertTrue(signatureGenerator.validateSignature(secretKey2, message, signature))
        } finally {
            secretKey1.fill('\u0000')
            secretKey2.fill('\u0000')
        }
    }
    
    @Test
    fun `test generateGetSignature with sorted parameters`() {
        val timestamp = 1234567890L
        val queryParams = mapOf(
            "z" to "last",
            "a" to "first", 
            "b" to "second"
        )
        
        // Create separate CharArrays for generation and validation
        val secretKey1 = "test_secret".toCharArray()
        val secretKey2 = "test_secret".toCharArray()
        
        try {
            val signature = signatureGenerator.generateGetSignature(secretKey1, timestamp, queryParams)
            
            assertNotNull(signature)
            assertTrue(signature.isNotBlank())
            
            // Verify that parameters are sorted alphabetically
            val expectedQueryString = "a=first&b=second&z=last"
            assertTrue(signatureGenerator.validateSignature(secretKey2, expectedQueryString, signature))
        } finally {
            secretKey1.fill('\u0000')
            secretKey2.fill('\u0000')
        }
    }
    
    @Test
    fun `test generatePostSignature with timestamp concatenation`() {
        val timestamp = 1234567890L
        val jsonBody = """{"symbol":"BTCUSDT"}"""
        
        // Create separate CharArrays for generation and validation
        val secretKey1 = "test_secret".toCharArray()
        val secretKey2 = "test_secret".toCharArray()
        
        try {
            val signature = signatureGenerator.generatePostSignature(secretKey1, timestamp, jsonBody)
            
            assertNotNull(signature)
            assertTrue(signature.isNotBlank())
            
            // Verify that message is timestamp + jsonBody
            val expectedMessage = "$timestamp$jsonBody"
            assertTrue(signatureGenerator.validateSignature(secretKey2, expectedMessage, signature))
        } finally {
            secretKey1.fill('\u0000')
            secretKey2.fill('\u0000')
        }
    }
    
    @Test
    fun `test signature generation with empty parameters`() {
        val secretKey = "test_secret".toCharArray()
        val timestamp = 1234567890L
        
        try {
            // Empty query params should work (empty string)
            val emptyGetSignature = signatureGenerator.generateGetSignature(secretKey, timestamp, emptyMap())
            
            // Empty JSON body should work (empty string)
            val emptyPostSignature = signatureGenerator.generatePostSignature(secretKey, timestamp, "")
            
            assertNotNull(emptyGetSignature)
            assertNotNull(emptyPostSignature)
            assertTrue(emptyGetSignature.isNotBlank())
            assertTrue(emptyPostSignature.isNotBlank())
        } finally {
            secretKey.fill('\u0000')
        }
    }
    
    @Test
    fun `test signature generation with special characters`() {
        val timestamp = 1234567890L
        val specialParams = mapOf(
            "symbol" to "BTC/USDT",
            "price" to "50,000.00",
            "amount" to "0.001"
        )
        
        // Create separate CharArrays for generation and validation
        val secretKey1 = "test_secret".toCharArray()
        val secretKey2 = "test_secret".toCharArray()
        
        try {
            val signature = signatureGenerator.generateGetSignature(secretKey1, timestamp, specialParams)
            
            assertNotNull(signature)
            assertTrue(signature.isNotBlank())
            
            // Verify sorted parameters with special characters
            val expectedQueryString = "amount=0.001&price=50,000.00&symbol=BTC/USDT"
            assertTrue(signatureGenerator.validateSignature(secretKey2, expectedQueryString, signature))
        } finally {
            secretKey1.fill('\u0000')
            secretKey2.fill('\u0000')
        }
    }
    
    @Test
    fun `test GET request authentication headers`() {
        val queryParams = mapOf(
            "category" to "spot",
            "accountType" to "UNIFIED"
        )
        
        // This test will fail if environment variables are not set
        // For testing, we need to set BYBIT_API_KEY and BYBIT_SECRET_KEY
        try {
            val headers = authService.generateGetAuthHeaders(queryParams)
            
            assertTrue(headers.apiKey.isNotBlank())
            assertTrue(headers.timestamp > 0)
            assertNotNull(headers.signature)
            assertEquals(5000L, headers.recvWindow)
        } catch (e: SecurityException) {
            // Expected if environment variables are not set
            assertTrue(e.message?.contains("BYBIT_") == true)
        }
    }
    
    @Test
    fun `test POST request authentication headers`() {
        val jsonBody = """{"category":"spot","symbol":"BTCUSDT"}"""
        
        // This test will fail if environment variables are not set
        try {
            val headers = authService.generatePostAuthHeaders(jsonBody)
            
            assertTrue(headers.apiKey.isNotBlank())
            assertTrue(headers.timestamp > 0)
            assertNotNull(headers.signature)
        } catch (e: SecurityException) {
            // Expected if environment variables are not set
            assertTrue(e.message?.contains("BYBIT_") == true)
        }
    }
    
    @Test
    fun `test timestamp validation`() {
        val currentTime = System.currentTimeMillis()
        
        // Valid timestamp (within window)
        assertTrue(authService.isTimestampValid(currentTime))
        
        // Invalid timestamp (too old)
        assertFalse(authService.isTimestampValid(currentTime - 10000))
        
        // Invalid timestamp (too new)
        assertFalse(authService.isTimestampValid(currentTime + 10000))
    }
    
    @Test
    fun `test timestamp validation with custom recvWindow`() {
        val currentTime = System.currentTimeMillis()
        val customRecvWindow = 10000L
        
        // Valid timestamp within custom window
        assertTrue(authService.isTimestampValid(currentTime, customRecvWindow))
        
        // Invalid timestamp outside custom window
        assertFalse(authService.isTimestampValid(currentTime - 15000, customRecvWindow))
        assertFalse(authService.isTimestampValid(currentTime + 15000, customRecvWindow))
    }
    
    @Test
    fun `test authentication response validation`() {
        // Success response
        val successResponse = AuthErrorResponse(retCode = 0, retMsg = "OK")
        assertTrue(authService.validateAuthResponse(successResponse))
        
        // Invalid API key
        val invalidKeyResponse = AuthErrorResponse(
            retCode = AuthErrorCodes.INVALID_API_KEY,
            retMsg = "Invalid API key"
        )
        assertFalse(authService.validateAuthResponse(invalidKeyResponse))
        
        // Invalid signature
        val invalidSigResponse = AuthErrorResponse(
            retCode = AuthErrorCodes.INVALID_SIGNATURE,
            retMsg = "Invalid signature"
        )
        assertFalse(authService.validateAuthResponse(invalidSigResponse))
        
        // Invalid timestamp
        val invalidTimestampResponse = AuthErrorResponse(
            retCode = AuthErrorCodes.INVALID_TIMESTAMP,
            retMsg = "Invalid timestamp"
        )
        assertFalse(authService.validateAuthResponse(invalidTimestampResponse))
        
        // Rate limit exceeded
        val rateLimitResponse = AuthErrorResponse(
            retCode = AuthErrorCodes.RATE_LIMIT_EXCEEDED,
            retMsg = "Rate limit exceeded"
        )
        assertFalse(authService.validateAuthResponse(rateLimitResponse))
        
        // Unknown error code
        val unknownErrorResponse = AuthErrorResponse(
            retCode = 99999,
            retMsg = "Unknown error"
        )
        assertFalse(authService.validateAuthResponse(unknownErrorResponse))
    }
    
    @Test
    fun `test random string generation`() {
        val randomString = authService.generateRandomString(16)
        
        assertEquals(16, randomString.length)
        assertTrue(randomString.matches(Regex("[A-Za-z0-9]+")))
    }
    
    @Test
    fun `test random string generation with default length`() {
        val randomString = authService.generateRandomString()
        
        assertEquals(32, randomString.length)
        assertTrue(randomString.matches(Regex("[A-Za-z0-9]+")))
    }
    
    @Test
    fun `test BybitCredentials validation`() {
        // Valid credentials
        val validCredentials = BybitCredentials(
            apiKey = "valid_key",
            secretKey = "valid_secret"
        )
        assertEquals("valid_key", validCredentials.apiKey)
        assertEquals("valid_secret", validCredentials.secretKey)
        
        // Invalid credentials should throw exception
        assertFailsWith<IllegalArgumentException> {
            BybitCredentials(apiKey = "", secretKey = "valid_secret")
        }
        
        assertFailsWith<IllegalArgumentException> {
            BybitCredentials(apiKey = "valid_key", secretKey = "")
        }
        
        assertFailsWith<IllegalArgumentException> {
            BybitCredentials(apiKey = "   ", secretKey = "valid_secret")
        }
    }
    
    @Test
    fun `test BybitAuthHeaders toMap conversion`() {
        val headers = BybitAuthHeaders(
            apiKey = "test_key",
            timestamp = 1234567890L,
            signature = "test_signature",
            recvWindow = 5000L
        )
        
        val headerMap = headers.toMap()
        
        assertEquals("test_key", headerMap["X-BAPI-API-KEY"])
        assertEquals("1234567890", headerMap["X-BAPI-TIMESTAMP"])
        assertEquals("test_signature", headerMap["X-BAPI-SIGN"])
        assertEquals("5000", headerMap["X-BAPI-RECV-WINDOW"])
    }
    
    @Test
    fun `test AuthRequestContext with testnet credentials`() {
        val credentials = BybitCredentials(
            apiKey = "test_key",
            secretKey = "test_secret", 
            testnet = true
        )
        
        val context = AuthRequestContext(credentials)
        
        assertEquals("https://api-testnet.bybit.com", context.getBaseUrl())
        assertEquals(credentials, context.credentials)
        assertTrue(context.timestamp > 0)
        assertEquals(5000L, context.recvWindow)
    }
    
    @Test
    fun `test AuthRequestContext with mainnet credentials`() {
        val credentials = BybitCredentials(
            apiKey = "test_key",
            secretKey = "test_secret",
            testnet = false
        )
        
        val context = AuthRequestContext(credentials)
        
        assertEquals("https://api.bybit.com", context.getBaseUrl())
    }
    
    @Test
    fun `test AuthRequestContext with custom timestamp and recvWindow`() {
        val credentials = BybitCredentials("test_key", "test_secret")
        val customTimestamp = 1234567890L
        val customRecvWindow = 10000L
        
        val context = AuthRequestContext(
            credentials = credentials,
            timestamp = customTimestamp,
            recvWindow = customRecvWindow
        )
        
        assertEquals(customTimestamp, context.timestamp)
        assertEquals(customRecvWindow, context.recvWindow)
        assertEquals(credentials, context.credentials)
    }
    
    @Test
    fun `test signature validation with different messages`() {
        val message1 = "test_message_1"
        val message2 = "test_message_2"
        
        // Create separate CharArrays for each operation
        val secretKey1 = "test_secret".toCharArray()
        val secretKey2 = "test_secret".toCharArray()
        val secretKey3 = "test_secret".toCharArray()
        val secretKey4 = "test_secret".toCharArray()
        val secretKey5 = "test_secret".toCharArray()
        val secretKey6 = "test_secret".toCharArray()
        
        try {
            val signature1 = signatureGenerator.generateSignature(secretKey1, message1)
            val signature2 = signatureGenerator.generateSignature(secretKey2, message2)
            
            // Same message should validate
            assertTrue(signatureGenerator.validateSignature(secretKey3, message1, signature1))
            assertTrue(signatureGenerator.validateSignature(secretKey4, message2, signature2))
            
            // Different messages should not validate
            assertFalse(signatureGenerator.validateSignature(secretKey5, message1, signature2))
            assertFalse(signatureGenerator.validateSignature(secretKey6, message2, signature1))
        } finally {
            secretKey1.fill('\u0000')
            secretKey2.fill('\u0000')
            secretKey3.fill('\u0000')
            secretKey4.fill('\u0000')
            secretKey5.fill('\u0000')
            secretKey6.fill('\u0000')
        }
    }
    
    @Test
    fun `test signature generation with unicode characters`() {
        val timestamp = 1234567890L
        val unicodeParams = mapOf(
            "symbol" to "BTC/USDT",
            "name" to "Bitcoin",
            "description" to "Криптовалюта"
        )
        
        // Create separate CharArrays for generation and validation
        val secretKey1 = "test_secret".toCharArray()
        val secretKey2 = "test_secret".toCharArray()
        
        try {
            val signature = signatureGenerator.generateGetSignature(secretKey1, timestamp, unicodeParams)
            
            assertNotNull(signature)
            assertTrue(signature.isNotBlank())
            
            // Verify sorted parameters with unicode
            val expectedQueryString = "description=Криптовалюта&name=Bitcoin&symbol=BTC/USDT"
            assertTrue(signatureGenerator.validateSignature(secretKey2, expectedQueryString, signature))
        } finally {
            secretKey1.fill('\u0000')
            secretKey2.fill('\u0000')
        }
    }
}

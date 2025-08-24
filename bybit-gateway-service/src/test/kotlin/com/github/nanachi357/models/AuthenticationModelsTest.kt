package com.github.nanachi357.models

import kotlin.test.*

class AuthenticationModelsTest {
    
    @Test
    fun `test BybitCredentials with valid data`() {
        val credentials = BybitCredentials(
            apiKey = "test_api_key",
            secretKey = "test_secret_key",
            testnet = true
        )
        
        assertEquals("test_api_key", credentials.apiKey)
        assertEquals("test_secret_key", credentials.secretKey)
        assertTrue(credentials.testnet)
    }
    
    @Test
    fun `test BybitCredentials with mainnet`() {
        val credentials = BybitCredentials(
            apiKey = "test_api_key",
            secretKey = "test_secret_key",
            testnet = false
        )
        
        assertFalse(credentials.testnet)
    }
    
    @Test
    fun `test BybitCredentials validation with empty apiKey`() {
        assertFailsWith<IllegalArgumentException> {
            BybitCredentials(apiKey = "", secretKey = "valid_secret")
        }
    }
    
    @Test
    fun `test BybitCredentials validation with empty secretKey`() {
        assertFailsWith<IllegalArgumentException> {
            BybitCredentials(apiKey = "valid_key", secretKey = "")
        }
    }
    
    @Test
    fun `test BybitCredentials validation with blank apiKey`() {
        assertFailsWith<IllegalArgumentException> {
            BybitCredentials(apiKey = "   ", secretKey = "valid_secret")
        }
    }
    
    @Test
    fun `test BybitCredentials validation with blank secretKey`() {
        assertFailsWith<IllegalArgumentException> {
            BybitCredentials(apiKey = "valid_key", secretKey = "   ")
        }
    }
    
    @Test
    fun `test BybitAuthHeaders with default recvWindow`() {
        val headers = BybitAuthHeaders(
            apiKey = "test_key",
            timestamp = 1234567890L,
            signature = "test_signature"
        )
        
        assertEquals("test_key", headers.apiKey)
        assertEquals(1234567890L, headers.timestamp)
        assertEquals("test_signature", headers.signature)
        assertEquals(5000L, headers.recvWindow) // Default value
    }
    
    @Test
    fun `test BybitAuthHeaders with custom recvWindow`() {
        val headers = BybitAuthHeaders(
            apiKey = "test_key",
            timestamp = 1234567890L,
            signature = "test_signature",
            recvWindow = 10000L
        )
        
        assertEquals(10000L, headers.recvWindow)
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
        
        // Verify all required headers are present
        assertEquals(4, headerMap.size)
        assertTrue(headerMap.containsKey("X-BAPI-API-KEY"))
        assertTrue(headerMap.containsKey("X-BAPI-TIMESTAMP"))
        assertTrue(headerMap.containsKey("X-BAPI-SIGN"))
        assertTrue(headerMap.containsKey("X-BAPI-RECV-WINDOW"))
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
    fun `test AuthRequestContext with default values`() {
        val credentials = BybitCredentials("test_key", "test_secret")
        val context = AuthRequestContext(credentials)
        
        assertTrue(context.timestamp > 0)
        assertEquals(5000L, context.recvWindow)
        assertEquals(credentials, context.credentials)
    }
    
    @Test
    fun `test AuthErrorResponse with minimal data`() {
        val response = AuthErrorResponse(
            retCode = 0,
            retMsg = "OK"
        )
        
        assertEquals(0, response.retCode)
        assertEquals("OK", response.retMsg)
        assertNull(response.retExtInfo)
        assertTrue(response.time > 0)
    }
    
    @Test
    fun `test AuthErrorResponse with extended info`() {
        val extendedInfo = mapOf(
            "errorCode" to "10001",
            "errorMessage" to "Invalid API key"
        )
        
        val response = AuthErrorResponse(
            retCode = 10001,
            retMsg = "Invalid API key",
            retExtInfo = extendedInfo,
            time = 1234567890L
        )
        
        assertEquals(10001, response.retCode)
        assertEquals("Invalid API key", response.retMsg)
        assertEquals(extendedInfo, response.retExtInfo)
        assertEquals(1234567890L, response.time)
    }
    
    @Test
    fun `test AuthErrorResponse with custom time`() {
        val customTime = 1234567890L
        val response = AuthErrorResponse(
            retCode = 0,
            retMsg = "OK",
            time = customTime
        )
        
        assertEquals(customTime, response.time)
    }
    
    @Test
    fun `test AuthErrorCodes constants`() {
        assertEquals(10001, AuthErrorCodes.INVALID_API_KEY)
        assertEquals(10002, AuthErrorCodes.INVALID_SIGNATURE)
        assertEquals(10003, AuthErrorCodes.INVALID_TIMESTAMP)
        assertEquals(10004, AuthErrorCodes.INVALID_RECV_WINDOW)
        assertEquals(10005, AuthErrorCodes.RATE_LIMIT_EXCEEDED)
        assertEquals(10006, AuthErrorCodes.INSUFFICIENT_PERMISSIONS)
    }
    
    @Test
    fun `test BybitCredentials data class equality`() {
        val credentials1 = BybitCredentials("key1", "secret1", true)
        val credentials2 = BybitCredentials("key1", "secret1", true)
        val credentials3 = BybitCredentials("key2", "secret1", true)
        
        assertEquals(credentials1, credentials2)
        assertNotEquals(credentials1, credentials3)
    }
    
    @Test
    fun `test BybitAuthHeaders data class equality`() {
        val headers1 = BybitAuthHeaders("key1", 123L, "sig1", 5000L)
        val headers2 = BybitAuthHeaders("key1", 123L, "sig1", 5000L)
        val headers3 = BybitAuthHeaders("key2", 123L, "sig1", 5000L)
        
        assertEquals(headers1, headers2)
        assertNotEquals(headers1, headers3)
    }
    
    @Test
    fun `test AuthRequestContext data class equality`() {
        val credentials = BybitCredentials("key1", "secret1")
        val context1 = AuthRequestContext(credentials, 123L, 5000L)
        val context2 = AuthRequestContext(credentials, 123L, 5000L)
        val context3 = AuthRequestContext(credentials, 456L, 5000L)
        
        assertEquals(context1, context2)
        assertNotEquals(context1, context3)
    }
    
    @Test
    fun `test AuthErrorResponse data class equality`() {
        val response1 = AuthErrorResponse(0, "OK", null, 123L)
        val response2 = AuthErrorResponse(0, "OK", null, 123L)
        val response3 = AuthErrorResponse(1, "OK", null, 123L)
        
        assertEquals(response1, response2)
        assertNotEquals(response1, response3)
    }
    
    @Test
    fun `test BybitCredentials toString representation`() {
        val credentials = BybitCredentials("test_key", "test_secret", true)
        val toString = credentials.toString()
        
        assertTrue(toString.contains("test_key"))
        assertTrue(toString.contains("test_secret"))
        assertTrue(toString.contains("testnet=true"))
    }
    
    @Test
    fun `test BybitAuthHeaders toString representation`() {
        val headers = BybitAuthHeaders("test_key", 123L, "test_sig", 5000L)
        val toString = headers.toString()
        
        assertTrue(toString.contains("test_key"))
        assertTrue(toString.contains("123"))
        assertTrue(toString.contains("test_sig"))
        assertTrue(toString.contains("5000"))
    }
}

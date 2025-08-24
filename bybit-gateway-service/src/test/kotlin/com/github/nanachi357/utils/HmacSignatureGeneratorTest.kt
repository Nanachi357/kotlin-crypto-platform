package com.github.nanachi357.utils

import kotlin.test.*

class HmacSignatureGeneratorTest {
    
    private lateinit var signatureGenerator: HmacSignatureGenerator
    
    @BeforeTest
    fun setup() {
        signatureGenerator = HmacSignatureGenerator()
    }
    
    @Test
    fun `test basic HMAC signature generation with CharArray`() {
        val secretKey = "test_secret_key".toCharArray()
        val message = "api_key=test_key&timestamp=1234567890&recv_window=5000"
        
        val signature = signatureGenerator.generateSignature(secretKey, message)
        
        assertNotNull(signature)
        assertTrue(signature.isNotBlank())
        assertTrue(signatureGenerator.validateSignature(secretKey, message, signature))
        
        // SECURITY: Verify secret key was cleared from memory
        assertTrue(secretKey.all { it == '\u0000' })
    }

    @Test
    fun `test basic HMAC signature generation with String (backward compatibility)`() {
        val secretKey = "test_secret_key"
        val message = "api_key=test_key&timestamp=1234567890&recv_window=5000"
        
        val signature = signatureGenerator.generateSignature(secretKey, message)
        
        assertNotNull(signature)
        assertTrue(signature.isNotBlank())
        assertTrue(signatureGenerator.validateSignature(secretKey, message, signature))
    }
    
    @Test
    fun `test generateGetSignature with sorted parameters`() {
        val secretKey = "test_secret"
        val timestamp = 1234567890L
        val queryParams = mapOf(
            "z" to "last",
            "a" to "first", 
            "b" to "second"
        )
        
        val signature = signatureGenerator.generateGetSignature(secretKey, timestamp, queryParams)
        
        assertNotNull(signature)
        assertTrue(signature.isNotBlank())
        
        // Verify that parameters are sorted alphabetically
        val expectedQueryString = "a=first&b=second&z=last"
        assertTrue(signatureGenerator.validateSignature(secretKey, expectedQueryString, signature))
    }
    
    @Test
    fun `test generatePostSignature with timestamp concatenation`() {
        val secretKey = "test_secret"
        val timestamp = 1234567890L
        val jsonBody = """{"symbol":"BTCUSDT"}"""
        
        val signature = signatureGenerator.generatePostSignature(secretKey, timestamp, jsonBody)
        
        assertNotNull(signature)
        assertTrue(signature.isNotBlank())
        
        // Verify that message is timestamp + jsonBody
        val expectedMessage = "$timestamp$jsonBody"
        assertTrue(signatureGenerator.validateSignature(secretKey, expectedMessage, signature))
    }
    
    @Test
    fun `test signature generation with empty parameters`() {
        val secretKey = "test_secret"
        val timestamp = 1234567890L
        
        // Empty query params
        val emptyGetSignature = signatureGenerator.generateGetSignature(secretKey, timestamp, emptyMap())
        
        // Empty JSON body  
        val emptyPostSignature = signatureGenerator.generatePostSignature(secretKey, timestamp, "")
        
        assertNotNull(emptyGetSignature)
        assertNotNull(emptyPostSignature)
        assertTrue(emptyGetSignature.isNotBlank())
        assertTrue(emptyPostSignature.isNotBlank())
    }
    
    @Test
    fun `test signature generation with special characters`() {
        val secretKey = "test_secret"
        val timestamp = 1234567890L
        val specialParams = mapOf(
            "symbol" to "BTC/USDT",
            "price" to "50,000.00",
            "amount" to "0.001"
        )
        
        val signature = signatureGenerator.generateGetSignature(secretKey, timestamp, specialParams)
        
        assertNotNull(signature)
        assertTrue(signature.isNotBlank())
        
        // Verify sorted parameters with special characters
        val expectedQueryString = "amount=0.001&price=50,000.00&symbol=BTC/USDT"
        assertTrue(signatureGenerator.validateSignature(secretKey, expectedQueryString, signature))
    }
    
    @Test
    fun `test signature generation with unicode characters`() {
        val secretKey = "test_secret"
        val timestamp = 1234567890L
        val unicodeParams = mapOf(
            "symbol" to "BTC/USDT",
            "name" to "Bitcoin",
            "description" to "Криптовалюта"
        )
        
        val signature = signatureGenerator.generateGetSignature(secretKey, timestamp, unicodeParams)
        
        assertNotNull(signature)
        assertTrue(signature.isNotBlank())
        
        // Verify sorted parameters with unicode
        val expectedQueryString = "description=Криптовалюта&name=Bitcoin&symbol=BTC/USDT"
        assertTrue(signatureGenerator.validateSignature(secretKey, expectedQueryString, signature))
    }
    
    @Test
    fun `test signature validation with different messages`() {
        val secretKey = "test_secret"
        val message1 = "test_message_1"
        val message2 = "test_message_2"
        
        val signature1 = signatureGenerator.generateSignature(secretKey, message1)
        val signature2 = signatureGenerator.generateSignature(secretKey, message2)
        
        // Same message should validate
        assertTrue(signatureGenerator.validateSignature(secretKey, message1, signature1))
        assertTrue(signatureGenerator.validateSignature(secretKey, message2, signature2))
        
        // Different messages should not validate
        assertFalse(signatureGenerator.validateSignature(secretKey, message1, signature2))
        assertFalse(signatureGenerator.validateSignature(secretKey, message2, signature1))
    }
    
    @Test
    fun `test signature generation with very long messages`() {
        val secretKey = "test_secret"
        val timestamp = 1234567890L
        val longJsonBody = """{"symbol":"BTCUSDT","price":"50000","amount":"0.001","side":"Buy","orderType":"Limit","timeInForce":"GTC","reduceOnly":false,"closeOnTrigger":false,"orderLinkId":"test_order_123456789","category":"spot","isLeverage":0,"orderFilter":"Order"}"""
        
        val signature = signatureGenerator.generatePostSignature(secretKey, timestamp, longJsonBody)
        
        assertNotNull(signature)
        assertTrue(signature.isNotBlank())
        
        val expectedMessage = "$timestamp$longJsonBody"
        assertTrue(signatureGenerator.validateSignature(secretKey, expectedMessage, signature))
    }
    
    @Test
    fun `test signature generation with numeric values`() {
        val secretKey = "test_secret"
        val timestamp = 1234567890L
        val numericParams = mapOf(
            "price" to "50000.123",
            "amount" to "0.001",
            "quantity" to "1000000"
        )
        
        val signature = signatureGenerator.generateGetSignature(secretKey, timestamp, numericParams)
        
        assertNotNull(signature)
        assertTrue(signature.isNotBlank())
        
        val expectedQueryString = "amount=0.001&price=50000.123&quantity=1000000"
        assertTrue(signatureGenerator.validateSignature(secretKey, expectedQueryString, signature))
    }
    
    @Test
    fun `test signature generation with boolean values`() {
        val secretKey = "test_secret"
        val timestamp = 1234567890L
        val booleanParams = mapOf(
            "reduceOnly" to "false",
            "closeOnTrigger" to "true",
            "isLeverage" to "0"
        )
        
        val signature = signatureGenerator.generateGetSignature(secretKey, timestamp, booleanParams)
        
        assertNotNull(signature)
        assertTrue(signature.isNotBlank())
        
        val expectedQueryString = "closeOnTrigger=true&isLeverage=0&reduceOnly=false"
        assertTrue(signatureGenerator.validateSignature(secretKey, expectedQueryString, signature))
    }
    
    @Test
    fun `test signature generation with duplicate parameter names`() {
        val secretKey = "test_secret"
        val timestamp = 1234567890L
        val duplicateParams = mapOf(
            "symbol" to "BTCUSDT",
            "symbol" to "ETHUSDT" // This will override the previous value
        )
        
        val signature = signatureGenerator.generateGetSignature(secretKey, timestamp, duplicateParams)
        
        assertNotNull(signature)
        assertTrue(signature.isNotBlank())
        
        // Should use the last value for duplicate keys
        val expectedQueryString = "symbol=ETHUSDT"
        assertTrue(signatureGenerator.validateSignature(secretKey, expectedQueryString, signature))
    }
    
    @Test
    fun `test signature generation with empty secret key`() {
        val secretKey = ""
        val message = "test_message"
        
        // Empty secret key should throw IllegalArgumentException
        assertFailsWith<IllegalArgumentException> {
            signatureGenerator.generateSignature(secretKey, message)
        }
    }
    
    @Test
    fun `test signature generation with empty message`() {
        val secretKey = "test_secret"
        val message = ""
        
        // Empty message should throw IllegalArgumentException
        assertFailsWith<IllegalArgumentException> {
            signatureGenerator.generateSignature(secretKey, message)
        }
    }
    
    @Test
    fun `test signature generation consistency`() {
        val secretKey = "test_secret"
        val message = "test_message"
        
        // Generate signature multiple times
        val signature1 = signatureGenerator.generateSignature(secretKey, message)
        val signature2 = signatureGenerator.generateSignature(secretKey, message)
        val signature3 = signatureGenerator.generateSignature(secretKey, message)
        
        // All signatures should be identical for the same input
        assertEquals(signature1, signature2)
        assertEquals(signature2, signature3)
        assertEquals(signature1, signature3)
    }
    
    @Test
    fun `test signature generation with blank secret key`() {
        val secretKey = "   "
        val message = "test_message"
        
        // Blank secret key should throw IllegalArgumentException
        assertFailsWith<IllegalArgumentException> {
            signatureGenerator.generateSignature(secretKey, message)
        }
    }
    
    @Test
    fun `test signature generation with blank message`() {
        val secretKey = "test_secret"
        val message = "   "
        
        // Blank message should throw IllegalArgumentException
        assertFailsWith<IllegalArgumentException> {
            signatureGenerator.generateSignature(secretKey, message)
        }
    }
    
    @Test
    fun `test signature generation with different secret keys`() {
        val message = "test_message"
        val secretKey1 = "secret_key_1"
        val secretKey2 = "secret_key_2"
        
        val signature1 = signatureGenerator.generateSignature(secretKey1, message)
        val signature2 = signatureGenerator.generateSignature(secretKey2, message)
        
        // Different secret keys should produce different signatures
        assertNotEquals(signature1, signature2)
        
        // Each signature should validate with its own secret key
        assertTrue(signatureGenerator.validateSignature(secretKey1, message, signature1))
        assertTrue(signatureGenerator.validateSignature(secretKey2, message, signature2))
        
        // Cross-validation should fail
        assertFalse(signatureGenerator.validateSignature(secretKey1, message, signature2))
        assertFalse(signatureGenerator.validateSignature(secretKey2, message, signature1))
    }
}

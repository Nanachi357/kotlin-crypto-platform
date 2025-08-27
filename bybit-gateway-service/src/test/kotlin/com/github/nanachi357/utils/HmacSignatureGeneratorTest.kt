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
        
        // Create a fresh copy for validation since original was cleared
        val secretKeyForValidation = "test_secret_key".toCharArray()
        assertTrue(signatureGenerator.validateSignature(secretKeyForValidation, message, signature))
        
        // SECURITY: Verify secret key was cleared from memory
        assertTrue(secretKey.all { it == '\u0000' })
    }

    @Test
    fun `test basic HMAC signature generation with String (backward compatibility)`() {
        val secretKey = "test_secret_key".toCharArray()
        val message = "api_key=test_key&timestamp=1234567890&recv_window=5000"
        
        try {
            val signature = signatureGenerator.generateSignature(secretKey, message)
            
            assertNotNull(signature)
            assertTrue(signature.isNotBlank())
            
            // Create a fresh secretKey for validation
            val validationKey = "test_secret_key".toCharArray()
            try {
                assertTrue(signatureGenerator.validateSignature(validationKey, message, signature))
            } finally {
                validationKey.fill('\u0000')
            }
        } finally {
            secretKey.fill('\u0000')
        }
    }
    
    @Test
    fun `test generateGetSignature with sorted parameters`() {
        val secretKey = "test_secret".toCharArray()
        val timestamp = 1234567890L
        val queryParams = mapOf(
            "z" to "last",
            "a" to "first", 
            "b" to "second"
        )
        
        try {
            val signature = signatureGenerator.generateGetSignature(secretKey, timestamp, queryParams)
            
            assertNotNull(signature)
            assertTrue(signature.isNotBlank())
            
            // Verify that parameters are sorted alphabetically
            val expectedQueryString = "a=first&b=second&z=last"
            
            // Create a fresh secretKey for validation
            val validationKey = "test_secret".toCharArray()
            try {
                assertTrue(signatureGenerator.validateSignature(validationKey, expectedQueryString, signature))
            } finally {
                validationKey.fill('\u0000')
            }
        } finally {
            secretKey.fill('\u0000')
        }
    }
    
    @Test
    fun `test generatePostSignature with timestamp concatenation`() {
        val secretKey = "test_secret".toCharArray()
        val timestamp = 1234567890L
        val jsonBody = """{"symbol":"BTCUSDT"}"""
        
        try {
            val signature = signatureGenerator.generatePostSignature(secretKey, timestamp, jsonBody)
            
            assertNotNull(signature)
            assertTrue(signature.isNotBlank())
            
            // Verify that message is timestamp + jsonBody
            val expectedMessage = "$timestamp$jsonBody"
            
            // Create a fresh secretKey for validation
            val validationKey = "test_secret".toCharArray()
            try {
                assertTrue(signatureGenerator.validateSignature(validationKey, expectedMessage, signature))
            } finally {
                validationKey.fill('\u0000')
            }
        } finally {
            secretKey.fill('\u0000')
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
        val secretKey = "test_secret".toCharArray()
        val timestamp = 1234567890L
        val specialParams = mapOf(
            "symbol" to "BTC/USDT",
            "price" to "50,000.00",
            "amount" to "0.001"
        )
        
        try {
            val signature = signatureGenerator.generateGetSignature(secretKey, timestamp, specialParams)
            
            assertNotNull(signature)
            assertTrue(signature.isNotBlank())
            
            // Verify sorted parameters with special characters
            val expectedQueryString = "amount=0.001&price=50,000.00&symbol=BTC/USDT"
            
            // Create a fresh secretKey for validation
            val validationKey = "test_secret".toCharArray()
            try {
                assertTrue(signatureGenerator.validateSignature(validationKey, expectedQueryString, signature))
            } finally {
                validationKey.fill('\u0000')
            }
        } finally {
            secretKey.fill('\u0000')
        }
    }
    
    @Test
    fun `test signature generation with unicode characters`() {
        val secretKey = "test_secret".toCharArray()
        val timestamp = 1234567890L
        val unicodeParams = mapOf(
            "symbol" to "BTC/USDT",
            "name" to "Bitcoin",
            "description" to "Криптовалюта"
        )
        
        try {
            val signature = signatureGenerator.generateGetSignature(secretKey, timestamp, unicodeParams)
            
            assertNotNull(signature)
            assertTrue(signature.isNotBlank())
            
            // Verify sorted parameters with unicode
            val expectedQueryString = "description=Криптовалюта&name=Bitcoin&symbol=BTC/USDT"
            
            // Create a fresh secretKey for validation
            val validationKey = "test_secret".toCharArray()
            try {
                assertTrue(signatureGenerator.validateSignature(validationKey, expectedQueryString, signature))
            } finally {
                validationKey.fill('\u0000')
            }
        } finally {
            secretKey.fill('\u0000')
        }
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
    fun `test signature generation with very long messages`() {
        val timestamp = 1234567890L
        val longJsonBody = """{"symbol":"BTCUSDT","price":"50000","amount":"0.001","side":"Buy","orderType":"Limit","timeInForce":"GTC","reduceOnly":false,"closeOnTrigger":false,"orderLinkId":"test_order_123456789","category":"spot","isLeverage":0,"orderFilter":"Order"}"""
        
        // Create separate CharArrays for generation and validation
        val secretKey1 = "test_secret".toCharArray()
        val secretKey2 = "test_secret".toCharArray()
        
        try {
            val signature = signatureGenerator.generatePostSignature(secretKey1, timestamp, longJsonBody)
            
            assertNotNull(signature)
            assertTrue(signature.isNotBlank())
            
            val expectedMessage = "$timestamp$longJsonBody"
            assertTrue(signatureGenerator.validateSignature(secretKey2, expectedMessage, signature))
        } finally {
            secretKey1.fill('\u0000')
            secretKey2.fill('\u0000')
        }
    }
    
    @Test
    fun `test signature generation with numeric values`() {
        val timestamp = 1234567890L
        val numericParams = mapOf(
            "price" to "50000.123",
            "amount" to "0.001",
            "quantity" to "1000000"
        )
        
        // Create separate CharArrays for generation and validation
        val secretKey1 = "test_secret".toCharArray()
        val secretKey2 = "test_secret".toCharArray()
        
        try {
            val signature = signatureGenerator.generateGetSignature(secretKey1, timestamp, numericParams)
            
            assertNotNull(signature)
            assertTrue(signature.isNotBlank())
            
            val expectedQueryString = "amount=0.001&price=50000.123&quantity=1000000"
            assertTrue(signatureGenerator.validateSignature(secretKey2, expectedQueryString, signature))
        } finally {
            secretKey1.fill('\u0000')
            secretKey2.fill('\u0000')
        }
    }
    
    @Test
    fun `test signature generation with boolean values`() {
        val timestamp = 1234567890L
        val booleanParams = mapOf(
            "reduceOnly" to "false",
            "closeOnTrigger" to "true",
            "isLeverage" to "0"
        )
        
        // Create separate CharArrays for generation and validation
        val secretKey1 = "test_secret".toCharArray()
        val secretKey2 = "test_secret".toCharArray()
        
        try {
            val signature = signatureGenerator.generateGetSignature(secretKey1, timestamp, booleanParams)
            
            assertNotNull(signature)
            assertTrue(signature.isNotBlank())
            
            val expectedQueryString = "closeOnTrigger=true&isLeverage=0&reduceOnly=false"
            assertTrue(signatureGenerator.validateSignature(secretKey2, expectedQueryString, signature))
        } finally {
            secretKey1.fill('\u0000')
            secretKey2.fill('\u0000')
        }
    }
    
    @Test
    fun `test signature generation with duplicate parameter names`() {
        val timestamp = 1234567890L
        val duplicateParams = mapOf(
            "symbol" to "BTCUSDT",
            "symbol" to "ETHUSDT" // This will override the previous value
        )
        
        // Create separate CharArrays for generation and validation
        val secretKey1 = "test_secret".toCharArray()
        val secretKey2 = "test_secret".toCharArray()
        
        try {
            val signature = signatureGenerator.generateGetSignature(secretKey1, timestamp, duplicateParams)
            
            assertNotNull(signature)
            assertTrue(signature.isNotBlank())
            
            // Should use the last value for duplicate keys
            val expectedQueryString = "symbol=ETHUSDT"
            assertTrue(signatureGenerator.validateSignature(secretKey2, expectedQueryString, signature))
        } finally {
            secretKey1.fill('\u0000')
            secretKey2.fill('\u0000')
        }
    }
    
    @Test
    fun `test signature generation with empty secret key`() {
        val secretKey = "".toCharArray()
        val message = "test_message"
        
        try {
            // Empty secret key should throw IllegalArgumentException
            assertFailsWith<IllegalArgumentException> {
                signatureGenerator.generateSignature(secretKey, message)
            }
        } finally {
            secretKey.fill('\u0000')
        }
    }
    
    @Test
    fun `test signature generation with empty message`() {
        val secretKey = "test_secret".toCharArray()
        val message = ""
        
        try {
            // Empty message should work now (we allow empty strings)
            val signature = signatureGenerator.generateSignature(secretKey, message)
            assertNotNull(signature)
            assertTrue(signature.isNotBlank())
        } finally {
            secretKey.fill('\u0000')
        }
    }
    
    @Test
    fun `test signature generation consistency`() {
        val message = "test_message"
        
        // Create separate CharArrays for each call to avoid clearing issues
        val secretKey1 = "test_secret".toCharArray()
        val secretKey2 = "test_secret".toCharArray()
        val secretKey3 = "test_secret".toCharArray()
        
        try {
            // Generate signature multiple times with separate keys
            val signature1 = signatureGenerator.generateSignature(secretKey1, message)
            val signature2 = signatureGenerator.generateSignature(secretKey2, message)
            val signature3 = signatureGenerator.generateSignature(secretKey3, message)
            
            // All signatures should be identical for the same input
            assertEquals(signature1, signature2)
            assertEquals(signature2, signature3)
            assertEquals(signature1, signature3)
        } finally {
            secretKey1.fill('\u0000')
            secretKey2.fill('\u0000')
            secretKey3.fill('\u0000')
        }
    }
    
    @Test
    fun `test signature generation with blank secret key`() {
        val secretKey = "   ".toCharArray()
        val message = "test_message"
        
        try {
            // Blank secret key should throw IllegalArgumentException
            assertFailsWith<IllegalArgumentException> {
                signatureGenerator.generateSignature(secretKey, message)
            }
        } finally {
            secretKey.fill('\u0000')
        }
    }
    
    @Test
    fun `test signature generation with blank message`() {
        val secretKey = "test_secret".toCharArray()
        val message = "   "
        
        try {
            // Blank message should work now (we allow empty strings)
            val signature = signatureGenerator.generateSignature(secretKey, message)
            assertNotNull(signature)
            assertTrue(signature.isNotBlank())
        } finally {
            secretKey.fill('\u0000')
        }
    }
    
    @Test
    fun `test signature generation with different secret keys`() {
        val message = "test_message"
        val secretKey1 = "secret_key_1".toCharArray()
        val secretKey2 = "secret_key_2".toCharArray()
        val secretKey3 = "secret_key_1".toCharArray()
        val secretKey4 = "secret_key_2".toCharArray()
        val secretKey5 = "secret_key_1".toCharArray()
        val secretKey6 = "secret_key_2".toCharArray()
        
        try {
            val signature1 = signatureGenerator.generateSignature(secretKey1, message)
            val signature2 = signatureGenerator.generateSignature(secretKey2, message)
            
            // Different secret keys should produce different signatures
            assertNotEquals(signature1, signature2)
            
            // Each signature should validate with its own secret key
            assertTrue(signatureGenerator.validateSignature(secretKey3, message, signature1))
            assertTrue(signatureGenerator.validateSignature(secretKey4, message, signature2))
            
            // Cross-validation should fail
            assertFalse(signatureGenerator.validateSignature(secretKey5, message, signature2))
            assertFalse(signatureGenerator.validateSignature(secretKey6, message, signature1))
        } finally {
            secretKey1.fill('\u0000')
            secretKey2.fill('\u0000')
            secretKey3.fill('\u0000')
            secretKey4.fill('\u0000')
            secretKey5.fill('\u0000')
            secretKey6.fill('\u0000')
        }
    }
}

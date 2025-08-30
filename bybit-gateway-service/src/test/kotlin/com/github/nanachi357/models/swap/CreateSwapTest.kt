package com.github.nanachi357.models.swap

import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class CreateSwapTest {

    @Test
    fun `should create valid CreateSwap with required fields`() {
        val swap = CreateSwap(
            assetGive = "BTC",
            assetTake = "USDT",
            networkGive = "Bitcoin",
            networkTake = "Ethereum",
            addressTake = "0x742d35Cc6634C0532925a3b8D4C9db96C4b4d8b6",
            amountGive = "0.1",
            type = "fix"
        )

        assertEquals("BTC", swap.assetGive)
        assertEquals("USDT", swap.assetTake)
        assertEquals("Bitcoin", swap.networkGive)
        assertEquals("Ethereum", swap.networkTake)
        assertEquals("0x742d35Cc6634C0532925a3b8D4C9db96C4b4d8b6", swap.addressTake)
        assertEquals("0.1", swap.amountGive)
        assertEquals("fix", swap.type)
        assertEquals(BigDecimal("0.1"), swap.getAmountGiveAsBigDecimal())
    }

    @Test
    fun `should create valid CreateSwap with optional fields`() {
        val swap = CreateSwap(
            assetGive = "XRP",
            assetTake = "ETH",
            networkGive = "Ripple",
            networkTake = "Ethereum",
            addressTake = "0x742d35Cc6634C0532925a3b8D4C9db96C4b4d8b6",
            amountGive = "1000",
            type = "float",
            memoTake = "123456",
            email = "user@example.com",
            refundAddress = "rXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
        )

        assertEquals("XRP", swap.assetGive)
        assertEquals("123456", swap.memoTake)
        assertEquals("user@example.com", swap.email)
        assertEquals("rXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", swap.refundAddress)
        assertEquals(BigDecimal("1000"), swap.getAmountGiveAsBigDecimal())
    }

    @Test
    fun `should throw exception for blank assetGive`() {
        val exception = assertThrows<IllegalArgumentException> {
            CreateSwap(
                assetGive = "",
                assetTake = "USDT",
                networkGive = "Bitcoin",
                networkTake = "Ethereum",
                addressTake = "0x742d35Cc6634C0532925a3b8D4C9db96C4b4d8b6",
                amountGive = "0.1",
                type = "fix"
            )
        }
        assertEquals("assetGive cannot be blank", exception.message)
    }

    @Test
    fun `should throw exception for blank assetTake`() {
        val exception = assertThrows<IllegalArgumentException> {
            CreateSwap(
                assetGive = "BTC",
                assetTake = "",
                networkGive = "Bitcoin",
                networkTake = "Ethereum",
                addressTake = "0x742d35Cc6634C0532925a3b8D4C9db96C4b4d8b6",
                amountGive = "0.1",
                type = "fix"
            )
        }
        assertEquals("assetTake cannot be blank", exception.message)
    }

    @Test
    fun `should throw exception for blank networkGive`() {
        val exception = assertThrows<IllegalArgumentException> {
            CreateSwap(
                assetGive = "BTC",
                assetTake = "USDT",
                networkGive = "",
                networkTake = "Ethereum",
                addressTake = "0x742d35Cc6634C0532925a3b8D4C9db96C4b4d8b6",
                amountGive = "0.1",
                type = "fix"
            )
        }
        assertEquals("networkGive cannot be blank", exception.message)
    }

    @Test
    fun `should throw exception for blank networkTake`() {
        val exception = assertThrows<IllegalArgumentException> {
            CreateSwap(
                assetGive = "BTC",
                assetTake = "USDT",
                networkGive = "Bitcoin",
                networkTake = "",
                addressTake = "0x742d35Cc6634C0532925a3b8D4C9db96C4b4d8b6",
                amountGive = "0.1",
                type = "fix"
            )
        }
        assertEquals("networkTake cannot be blank", exception.message)
    }

    @Test
    fun `should throw exception for blank addressTake`() {
        val exception = assertThrows<IllegalArgumentException> {
            CreateSwap(
                assetGive = "BTC",
                assetTake = "USDT",
                networkGive = "Bitcoin",
                networkTake = "Ethereum",
                addressTake = "",
                amountGive = "0.1",
                type = "fix"
            )
        }
        assertEquals("addressTake cannot be blank", exception.message)
    }

    @Test
    fun `should throw exception for blank amountGive`() {
        val exception = assertThrows<IllegalArgumentException> {
            CreateSwap(
                assetGive = "BTC",
                assetTake = "USDT",
                networkGive = "Bitcoin",
                networkTake = "Ethereum",
                addressTake = "0x742d35Cc6634C0532925a3b8D4C9db96C4b4d8b6",
                amountGive = "",
                type = "fix"
            )
        }
        assertEquals("amountGive cannot be blank", exception.message)
    }

    @Test
    fun `should throw exception for blank type`() {
        val exception = assertThrows<IllegalArgumentException> {
            CreateSwap(
                assetGive = "BTC",
                assetTake = "USDT",
                networkGive = "Bitcoin",
                networkTake = "Ethereum",
                addressTake = "0x742d35Cc6634C0532925a3b8D4C9db96C4b4d8b6",
                amountGive = "0.1",
                type = ""
            )
        }
        assertEquals("type cannot be blank", exception.message)
    }

    @Test
    fun `should throw exception for negative amountGive`() {
        val exception = assertThrows<IllegalArgumentException> {
            CreateSwap(
                assetGive = "BTC",
                assetTake = "USDT",
                networkGive = "Bitcoin",
                networkTake = "Ethereum",
                addressTake = "0x742d35Cc6634C0532925a3b8D4C9db96C4b4d8b6",
                amountGive = "-0.1",
                type = "fix"
            )
        }
        assertEquals("amountGive must be a positive number", exception.message)
    }

    @Test
    fun `should throw exception for zero amountGive`() {
        val exception = assertThrows<IllegalArgumentException> {
            CreateSwap(
                assetGive = "BTC",
                assetTake = "USDT",
                networkGive = "Bitcoin",
                networkTake = "Ethereum",
                addressTake = "0x742d35Cc6634C0532925a3b8D4C9db96C4b4d8b6",
                amountGive = "0",
                type = "fix"
            )
        }
        assertEquals("amountGive must be a positive number", exception.message)
    }

    @Test
    fun `should throw exception for invalid amountGive format`() {
        val exception = assertThrows<IllegalArgumentException> {
            CreateSwap(
                assetGive = "BTC",
                assetTake = "USDT",
                networkGive = "Bitcoin",
                networkTake = "Ethereum",
                addressTake = "0x742d35Cc6634C0532925a3b8D4C9db96C4b4d8b6",
                amountGive = "invalid",
                type = "fix"
            )
        }
        assertEquals("amountGive must be a positive number", exception.message)
    }

    @Test
    fun `should convert amountGive to BigDecimal correctly`() {
        val swap = CreateSwap(
            assetGive = "BTC",
            assetTake = "USDT",
            networkGive = "Bitcoin",
            networkTake = "Ethereum",
            addressTake = "0x742d35Cc6634C0532925a3b8D4C9db96C4b4d8b6",
            amountGive = "0.123456789",
            type = "fix"
        )

        assertEquals(BigDecimal("0.123456789"), swap.getAmountGiveAsBigDecimal())
    }

    @Test
    fun `should handle large amounts correctly`() {
        val swap = CreateSwap(
            assetGive = "BTC",
            assetTake = "USDT",
            networkGive = "Bitcoin",
            networkTake = "Ethereum",
            addressTake = "0x742d35Cc6634C0532925a3b8D4C9db96C4b4d8b6",
            amountGive = "1000000.123456789",
            type = "fix"
        )

        assertEquals(BigDecimal("1000000.123456789"), swap.getAmountGiveAsBigDecimal())
    }
}

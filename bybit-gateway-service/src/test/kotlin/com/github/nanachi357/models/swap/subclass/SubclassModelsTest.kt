package com.github.nanachi357.models.swap.subclass

import com.github.nanachi357.models.swap.subclass.enums.SwapStatus
import com.github.nanachi357.models.swap.subclass.enums.SwapType
import com.github.nanachi357.models.swap.subclass.enums.SwapStep
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SubclassModelsTest {

    @Test
    fun `should create valid Info`() {
        val info = Info(
            uid = "abc123",
            status = SwapStatus.Waiting,
            type = SwapType.fix,
            fee = "0.001",
            link = "https://swap.example.com/abc123",
            equivalent = "4000"
        )

        assertEquals("abc123", info.uid)
        assertEquals(SwapStatus.Waiting, info.status)
        assertEquals(SwapType.fix, info.type)
        assertEquals("0.001", info.fee)
        assertEquals("https://swap.example.com/abc123", info.link)
        assertEquals("4000", info.equivalent)
        assertEquals(BigDecimal("0.001"), info.getFeeAsBigDecimal())
        assertEquals(BigDecimal("4000"), info.getEquivalentAsBigDecimal())
    }

    @Test
    fun `should throw exception for blank uid in Info`() {
        val exception = assertThrows<IllegalArgumentException> {
            Info(
                uid = "",
                status = SwapStatus.Waiting,
                type = SwapType.fix,
                fee = "0.001",
                link = "https://swap.example.com/abc123",
                equivalent = "4000"
            )
        }
        assertEquals("uid cannot be blank", exception.message)
    }

    @Test
    fun `should create valid Deposit`() {
        val deposit = Deposit(
            coin = "BTC",
            network = "Bitcoin",
            amount = "0.1",
            actual = "0.0995",
            address = "1ABC...",
            memo = "123456",
            platform = "Bybit",
            account = "user123"
        )

        assertEquals("BTC", deposit.coin)
        assertEquals("Bitcoin", deposit.network)
        assertEquals("0.1", deposit.amount)
        assertEquals("0.0995", deposit.actual)
        assertEquals("1ABC...", deposit.address)
        assertEquals("123456", deposit.memo)
        assertEquals("Bybit", deposit.platform)
        assertEquals("user123", deposit.account)
        assertEquals(BigDecimal("0.1"), deposit.getAmountAsBigDecimal())
        assertEquals(BigDecimal("0.0995"), deposit.getActualAsBigDecimal())
    }

    @Test
    fun `should handle null actual in Deposit`() {
        val deposit = Deposit(
            coin = "BTC",
            network = "Bitcoin",
            amount = "0.1",
            actual = null,
            address = "1ABC...",
            memo = null,
            platform = "Bybit",
            account = "user123"
        )

        assertNull(deposit.actual)
        assertNull(deposit.getActualAsBigDecimal())
    }

    @Test
    fun `should throw exception for negative amount in Deposit`() {
        val exception = assertThrows<IllegalArgumentException> {
            Deposit(
                coin = "BTC",
                network = "Bitcoin",
                amount = "-0.1",
                actual = null,
                address = "1ABC...",
                memo = null,
                platform = "Bybit",
                account = "user123"
            )
        }
        assertEquals("amount must be a positive number", exception.message)
    }

    @Test
    fun `should create valid Withdraw`() {
        val withdraw = Withdraw(
            coin = "USDT",
            network = "Ethereum",
            amount = "4000",
            address = "0x123...",
            memo = "memo123",
            platform = "Bybit"
        )

        assertEquals("USDT", withdraw.coin)
        assertEquals("Ethereum", withdraw.network)
        assertEquals("4000", withdraw.amount)
        assertEquals("0x123...", withdraw.address)
        assertEquals("memo123", withdraw.memo)
        assertEquals("Bybit", withdraw.platform)
        assertEquals(BigDecimal("4000"), withdraw.getAmountAsBigDecimal())
    }

    @Test
    fun `should handle null amount in Withdraw`() {
        val withdraw = Withdraw(
            coin = "USDT",
            network = "Ethereum",
            amount = null,
            address = "0x123...",
            memo = null,
            platform = "Bybit"
        )

        assertNull(withdraw.amount)
        assertNull(withdraw.getAmountAsBigDecimal())
    }

    @Test
    fun `should create valid Expectations`() {
        val expectations = Expectations(
            wait = "4000",
            confirm = "3950",
            sellTransfer = "3950",
            sell = "3950",
            buyTransfer = "4000",
            buy = "4000"
        )

        assertEquals("4000", expectations.wait)
        assertEquals("3950", expectations.confirm)
        assertEquals("3950", expectations.sellTransfer)
        assertEquals("3950", expectations.sell)
        assertEquals("4000", expectations.buyTransfer)
        assertEquals("4000", expectations.buy)
        assertEquals(BigDecimal("4000"), expectations.getWaitAsBigDecimal())
        assertEquals(BigDecimal("3950"), expectations.getConfirmAsBigDecimal())
        assertEquals(BigDecimal("3950"), expectations.getSellTransferAsBigDecimal())
        assertEquals(BigDecimal("3950"), expectations.getSellAsBigDecimal())
        assertEquals(BigDecimal("4000"), expectations.getBuyTransferAsBigDecimal())
        assertEquals(BigDecimal("4000"), expectations.getBuyAsBigDecimal())
    }

    @Test
    fun `should handle null values in Expectations`() {
        val expectations = Expectations(
            wait = "4000",
            confirm = null,
            sellTransfer = null,
            sell = null,
            buyTransfer = null,
            buy = null
        )

        assertEquals("4000", expectations.wait)
        assertNull(expectations.confirm)
        assertNull(expectations.sellTransfer)
        assertNull(expectations.sell)
        assertNull(expectations.buyTransfer)
        assertNull(expectations.buy)
        assertEquals(BigDecimal("4000"), expectations.getWaitAsBigDecimal())
        assertNull(expectations.getConfirmAsBigDecimal())
        assertNull(expectations.getSellTransferAsBigDecimal())
        assertNull(expectations.getSellAsBigDecimal())
        assertNull(expectations.getBuyTransferAsBigDecimal())
        assertNull(expectations.getBuyAsBigDecimal())
    }

    @Test
    fun `should create valid Time`() {
        val currentTime = System.currentTimeMillis()
        val time = Time(
            create = currentTime,
            confirm = currentTime + 1000,
            sellTransfer = currentTime + 2000,
            sell = currentTime + 3000,
            buyTransfer = currentTime + 4000,
            buy = currentTime + 5000,
            withdrawTransfer = currentTime + 6000,
            send = currentTime + 7000,
            success = currentTime + 8000,
            overdue = null,
            refund = null,
            frozen = null,
            suspended = null,
            cancel = null
        )

        assertEquals(currentTime, time.create)
        assertEquals(currentTime + 1000, time.confirm)
        assertEquals(currentTime + 2000, time.sellTransfer)
        assertEquals(currentTime + 3000, time.sell)
        assertEquals(currentTime + 4000, time.buyTransfer)
        assertEquals(currentTime + 5000, time.buy)
        assertEquals(currentTime + 6000, time.withdrawTransfer)
        assertEquals(currentTime + 7000, time.send)
        assertEquals(currentTime + 8000, time.success)
        assertNull(time.overdue)
        assertNull(time.refund)
        assertNull(time.frozen)
        assertNull(time.suspended)
        assertNull(time.cancel)
    }

    @Test
    fun `should throw exception for negative create time`() {
        val exception = assertThrows<IllegalArgumentException> {
            Time(
                create = -1,
                confirm = null,
                sellTransfer = null,
                sell = null,
                buyTransfer = null,
                buy = null,
                withdrawTransfer = null,
                send = null,
                success = null,
                overdue = null,
                refund = null,
                frozen = null,
                suspended = null,
                cancel = null
            )
        }
        assertEquals("create timestamp must be positive", exception.message)
    }

    @Test
    fun `should create valid Profit`() {
        val profit = Profit(
            amount = "50.25",
            percent = "1.25"
        )

        assertEquals("50.25", profit.amount)
        assertEquals("1.25", profit.percent)
        assertEquals(BigDecimal("50.25"), profit.getAmountAsBigDecimal())
        assertEquals(BigDecimal("1.25"), profit.getPercentAsBigDecimal())
    }

    @Test
    fun `should throw exception for negative profit amount`() {
        val exception = assertThrows<IllegalArgumentException> {
            Profit(
                amount = "-10.50",
                percent = "1.25"
            )
        }
        assertEquals("amount must be a non-negative number", exception.message)
    }

    @Test
    fun `should create valid Partner`() {
        val profit = Profit(amount = "0", percent = "0")
        val partner = Partner(
            key = "partner1",
            fee = "0.001",
            profit = profit,
            name = "Partner Name"
        )

        assertEquals("partner1", partner.key)
        assertEquals("0.001", partner.fee)
        assertEquals(profit, partner.profit)
        assertEquals("Partner Name", partner.name)
        assertEquals(BigDecimal("0.001"), partner.getFeeAsBigDecimal())
    }

    @Test
    fun `should create valid Internal`() {
        val virtual = Virtual(deposit = false, withdraw = false)
        val internal = Internal(
            active = true,
            zipped = false,
            step = SwapStep.DepositProcessing,
            virtual = virtual
        )

        assertEquals(true, internal.active)
        assertEquals(false, internal.zipped)
        assertEquals(SwapStep.DepositProcessing, internal.step)
        assertEquals(virtual, internal.virtual)
    }

    @Test
    fun `should create valid User`() {
        val user = User(
            email = "user@example.com",
            refundAddress = "refund@example.com"
        )

        assertEquals("user@example.com", user.email)
        assertEquals("refund@example.com", user.refundAddress)
    }

    @Test
    fun `should create valid Linked`() {
        val linked = Linked()
        linked.deposits.add("deposit123")
        linked.withdraws.add("withdraw456")
        linked.notes.add("Important note")

        assertEquals(1, linked.deposits.size)
        assertEquals(1, linked.withdraws.size)
        assertEquals(1, linked.notes.size)
        assert(linked.deposits.contains("deposit123"))
        assert(linked.withdraws.contains("withdraw456"))
        assert(linked.notes.contains("Important note"))
    }

    @Test
    fun `should create valid Route`() {
        val sellRoute = RouteData("Bybit", "1ABC...", null, "user123")
        val buyRoute = RouteData("Bybit", "0x123...", null, "user123")
        val withdrawRoute = RouteData("Bybit", "0x123...", null, "user123")

        val route = Route(
            sell = sellRoute,
            buy = buyRoute,
            withdraw = withdrawRoute
        )

        assertEquals(sellRoute, route.sell)
        assertEquals(buyRoute, route.buy)
        assertEquals(withdrawRoute, route.withdraw)
    }

    @Test
    fun `should create valid RouteData`() {
        val routeData = RouteData(
            platform = "Bybit",
            address = "1ABC...",
            memo = "123456",
            account = "user123"
        )

        assertEquals("Bybit", routeData.platform)
        assertEquals("1ABC...", routeData.address)
        assertEquals("123456", routeData.memo)
        assertEquals("user123", routeData.account)
    }

    @Test
    fun `should throw exception for blank platform in RouteData`() {
        val exception = assertThrows<IllegalArgumentException> {
            RouteData(
                platform = "",
                address = "1ABC...",
                memo = null,
                account = "user123"
            )
        }
        assertEquals("platform cannot be blank", exception.message)
    }

    @Test
    fun `should create valid Virtual`() {
        val virtual = Virtual(
            deposit = true,
            withdraw = false
        )

        assertEquals(true, virtual.deposit)
        assertEquals(false, virtual.withdraw)
    }
}

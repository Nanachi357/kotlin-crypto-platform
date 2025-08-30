package com.github.nanachi357.models.swap

import com.github.nanachi357.models.swap.subclass.*
import com.github.nanachi357.models.swap.subclass.enums.SwapStatus
import com.github.nanachi357.models.swap.subclass.enums.SwapStep
import com.github.nanachi357.models.swap.subclass.enums.SwapType
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SwapOperationTest {

    @Test
    fun `should create valid SwapOperation`() {
        val swap = createTestSwap()

        assertEquals("abc123", swap.info.uid)
        assertEquals(SwapStatus.Waiting, swap.info.status)
        assertEquals(SwapType.fix, swap.info.type)
        assertEquals("0.001", swap.info.fee)
        assertEquals("https://swap.example.com/abc123", swap.info.link)
        assertEquals("4000", swap.info.equivalent)

        assertEquals("BTC", swap.deposit.coin)
        assertEquals("Bitcoin", swap.deposit.network)
        assertEquals("0.1", swap.deposit.amount)
        assertEquals("1ABC...", swap.deposit.address)
        assertEquals("Bybit", swap.deposit.platform)
        assertEquals("user123", swap.deposit.account)

        assertEquals("USDT", swap.withdraw.coin)
        assertEquals("Ethereum", swap.withdraw.network)
        assertEquals("4000", swap.withdraw.amount)
        assertEquals("0x123...", swap.withdraw.address)
        assertEquals("Bybit", swap.withdraw.platform)

        assertEquals("4000", swap.expectations.wait)
        assertEquals(BigDecimal("4000"), swap.expectations.getWaitAsBigDecimal())

        assertNotNull(swap.time.create)
        assertEquals("0", swap.profit.amount)
        assertEquals("0", swap.profit.percent)
    }

    @Test
    fun `should update status correctly`() {
        val swap = createTestSwap()

        // Initial status
        assertEquals(SwapStatus.Waiting, swap.info.status)
        assertEquals(SwapStep.DepositProcessing, swap.internal.step)

        // Update status
        swap.info.status = SwapStatus.Confirmation
        assertEquals(SwapStatus.Confirmation, swap.info.status)

        // Update step
        swap.internal.step = SwapStep.DepositProcessed
        assertEquals(SwapStep.DepositProcessed, swap.internal.step)
    }

    @Test
    fun `should update deposit status`() {
        val swap = createTestSwap()

        // Update actual deposit amount
        swap.deposit.actual = "0.0995"
        assertEquals("0.0995", swap.deposit.actual)
        assertEquals(BigDecimal("0.0995"), swap.deposit.getActualAsBigDecimal())
    }

    @Test
    fun `should update expectations`() {
        val swap = createTestSwap()

        // Update expectations
        swap.expectations.confirm = "3950"
        swap.expectations.sell = "3950"
        swap.expectations.buy = "4000"

        assertEquals("3950", swap.expectations.confirm)
        assertEquals("3950", swap.expectations.sell)
        assertEquals("4000", swap.expectations.buy)
        assertEquals(BigDecimal("3950"), swap.expectations.getConfirmAsBigDecimal())
        assertEquals(BigDecimal("3950"), swap.expectations.getSellAsBigDecimal())
        assertEquals(BigDecimal("4000"), swap.expectations.getBuyAsBigDecimal())
    }

    @Test
    fun `should update profit`() {
        val swap = createTestSwap()

        // Update profit
        swap.profit.amount = "50.25"
        swap.profit.percent = "1.25"

        assertEquals("50.25", swap.profit.amount)
        assertEquals("1.25", swap.profit.percent)
        assertEquals(BigDecimal("50.25"), swap.profit.getAmountAsBigDecimal())
        assertEquals(BigDecimal("1.25"), swap.profit.getPercentAsBigDecimal())
    }

    @Test
    fun `should update time stamps`() {
        val swap = createTestSwap()
        val currentTime = System.currentTimeMillis()

        // Update timestamps
        swap.time.confirm = currentTime
        swap.time.sell = currentTime + 1000
        swap.time.success = currentTime + 5000

        assertEquals(currentTime, swap.time.confirm)
        assertEquals(currentTime + 1000, swap.time.sell)
        assertEquals(currentTime + 5000, swap.time.success)
    }

    @Test
    fun `should update user data`() {
        val swap = createTestSwap()

        // Update user data
        swap.user.email = "newemail@example.com"
        swap.user.refundAddress = "newrefund@example.com"

        assertEquals("newemail@example.com", swap.user.email)
        assertEquals("newrefund@example.com", swap.user.refundAddress)
    }

    @Test
    fun `should update linked operations`() {
        val swap = createTestSwap()

        // Add linked operations
        swap.linked.deposits.add("deposit123")
        swap.linked.withdraws.add("withdraw456")
        swap.linked.notes.add("Important note")

        assertEquals(1, swap.linked.deposits.size)
        assertEquals(1, swap.linked.withdraws.size)
        assertEquals(1, swap.linked.notes.size)
        assert(swap.linked.deposits.contains("deposit123"))
        assert(swap.linked.withdraws.contains("withdraw456"))
        assert(swap.linked.notes.contains("Important note"))
    }

    @Test
    fun `should update route data`() {
        val swap = createTestSwap()

        // Update route data
        swap.route.sell.platform = "Binance"
        swap.route.buy.platform = "Coinbase"
        swap.route.withdraw.platform = "Kraken"

        assertEquals("Binance", swap.route.sell.platform)
        assertEquals("Coinbase", swap.route.buy.platform)
        assertEquals("Kraken", swap.route.withdraw.platform)
    }

    @Test
    fun `should handle virtual operations`() {
        val swap = createTestSwap()

        // Update virtual operations
        swap.internal.virtual.deposit = true
        swap.internal.virtual.withdraw = true

        assert(swap.internal.virtual.deposit)
        assert(swap.internal.virtual.withdraw)
    }

    @Test
    fun `should update partner data`() {
        val swap = createTestSwap()

        // Update partner data
        swap.partner.key = "newpartner"
        swap.partner.name = "New Partner Name"
        swap.partner.fee = "0.002"

        assertEquals("newpartner", swap.partner.key)
        assertEquals("New Partner Name", swap.partner.name)
        assertEquals("0.002", swap.partner.fee)
        assertEquals(BigDecimal("0.002"), swap.partner.getFeeAsBigDecimal())
    }

    @Test
    fun `should handle complete swap lifecycle`() {
        val swap = createTestSwap()
        val currentTime = System.currentTimeMillis()

        // 1. Creation
        assertEquals(SwapStatus.Waiting, swap.info.status)
        assertEquals(SwapStep.DepositProcessing, swap.internal.step)

        // 2. Deposit confirmation
        swap.info.status = SwapStatus.Confirmation
        swap.internal.step = SwapStep.DepositProcessed
        swap.time.confirm = currentTime

        assertEquals(SwapStatus.Confirmation, swap.info.status)
        assertEquals(SwapStep.DepositProcessed, swap.internal.step)
        assertEquals(currentTime, swap.time.confirm)

        // 3. Selling
        swap.info.status = SwapStatus.Selling
        swap.internal.step = SwapStep.SellProcessing
        swap.time.sell = currentTime + 1000

        assertEquals(SwapStatus.Selling, swap.info.status)
        assertEquals(SwapStep.SellProcessing, swap.internal.step)
        assertEquals(currentTime + 1000, swap.time.sell)

        // 4. Successful completion
        swap.info.status = SwapStatus.Success
        swap.internal.step = SwapStep.WithdrawProcessed
        swap.time.success = currentTime + 5000
        swap.profit.amount = "50.25"
        swap.profit.percent = "1.25"

        assertEquals(SwapStatus.Success, swap.info.status)
        assertEquals(SwapStep.WithdrawProcessed, swap.internal.step)
        assertEquals(currentTime + 5000, swap.time.success)
        assertEquals("50.25", swap.profit.amount)
        assertEquals("1.25", swap.profit.percent)
    }

    private fun createTestSwap(): SwapOperation {
        return SwapOperation(
            info = Info(
                uid = "abc123",
                status = SwapStatus.Waiting,
                type = SwapType.fix,
                fee = "0.001",
                link = "https://swap.example.com/abc123",
                equivalent = "4000"
            ),
            deposit = Deposit(
                coin = "BTC",
                network = "Bitcoin",
                amount = "0.1",
                actual = null,
                address = "1ABC...",
                memo = null,
                platform = "Bybit",
                account = "user123"
            ),
            withdraw = Withdraw(
                coin = "USDT",
                network = "Ethereum",
                amount = "4000",
                address = "0x123...",
                memo = null,
                platform = "Bybit"
            ),
            expectations = Expectations(
                wait = "4000",
                confirm = null,
                sellTransfer = null,
                sell = null,
                buyTransfer = null,
                buy = null
            ),
            time = Time(
                create = System.currentTimeMillis(),
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
            ),
            profit = Profit(
                amount = "0",
                percent = "0"
            ),
            partner = Partner(
                key = "partner1",
                fee = "0.001",
                profit = Profit(amount = "0", percent = "0"),
                name = "Partner Name"
            ),
            internal = Internal(
                active = true,
                zipped = false,
                step = SwapStep.DepositProcessing,
                virtual = Virtual(deposit = false, withdraw = false)
            ),
            user = User(
                email = "user@example.com",
                refundAddress = "refund@example.com"
            ),
            linked = Linked(),
            route = Route(
                sell = RouteData("Bybit", "1ABC...", null, "user123"),
                buy = RouteData("Bybit", "0x123...", null, "user123"),
                withdraw = RouteData("Bybit", "0x123...", null, "user123")
            )
        )
    }
}

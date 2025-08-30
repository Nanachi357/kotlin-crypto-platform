package com.github.nanachi357.models.swap.enums

import com.github.nanachi357.models.swap.subclass.enums.SwapStatus
import com.github.nanachi357.models.swap.subclass.enums.SwapStep
import com.github.nanachi357.models.swap.subclass.enums.SwapType
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SwapEnumsTest {

    @Test
    fun `should have correct SwapStatus values`() {
        assertEquals("Waiting", SwapStatus.Waiting.name)
        assertEquals("Confirmation", SwapStatus.Confirmation.name)
        assertEquals("SellTransfer", SwapStatus.SellTransfer.name)
        assertEquals("Selling", SwapStatus.Selling.name)
        assertEquals("BuyTransfer", SwapStatus.BuyTransfer.name)
        assertEquals("Buying", SwapStatus.Buying.name)
        assertEquals("WithdrawTransfer", SwapStatus.WithdrawTransfer.name)
        assertEquals("Sending", SwapStatus.Sending.name)
        assertEquals("Success", SwapStatus.Success.name)
        assertEquals("Frozen", SwapStatus.Frozen.name)
        assertEquals("Refunded", SwapStatus.Refunded.name)
        assertEquals("Overdue", SwapStatus.Overdue.name)
        assertEquals("Suspended", SwapStatus.Suspended.name)
        assertEquals("Cancelled", SwapStatus.Cancelled.name)
        assertEquals("Transferring", SwapStatus.Transferring.name)
        assertEquals("Exchanging", SwapStatus.Exchanging.name)
    }

    @Test
    fun `should convert SwapStatus from string`() {
        assertEquals(SwapStatus.Waiting, SwapStatus.valueOf("Waiting"))
        assertEquals(SwapStatus.Success, SwapStatus.valueOf("Success"))
        assertEquals(SwapStatus.Cancelled, SwapStatus.valueOf("Cancelled"))
        assertEquals(SwapStatus.Frozen, SwapStatus.valueOf("Frozen"))
    }

    @Test
    fun `should have correct SwapStep values`() {
        assertEquals("DepositProcessing", SwapStep.DepositProcessing.name)
        assertEquals("DepositProcessed", SwapStep.DepositProcessed.name)
        assertEquals("SellTransferProcessing", SwapStep.SellTransferProcessing.name)
        assertEquals("SellTransferProcessed", SwapStep.SellTransferProcessed.name)
        assertEquals("SellProcessing", SwapStep.SellProcessing.name)
        assertEquals("SellProcessed", SwapStep.SellProcessed.name)
        assertEquals("BuyTransferProcessing", SwapStep.BuyTransferProcessing.name)
        assertEquals("BuyTransferProcessed", SwapStep.BuyTransferProcessed.name)
        assertEquals("BuyProcessing", SwapStep.BuyProcessing.name)
        assertEquals("BuyProcessed", SwapStep.BuyProcessed.name)
        assertEquals("WithdrawTransferProcessing", SwapStep.WithdrawTransferProcessing.name)
        assertEquals("WithdrawTransferProcessed", SwapStep.WithdrawTransferProcessed.name)
        assertEquals("WithdrawProcessing", SwapStep.WithdrawProcessing.name)
        assertEquals("WithdrawProcessed", SwapStep.WithdrawProcessed.name)
        assertEquals("AmlFrozen", SwapStep.AmlFrozen.name)
        assertEquals("AmlProcessing", SwapStep.AmlProcessing.name)
        assertEquals("NA", SwapStep.NA.name)
    }

    @Test
    fun `should convert SwapStep from string`() {
        assertEquals(SwapStep.DepositProcessing, SwapStep.valueOf("DepositProcessing"))
        assertEquals(SwapStep.SellProcessed, SwapStep.valueOf("SellProcessed"))
        assertEquals(SwapStep.WithdrawProcessed, SwapStep.valueOf("WithdrawProcessed"))
        assertEquals(SwapStep.NA, SwapStep.valueOf("NA"))
    }

    @Test
    fun `should have correct SwapType values`() {
        assertEquals("fix", SwapType.fix.name)
        assertEquals("float", SwapType.float.name)
    }

    @Test
    fun `should convert SwapType from string`() {
        assertEquals(SwapType.fix, SwapType.valueOf("fix"))
        assertEquals(SwapType.float, SwapType.valueOf("float"))
    }

    @Test
    fun `should have correct number of enum values`() {
        assertEquals(16, SwapStatus.values().size)
        assertEquals(17, SwapStep.values().size)
        assertEquals(2, SwapType.values().size)
    }

    @Test
    fun `should handle enum ordinal values`() {
        assertEquals(0, SwapStatus.Waiting.ordinal)
        assertEquals(1, SwapStatus.Confirmation.ordinal)
        assertEquals(15, SwapStatus.Exchanging.ordinal)

        assertEquals(0, SwapStep.DepositProcessing.ordinal)
        assertEquals(1, SwapStep.DepositProcessed.ordinal)
        assertEquals(16, SwapStep.NA.ordinal)

        assertEquals(0, SwapType.fix.ordinal)
        assertEquals(1, SwapType.float.ordinal)
    }

    @Test
    fun `should compare enum values correctly`() {
        // Simple ordinal comparisons that we know are true
        assertEquals(0, SwapStatus.Waiting.ordinal)
        assertEquals(1, SwapStatus.Confirmation.ordinal)
        assert(SwapStatus.Waiting.ordinal < SwapStatus.Confirmation.ordinal)
        
        assertEquals(0, SwapStep.DepositProcessing.ordinal)
        assertEquals(1, SwapStep.DepositProcessed.ordinal)
        assert(SwapStep.DepositProcessing.ordinal < SwapStep.DepositProcessed.ordinal)
        
        assertEquals(0, SwapType.fix.ordinal)
        assertEquals(1, SwapType.float.ordinal)
        assert(SwapType.fix.ordinal < SwapType.float.ordinal)
    }

    @Test
    fun `should handle enum in when expressions`() {
        val status = SwapStatus.Success
        val result = when (status) {
            SwapStatus.Waiting -> "waiting"
            SwapStatus.Confirmation -> "confirming"
            SwapStatus.Success -> "success"
            SwapStatus.Cancelled -> "cancelled"
            else -> "other"
        }
        assertEquals("success", result)
    }

    @Test
    fun `should handle enum in collections`() {
        val statuses = listOf(SwapStatus.Waiting, SwapStatus.Success, SwapStatus.Cancelled)
        assertEquals(3, statuses.size)
        assert(statuses.contains(SwapStatus.Waiting))
        assert(statuses.contains(SwapStatus.Success))
        assert(statuses.contains(SwapStatus.Cancelled))
    }

    @Test
    fun `should handle enum serialization`() {
        // Test that enums can be converted to string and back
        val status = SwapStatus.Success
        val statusString = status.name
        val convertedStatus = SwapStatus.valueOf(statusString)
        
        assertEquals(status, convertedStatus)
        assertEquals("Success", statusString)
    }
}

package com.github.nanachi357.models

import com.github.nanachi357.models.exchange.*
import com.github.nanachi357.models.bybit.BybitResponse
import com.github.nanachi357.models.bybit.BybitTickerItem
import com.github.nanachi357.utils.ResponseMapper

/**
 * Extension functions for backward compatibility
 */
fun <T> BybitResponse<T>.toExchangeResponse(
    exchange: Exchange = Exchange.BYBIT,
    mapper: (T) -> Any
): ExchangeResponse<Any> {
    return ResponseMapper.mapBybitResponse(this, exchange, mapper)
}

fun BybitTickerItem.toPriceData(): PriceData {
    return ResponseMapper.mapBybitTickerToPriceData(this)
}

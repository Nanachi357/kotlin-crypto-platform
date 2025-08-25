package com.github.nanachi357.utils

import com.github.nanachi357.models.exchange.*
import com.github.nanachi357.models.bybit.BybitResponse
import com.github.nanachi357.models.bybit.BybitTickerItem
import com.github.nanachi357.models.bybit.BybitTickerResult
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.assertions.throwables.shouldThrow

class ResponseMapperTest : FunSpec({
    
    context("ResponseMapper success method") {
        
        test("should create successful response with data") {
            val priceData = PriceData(
                symbol = "BTCUSDT",
                price = "50000",
                exchange = Exchange.BYBIT
            )
            
            val response = ResponseMapper.success(priceData, Exchange.BYBIT)
            
            response.success shouldBe true
            response.data shouldBe priceData
            response.error shouldBe null
            response.exchange shouldBe Exchange.BYBIT
            response.originalResponse shouldBe null // debug info disabled by default
        }
        
        test("should include debug info when requested") {
            val priceData = PriceData(
                symbol = "BTCUSDT",
                price = "50000",
                exchange = Exchange.BYBIT
            )
            
            val originalResponse = "{\"retCode\":0,\"retMsg\":\"OK\",\"result\":{}}"
            val response = ResponseMapper.success(
                priceData, 
                Exchange.BYBIT, 
                originalResponse = originalResponse,
                includeDebugInfo = true
            )
            
            response.success shouldBe true
            response.data shouldBe priceData
            response.originalResponse shouldBe originalResponse
        }
        
        test("should not include debug info by default") {
            val priceData = PriceData(
                symbol = "BTCUSDT",
                price = "50000",
                exchange = Exchange.BYBIT
            )
            
            val originalResponse = "{\"retCode\":0,\"retMsg\":\"OK\",\"result\":{}}"
            val response = ResponseMapper.success(
                priceData, 
                Exchange.BYBIT, 
                originalResponse = originalResponse
            )
            
            response.originalResponse shouldBe null
        }
    }
    
    context("ResponseMapper error method") {
        
        test("should create error response") {
            val response = ResponseMapper.error<PriceData>(
                "API key invalid", 
                Exchange.BYBIT
            )
            
            response.success shouldBe false
            response.data shouldBe null
            response.error shouldBe "API key invalid"
            response.exchange shouldBe Exchange.BYBIT
            response.originalResponse shouldBe null
        }
        
        test("should include debug info in error when requested") {
            val originalResponse = "{\"retCode\":10001,\"retMsg\":\"Invalid API key\"}"
            val response = ResponseMapper.error<PriceData>(
                "API key invalid", 
                Exchange.BYBIT,
                originalResponse = originalResponse,
                includeDebugInfo = true
            )
            
            response.success shouldBe false
            response.error shouldBe "API key invalid"
            response.originalResponse shouldBe originalResponse
        }
    }
    
    context("ResponseMapper mapBybitTickerToPriceData") {
        
        test("should map Bybit ticker to universal price data") {
            val bybitTicker = BybitTickerItem(
                symbol = "BTCUSDT",
                lastPrice = "50000",
                bid1Price = "49999",
                ask1Price = "50001",
                volume24h = "20.5",
                turnover24h = "1000000",
                price24hPcnt = "2.04",
                usdIndexPrice = "50000"
            )
            
            val priceData = ResponseMapper.mapBybitTickerToPriceData(bybitTicker)
            
            priceData.symbol shouldBe "BTCUSDT"
            priceData.price shouldBe "50000"
            priceData.exchange shouldBe Exchange.BYBIT
        }
        
        test("should map ticker with null usdIndexPrice") {
            val bybitTicker = BybitTickerItem(
                symbol = "BTCUSDT",
                lastPrice = "50000",
                bid1Price = "49999",
                ask1Price = "50001",
                volume24h = "20.5",
                turnover24h = "1000000",
                price24hPcnt = "2.04",
                usdIndexPrice = null
            )
            
            val priceData = ResponseMapper.mapBybitTickerToPriceData(bybitTicker)
            
            priceData.symbol shouldBe "BTCUSDT"
            priceData.price shouldBe "50000"
            priceData.exchange shouldBe Exchange.BYBIT
        }
    }
    
    context("ResponseMapper mapBybitResponse") {
        
        test("should map successful Bybit response") {
            val tickerItem = BybitTickerItem(
                symbol = "BTCUSDT",
                lastPrice = "50000",
                bid1Price = "49999",
                ask1Price = "50001",
                volume24h = "20.5",
                turnover24h = "1000000",
                price24hPcnt = "2.04",
                usdIndexPrice = "50000"
            )
            
            val tickerResult = BybitTickerResult(
                category = "spot",
                list = listOf(tickerItem)
            )
            
            val bybitResponse = BybitResponse(
                retCode = 0,
                retMsg = "OK",
                result = tickerResult,
                retExtInfo = null,
                time = 1234567890L
            )
            
            val response = ResponseMapper.mapBybitResponse(
                bybitResponse, 
                Exchange.BYBIT,
                mapper = { result -> ResponseMapper.mapBybitTickerToPriceData(result.list.first()) }
            )
            
            response.success shouldBe true
            response.data shouldNotBe null
            response.exchange shouldBe Exchange.BYBIT
            response.originalResponse shouldBe null // debug info disabled by default
        }
        
        test("should map error Bybit response") {
            val bybitResponse = BybitResponse<BybitTickerResult>(
                retCode = 10001,
                retMsg = "Invalid API key",
                result = BybitTickerResult("spot", emptyList()), // result cannot be null
                retExtInfo = null,
                time = 1234567890L
            )
            
            val response = ResponseMapper.mapBybitResponse(
                bybitResponse, 
                Exchange.BYBIT,
                mapper = { result -> result }
            )
            
            response.success shouldBe false
            response.data shouldBe null
            response.error shouldBe "Invalid API key"
            response.exchange shouldBe Exchange.BYBIT
        }
        
        test("should handle empty ticker list") {
            val tickerResult = BybitTickerResult(
                category = "spot",
                list = emptyList()
            )
            
            val bybitResponse = BybitResponse(
                retCode = 0,
                retMsg = "OK",
                result = tickerResult,
                retExtInfo = null,
                time = 1234567890L
            )
            
            // Test that the mapper throws exception for empty list
            val exception: IllegalArgumentException = shouldThrow<IllegalArgumentException> {
                ResponseMapper.mapBybitResponse(
                    bybitResponse, 
                    Exchange.BYBIT,
                    mapper = { result -> 
                        if (result.list.isEmpty()) throw IllegalArgumentException("No ticker data available")
                        else ResponseMapper.mapBybitTickerToPriceData(result.list.first())
                    }
                )
            }
            
            exception.message shouldBe "No ticker data available"
        }
    }
    
    context("ResponseMapper mapExchangeError") {
        
        test("should map Bybit error codes") {
            val error1 = ResponseMapper.mapExchangeError("10001", "Invalid API key", Exchange.BYBIT)
            error1.message shouldBe "Invalid API key"
            error1.httpStatus shouldBe 401
            error1.exchange shouldBe "BYBIT"
            error1.originalCode shouldBe "10001"
            error1.details["field"] shouldBe "apiKey"
            
            val error2 = ResponseMapper.mapExchangeError("10005", "Rate limit exceeded", Exchange.BYBIT)
            error2.message shouldBe "Rate limit exceeded"
            error2.httpStatus shouldBe 429
            error2.details["constraint"] shouldBe "limit"
        }
        
        test("should map Binance error codes") {
            val error = ResponseMapper.mapExchangeError("-2011", "Invalid API key", Exchange.BINANCE)
            error.message shouldBe "Invalid API key"
            error.httpStatus shouldBe 401
            error.exchange shouldBe "BINANCE"
            error.originalCode shouldBe "-2011"
        }
        
        test("should map Coinbase error codes") {
            val error = ResponseMapper.mapExchangeError("authentication_error", "Invalid API key", Exchange.COINBASE)
            error.message shouldBe "Invalid API key"
            error.httpStatus shouldBe 401
            error.exchange shouldBe "COINBASE"
            error.originalCode shouldBe "authentication_error"
        }
    }
    
    context("ResponseMapper validateSymbol") {
        
        test("should validate correct symbols") {
            ResponseMapper.validateSymbol("BTCUSDT") shouldBe true
            ResponseMapper.validateSymbol("ETHUSDT") shouldBe true
            ResponseMapper.validateSymbol("ADAUSDT") shouldBe true
            ResponseMapper.validateSymbol("BTC") shouldBe true
            ResponseMapper.validateSymbol("ETH") shouldBe true
        }
        
        test("should reject invalid symbols") {
            ResponseMapper.validateSymbol("") shouldBe false
            ResponseMapper.validateSymbol("A") shouldBe false // too short
            ResponseMapper.validateSymbol("BTCUSDTBTCUSDTBTCUSDT") shouldBe false // too long
            ResponseMapper.validateSymbol("btcusdt") shouldBe false // lowercase
            ResponseMapper.validateSymbol("BTC-USDT") shouldBe false // contains dash
            ResponseMapper.validateSymbol("BTC_USDT") shouldBe false // contains underscore
            ResponseMapper.validateSymbol("BTC'USDT") shouldBe false // contains single quote
            ResponseMapper.validateSymbol("BTC\"USDT") shouldBe false // contains double quote
            ResponseMapper.validateSymbol("BTC;USDT") shouldBe false // contains semicolon
            ResponseMapper.validateSymbol("BTC--USDT") shouldBe false // contains double dash
        }
        
        test("should reject SQL injection attempts") {
            ResponseMapper.validateSymbol("'; DROP TABLE users; --") shouldBe false
            ResponseMapper.validateSymbol("' OR 1=1 --") shouldBe false
            ResponseMapper.validateSymbol("'; INSERT INTO users VALUES ('hacker', 'password'); --") shouldBe false
        }
    }
})

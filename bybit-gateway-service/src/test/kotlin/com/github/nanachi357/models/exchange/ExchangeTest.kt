package com.github.nanachi357.models.exchange

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class ExchangeTest : FunSpec({
    
    context("Exchange enum") {
        
        test("should have correct properties for BYBIT") {
            Exchange.BYBIT.displayName shouldBe "Bybit"
            Exchange.BYBIT.baseUrl shouldBe "https://api.bybit.com"
            Exchange.BYBIT.testnetUrl shouldBe "https://api-testnet.bybit.com"
            Exchange.BYBIT.apiVersion shouldBe "v5"
            Exchange.BYBIT.requiresAuth shouldBe true
        }
        
        test("should have correct properties for BINANCE") {
            Exchange.BINANCE.displayName shouldBe "Binance"
            Exchange.BINANCE.baseUrl shouldBe "https://api.binance.com"
            Exchange.BINANCE.testnetUrl shouldBe "https://testnet.binance.vision"
            Exchange.BINANCE.apiVersion shouldBe "v3"
            Exchange.BINANCE.requiresAuth shouldBe true
        }
        
        test("should have correct properties for COINBASE") {
            Exchange.COINBASE.displayName shouldBe "Coinbase"
            Exchange.COINBASE.baseUrl shouldBe "https://api.coinbase.com"
            Exchange.COINBASE.testnetUrl shouldBe "https://api-public.sandbox.exchange.coinbase.com"
            Exchange.COINBASE.apiVersion shouldBe "v2"
            Exchange.COINBASE.requiresAuth shouldBe true
        }
        
        test("should generate correct API URLs") {
            val bybitUrl = Exchange.BYBIT.getApiUrl(false)
            bybitUrl shouldBe "https://api.bybit.com"
            
            val bybitTestnetUrl = Exchange.BYBIT.getApiUrl(true)
            bybitTestnetUrl shouldBe "https://api-testnet.bybit.com"
        }
        
        test("should generate correct full API URLs") {
            val bybitFullUrl = Exchange.BYBIT.getFullApiUrl("market/tickers", false)
            bybitFullUrl shouldBe "https://api.bybit.com/v5/market/tickers"
            
            val binanceFullUrl = Exchange.BINANCE.getFullApiUrl("ticker/price", true)
            binanceFullUrl shouldBe "https://testnet.binance.vision/v3/ticker/price"
        }
        
        test("should have unique display names") {
            val displayNames = Exchange.values().map { it.displayName }
            displayNames.toSet().size shouldBe displayNames.size
        }
        
        test("should have unique base URLs") {
            val baseUrls = Exchange.values().map { it.baseUrl }
            baseUrls.toSet().size shouldBe baseUrls.size
        }
    }
    
    context("ExchangeInfo") {
        
        test("should create ExchangeInfo with required fields") {
            val exchangeInfo = ExchangeInfo(
                exchange = Exchange.BYBIT,
                serverTime = 1234567890L,
                timezone = "UTC",
                rateLimits = emptyList(),
                symbols = emptyList()
            )
            
            exchangeInfo.exchange shouldBe Exchange.BYBIT
            exchangeInfo.serverTime shouldBe 1234567890L
            exchangeInfo.timezone shouldBe "UTC"
            exchangeInfo.status shouldBe "TRADING" // default value
        }
        
        test("should have default values for optional fields") {
            val exchangeInfo = ExchangeInfo(
                exchange = Exchange.BINANCE,
                serverTime = 1234567890L,
                timezone = "UTC",
                rateLimits = emptyList(),
                symbols = emptyList()
            )
            
            exchangeInfo.permissions shouldBe emptyList()
            exchangeInfo.status shouldBe "TRADING"
        }
    }
    
    context("RateLimit") {
        
        test("should create RateLimit with required fields") {
            val rateLimit = RateLimit(
                rateLimitType = "REQUEST_WEIGHT",
                interval = "MINUTE",
                intervalNum = 1,
                limit = 1200
            )
            
            rateLimit.rateLimitType shouldBe "REQUEST_WEIGHT"
            rateLimit.interval shouldBe "MINUTE"
            rateLimit.intervalNum shouldBe 1
            rateLimit.limit shouldBe 1200
            rateLimit.currentUsage shouldBe null
        }
        
        test("should check if rate limit is exceeded") {
            val rateLimit = RateLimit(
                rateLimitType = "REQUEST_WEIGHT",
                interval = "MINUTE",
                intervalNum = 1,
                limit = 1200,
                currentUsage = 1200
            )
            
            rateLimit.isExceeded() shouldBe true
        }
        
        test("should return remaining requests") {
            val rateLimit = RateLimit(
                rateLimitType = "REQUEST_WEIGHT",
                interval = "MINUTE",
                intervalNum = 1,
                limit = 1200,
                currentUsage = 800
            )
            
            rateLimit.getRemaining() shouldBe 400
        }
        
        test("should return null for remaining when currentUsage is null") {
            val rateLimit = RateLimit(
                rateLimitType = "REQUEST_WEIGHT",
                interval = "MINUTE",
                intervalNum = 1,
                limit = 1200
            )
            
            rateLimit.getRemaining() shouldBe null
        }
    }
    
    context("SymbolInfo") {
        
        test("should create SymbolInfo with required fields") {
            val symbolInfo = SymbolInfo(
                symbol = "BTCUSDT",
                status = "TRADING",
                baseAsset = "BTC",
                quoteAsset = "USDT"
            )
            
            symbolInfo.symbol shouldBe "BTCUSDT"
            symbolInfo.status shouldBe "TRADING"
            symbolInfo.baseAsset shouldBe "BTC"
            symbolInfo.quoteAsset shouldBe "USDT"
        }
        
        test("should check if symbol is trading") {
            val tradingSymbol = SymbolInfo(
                symbol = "BTCUSDT",
                status = "TRADING",
                baseAsset = "BTC",
                quoteAsset = "USDT"
            )
            
            val breakSymbol = SymbolInfo(
                symbol = "BTCUSDT",
                status = "BREAK",
                baseAsset = "BTC",
                quoteAsset = "USDT"
            )
            
            tradingSymbol.isTrading() shouldBe true
            breakSymbol.isTrading() shouldBe false
        }
        
        test("should generate correct display name") {
            val symbolInfo = SymbolInfo(
                symbol = "BTCUSDT",
                status = "TRADING",
                baseAsset = "BTC",
                quoteAsset = "USDT"
            )
            
            symbolInfo.getDisplayName() shouldBe "BTC/USDT"
        }
        
        test("should have default values for optional fields") {
            val symbolInfo = SymbolInfo(
                symbol = "BTCUSDT",
                status = "TRADING",
                baseAsset = "BTC",
                quoteAsset = "USDT"
            )
            
            symbolInfo.minPrice shouldBe null
            symbolInfo.maxPrice shouldBe null
            symbolInfo.tickSize shouldBe null
            symbolInfo.minQty shouldBe null
            symbolInfo.maxQty shouldBe null
            symbolInfo.stepSize shouldBe null
            symbolInfo.minNotional shouldBe null
            symbolInfo.filters shouldBe emptyList()
        }
    }
    
    context("SymbolFilter") {
        
        test("should create SymbolFilter with required fields") {
            val filter = SymbolFilter(
                filterType = "PRICE_FILTER",
                minPrice = "0.01",
                maxPrice = "1000000.00",
                tickSize = "0.01"
            )
            
            filter.filterType shouldBe "PRICE_FILTER"
            filter.minPrice shouldBe "0.01"
            filter.maxPrice shouldBe "1000000.00"
            filter.tickSize shouldBe "0.01"
        }
        
        test("should have null values for unused fields") {
            val filter = SymbolFilter(filterType = "PRICE_FILTER")
            
            filter.minQty shouldBe null
            filter.maxQty shouldBe null
            filter.stepSize shouldBe null
            filter.minNotional shouldBe null
        }
    }
    
    context("ExchangePermission enum") {
        
        test("should have all required permissions") {
            ExchangePermission.values().size shouldBe 8
            
            ExchangePermission.SPOT_TRADING shouldNotBe null
            ExchangePermission.MARGIN_TRADING shouldNotBe null
            ExchangePermission.FUTURES_TRADING shouldNotBe null
            ExchangePermission.OPTIONS_TRADING shouldNotBe null
            ExchangePermission.LEVERAGED_TRADING shouldNotBe null
            ExchangePermission.READ_ONLY shouldNotBe null
            ExchangePermission.WITHDRAW shouldNotBe null
            ExchangePermission.DEPOSIT shouldNotBe null
        }
    }
    
    context("ExchangeStatus enum") {
        
        test("should have all required statuses") {
            ExchangeStatus.values().size shouldBe 5
            
            ExchangeStatus.TRADING shouldNotBe null
            ExchangeStatus.MAINTENANCE shouldNotBe null
            ExchangeStatus.BREAK shouldNotBe null
            ExchangeStatus.AUCTION_MATCH shouldNotBe null
            ExchangeStatus.CLOSED shouldNotBe null
        }
    }
})

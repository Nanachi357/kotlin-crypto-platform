# Bybit Gateway Service - Testing Suite

## 📋 Overview

Comprehensive tests for **Commit 7 - Batch API with Domain Categories**.

## 📁 Files

```
testing/
├── README.md                           # This file
└── comprehensive-postman-collection.json # Complete test suite (25+ tests)
```

## 🚀 Quick Start

### 1. Start server
```bash
cd bybit-gateway-service
./gradlew run
```

### 2. Run tests
1. **Open Postman**
2. **Import** `comprehensive-postman-collection.json`
3. **Set variable** `baseUrl = http://localhost:8080`
4. **Run collection**

## 🧪 Test Coverage

### **Health & Basic Functionality** (3 tests)
- Health Check, Root Endpoint, Server Time

### **Market Data** (4 tests)  
- Single/Multiple symbols, All symbols

### **Error Handling** (9 tests)
- Validation errors, Invalid symbols, Mixed input, Exception types

### **Batch API - Core** (8 tests)
- Adaptive strategies (single/parallel/batch)
- Domain categories (SPOT/LINEAR)
- Default handling

### **Performance & Edge Cases** (6 tests)
- Invalid symbols, Long lists, Duplicates
- Performance comparison, Structure validation

### **Stress Testing** (3 tests)
- Concurrency, Large lists, Legacy performance

## 📊 Expected Results

### ✅ Success Response
```json
{
  "data": {
    "prices": [{"symbol": "BTCUSDT", "lastPrice": "43250.50"}],
    "metadata": {
      "strategy": "parallel",
      "category": "SPOT", 
      "requestTimeMs": 245,
      "successRate": 1.0
    }
  }
}
```

### ❌ Error Response
```json
{
  "data": {
    "prices": [{"symbol": "BTCUSDT", "lastPrice": "43250.50"}],
    "metadata": {"successRate": 0.33},
    "errors": {"INVALID1": "Symbol not found"}
  }
}
```

## 🔍 Key Features Tested

| Feature | Test Cases |
|---------|------------|
| **Adaptive Strategies** | Single (1), Parallel (2-3), Batch (4+) |
| **Domain Categories** | SPOT, LINEAR, Invalid → SPOT fallback |
| **Performance** | < 2000ms single, < 4000ms batch |
| **Error Handling** | Graceful degradation, Mixed valid/invalid |
| **Edge Cases** | Duplicates, Long lists, Concurrency |

## 🚨 Troubleshooting

- **Connection refused** → Start server with `./gradlew run`
- **Tests failing** → Check `baseUrl` variable in Postman
- **Performance fails** → Adjust timing thresholds if needed

## 🎯 Success Criteria

**All tests pass** + **Performance targets met** + **Error handling works** = ✅ Ready for production

---

**Import `comprehensive-postman-collection.json` and run! 🚀**

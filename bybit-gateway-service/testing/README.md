# Bybit Gateway Service - Error Handling Tests

## 📋 Overview

Цей каталог містить тести для **Phase 1: Commit 6 - Error Handling Foundation** проекту Bybit Gateway Service.

## 📁 Files Structure

```
testing/
├── README.md                    # Цей файл з інструкціями
├── curl-tests.sh               # Bash скрипт для curl тестування
└── postman-collection.json     # Postman collection для GUI тестування
```

## 🚀 Quick Start

### 1. Запуск сервера

```bash
# В корені проекту
cd bybit-gateway-service
./gradlew run
```

### 2. Перевірка що сервер працює

```bash
curl http://localhost:8080/health
```

Очікуваний результат:
```json
{
  "success": true,
  "data": {
    "status": "OK",
    "timestamp": "2025-01-07T12:00:00Z",
    "version": "1.0.0",
    "uptime": 1704627890123
  }
}
```

## 🧪 Testing Options

### Option 1: Curl Tests (Рекомендовано)

#### Переваги:
- ✅ Не потребує додаткового ПО
- ✅ Швидко і просто
- ✅ Кольорові результати
- ✅ Автоматичні перевірки

#### Запуск:

```bash
# Зробити файл виконуваним
chmod +x testing/curl-tests.sh

# Запустити всі тести
./testing/curl-tests.sh
```

#### Що тестує:
1. **Health & Basic Functionality** - базові endpoint'и
2. **Successful Market Data** - успішні запити ринкових даних
3. **Validation Errors** - помилки валідації символів
4. **Graceful Degradation** - graceful degradation для списків символів
5. **Exception Handler** - тести exception handler'а
6. **HTTP Status Codes** - перевірка правильних HTTP статусів
7. **Performance** - тести продуктивності
8. **Error Response Structure** - перевірка структури error responses

### Option 2: Postman Collection

#### Переваги:
- ✅ Графічний інтерфейс
- ✅ Автоматичні тести з JavaScript
- ✅ Збереження результатів
- ✅ Можливість експорту

#### Використання:

1. **Відкрити Postman**
2. **Import Collection** → вибрати `postman-collection.json`
3. **Set Environment Variable** `baseUrl = http://localhost:8080`
4. **Run Collection** → вибрати всю collection

#### Структура Collection:
- **1. Health & Basic Functionality** - базові тести
- **2. Successful Market Data** - успішні запити
- **3. Validation Errors** - помилки валідації
- **4. Graceful Degradation** - graceful degradation
- **5. Exception Handler Tests** - тести exception handler'а
- **6. Performance Tests** - тести продуктивності

## 📊 Expected Results

### ✅ Successful Responses

#### Health Check (200 OK):
```json
{
  "success": true,
  "data": {
    "status": "OK",
    "timestamp": "2025-01-07T12:00:00Z",
    "version": "1.0.0",
    "uptime": 1704627890123
  }
}
```

#### Market Data (200 OK):
```json
{
  "success": true,
  "data": {
    "retCode": 0,
    "retMsg": "OK",
    "result": {
      "category": "spot",
      "list": [
        {
          "symbol": "BTCUSDT",
          "lastPrice": "43250.50",
          "prevPrice24h": "43000.00",
          "price24hPcnt": "0.0058",
          "highPrice24h": "43500.00",
          "lowPrice24h": "42800.00"
        }
      ]
    }
  }
}
```

### ❌ Error Responses

#### Validation Error (400 Bad Request):
```json
{
  "error": "VALIDATION_ERROR",
  "message": "Invalid symbol: Symbol cannot be blank or null",
  "timestamp": 1704627890123,
  "path": "/api/market/",
  "details": {
    "field": "symbol"
  }
}
```

#### Not Found Error (404 Not Found):
```json
{
  "error": "NOT_FOUND",
  "message": "endpoint not found: /non-existent",
  "timestamp": 1704627890123,
  "path": "/non-existent",
  "details": {
    "resource": "endpoint",
    "identifier": "/non-existent"
  }
}
```

#### External API Error (504 Gateway Timeout):
```json
{
  "error": "EXTERNAL_API_ERROR",
  "message": "External service error (external): Request timeout: Test timeout error",
  "timestamp": 1704627890123,
  "path": "/test/timeout",
  "details": {
    "service": "external"
  }
}
```

## 🔍 Key Test Scenarios

### 1. Symbol Validation

| Test Case | Input | Expected Status | Expected Error Code |
|-----------|-------|-----------------|-------------------|
| Valid Symbol | `BTCUSDT` | 200 | - |
| Empty Symbol | `` | 400 | `VALIDATION_ERROR` |
| Too Short | `BT` | 400 | `VALIDATION_ERROR` |
| Special Chars | `btc-usdt` | 400 | `VALIDATION_ERROR` |
| Numbers Only | `123456` | 400 | `VALIDATION_ERROR` |
| Too Long | `BTCUSDT12345678901234567890` | 400 | `VALIDATION_ERROR` |

### 2. Graceful Degradation

| Test Case | Input | Expected Status | Behavior |
|-----------|-------|-----------------|----------|
| All Valid | `BTCUSDT,ETHUSDC` | 200 | Return data for all |
| Mixed | `BTCUSDT,invalid,ETHUSDC` | 200 | Filter invalid, return valid |
| All Invalid | `invalid1,invalid2` | 400 | Return validation error |

### 3. Exception Handler

| Test Case | Endpoint | Expected Status | Expected Error Code |
|-----------|----------|-----------------|-------------------|
| Validation | `/test/validation-error` | 400 | `VALIDATION_ERROR` |
| Not Found | `/test/not-found` | 404 | `NOT_FOUND` |
| Timeout | `/test/timeout` | 504 | `EXTERNAL_API_ERROR` |
| Unhandled | `/test/unhandled` | 500 | `INTERNAL_ERROR` |

## 🚨 Troubleshooting

### Common Issues:

#### 1. Server not running
```bash
# Помилка: Connection refused
# Рішення: Запустити сервер
./gradlew run
```

#### 2. Port already in use
```bash
# Помилка: Address already in use
# Рішення: Змінити порт або зупинити інший процес
lsof -ti:8080 | xargs kill -9
```

#### 3. jq not installed
```bash
# Помилка: jq: command not found
# Рішення: Встановити jq
# Ubuntu/Debian:
sudo apt-get install jq
# macOS:
brew install jq
# Windows:
# Завантажити з https://stedolan.github.io/jq/download/
```

#### 4. Permission denied
```bash
# Помилка: Permission denied
# Рішення: Зробити файл виконуваним
chmod +x testing/curl-tests.sh
```

### Debug Mode:

```bash
# Запустити тести з детальним виводом
bash -x testing/curl-tests.sh

# Запустити окремий тест
curl -v -X GET "http://localhost:8080/api/market/BTCUSDT"
```

## 📈 Performance Benchmarks

### Expected Performance:

| Metric | Target | Current |
|--------|--------|---------|
| Response Time | < 1s | ~200-500ms |
| Error Rate | < 1% | 0% |
| Memory Usage | Stable | Stable |
| Concurrent Requests | 10+ | 10+ |

### Load Testing:

```bash
# Тест паралельних запитів
for i in {1..10}; do
  curl -s -X GET "http://localhost:8080/api/market/BTCUSDT" &
done
wait
```

## 🔄 Continuous Integration

### GitHub Actions (Optional):

```yaml
# .github/workflows/test.yml
name: Error Handling Tests
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Start Server
        run: |
          cd bybit-gateway-service
          ./gradlew run &
          sleep 30
      - name: Run Tests
        run: |
          chmod +x testing/curl-tests.sh
          ./testing/curl-tests.sh
```

## 📝 Test Results Log

Після запуску тестів, перевірте:

- ✅ Всі HTTP статуси правильні
- ✅ Error responses мають консистентну структуру
- ✅ Graceful degradation працює
- ✅ Exception handler обробляє всі типи помилок
- ✅ Продуктивність в межах норми

## 🎯 Success Criteria

**Commit 6 вважається успішним, якщо:**

- [ ] Всі curl тести проходять
- [ ] Всі Postman тести проходять
- [ ] Error handling працює для всіх сценаріїв
- [ ] Graceful degradation працює
- [ ] Продуктивність задовільна
- [ ] Код готовий для Phase 2

---

**Готовий до тестування? Запускай `./testing/curl-tests.sh`! 🚀**

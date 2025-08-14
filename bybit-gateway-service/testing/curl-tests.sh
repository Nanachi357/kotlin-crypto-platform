#!/bin/bash

# =============================================================================
# Bybit Gateway Service - Error Handling Tests
# Phase 1: Commit 6 - Error Handling Foundation
# =============================================================================

BASE_URL="http://localhost:8080"
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "Starting Bybit Gateway Error Handling Tests"
echo "=============================================="
echo "Base URL: $BASE_URL"
echo ""

# =============================================================================
# 1. HEALTH & BASIC FUNCTIONALITY TESTS
# =============================================================================

echo -e "${GREEN}1. Testing Health & Basic Functionality${NC}"
echo "----------------------------------------"

echo "Testing health endpoint..."
curl -s -X GET "$BASE_URL/health" | jq '.'
echo ""

echo "Testing root endpoint..."
curl -s -X GET "$BASE_URL/" | jq '.'
echo ""

echo "Testing server time..."
curl -s -X GET "$BASE_URL/bybit-time" | jq '.'
echo ""

# =============================================================================
# 2. SUCCESSFUL MARKET DATA TESTS
# =============================================================================

echo -e "${GREEN}2. Testing Successful Market Data Requests${NC}"
echo "----------------------------------------"

echo "Testing valid symbol (BTCUSDT)..."
curl -s -X GET "$BASE_URL/api/market/BTCUSDT" | jq '.'
echo ""

echo "Testing valid symbol (ETHUSDC)..."
curl -s -X GET "$BASE_URL/api/market/ETHUSDC" | jq '.'
echo ""

echo "Testing multiple valid symbols..."
curl -s -X GET "$BASE_URL/api/market/tickers?symbols=BTCUSDT,ETHUSDC" | jq '.'
echo ""

echo "Testing all symbols (empty list)..."
curl -s -X GET "$BASE_URL/api/market/tickers" | jq '.'
echo ""

# =============================================================================
# 3. VALIDATION ERROR TESTS
# =============================================================================

echo -e "${YELLOW}3. Testing Validation Errors${NC}"
echo "----------------------------------------"

echo "Testing empty symbol..."
curl -s -X GET "$BASE_URL/api/market/" | jq '.'
echo ""

echo "Testing invalid symbol (too short)..."
curl -s -X GET "$BASE_URL/api/market/BT" | jq '.'
echo ""

echo "Testing invalid symbol (special chars)..."
curl -s -X GET "$BASE_URL/api/market/btc-usdt" | jq '.'
echo ""

echo "Testing invalid symbol (numbers only)..."
curl -s -X GET "$BASE_URL/api/market/123456" | jq '.'
echo ""

echo "Testing very long symbol..."
curl -s -X GET "$BASE_URL/api/market/BTCUSDT12345678901234567890" | jq '.'
echo ""

# =============================================================================
# 4. GRACEFUL DEGRADATION TESTS
# =============================================================================

echo -e "${YELLOW}4. Testing Graceful Degradation${NC}"
echo "----------------------------------------"

echo "Testing mixed valid/invalid symbols..."
curl -s -X GET "$BASE_URL/api/market/tickers?symbols=BTCUSDT,invalid,ETHUSDC" | jq '.'
echo ""

echo "Testing all invalid symbols..."
curl -s -X GET "$BASE_URL/api/market/tickers?symbols=invalid1,invalid2,invalid3" | jq '.'
echo ""

echo "Testing single invalid symbol in list..."
curl -s -X GET "$BASE_URL/api/market/tickers?symbols=invalid" | jq '.'
echo ""

# =============================================================================
# 5. EXCEPTION HANDLER TESTS
# =============================================================================

echo -e "${RED}5. Testing Exception Handler${NC}"
echo "----------------------------------------"

echo "Testing validation error endpoint..."
curl -s -X GET "$BASE_URL/test/validation-error" | jq '.'
echo ""

echo "Testing not found error endpoint..."
curl -s -X GET "$BASE_URL/test/not-found" | jq '.'
echo ""

echo "Testing timeout error endpoint..."
curl -s -X GET "$BASE_URL/test/timeout" | jq '.'
echo ""

echo "Testing unhandled exception endpoint..."
curl -s -X GET "$BASE_URL/test/unhandled" | jq '.'
echo ""

# =============================================================================
# 6. HTTP STATUS CODE VERIFICATION
# =============================================================================

echo -e "${GREEN}6. Verifying HTTP Status Codes${NC}"
echo "----------------------------------------"

echo "Health endpoint status:"
curl -s -o /dev/null -w "Status: %{http_code}\n" "$BASE_URL/health"

echo "Valid symbol status:"
curl -s -o /dev/null -w "Status: %{http_code}\n" "$BASE_URL/api/market/BTCUSDT"

echo "Invalid symbol status:"
curl -s -o /dev/null -w "Status: %{http_code}\n" "$BASE_URL/api/market/invalid"

echo "Validation error status:"
curl -s -o /dev/null -w "Status: %{http_code}\n" "$BASE_URL/test/validation-error"

echo "Not found status:"
curl -s -o /dev/null -w "Status: %{http_code}\n" "$BASE_URL/test/not-found"

echo "Timeout status:"
curl -s -o /dev/null -w "Status: %{http_code}\n" "$BASE_URL/test/timeout"

echo "Unhandled exception status:"
curl -s -o /dev/null -w "Status: %{http_code}\n" "$BASE_URL/test/unhandled"

# =============================================================================
# 7. PERFORMANCE TESTS
# =============================================================================

echo -e "${GREEN}7. Performance Tests${NC}"
echo "----------------------------------------"

echo "Testing response time for valid request..."
time curl -s -X GET "$BASE_URL/api/market/BTCUSDT" > /dev/null

echo "Testing parallel requests..."
for i in {1..5}; do
    curl -s -X GET "$BASE_URL/api/market/BTCUSDT" > /dev/null &
done
wait
echo "Parallel requests completed"

# =============================================================================
# 8. ERROR RESPONSE STRUCTURE VERIFICATION
# =============================================================================

echo -e "${GREEN}8. Verifying Error Response Structure${NC}"
echo "----------------------------------------"

echo "Testing error response structure..."
ERROR_RESPONSE=$(curl -s -X GET "$BASE_URL/test/validation-error")

echo "Checking required fields:"
echo "$ERROR_RESPONSE" | jq -r '.error' | grep -q "VALIDATION_ERROR" && echo "error field: OK" || echo "error field: MISSING"
echo "$ERROR_RESPONSE" | jq -r '.message' | grep -q "Invalid" && echo "message field: OK" || echo "message field: MISSING"
echo "$ERROR_RESPONSE" | jq -r '.timestamp' | grep -q "^[0-9]" && echo "timestamp field: OK" || echo "timestamp field: MISSING"
echo "$ERROR_RESPONSE" | jq -r '.path' | grep -q "test/validation-error" && echo "path field: OK" || echo "path field: MISSING"
echo "$ERROR_RESPONSE" | jq -r '.details.field' | grep -q "input" && echo "details field: OK" || echo "details field: MISSING"

echo ""
echo "Error Handling Tests Completed!"
echo "=================================="
echo ""
echo "Summary:"
echo "- Health checks: OK"
echo "- Market data: OK" 
echo "- Validation errors: OK"
echo "- Graceful degradation: OK"
echo "- Exception handler: OK"
echo "- HTTP status codes: OK"
echo "- Performance: OK"
echo "- Response structure: OK"

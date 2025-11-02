#!/bin/bash

# סקריפט לבדיקת ה-API
echo "🧪 Testing Delivery API..."
echo ""

BASE_URL="http://localhost:8080"

# Test 1: Resolve Address
echo "1️⃣ Testing /resolve-address"
curl -s -X POST "$BASE_URL/resolve-address" \
  -H "Content-Type: application/json" \
  -d '{"searchTerm": "Dizengoff 50, Tel Aviv"}' | jq '.'
echo ""

# Test 2: Get Timeslots
echo "2️⃣ Testing /timeslots"
curl -s -X POST "$BASE_URL/timeslots" \
  -H "Content-Type: application/json" \
  -d '{
    "address": {
      "street": "Dizengoff",
      "line1": "50",
      "line2": "",
      "city": "Tel Aviv",
      "country": "IL",
      "postcode": "6688102"
    }
  }' | jq '.'
echo ""

# Test 3: Book Delivery
echo "3️⃣ Testing /deliveries (booking)"
BOOKING_RESPONSE=$(curl -s -X POST "$BASE_URL/deliveries" \
  -H "Content-Type: application/json" \
  -d '{
    "user": "test@example.com",
    "timeslotId": "ts-2025-11-01-morning"
  }')
echo "$BOOKING_RESPONSE" | jq '.'
DELIVERY_ID=$(echo "$BOOKING_RESPONSE" | jq -r '.id')
echo ""

# Test 4: Get Daily Deliveries
echo "4️⃣ Testing /deliveries/daily"
curl -s "$BASE_URL/deliveries/daily" | jq '.'
echo ""

# Test 5: Complete Delivery
if [ "$DELIVERY_ID" != "null" ] && [ -n "$DELIVERY_ID" ]; then
  echo "5️⃣ Testing /deliveries/$DELIVERY_ID/complete"
  curl -s -X POST "$BASE_URL/deliveries/$DELIVERY_ID/complete" | jq '.'
  echo ""
fi

echo "✅ Tests completed!"


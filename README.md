# FinSecure Pay - Transaction Validator

A Spring Boot transaction validation API built for FinSecure Pay to provide a fast, repeatable, and reliable way to validate financial transactions.

## Prerequisites
To run this project locally, you will need:
- Java 17 or higher
- Maven (or use the included `mvnw` wrapper)

## How to Run
1. Clone the repository:
   ```bash
   git clone https://github.com/ahmadzhufar/finsecure-pay.git
   cd finsecure-pay

2. Start the application
   # On Windows
.\mvnw spring-boot:run

# On Mac/Linux
./mvnw spring-boot:run

The application will start on http://localhost:8080.

## How to Test
.\mvnw test

## API Endpoints
1. GET /HEALTH
   curl.exe http://localhost:8080/health

   Response:
   {"status":"UP","service":"finsecure-pay"}

3. POST /validate
   curl.exe -X POST http://localhost:8080/validate \
  -H "Content-Type: application/json" \
  -d '{\"accountId\":\"A\",\"destinationAccountId\":\"B\",\"amount\":100,\"currency\":\"USD\"}'

   Response(valid):
   {"valid":true,"reasons":[]}



 Business Rules Implemented
1. accountId and destinationAccountId are required.

2. Self-transfers are not allowed (e.g., Account A to Account A).

3. Amount must be greater than $0.01.

4. Amount must not exceed the daily limit of $10,000.00.

5. Currency must be one of the supported ISO 4217 codes: USD, EUR, GBP, SGD.



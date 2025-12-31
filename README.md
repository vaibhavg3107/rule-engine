# Rule Engine

A flexible, extensible rule engine for policy evaluation supporting Boolean (eligibility) and Offer (loan/product details) policies with tree-based rule composition.

## Features

- **Boolean Policies**: Evaluate eligibility criteria (APPROVED/REJECTED)
- **Offer Policies**: Calculate dynamic offers based on rules (loan amount, interest rate, etc.)
- **Policy Sets**: Combine Boolean + multiple Offer policies with priority-based selection
- **Tree-based Rules**: Compose complex rules using AND/OR logic
- **20 Operators**: Comprehensive operator support using Strategy pattern
- **Execution Logging**: Track all policy evaluations with detailed logs
- **Input Validation**: 400 Bad Request for missing required features

## Tech Stack

- Java 21
- Spring Boot 3.2.1
- PostgreSQL 16
- Flyway (database migrations)
- Lombok
- OpenAPI/Swagger

## Quick Start

### Start PostgreSQL + Application (One Command)

```bash
docker compose up --build -d
```

This will:
- Start PostgreSQL 16 on port 5432
- Build and start the Rule Engine app on port 8080
- Run Flyway migrations automatically
- Seed sample data (features, rules, policies)

### Access the Application

- **API Base URL**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html

### Stop Services

```bash
# Stop services (preserves data)
docker compose down

# Stop and remove all data
docker compose down -v
```

### View Logs

```bash
docker compose logs -f rule-engine
```

### Run Tests

```bash
docker run --rm -v "$(pwd)":/app -w /app maven:3.9.6-eclipse-temurin-21-alpine mvn test
```

## API Reference

### Features API
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/features` | Create a feature |
| GET | `/api/v1/features` | List all features |
| GET | `/api/v1/features/{id}` | Get feature by ID |
| PUT | `/api/v1/features/{id}` | Update a feature |
| DELETE | `/api/v1/features/{id}` | Delete a feature |

### Operators API
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/operators` | List all operators (20 available) |
| GET | `/api/v1/operators/{code}` | Get operator by code |

### Rules API
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/rules` | Create a rule |
| GET | `/api/v1/rules` | List all rules |
| GET | `/api/v1/rules/{id}` | Get rule by ID |
| POST | `/api/v1/rules/test` | Test a rule with input data |

### Policies API
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/policies` | Create a policy (BOOLEAN or OFFER) |
| GET | `/api/v1/policies` | List all policies |
| GET | `/api/v1/policies/{id}` | Get policy by ID |

### Policy Sets API
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/policy-sets` | Create a policy set |
| GET | `/api/v1/policy-sets` | List all policy sets |
| GET | `/api/v1/policy-sets/{id}` | Get policy set by ID |
| **POST** | **`/api/v1/policy-sets/{id}/evaluate`** | **Evaluate a policy set** |

## Evaluation API

### Request
```bash
curl -X POST http://localhost:8080/api/v1/policy-sets/{policySetId}/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "inputData": {
      "applicant": {
        "age": 30,
        "monthlyIncome": 50000,
        "creditScore": 720,
        "employmentType": "SALARIED"
      }
    }
  }'
```

### Response (Approved)
```json
{
  "decision": {
    "status": "APPROVED",
    "reasons": null
  },
  "offer": {
    "loanAmount": 300000.0,
    "rateOfInterest": 12.0,
    "processingFee": 1.5,
    "tenure": 48,
    "emi": null
  }
}
```

### Response (Missing Input - 400 Bad Request)
```json
{
  "code": "VALIDATION_ERROR",
  "message": "Missing required input for feature(s): applicant_income, applicant_credit_score",
  "timestamp": "2025-12-31T10:29:48.366506553"
}
```

## Supported Operators

| Code | Name | Operand Type |
|------|------|--------------|
| `EQ` | Equals | SINGLE |
| `NEQ` | Not Equals | SINGLE |
| `LT` | Less Than | SINGLE |
| `LTE` | Less Than or Equal | SINGLE |
| `GT` | Greater Than | SINGLE |
| `GTE` | Greater Than or Equal | SINGLE |
| `IN` | In List | LIST |
| `NOT_IN` | Not In List | LIST |
| `BETWEEN` | Between | RANGE |
| `CONTAINS` | Contains | SINGLE |
| `STARTS_WITH` | Starts With | SINGLE |
| `ENDS_WITH` | Ends With | SINGLE |
| `REGEX` | Regex Match | SINGLE |
| `CONTAINS_ALL` | Contains All | LIST |
| `CONTAINS_ANY` | Contains Any | LIST |
| `IS_EMPTY` | Is Empty | NONE |
| `IS_NOT_EMPTY` | Is Not Empty | NONE |
| `SIZE_EQ` | Size Equals | SINGLE |
| `SIZE_GT` | Size Greater Than | SINGLE |
| `SIZE_LT` | Size Less Than | SINGLE |

## Project Structure

```
rule-engine/
├── src/main/java/com/example/ruleengine/
│   ├── controller/           # REST controllers
│   ├── dto/                  # Request/Response DTOs
│   ├── entity/               # JPA entities
│   ├── exception/            # Exception handling
│   ├── repository/           # Data access layer
│   └── service/
│       ├── operator/         # Strategy pattern for operators
│       │   ├── OperatorStrategy.java
│       │   ├── OperatorStrategyFactory.java
│       │   └── impl/         # 20 operator implementations
│       ├── PolicyEvaluationService.java
│       ├── UnifiedEvaluationService.java
│       └── ...
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/         # Flyway migrations (V1-V6)
├── Rule_Engine_API_Collection.json   # Postman collection
├── docker-compose.yml
└── Dockerfile
```

## Postman Collection

Import the Postman collection from `Rule_Engine_API_Collection.json` for ready-to-use API requests.

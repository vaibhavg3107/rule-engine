# Rule Engine Design Document

## 1. System Overview

A flexible rule engine that evaluates input data against configurable policies to produce either **boolean decisions** or **JSON outputs**. The engine supports complex rule compositions using logical operators (AND/OR) organized in a tree structure.

### 1.1 Key Capabilities
- **Feature Extraction**: Transform raw input into typed features
- **Rule Evaluation**: Compare features against conditions using type-appropriate operators
- **Policy Composition**: Combine rules using AND/OR logic in a tree structure
- **Dual Output Modes**: Boolean policies (pass/fail) and Output policies (JSON results)

### 1.2 High-Level Flow
```
Input Data → Feature Extraction → Rule Tree Evaluation → Policy Output
```

---

## 2. Core Entities

### 2.1 Feature
Represents a computed/extracted value from input data.

```json
{
  "id": "uuid",
  "name": "user_age",
  "description": "Age of the user in years",
  "featureType": "NUMERIC",  // NUMERIC, STRING, BOOLEAN, DATE, LIST
  "extractionPath": "$.user.age",  // JSONPath or custom extractor
  "defaultValue": null,
  "version": 1,
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

### 2.2 Operator
Operators are pre-defined with their compatible LHS (feature) types and RHS (operand) types.

```json
{
  "id": "uuid",
  "code": "IN",
  "name": "In List",
  "description": "Check if value exists in a list",
  "compatibleFeatureTypes": ["NUMERIC", "STRING"],  // LHS types
  "operandType": "LIST",  // RHS type: SINGLE, LIST, RANGE, NONE
  "operandElementType": "SAME_AS_FEATURE"  // Element type matches feature type
}
```

**Operator Definitions:**

| Operator | Code | Compatible Feature Types (LHS) | Operand Type (RHS) | Operand Element Type |
|----------|------|-------------------------------|-------------------|---------------------|
| Equals | `EQ` | NUMERIC, STRING, BOOLEAN, DATE | SINGLE | SAME_AS_FEATURE |
| Not Equals | `NEQ` | NUMERIC, STRING, BOOLEAN, DATE | SINGLE | SAME_AS_FEATURE |
| Less Than | `LT` | NUMERIC, DATE | SINGLE | SAME_AS_FEATURE |
| Less Than or Equal | `LTE` | NUMERIC, DATE | SINGLE | SAME_AS_FEATURE |
| Greater Than | `GT` | NUMERIC, DATE | SINGLE | SAME_AS_FEATURE |
| Greater Than or Equal | `GTE` | NUMERIC, DATE | SINGLE | SAME_AS_FEATURE |
| In | `IN` | NUMERIC, STRING, DATE | LIST | SAME_AS_FEATURE |
| Not In | `NOT_IN` | NUMERIC, STRING, DATE | LIST | SAME_AS_FEATURE |
| Between | `BETWEEN` | NUMERIC, DATE | RANGE | SAME_AS_FEATURE |
| Contains | `CONTAINS` | STRING, LIST | SINGLE | STRING (for STRING), ANY (for LIST) |
| Starts With | `STARTS_WITH` | STRING | SINGLE | STRING |
| Ends With | `ENDS_WITH` | STRING | SINGLE | STRING |
| Regex Match | `REGEX` | STRING | SINGLE | STRING (pattern) |
| Contains All | `CONTAINS_ALL` | LIST | LIST | ANY |
| Contains Any | `CONTAINS_ANY` | LIST | LIST | ANY |
| Is Empty | `IS_EMPTY` | LIST, STRING | NONE | N/A |
| Is Not Empty | `IS_NOT_EMPTY` | LIST, STRING | NONE | N/A |
| Size Equals | `SIZE_EQ` | LIST | SINGLE | NUMERIC |
| Size Greater Than | `SIZE_GT` | LIST | SINGLE | NUMERIC |
| Size Less Than | `SIZE_LT` | LIST | SINGLE | NUMERIC |

**Operand Type Definitions:**
- **SINGLE**: Single value (e.g., `18`, `"active"`, `true`)
- **LIST**: Array of values (e.g., `[1, 2, 3]`, `["a", "b"]`)
- **RANGE**: Object with `min` and `max` (e.g., `{"min": 18, "max": 65}`)
- **NONE**: No operand required (e.g., `IS_EMPTY`)

**Operator Entity:**
```json
{
  "id": "op-001",
  "code": "IN",
  "name": "In List",
  "description": "Check if feature value exists in the provided list",
  "compatibleFeatureTypes": ["NUMERIC", "STRING", "DATE"],
  "operandType": "LIST",
  "operandElementType": "SAME_AS_FEATURE"
}
```

**FeatureType to Operators Mapping (derived):**

| Feature Type | Available Operators |
|--------------|---------------------|
| NUMERIC      | `EQ`, `NEQ`, `LT`, `LTE`, `GT`, `GTE`, `IN`, `NOT_IN`, `BETWEEN` |
| STRING       | `EQ`, `NEQ`, `IN`, `NOT_IN`, `CONTAINS`, `STARTS_WITH`, `ENDS_WITH`, `REGEX`, `IS_EMPTY`, `IS_NOT_EMPTY` |
| BOOLEAN      | `EQ`, `NEQ` |
| DATE         | `EQ`, `NEQ`, `LT`, `LTE`, `GT`, `GTE`, `IN`, `NOT_IN`, `BETWEEN` |
| LIST         | `CONTAINS`, `CONTAINS_ALL`, `CONTAINS_ANY`, `IS_EMPTY`, `IS_NOT_EMPTY`, `SIZE_EQ`, `SIZE_GT`, `SIZE_LT` |

### 2.3 Rule (Leaf Node)
A single condition comparing a feature against a value. The `operatorCode` must be compatible with the feature's type.

```json
{
  "id": "uuid",
  "name": "age_check",
  "description": "Check if user is adult",
  "featureId": "feature-uuid",
  "operatorCode": "GTE",  // References Operator.code - validated against feature type
  "operand": 18,  // RHS value - type validated against operator's operandType
  "enabled": true,
  "version": 1
}
```

**Validation on Rule Creation:**
1. Fetch the Feature by `featureId` → get `featureType`
2. Fetch the Operator by `operatorCode` → get `compatibleFeatureTypes` and `operandType`
3. Validate: `featureType` ∈ `compatibleFeatureTypes`
4. Validate: `operand` matches `operandType` (SINGLE/LIST/RANGE/NONE)
5. If `operandType` is LIST and `operandElementType` is `SAME_AS_FEATURE`, validate each element type

### 2.4 RuleNode (Tree Node)
Represents either a leaf rule or a composite node with children.

```json
{
  "id": "uuid",
  "nodeType": "COMPOSITE",  // LEAF or COMPOSITE
  "logicalOperator": "AND",  // AND, OR (only for COMPOSITE)
  "ruleId": null,  // Only for LEAF nodes
  "children": [  // Only for COMPOSITE nodes
    { "nodeType": "LEAF", "ruleId": "rule-uuid-1" },
    { "nodeType": "LEAF", "ruleId": "rule-uuid-2" }
  ],
  "negated": false  // Apply NOT to this node's result
}
```

### 2.5 Policy Set
A **Policy Set** groups a Boolean policy (eligibility) and an Offer policy (offer details) together. The evaluation API runs both and returns a unified response.

```json
{
  "id": "uuid",
  "name": "personal_loan_policy_set",
  "description": "Personal loan eligibility and offer policies",
  "booleanPolicy": {
    "id": "boolean-policy-uuid",
    "name": "loan_eligibility",
    "description": "Determines if user is eligible for loan",
    "rootNode": { /* RuleNode tree for eligibility */ }
  },
  "offerPolicy": {
    "id": "offer-policy-uuid",
    "name": "loan_offer",
    "description": "Computes loan offer details",
    "rootNode": { /* RuleNode tree for offer tiers */ },
    "outputMapping": { /* offer output configuration */ }
  },
  "evaluationStrategy": "SEQUENTIAL",  // SEQUENTIAL: Boolean first, then Offer if approved
  "enabled": true,
  "version": 1,
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

**Evaluation Strategy:**
- **SEQUENTIAL**: Run Boolean policy first. If APPROVED, run Offer policy. If REJECTED, skip Offer.
- **PARALLEL**: Run both policies simultaneously (for analytics/logging even on rejection).

### 2.6 Individual Policy
Each policy within a Policy Set.

```json
{
  "id": "uuid",
  "name": "loan_eligibility",
  "description": "Determines if user is eligible for loan",
  "policyType": "BOOLEAN",  // BOOLEAN or OFFER
  "rootNode": { /* RuleNode tree */ },
  "outputMapping": null,  // Required for OFFER type
  "enabled": true,
  "version": 1
}
```

### 2.7 Policy Output Structures

#### 2.7.1 Boolean Policy Output
Boolean policies return a simple approval/rejection decision.

**Output Structure:**
```json
{
  "decision": "APPROVED",  // APPROVED or REJECTED
  "reasons": [  // Optional: reasons for rejection
    {
      "code": "AGE_CHECK_FAILED",
      "message": "Applicant must be at least 21 years old"
    }
  ]
}
```

#### 2.7.2 Offer Policy Output
Offer policies return computed values based on rule evaluations. Used for loan offers, pricing, etc.

**OutputMapping for Offer Policy:**
```json
{
  "outputSchema": {
    "loanAmount": { "type": "NUMERIC", "description": "Approved loan amount" },
    "rateOfInterest": { "type": "NUMERIC", "description": "Annual ROI in percentage" },
    "processingFee": { "type": "NUMERIC", "description": "Processing fee in percentage" },
    "tenure": { "type": "NUMERIC", "description": "Loan tenure in months" },
    "emi": { "type": "NUMERIC", "description": "Monthly EMI amount" }
  },
  "defaultOutput": {
    "loanAmount": 0,
    "rateOfInterest": 0,
    "processingFee": 0,
    "tenure": 0,
    "emi": 0
  },
  "conditionalOutputs": [
    {
      "condition": "PASS",
      "output": {
        "loanAmount": 500000,
        "rateOfInterest": 10.5,
        "processingFee": 1.5,
        "tenure": 60,
        "emi": 10747
      }
    },
    {
      "condition": "FAIL",
      "output": null  // No offer if rules fail
    }
  ],
  "includeEvaluationDetails": true
}
```

**Tiered Offer Example (based on credit score):**
```json
{
  "conditionalOutputs": [
    {
      "conditionNodeId": "premium-tier-node",
      "condition": "PASS",
      "output": {
        "loanAmount": 1000000,
        "rateOfInterest": 9.5,
        "processingFee": 1.0,
        "tenure": 84,
        "tier": "PREMIUM"
      }
    },
    {
      "conditionNodeId": "standard-tier-node",
      "condition": "PASS",
      "output": {
        "loanAmount": 500000,
        "rateOfInterest": 12.0,
        "processingFee": 2.0,
        "tenure": 60,
        "tier": "STANDARD"
      }
    },
    {
      "conditionNodeId": "basic-tier-node",
      "condition": "PASS",
      "output": {
        "loanAmount": 200000,
        "rateOfInterest": 14.5,
        "processingFee": 2.5,
        "tenure": 36,
        "tier": "BASIC"
      }
    }
  ]
}
```

---

## 3. Rule Tree Structure

### 3.1 Tree Representation
Rules are organized as a binary expression tree where:
- **Leaf nodes** contain actual rules (conditions)
- **Composite nodes** combine children with AND/OR operators

### 3.2 Example Tree
```
Policy: Loan Eligibility
├── AND (root)
│   ├── LEAF: age >= 21
│   ├── OR
│   │   ├── LEAF: income >= 50000
│   │   └── LEAF: has_collateral = true
│   └── LEAF: credit_score >= 650
```

**JSON Representation:**
```json
{
  "nodeType": "COMPOSITE",
  "logicalOperator": "AND",
  "children": [
    {
      "nodeType": "LEAF",
      "ruleId": "age-rule-id"
    },
    {
      "nodeType": "COMPOSITE",
      "logicalOperator": "OR",
      "children": [
        { "nodeType": "LEAF", "ruleId": "income-rule-id" },
        { "nodeType": "LEAF", "ruleId": "collateral-rule-id" }
      ]
    },
    {
      "nodeType": "LEAF",
      "ruleId": "credit-score-rule-id"
    }
  ]
}
```

### 3.3 Evaluation Algorithm
```
function evaluate(node, featureContext):
    if node.negated:
        return NOT evaluate_inner(node, featureContext)
    return evaluate_inner(node, featureContext)

function evaluate_inner(node, featureContext):
    if node.nodeType == LEAF:
        rule = getRule(node.ruleId)
        featureValue = featureContext.get(rule.featureId)
        return applyOperator(featureValue, rule.operator, rule.operand)
    
    if node.logicalOperator == AND:
        return ALL(evaluate(child, featureContext) for child in node.children)
    
    if node.logicalOperator == OR:
        return ANY(evaluate(child, featureContext) for child in node.children)
```

---

## 4. Database Schema

### 4.1 Entity Relationship Diagram
```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Feature   │     │  Operator   │     │    Rule     │     │   Policy    │
├─────────────┤     ├─────────────┤     ├─────────────┤     ├─────────────┤
│ id (PK)     │     │ id (PK)     │     │ id (PK)     │     │ id (PK)     │
│ name        │     │ code (UK)   │     │ name        │     │ name        │
│ feature_type│     │ name        │     │ feature_id  │────►│ policy_type │
│ extraction  │     │ operand_type│     │ operator_cd │────►│ root_node   │
│ default_val │     │ compat_types│     │ operand     │     │ output_map  │
└─────────────┘     └─────────────┘     │ enabled     │     │ enabled     │
       ▲                   ▲            └─────────────┘     └──────┬──────┘
       │                   │                                       │
       └───────────────────┴── Rule references Feature & Operator  │
                                                                   │
┌──────────────────┐     ┌────────────────────────┐                │
│    PolicySet     │     │ PolicySetOfferPolicy   │                │
├──────────────────┤     ├────────────────────────┤                │
│ id (PK)          │     │ id (PK)                │                │
│ name             │     │ policy_set_id (FK)─────┼───┐            │
│ boolean_policy_id│────►│ offer_policy_id (FK)───┼───┼────────────┘
│ eval_strategy    │◄────┤ priority               │   │
│ enabled          │     │ enabled                │   │
└──────────────────┘     └────────────────────────┘   │
         │                                            │
         └────────────────────────────────────────────┘

PolicySet has ONE Boolean Policy and MANY Offer Policies (via PolicySetOfferPolicy)
```

### 4.2 Tables

**features**
```sql
CREATE TABLE features (
    id UUID PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    feature_type VARCHAR(50) NOT NULL,
    extraction_config JSONB NOT NULL,
    default_value JSONB,
    version INT DEFAULT 1,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);
```

**operators** (seed data - typically read-only)
```sql
CREATE TABLE operators (
    id UUID PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,  -- EQ, NEQ, IN, GTE, etc.
    name VARCHAR(100) NOT NULL,
    description TEXT,
    compatible_feature_types VARCHAR(50)[] NOT NULL,  -- {NUMERIC, STRING, DATE}
    operand_type VARCHAR(20) NOT NULL,  -- SINGLE, LIST, RANGE, NONE
    operand_element_type VARCHAR(50),  -- SAME_AS_FEATURE, STRING, NUMERIC, ANY
    created_at TIMESTAMP DEFAULT NOW()
);

-- Seed data examples
INSERT INTO operators (id, code, name, compatible_feature_types, operand_type, operand_element_type) VALUES
  (gen_random_uuid(), 'EQ', 'Equals', '{NUMERIC,STRING,BOOLEAN,DATE}', 'SINGLE', 'SAME_AS_FEATURE'),
  (gen_random_uuid(), 'IN', 'In List', '{NUMERIC,STRING,DATE}', 'LIST', 'SAME_AS_FEATURE'),
  (gen_random_uuid(), 'GTE', 'Greater Than or Equal', '{NUMERIC,DATE}', 'SINGLE', 'SAME_AS_FEATURE'),
  (gen_random_uuid(), 'BETWEEN', 'Between', '{NUMERIC,DATE}', 'RANGE', 'SAME_AS_FEATURE'),
  (gen_random_uuid(), 'IS_EMPTY', 'Is Empty', '{LIST,STRING}', 'NONE', NULL);
```

**rules**
```sql
CREATE TABLE rules (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    feature_id UUID NOT NULL REFERENCES features(id),
    operator_code VARCHAR(50) NOT NULL REFERENCES operators(code),
    operand JSONB,  -- NULL for NONE operand_type (e.g., IS_EMPTY)
    enabled BOOLEAN DEFAULT true,
    version INT DEFAULT 1,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    CONSTRAINT valid_operator_for_feature CHECK (/* validated at app layer */)
);
```

**policies**
```sql
CREATE TABLE policies (
    id UUID PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    policy_type VARCHAR(20) NOT NULL,  -- BOOLEAN, OFFER
    root_node JSONB NOT NULL,  -- Tree structure
    output_mapping JSONB,  -- Required for OFFER type
    enabled BOOLEAN DEFAULT true,
    version INT DEFAULT 1,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);
```

**policy_sets**
```sql
CREATE TABLE policy_sets (
    id UUID PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    boolean_policy_id UUID NOT NULL REFERENCES policies(id),
    evaluation_strategy VARCHAR(20) DEFAULT 'BOOLEAN_FIRST',
    enabled BOOLEAN DEFAULT true,
    version INT DEFAULT 1,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Multiple offer policies with priorities
CREATE TABLE policy_set_offer_policies (
    id UUID PRIMARY KEY,
    policy_set_id UUID NOT NULL REFERENCES policy_sets(id),
    offer_policy_id UUID NOT NULL REFERENCES policies(id),
    priority INT NOT NULL DEFAULT 1,
    enabled BOOLEAN DEFAULT true,
    UNIQUE(policy_set_id, offer_policy_id)
);
```

**policy_set_execution_logs** (for audit)
```sql
CREATE TABLE policy_set_execution_logs (
    id UUID PRIMARY KEY,
    policy_set_id UUID REFERENCES policy_sets(id),
    policy_set_version INT,
    input_data JSONB,
    extracted_features JSONB,
    boolean_policy_result JSONB,  -- {status, reasons}
    offer_policy_result JSONB,    -- {loanAmount, roi, ...} or null
    decision_status VARCHAR(20),  -- APPROVED, REJECTED
    execution_time_ms INT,
    executed_at TIMESTAMP DEFAULT NOW()
);
```

---

## 5. API Contracts

### 5.1 Feature APIs

**Create Feature**
```
POST /api/v1/features
```
Request:
```json
{
  "name": "user_age",
  "description": "Age of the user",
  "featureType": "NUMERIC",
  "extractionConfig": {
    "type": "JSON_PATH",
    "path": "$.user.age"
  },
  "defaultValue": null
}
```
Response: `201 Created`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "name": "user_age",
  "featureType": "NUMERIC",
  "version": 1,
  "createdAt": "2024-01-15T10:30:00Z"
}
```

**Get Feature**
```
GET /api/v1/features/{featureId}
```

**List Features**
```
GET /api/v1/features?page=0&size=20&featureType=NUMERIC
```

**Update Feature**
```
PUT /api/v1/features/{featureId}
```

**Delete Feature**
```
DELETE /api/v1/features/{featureId}
```

---

### 5.2 Operator APIs

Operators are typically seeded and read-only. These APIs are for listing/querying available operators.

**List All Operators**
```
GET /api/v1/operators
```
Response: `200 OK`
```json
{
  "operators": [
    {
      "id": "op-001",
      "code": "EQ",
      "name": "Equals",
      "description": "Check if values are equal",
      "compatibleFeatureTypes": ["NUMERIC", "STRING", "BOOLEAN", "DATE"],
      "operandType": "SINGLE",
      "operandElementType": "SAME_AS_FEATURE"
    },
    {
      "id": "op-002",
      "code": "IN",
      "name": "In List",
      "description": "Check if value exists in a list",
      "compatibleFeatureTypes": ["NUMERIC", "STRING", "DATE"],
      "operandType": "LIST",
      "operandElementType": "SAME_AS_FEATURE"
    }
  ]
}
```

**Get Operators by Feature Type**
```
GET /api/v1/operators?featureType=NUMERIC
```
Response: `200 OK`
```json
{
  "featureType": "NUMERIC",
  "operators": [
    { "code": "EQ", "name": "Equals", "operandType": "SINGLE" },
    { "code": "NEQ", "name": "Not Equals", "operandType": "SINGLE" },
    { "code": "LT", "name": "Less Than", "operandType": "SINGLE" },
    { "code": "LTE", "name": "Less Than or Equal", "operandType": "SINGLE" },
    { "code": "GT", "name": "Greater Than", "operandType": "SINGLE" },
    { "code": "GTE", "name": "Greater Than or Equal", "operandType": "SINGLE" },
    { "code": "IN", "name": "In List", "operandType": "LIST" },
    { "code": "NOT_IN", "name": "Not In List", "operandType": "LIST" },
    { "code": "BETWEEN", "name": "Between", "operandType": "RANGE" }
  ]
}
```

**Get Single Operator**
```
GET /api/v1/operators/{operatorCode}
```
Response: `200 OK`
```json
{
  "id": "op-002",
  "code": "IN",
  "name": "In List",
  "description": "Check if feature value exists in the provided list",
  "compatibleFeatureTypes": ["NUMERIC", "STRING", "DATE"],
  "operandType": "LIST",
  "operandElementType": "SAME_AS_FEATURE"
}
```

---

### 5.3 Rule APIs

**Create Rule**
```
POST /api/v1/rules
```
Request:
```json
{
  "name": "adult_check",
  "description": "Check if user is 18 or older",
  "featureId": "550e8400-e29b-41d4-a716-446655440001",
  "operatorCode": "GTE",
  "operand": 18
}
```
Response: `201 Created`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440002",
  "name": "adult_check",
  "featureId": "550e8400-e29b-41d4-a716-446655440001",
  "featureName": "user_age",
  "featureType": "NUMERIC",
  "operatorCode": "GTE",
  "operatorName": "Greater Than or Equal",
  "operand": 18,
  "enabled": true,
  "version": 1
}
```

**Get Rule**
```
GET /api/v1/rules/{ruleId}
```

**List Rules**
```
GET /api/v1/rules?featureId={featureId}&page=0&size=20
```

**Update Rule**
```
PUT /api/v1/rules/{ruleId}
```

**Delete Rule**
```
DELETE /api/v1/rules/{ruleId}
```

**Test Rule** (evaluate a single rule)
```
POST /api/v1/rules/{ruleId}/test
```
Request:
```json
{
  "inputData": {
    "user": { "age": 25 }
  }
}
```
Response:
```json
{
  "ruleId": "550e8400-e29b-41d4-a716-446655440002",
  "ruleName": "adult_check",
  "featureName": "user_age",
  "featureValue": 25,
  "operatorCode": "GTE",
  "operatorName": "Greater Than or Equal",
  "operand": 18,
  "result": true
}
```

---

### 5.4 Policy APIs

**Create Policy**
```
POST /api/v1/policies
```
Request (Boolean Policy):
```json
{
  "name": "loan_eligibility",
  "description": "Determines loan eligibility",
  "policyType": "BOOLEAN",
  "rootNode": {
    "nodeType": "COMPOSITE",
    "logicalOperator": "AND",
    "children": [
      {
        "nodeType": "LEAF",
        "ruleId": "age-rule-id"
      },
      {
        "nodeType": "COMPOSITE",
        "logicalOperator": "OR",
        "children": [
          { "nodeType": "LEAF", "ruleId": "income-rule-id" },
          { "nodeType": "LEAF", "ruleId": "collateral-rule-id" }
        ]
      }
    ]
  },
  "priority": 100
}
```

Request (Output Policy):
```json
{
  "name": "user_tier_assignment",
  "description": "Assigns user to a tier",
  "policyType": "OUTPUT",
  "rootNode": { /* same tree structure */ },
  "outputMapping": {
    "defaultOutput": { "tier": "STANDARD" },
    "conditionalOutputs": [
      {
        "condition": "PASS",
        "output": { "tier": "PREMIUM", "discount": 20 }
      },
      {
        "condition": "FAIL",
        "output": { "tier": "BASIC", "discount": 0 }
      }
    ]
  }
}
```

**Get Policy**
```
GET /api/v1/policies/{policyId}
```

**List Policies**
```
GET /api/v1/policies?policyType=BOOLEAN&enabled=true&page=0&size=20
```

**Update Policy**
```
PUT /api/v1/policies/{policyId}
```

**Delete Policy**
```
DELETE /api/v1/policies/{policyId}
```

---

### 5.5 Policy Set APIs

**Create Policy Set**
```
POST /api/v1/policy-sets
```
Request:
```json
{
  "name": "personal_loan_policy_set",
  "description": "Personal loan eligibility and offer determination",
  "booleanPolicyId": "boolean-policy-uuid",
  "offerPolicyId": "offer-policy-uuid",
  "evaluationStrategy": "SEQUENTIAL"
}
```
Response: `201 Created`
```json
{
  "id": "policy-set-uuid",
  "name": "personal_loan_policy_set",
  "booleanPolicy": {
    "id": "boolean-policy-uuid",
    "name": "loan_eligibility"
  },
  "offerPolicy": {
    "id": "offer-policy-uuid",
    "name": "loan_offer"
  },
  "evaluationStrategy": "SEQUENTIAL",
  "enabled": true,
  "version": 1
}
```

**Get Policy Set**
```
GET /api/v1/policy-sets/{policySetId}
```

**List Policy Sets**
```
GET /api/v1/policy-sets?enabled=true&page=0&size=20
```

**Update Policy Set**
```
PUT /api/v1/policy-sets/{policySetId}
```

**Delete Policy Set**
```
DELETE /api/v1/policy-sets/{policySetId}
```

---

### 5.6 Evaluation API

Single unified API that evaluates both Boolean (eligibility) and Offer policies, returning a simplified response with decision and offer.

**Evaluate Policy Set**
```
POST /api/v1/policy-sets/{policySetId}/evaluate
```
Request:
```json
{
  "inputData": {
    "applicant": {
      "age": 30,
      "monthlyIncome": 50000,
      "creditScore": 720,
      "employmentType": "SALARIED"
    }
  }
}
```

**Response (Approved with Offer):**
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

**Response (Rejected - No Offer):**
```json
{
  "decision": {
    "status": "REJECTED",
    "reasons": [
      "Rule 'minimum_age_rule' failed: 18 GTE 21 = false"
    ]
  },
  "offer": null
}
```

**Response (Missing Input - 400 Bad Request):**
```json
{
  "code": "VALIDATION_ERROR",
  "message": "Missing required input for feature(s): applicant_income, applicant_credit_score",
  "details": null,
  "timestamp": "2025-12-31T10:30:00.123456"
}
```

**Dry Run (validate without logging)**
```
POST /api/v1/evaluate/dry-run
```
Request:
```json
{
  "policySetId": "policy-set-uuid",
  "inputData": { /* ... */ }
}
```

---

## 6. Advanced Features

### 6.1 Feature Extraction Strategies

```json
{
  "extractionConfig": {
    "type": "JSON_PATH",  // JSON_PATH, SCRIPT, COMPOSITE
    "path": "$.user.transactions[*].amount"
  }
}
```

**Composite Feature Example:**
```json
{
  "name": "debt_to_income_ratio",
  "extractionConfig": {
    "type": "COMPOSITE",
    "expression": "feature('total_debt') / feature('annual_income')"
  }
}
```

### 6.2 Versioning Strategy
- Features, Rules, and Policies are versioned
- Evaluation logs store the version used
- Support for rollback to previous versions

### 6.3 Caching Strategy
- Cache compiled rule trees in memory
- Invalidate on policy update
- Feature extraction results can be cached per request

---

## 7. Implementation Status

### Phase 1: Project Setup & Core Entities ✅ COMPLETED
- [x] Project structure with build configuration (Java 21, Spring Boot 3.2.1)
- [x] Database migrations for `features`, `operators`, `rules` tables
- [x] Feature APIs: `POST/GET/PUT/DELETE /api/v1/features`
- [x] Rule APIs: `POST/GET/PUT/DELETE /api/v1/rules`
- [x] Operator APIs: `GET /api/v1/operators`

### Phase 2: Operator Validation & Feature Extraction ✅ COMPLETED
- [x] Rule creation validates operator compatibility with feature type
- [x] Operand validation based on operator's operandType
- [x] Feature extraction from JSON input using JSONPath
- [x] Rule test endpoint: `POST /api/v1/rules/test`
- [x] **Strategy Pattern for Operators**: 20 operators implemented as individual classes

### Phase 3: Policy & Rule Tree Engine ✅ COMPLETED
- [x] Policy APIs: `POST/GET/PUT/DELETE /api/v1/policies`
- [x] Boolean policy: Returns APPROVED/REJECTED with reasons
- [x] Offer policy: Returns loan amount, ROI, processing fee, tenure, EMI
- [x] Tree evaluation with AND/OR logic

### Phase 4: Policy Set & Unified Evaluation ✅ COMPLETED
- [x] PolicySet APIs: `POST/GET/PUT/DELETE /api/v1/policy-sets`
- [x] **Multiple Offer Policies with Priorities**: Highest priority approved offer wins
- [x] Unified evaluation: `POST /api/v1/policy-sets/{id}/evaluate`
- [x] Simplified response: `{decision: {status, reasons}, offer: {...}}`
- [x] Execution logging to `policy_set_execution_logs` table

### Phase 5: Production Readiness ✅ COMPLETED
- [x] Input validation: 400 Bad Request for missing required features
- [x] Error handling with standardized error responses
- [x] OpenAPI/Swagger documentation
- [x] Docker Compose for one-command setup
- [x] Postman collection for API testing

### Phase 6: Future Enhancements (Planned)
- [ ] Policy versioning & rollback
- [ ] Dry-run evaluation (validate without logging)
- [ ] Composite features (calculated from other features)
- [ ] Policy builder UI (drag-and-drop tree)
- [ ] Caching for frequently accessed entities
- [ ] A/B testing support for policies

---

## 8. Technology Stack (Implemented)

| Component | Implementation |
|-----------|----------------|
| Language | Java 21 |
| Framework | Spring Boot 3.2.1 |
| Database | PostgreSQL 16 (JSONB support) |
| Migrations | Flyway |
| API Docs | OpenAPI 3.0 / Swagger UI |
| Testing | JUnit 5, Testcontainers |
| Containerization | Docker, Docker Compose |

---

## 9. Error Handling

### Error Response Format
```json
{
  "code": "VALIDATION_ERROR",
  "message": "Missing required input for feature(s): applicant_income, applicant_credit_score",
  "details": {
    "fieldName": "error message"
  },
  "timestamp": "2025-12-31T10:30:00.123456"
}
```

### Error Codes
| Code | HTTP Status | Description |
|------|-------------|-------------|
| `RESOURCE_NOT_FOUND` | 404 | Feature/Rule/Policy/PolicySet does not exist |
| `VALIDATION_ERROR` | 400 | Validation failed (invalid operator, missing input, etc.) |
| `INTERNAL_ERROR` | 500 | Unexpected server error |

---

## 10. Security Considerations

1. **Input Validation**: Sanitize all JSONPath expressions to prevent injection
2. **Rate Limiting**: Limit evaluation API calls per client
3. **Audit Trail**: Log all policy changes and evaluations
4. **Access Control**: Role-based access for policy management vs evaluation
5. **Data Encryption**: Encrypt sensitive operand values at rest

-- V4: Seed sample data for loan application use case

-- Sample Features
INSERT INTO features (id, name, description, feature_type, extraction_config, version, created_at, updated_at) VALUES
-- Applicant features
('a1000000-0000-0000-0000-000000000001', 'applicant_age', 'Age of the loan applicant', 'NUMERIC', '{"type": "JSON_PATH", "path": "$.applicant.age"}', 1, NOW(), NOW()),
('a1000000-0000-0000-0000-000000000002', 'applicant_income', 'Monthly income of the applicant', 'NUMERIC', '{"type": "JSON_PATH", "path": "$.applicant.monthlyIncome"}', 1, NOW(), NOW()),
('a1000000-0000-0000-0000-000000000003', 'applicant_credit_score', 'Credit score of the applicant', 'NUMERIC', '{"type": "JSON_PATH", "path": "$.applicant.creditScore"}', 1, NOW(), NOW()),
('a1000000-0000-0000-0000-000000000004', 'applicant_employment_type', 'Employment type (SALARIED, SELF_EMPLOYED, etc)', 'STRING', '{"type": "JSON_PATH", "path": "$.applicant.employmentType"}', 1, NOW(), NOW()),
('a1000000-0000-0000-0000-000000000005', 'applicant_experience_years', 'Years of work experience', 'NUMERIC', '{"type": "JSON_PATH", "path": "$.applicant.experienceYears"}', 1, NOW(), NOW()),
-- Loan features
('a1000000-0000-0000-0000-000000000006', 'loan_amount_requested', 'Loan amount requested by applicant', 'NUMERIC', '{"type": "JSON_PATH", "path": "$.loan.amountRequested"}', 1, NOW(), NOW()),
('a1000000-0000-0000-0000-000000000007', 'loan_tenure_months', 'Loan tenure in months', 'NUMERIC', '{"type": "JSON_PATH", "path": "$.loan.tenureMonths"}', 1, NOW(), NOW()),
('a1000000-0000-0000-0000-000000000008', 'loan_purpose', 'Purpose of the loan', 'STRING', '{"type": "JSON_PATH", "path": "$.loan.purpose"}', 1, NOW(), NOW()),
-- Derived features
('a1000000-0000-0000-0000-000000000009', 'debt_to_income_ratio', 'Existing debt to income ratio', 'NUMERIC', '{"type": "JSON_PATH", "path": "$.applicant.debtToIncomeRatio"}', 1, NOW(), NOW()),
('a1000000-0000-0000-0000-000000000010', 'existing_loans_count', 'Number of existing active loans', 'NUMERIC', '{"type": "JSON_PATH", "path": "$.applicant.existingLoansCount"}', 1, NOW(), NOW());

-- Sample Rules
INSERT INTO rules (id, name, description, feature_id, operator_code, operand, enabled, version, created_at, updated_at) VALUES
-- Age rules
('b1000000-0000-0000-0000-000000000001', 'minimum_age_rule', 'Applicant must be at least 21 years old', 'a1000000-0000-0000-0000-000000000001', 'GTE', '21', true, 1, NOW(), NOW()),
('b1000000-0000-0000-0000-000000000002', 'maximum_age_rule', 'Applicant must be at most 60 years old', 'a1000000-0000-0000-0000-000000000001', 'LTE', '60', true, 1, NOW(), NOW()),
-- Income rules
('b1000000-0000-0000-0000-000000000003', 'minimum_income_rule', 'Monthly income must be at least 25000', 'a1000000-0000-0000-0000-000000000002', 'GTE', '25000', true, 1, NOW(), NOW()),
-- Credit score rules
('b1000000-0000-0000-0000-000000000004', 'minimum_credit_score_rule', 'Credit score must be at least 650', 'a1000000-0000-0000-0000-000000000003', 'GTE', '650', true, 1, NOW(), NOW()),
('b1000000-0000-0000-0000-000000000005', 'excellent_credit_score_rule', 'Credit score is excellent (750+)', 'a1000000-0000-0000-0000-000000000003', 'GTE', '750', true, 1, NOW(), NOW()),
-- Employment rules
('b1000000-0000-0000-0000-000000000006', 'employment_type_rule', 'Employment type must be SALARIED or SELF_EMPLOYED', 'a1000000-0000-0000-0000-000000000004', 'IN', '["SALARIED", "SELF_EMPLOYED", "BUSINESS"]', true, 1, NOW(), NOW()),
('b1000000-0000-0000-0000-000000000007', 'minimum_experience_rule', 'Must have at least 2 years of experience', 'a1000000-0000-0000-0000-000000000005', 'GTE', '2', true, 1, NOW(), NOW()),
-- Debt rules
('b1000000-0000-0000-0000-000000000008', 'debt_to_income_rule', 'Debt to income ratio must be less than 50%', 'a1000000-0000-0000-0000-000000000009', 'LT', '0.5', true, 1, NOW(), NOW()),
('b1000000-0000-0000-0000-000000000009', 'existing_loans_rule', 'Must have less than 3 existing loans', 'a1000000-0000-0000-0000-000000000010', 'LT', '3', true, 1, NOW(), NOW()),
-- Loan amount rules
('b1000000-0000-0000-0000-000000000010', 'max_loan_amount_rule', 'Loan amount must not exceed 5000000', 'a1000000-0000-0000-0000-000000000006', 'LTE', '5000000', true, 1, NOW(), NOW());

-- Sample Policies
-- Boolean Policy: Basic Eligibility Check
INSERT INTO policies (id, name, description, policy_type, root_node, enabled, version, created_at, updated_at) VALUES
('c1000000-0000-0000-0000-000000000001', 'basic_eligibility_policy', 'Basic loan eligibility check', 'BOOLEAN', 
'{
  "type": "COMPOSITE",
  "operator": "AND",
  "children": [
    {
      "type": "COMPOSITE",
      "operator": "AND",
      "children": [
        {"type": "LEAF", "ruleId": "b1000000-0000-0000-0000-000000000001"},
        {"type": "LEAF", "ruleId": "b1000000-0000-0000-0000-000000000002"}
      ]
    },
    {"type": "LEAF", "ruleId": "b1000000-0000-0000-0000-000000000003"},
    {"type": "LEAF", "ruleId": "b1000000-0000-0000-0000-000000000004"},
    {"type": "LEAF", "ruleId": "b1000000-0000-0000-0000-000000000006"}
  ]
}', true, 1, NOW(), NOW());

-- Boolean Policy: Advanced Eligibility Check (includes debt checks)
INSERT INTO policies (id, name, description, policy_type, root_node, enabled, version, created_at, updated_at) VALUES
('c1000000-0000-0000-0000-000000000002', 'advanced_eligibility_policy', 'Advanced loan eligibility with debt checks', 'BOOLEAN', 
'{
  "type": "COMPOSITE",
  "operator": "AND",
  "children": [
    {
      "type": "COMPOSITE",
      "operator": "AND",
      "children": [
        {"type": "LEAF", "ruleId": "b1000000-0000-0000-0000-000000000001"},
        {"type": "LEAF", "ruleId": "b1000000-0000-0000-0000-000000000002"}
      ]
    },
    {"type": "LEAF", "ruleId": "b1000000-0000-0000-0000-000000000003"},
    {"type": "LEAF", "ruleId": "b1000000-0000-0000-0000-000000000004"},
    {"type": "LEAF", "ruleId": "b1000000-0000-0000-0000-000000000007"},
    {"type": "LEAF", "ruleId": "b1000000-0000-0000-0000-000000000008"},
    {"type": "LEAF", "ruleId": "b1000000-0000-0000-0000-000000000009"},
    {"type": "LEAF", "ruleId": "b1000000-0000-0000-0000-000000000010"}
  ]
}', true, 1, NOW(), NOW());

-- Offer Policy: Standard Loan Offer
INSERT INTO policies (id, name, description, policy_type, root_node, output_mapping, enabled, version, created_at, updated_at) VALUES
('c1000000-0000-0000-0000-000000000003', 'standard_loan_offer_policy', 'Standard loan offer based on eligibility', 'OFFER', 
'{
  "type": "COMPOSITE",
  "operator": "AND",
  "children": [
    {"type": "LEAF", "ruleId": "b1000000-0000-0000-0000-000000000001"},
    {"type": "LEAF", "ruleId": "b1000000-0000-0000-0000-000000000004"}
  ]
}',
'{
  "defaultOutput": {
    "loanAmount": 100000,
    "rateOfInterest": 14.5,
    "processingFee": 2.0,
    "tenure": 36
  },
  "conditionalOutputs": [
    {
      "condition": "applicant_credit_score >= 750",
      "output": {
        "loanAmount": 500000,
        "rateOfInterest": 10.5,
        "processingFee": 1.0,
        "tenure": 60
      }
    },
    {
      "condition": "applicant_credit_score >= 700",
      "output": {
        "loanAmount": 300000,
        "rateOfInterest": 12.0,
        "processingFee": 1.5,
        "tenure": 48
      }
    }
  ]
}', true, 1, NOW(), NOW());

-- Offer Policy: Premium Loan Offer (for high income applicants)
INSERT INTO policies (id, name, description, policy_type, root_node, output_mapping, enabled, version, created_at, updated_at) VALUES
('c1000000-0000-0000-0000-000000000004', 'premium_loan_offer_policy', 'Premium loan offer for high income applicants', 'OFFER', 
'{
  "type": "COMPOSITE",
  "operator": "AND",
  "children": [
    {"type": "LEAF", "ruleId": "b1000000-0000-0000-0000-000000000001"},
    {"type": "LEAF", "ruleId": "b1000000-0000-0000-0000-000000000005"},
    {"type": "LEAF", "ruleId": "b1000000-0000-0000-0000-000000000008"}
  ]
}',
'{
  "defaultOutput": {
    "loanAmount": 1000000,
    "rateOfInterest": 9.5,
    "processingFee": 0.5,
    "tenure": 84
  },
  "conditionalOutputs": [
    {
      "condition": "applicant_income >= 100000",
      "output": {
        "loanAmount": 2500000,
        "rateOfInterest": 8.5,
        "processingFee": 0.25,
        "tenure": 120
      }
    }
  ]
}', true, 1, NOW(), NOW());

-- Sample PolicySets
INSERT INTO policy_sets (id, name, description, boolean_policy_id, offer_policy_id, evaluation_strategy, enabled, version, created_at, updated_at) VALUES
('d1000000-0000-0000-0000-000000000001', 'basic_loan_application', 'Basic loan application with standard offer', 'c1000000-0000-0000-0000-000000000001', 'c1000000-0000-0000-0000-000000000003', 'BOOLEAN_FIRST', true, 1, NOW(), NOW()),
('d1000000-0000-0000-0000-000000000002', 'premium_loan_application', 'Premium loan application with advanced checks', 'c1000000-0000-0000-0000-000000000002', 'c1000000-0000-0000-0000-000000000004', 'BOOLEAN_FIRST', true, 1, NOW(), NOW());

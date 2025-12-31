-- V1: Create initial schema for Rule Engine

-- Features table
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

-- Operators table (seed data)
CREATE TABLE operators (
    id UUID PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    compatible_feature_types JSONB NOT NULL,
    operand_type VARCHAR(20) NOT NULL,
    operand_element_type VARCHAR(50),
    created_at TIMESTAMP DEFAULT NOW()
);

-- Rules table
CREATE TABLE rules (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    feature_id UUID NOT NULL REFERENCES features(id),
    operator_code VARCHAR(50) NOT NULL REFERENCES operators(code),
    operand JSONB,
    enabled BOOLEAN DEFAULT true,
    version INT DEFAULT 1,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Policies table
CREATE TABLE policies (
    id UUID PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    policy_type VARCHAR(20) NOT NULL,
    root_node JSONB NOT NULL,
    output_mapping JSONB,
    enabled BOOLEAN DEFAULT true,
    version INT DEFAULT 1,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Policy Sets table
CREATE TABLE policy_sets (
    id UUID PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    boolean_policy_id UUID REFERENCES policies(id),
    offer_policy_id UUID REFERENCES policies(id),
    evaluation_strategy VARCHAR(20) DEFAULT 'BOOLEAN_FIRST',
    enabled BOOLEAN DEFAULT true,
    version INT DEFAULT 1,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Policy Set Execution Logs table
CREATE TABLE policy_set_execution_logs (
    id UUID PRIMARY KEY,
    policy_set_id UUID REFERENCES policy_sets(id),
    policy_set_version INT,
    input_data JSONB,
    extracted_features JSONB,
    boolean_policy_result JSONB,
    offer_policy_result JSONB,
    decision_status VARCHAR(20),
    execution_time_ms INT,
    executed_at TIMESTAMP DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_features_name ON features(name);
CREATE INDEX idx_features_type ON features(feature_type);
CREATE INDEX idx_rules_feature_id ON rules(feature_id);
CREATE INDEX idx_rules_operator_code ON rules(operator_code);
CREATE INDEX idx_policies_type ON policies(policy_type);
CREATE INDEX idx_policies_enabled ON policies(enabled);
CREATE INDEX idx_policy_sets_enabled ON policy_sets(enabled);
CREATE INDEX idx_execution_logs_policy_set_id ON policy_set_execution_logs(policy_set_id);
CREATE INDEX idx_execution_logs_executed_at ON policy_set_execution_logs(executed_at);

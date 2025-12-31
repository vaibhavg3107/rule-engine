-- V4: Add support for multiple offer policies with priorities in PolicySet

-- Create join table for PolicySet to Offer Policies (many-to-many with priority)
CREATE TABLE policy_set_offer_policies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    policy_set_id UUID NOT NULL REFERENCES policy_sets(id) ON DELETE CASCADE,
    offer_policy_id UUID NOT NULL REFERENCES policies(id) ON DELETE CASCADE,
    priority INTEGER NOT NULL DEFAULT 0,
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(policy_set_id, offer_policy_id)
);

-- Create index for faster lookups
CREATE INDEX idx_policy_set_offer_policies_set_id ON policy_set_offer_policies(policy_set_id);
CREATE INDEX idx_policy_set_offer_policies_priority ON policy_set_offer_policies(policy_set_id, priority DESC);

-- Migrate existing offer_policy_id data to the new join table
INSERT INTO policy_set_offer_policies (policy_set_id, offer_policy_id, priority, enabled)
SELECT id, offer_policy_id, 1, true
FROM policy_sets
WHERE offer_policy_id IS NOT NULL;

-- Note: We keep the offer_policy_id column for backward compatibility but it will be deprecated
-- In future, you can drop it with: ALTER TABLE policy_sets DROP COLUMN offer_policy_id;

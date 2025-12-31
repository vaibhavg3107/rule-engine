-- V6: Drop deprecated offer_policy_id column from policy_sets table
-- This column is no longer needed as we now use the policy_set_offer_policies join table

ALTER TABLE policy_sets DROP COLUMN IF EXISTS offer_policy_id;

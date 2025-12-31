-- V2: Seed operator data

INSERT INTO operators (id, code, name, description, compatible_feature_types, operand_type, operand_element_type) VALUES
-- Equality operators
(gen_random_uuid(), 'EQ', 'Equals', 'Check if values are equal', '["NUMERIC", "STRING", "BOOLEAN", "DATE"]', 'SINGLE', 'SAME_AS_FEATURE'),
(gen_random_uuid(), 'NEQ', 'Not Equals', 'Check if values are not equal', '["NUMERIC", "STRING", "BOOLEAN", "DATE"]', 'SINGLE', 'SAME_AS_FEATURE'),

-- Comparison operators
(gen_random_uuid(), 'LT', 'Less Than', 'Check if value is less than operand', '["NUMERIC", "DATE"]', 'SINGLE', 'SAME_AS_FEATURE'),
(gen_random_uuid(), 'LTE', 'Less Than or Equal', 'Check if value is less than or equal to operand', '["NUMERIC", "DATE"]', 'SINGLE', 'SAME_AS_FEATURE'),
(gen_random_uuid(), 'GT', 'Greater Than', 'Check if value is greater than operand', '["NUMERIC", "DATE"]', 'SINGLE', 'SAME_AS_FEATURE'),
(gen_random_uuid(), 'GTE', 'Greater Than or Equal', 'Check if value is greater than or equal to operand', '["NUMERIC", "DATE"]', 'SINGLE', 'SAME_AS_FEATURE'),

-- List operators
(gen_random_uuid(), 'IN', 'In List', 'Check if value exists in the provided list', '["NUMERIC", "STRING", "DATE"]', 'LIST', 'SAME_AS_FEATURE'),
(gen_random_uuid(), 'NOT_IN', 'Not In List', 'Check if value does not exist in the provided list', '["NUMERIC", "STRING", "DATE"]', 'LIST', 'SAME_AS_FEATURE'),

-- Range operator
(gen_random_uuid(), 'BETWEEN', 'Between', 'Check if value is between min and max (inclusive)', '["NUMERIC", "DATE"]', 'RANGE', 'SAME_AS_FEATURE'),

-- String operators
(gen_random_uuid(), 'CONTAINS', 'Contains', 'Check if string contains substring', '["STRING", "LIST"]', 'SINGLE', 'STRING'),
(gen_random_uuid(), 'STARTS_WITH', 'Starts With', 'Check if string starts with prefix', '["STRING"]', 'SINGLE', 'STRING'),
(gen_random_uuid(), 'ENDS_WITH', 'Ends With', 'Check if string ends with suffix', '["STRING"]', 'SINGLE', 'STRING'),
(gen_random_uuid(), 'REGEX', 'Regex Match', 'Check if string matches regex pattern', '["STRING"]', 'SINGLE', 'STRING'),

-- List-specific operators
(gen_random_uuid(), 'CONTAINS_ALL', 'Contains All', 'Check if list contains all specified values', '["LIST"]', 'LIST', 'ANY'),
(gen_random_uuid(), 'CONTAINS_ANY', 'Contains Any', 'Check if list contains any of the specified values', '["LIST"]', 'LIST', 'ANY'),

-- Empty check operators
(gen_random_uuid(), 'IS_EMPTY', 'Is Empty', 'Check if value is empty (null, empty string, or empty list)', '["LIST", "STRING"]', 'NONE', NULL),
(gen_random_uuid(), 'IS_NOT_EMPTY', 'Is Not Empty', 'Check if value is not empty', '["LIST", "STRING"]', 'NONE', NULL),

-- Size operators for lists
(gen_random_uuid(), 'SIZE_EQ', 'Size Equals', 'Check if list size equals the operand', '["LIST"]', 'SINGLE', 'NUMERIC'),
(gen_random_uuid(), 'SIZE_GT', 'Size Greater Than', 'Check if list size is greater than operand', '["LIST"]', 'SINGLE', 'NUMERIC'),
(gen_random_uuid(), 'SIZE_LT', 'Size Less Than', 'Check if list size is less than operand', '["LIST"]', 'SINGLE', 'NUMERIC');

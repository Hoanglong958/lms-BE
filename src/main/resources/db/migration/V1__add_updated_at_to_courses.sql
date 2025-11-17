ALTER TABLE courses
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP;

UPDATE courses
SET updated_at = created_at
WHERE updated_at IS NULL;


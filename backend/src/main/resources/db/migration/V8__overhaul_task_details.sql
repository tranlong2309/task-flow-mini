-- Add new date and json fields to tasks
ALTER TABLE tasks 
ADD COLUMN assigned_date DATETIME NULL,
ADD COLUMN start_date DATETIME NULL,
ADD COLUMN assignee_ids JSON NULL,
ADD COLUMN subtasks JSON NULL,
ADD COLUMN comments JSON NULL,
ADD COLUMN attachments JSON NULL;

-- Migrate existing assignees to assignee_ids JSON array
UPDATE tasks 
SET assignee_ids = JSON_ARRAY(assignee_id) 
WHERE assignee_id IS NOT NULL;

UPDATE tasks 
SET assignee_ids = JSON_ARRAY() 
WHERE assignee_id IS NULL;

-- Remove old assignee_id from tasks
SELECT CONSTRAINT_NAME INTO @fk_name 
FROM information_schema.KEY_COLUMN_USAGE 
WHERE TABLE_NAME = 'tasks' AND COLUMN_NAME = 'assignee_id' AND TABLE_SCHEMA = DATABASE() LIMIT 1;

SET @drop_fk_query = IF(@fk_name IS NOT NULL, CONCAT('ALTER TABLE tasks DROP FOREIGN KEY ', @fk_name), 'SELECT 1');
PREPARE stmt FROM @drop_fk_query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE tasks DROP COLUMN assignee_id;

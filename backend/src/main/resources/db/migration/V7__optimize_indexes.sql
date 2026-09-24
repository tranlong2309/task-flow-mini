DROP INDEX idx_tasks_title;
CREATE INDEX idx_tasks_board_assignee ON tasks(board_id, assignee_id);

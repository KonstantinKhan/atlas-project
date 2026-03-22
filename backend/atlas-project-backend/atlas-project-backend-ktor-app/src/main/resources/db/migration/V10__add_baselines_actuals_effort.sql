ALTER TABLE project_tasks ADD COLUMN baseline_start DATE;
ALTER TABLE project_tasks ADD COLUMN baseline_end DATE;
ALTER TABLE project_tasks ADD COLUMN actual_start DATE;
ALTER TABLE project_tasks ADD COLUMN actual_end DATE;
ALTER TABLE project_tasks ADD COLUMN baseline_effort_hours DOUBLE PRECISION;
ALTER TABLE project_tasks ADD COLUMN additional_effort_hours DOUBLE PRECISION;

ALTER TABLE task_assignments ADD COLUMN planned_effort_hours NUMERIC(6,1) NULL;

CREATE TABLE assignment_day_overrides (
    id SERIAL PRIMARY KEY,
    assignment_id UUID NOT NULL REFERENCES task_assignments(id) ON DELETE CASCADE,
    override_date DATE NOT NULL,
    hours NUMERIC(4,1) NOT NULL DEFAULT 0,
    UNIQUE (assignment_id, override_date)
);

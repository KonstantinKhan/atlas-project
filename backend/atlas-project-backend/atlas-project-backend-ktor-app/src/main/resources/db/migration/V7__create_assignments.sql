CREATE TABLE task_assignments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_plan_id UUID NOT NULL REFERENCES project_plans(id) ON DELETE CASCADE,
    task_id UUID NOT NULL REFERENCES project_tasks(id) ON DELETE CASCADE,
    resource_id UUID NOT NULL REFERENCES resources(id) ON DELETE CASCADE,
    hours_per_day NUMERIC(4,1) NOT NULL DEFAULT 8.0,
    UNIQUE (task_id, resource_id)
);

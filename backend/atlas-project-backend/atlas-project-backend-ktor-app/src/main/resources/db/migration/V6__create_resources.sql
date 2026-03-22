CREATE TABLE resources (
    id UUID PRIMARY KEY,
    project_plan_id UUID NOT NULL REFERENCES project_plans(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL DEFAULT 'PERSON',
    capacity_hours_per_day NUMERIC(4,1) NOT NULL DEFAULT 8.0,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE resource_calendar_overrides (
    id SERIAL PRIMARY KEY,
    resource_id UUID NOT NULL REFERENCES resources(id) ON DELETE CASCADE,
    override_date DATE NOT NULL,
    available_hours NUMERIC(4,1) NOT NULL DEFAULT 0,
    UNIQUE (resource_id, override_date)
);

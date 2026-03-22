-- Создать отдельную таблицу проектов
CREATE TABLE projects (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL DEFAULT 'Default Project',
    portfolio_id UUID NOT NULL REFERENCES portfolios(id) ON DELETE CASCADE,
    priority INTEGER NOT NULL DEFAULT 0
);

-- Перенести данные из project_plans (те же UUID — backward compat)
INSERT INTO projects (id, name, portfolio_id, priority)
SELECT id, name, portfolio_id, priority FROM project_plans;

-- Добавить FK project_id в project_plans
ALTER TABLE project_plans
    ADD COLUMN project_id UUID REFERENCES projects(id) ON DELETE CASCADE;
UPDATE project_plans SET project_id = id;
ALTER TABLE project_plans ALTER COLUMN project_id SET NOT NULL;

-- Удалить метаданные из project_plans (переехали в projects)
ALTER TABLE project_plans
    DROP COLUMN name,
    DROP COLUMN portfolio_id,
    DROP COLUMN priority;

-- V14: Normalize Project-Portfolio relationship
-- Convert from 1:N (project belongs to one portfolio) to M:N (projects can belong to multiple portfolios)

-- 1. Create portfolio_projects join table
CREATE TABLE portfolio_projects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    portfolio_id UUID NOT NULL REFERENCES portfolios(id) ON DELETE CASCADE,
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    priority INTEGER NOT NULL DEFAULT 1,
    sort_order INTEGER NOT NULL DEFAULT 0,
    UNIQUE (portfolio_id, project_id)
);

-- 2. Migrate existing data from projects table to portfolio_projects
-- Copy existing portfolio_id and priority relationships
INSERT INTO portfolio_projects (portfolio_id, project_id, priority, sort_order)
SELECT 
    p.portfolio_id,
    p.id,
    p.priority,
    COALESCE(pso.sort_order, 0)
FROM projects p
LEFT JOIN project_sort_orders pso ON p.id = pso.project_id AND p.portfolio_id = pso.portfolio_id
WHERE p.portfolio_id IS NOT NULL;

-- 3. Drop portfolio_id and priority columns from projects table
ALTER TABLE projects DROP COLUMN IF EXISTS portfolio_id;
ALTER TABLE projects DROP COLUMN IF EXISTS priority;

-- 4. Drop the old project_sort_orders table (replaced by sort_order in portfolio_projects)
DROP TABLE IF EXISTS project_sort_orders;

-- 5. Create index for faster lookups
CREATE INDEX idx_portfolio_projects_portfolio_id ON portfolio_projects(portfolio_id);
CREATE INDEX idx_portfolio_projects_project_id ON portfolio_projects(project_id);

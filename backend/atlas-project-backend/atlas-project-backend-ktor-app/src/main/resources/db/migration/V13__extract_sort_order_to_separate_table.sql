-- V13: Extract sort_order from projects table to separate project_sort_orders table

-- Create project_sort_orders table
CREATE TABLE project_sort_orders (
    portfolio_id UUID NOT NULL REFERENCES portfolios(id) ON DELETE CASCADE,
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    sort_order INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (portfolio_id, project_id)
);

-- Clean up orphaned projects (from legacy test data with non-existent portfolio_id)
DELETE FROM projects WHERE portfolio_id NOT IN (SELECT id FROM portfolios);

-- Migrate existing data from projects table
INSERT INTO project_sort_orders (portfolio_id, project_id, sort_order)
SELECT p.portfolio_id, p.id, p.sort_order
FROM projects p
INNER JOIN portfolios pf ON p.portfolio_id = pf.id;

-- Drop sort_order column from projects table
ALTER TABLE projects DROP COLUMN sort_order;

-- 1. Portfolios table
CREATE TABLE portfolios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL DEFAULT '',
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 2. Extend project_plans with metadata
ALTER TABLE project_plans ADD COLUMN name VARCHAR(255) NOT NULL DEFAULT 'Default Project';
ALTER TABLE project_plans ADD COLUMN portfolio_id UUID;
ALTER TABLE project_plans ADD COLUMN priority INT NOT NULL DEFAULT 0;
ALTER TABLE project_plans ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT NOW();

-- 3. Create default portfolio, assign existing plan
INSERT INTO portfolios (id, name) VALUES ('00000000-0000-0000-0000-000000000100', 'Default Portfolio');
UPDATE project_plans SET portfolio_id = '00000000-0000-0000-0000-000000000100';
ALTER TABLE project_plans ALTER COLUMN portfolio_id SET NOT NULL;
ALTER TABLE project_plans ADD CONSTRAINT fk_project_plans_portfolio FOREIGN KEY (portfolio_id) REFERENCES portfolios(id) ON DELETE CASCADE;

-- 4. Make resources global (remove project_plan_id FK)
ALTER TABLE resources DROP COLUMN project_plan_id;

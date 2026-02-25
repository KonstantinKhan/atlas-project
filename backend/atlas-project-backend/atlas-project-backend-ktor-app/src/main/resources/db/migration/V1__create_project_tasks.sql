CREATE TABLE project_plans
(
    id UUID PRIMARY KEY
);

CREATE TABLE project_tasks
(
    id              UUID PRIMARY KEY,
    project_plan_id UUID         NOT NULL REFERENCES project_plans (id) ON DELETE CASCADE,
    title           VARCHAR(255) NOT NULL,
    description     TEXT         NOT NULL DEFAULT '',
    duration_days   INT          NOT NULL,
    status          VARCHAR(50)  NOT NULL DEFAULT 'EMPTY'
);

CREATE TABLE task_dependencies
(
    id                  SERIAL PRIMARY KEY,
    project_plan_id     UUID NOT NULL REFERENCES project_plans (id) ON DELETE CASCADE,
    predecessor_task_id UUID NOT NULL REFERENCES project_tasks (id),
    successor_task_id   UUID NOT NULL REFERENCES project_tasks (id),
    type                TEXT NOT NULL,
    lag_days            INT  NOT NULL DEFAULT 0,

    UNIQUE (predecessor_task_id, successor_task_id)
);

CREATE TABLE task_schedules
(
    task_id       UUID PRIMARY KEY REFERENCES project_tasks (id) ON DELETE CASCADE,
    planned_start DATE NOT NULL,
    planned_end   DATE NOT NULL
);

-- Insert project plan
INSERT INTO project_plans (id)
VALUES ('550e8400-e29b-41d4-a716-446655440000');

-- Insert project tasks
INSERT INTO project_tasks (id, project_plan_id, title, description, duration_days, status)
VALUES ('00000000-0000-0000-0000-000000000001', '550e8400-e29b-41d4-a716-446655440000', 'Обследование процессов КТПП', '', 3, 'DONE'),
       ('00000000-0000-0000-0000-000000000002', '550e8400-e29b-41d4-a716-446655440000', 'Подготовка отчёта об обследовании', '', 2, 'IN_PROGRESS'),
       ('00000000-0000-0000-0000-000000000003', '550e8400-e29b-41d4-a716-446655440000', 'Разработка концепции автоматизации КТПП', '', 14, 'IN_PROGRESS'),
       ('00000000-0000-0000-0000-000000000004', '550e8400-e29b-41d4-a716-446655440000', 'Разработка архитектуры КТПП', '', 20, 'IN_PROGRESS'),
       ('00000000-0000-0000-0000-000000000005', '550e8400-e29b-41d4-a716-446655440000', 'Разработка ЧТЗ на подсистему КТПП', '', 24, 'IN_PROGRESS'),
       ('00000000-0000-0000-0000-000000000006', '550e8400-e29b-41d4-a716-446655440000', 'Разработка концепции миграции и синхронизации НСИ', '', 17, 'BACKLOG');

-- Insert task schedules
INSERT INTO task_schedules (task_id, planned_start, planned_end)
VALUES ('00000000-0000-0000-0000-000000000001', '2026-02-11', '2026-02-13'),
       ('00000000-0000-0000-0000-000000000002', '2026-02-16', '2026-02-17'),
       ('00000000-0000-0000-0000-000000000003', '2026-02-18', '2026-03-03'),
       ('00000000-0000-0000-0000-000000000004', '2026-02-18', '2026-03-09'),
       ('00000000-0000-0000-0000-000000000005', '2026-03-09', '2026-04-01'),
       ('00000000-0000-0000-0000-000000000006', '2026-02-24', '2026-03-12');

-- Insert task dependencies
INSERT INTO task_dependencies (project_plan_id, predecessor_task_id, successor_task_id, type, lag_days)
VALUES ('550e8400-e29b-41d4-a716-446655440000', '00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002', 'FS', 0),
       ('550e8400-e29b-41d4-a716-446655440000', '00000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000003', 'FS', 0),
       ('550e8400-e29b-41d4-a716-446655440000', '00000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000004', 'FS', 0),
       ('550e8400-e29b-41d4-a716-446655440000', '00000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000005', 'FS', 0),
       ('550e8400-e29b-41d4-a716-446655440000', '00000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000005', 'FS', 0);

CREATE TABLE timeline_calendar
(
    id                SERIAL PRIMARY KEY,
    name              VARCHAR(255) NOT NULL,
    working_week_days INT[] NOT NULL,
    weekend_week_days INT[] NOT NULL,
    created_at        TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP    NOT NULL DEFAULT NOW()
);

INSERT INTO timeline_calendar(name, working_week_days, weekend_week_days)
VALUES ('Default calendar', ARRAY[1, 2, 3, 4, 5], ARRAY[6, 7]);

CREATE TABLE timeline_calendar_holidays
(
    id           SERIAL PRIMARY KEY,
    calendar_id  INT  NOT NULL REFERENCES timeline_calendar (id) ON DELETE CASCADE,
    holiday_date DATE NOT NULL,
    UNIQUE (calendar_id, holiday_date)
);

INSERT INTO timeline_calendar_holidays(calendar_id, holiday_date)
VALUES (1, '2026-02-23'),
       (1, '2026-03-09');

CREATE TABLE timeline_calendar_working_weekends
(
    id           SERIAL PRIMARY KEY,
    calendar_id  INT  NOT NULL REFERENCES timeline_calendar (id) ON DELETE CASCADE,
    working_date DATE NOT NULL,
    UNIQUE (calendar_id, working_date)
);
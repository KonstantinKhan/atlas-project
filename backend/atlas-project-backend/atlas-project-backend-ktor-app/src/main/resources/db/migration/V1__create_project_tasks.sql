CREATE TABLE project_tasks
(
    id                        VARCHAR(255) PRIMARY KEY,
    title                     VARCHAR(255) NOT NULL,
    description               TEXT         NOT NULL DEFAULT '',
    planned_calendar_duration INT,
    actual_calendar_duration  INT,
    planned_start_date        DATE,
    planned_end_date          DATE,
    actual_start_date         DATE,
    actual_end_date           DATE,
    status                    VARCHAR(50)  NOT NULL DEFAULT 'EMPTY',
    depends_on                TEXT         NOT NULL DEFAULT '',
    depends_on_lag            TEXT         NOT NULL DEFAULT ''
);

INSERT INTO project_tasks
VALUES ('0001', 'Обследование процессов КТПП', '', 3, 3, '2026-02-11', '2026-02-13', '2026-02-11', '2026-02-13',
        'DONE', '', ''),
       ('0002',
        'Подготовка отчёта об обследовании',
        '',
        2,
        NULL,
        '2026-02-16',
        '2026-02-17',
        '2026-02-16',
        '2026-02-17',
        'IN_PROGRESS',
        '0001',
        ''),
       ('0003', 'Разработка концепции автоматизации КТПП', '', 14, 14, '2026-02-18', '2026-03-03', '2026-02-18',
        '2026-03-03', 'IN_PROGRESS', '0002', ''),
       ('0004', 'Разработка архитектуры КТПП', '', 20, 20, '2026-02-18', '2026-03-09', '2026-02-18', '2026-03-09',
        'IN_PROGRESS', '0002', ''),
       ('0005', 'Разработка ЧТЗ на подсистему КТПП', '', 24, 24, '2026-03-09', '2026-04-01', '2026-03-09',
        '2026-04-01', 'IN_PROGRESS', '0003,0004', ''),
       ('0006', 'Разработка концепции миграции и синхронизации НСИ', '', 17, 17, '2026-02-24', '2026-03-12',
        '2026-02-24', '2026-03-12', 'BACKLOG', '', '');
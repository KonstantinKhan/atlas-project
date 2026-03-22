-- Добавить колонку sortOrder для позиционирования проектов в списке
ALTER TABLE projects ADD COLUMN sort_order INTEGER NOT NULL DEFAULT 0;

-- Перенести существующие значения priority в sortOrder (сохраняем текущий порядок)
UPDATE projects SET sort_order = priority;

-- Конвертировать priority из Int в ProjectPriority (0=LOW, 1=MEDIUM, 2=HIGH)
-- Обновляем priority в соответствии с enum ordinal
UPDATE projects SET priority = 0 WHERE priority = 0; -- LOW
UPDATE projects SET priority = 1 WHERE priority = 1; -- MEDIUM
UPDATE projects SET priority = 2 WHERE priority = 2; -- HIGH

-- Сбросить priority в MEDIUM (1) для всех проектов, где нужно значение по умолчанию
-- (эта строка опциональна, если нужно сбросить старые значения)
-- UPDATE projects SET priority = 1 WHERE priority NOT IN (0, 1, 2);

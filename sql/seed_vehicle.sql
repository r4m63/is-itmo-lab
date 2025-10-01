-- Usage:
-- psql "postgresql://user:pass@host:port/db_name" -v seed_count=50 -v seed_persons=10 -f sql/seed_vehicle.sql

\if :{?seed_count}
\else
\set seed_count 50
\endif

\if :{?seed_persons}
\else
\set seed_persons 10
\endif

BEGIN;

-- 1) Админ, от имени которого создаём записи
INSERT INTO admin (login, pass_hash, salt)
VALUES ('seed_admin', 'seed_pass_hash_for_demo', 'seed_salt_for_demo')
ON CONFLICT (login) DO NOTHING;

-- 2) Создаём N владельцев для этого админа
WITH sa AS (SELECT id FROM admin WHERE login = 'seed_admin' LIMIT 1)
INSERT INTO person (full_name, admin_id)
SELECT 'Person #' || gs::text, (SELECT id FROM sa)
FROM generate_series(1, :seed_persons) AS gs;

-- 2.1) Гарантируем, что хотя бы один владелец существует
WITH sa AS (SELECT id FROM admin WHERE login = 'seed_admin' LIMIT 1)
INSERT INTO person (full_name, admin_id)
SELECT 'Person #1', (SELECT id FROM sa)
WHERE NOT EXISTS (
  SELECT 1 FROM person p WHERE p.admin_id = (SELECT id FROM sa)
);

-- 3) Вставляем машины и равномерно распределяем по владельцам seed_admin
WITH sa AS (
  SELECT id FROM admin WHERE login = 'seed_admin' LIMIT 1
),
pp AS (
  SELECT p.id,
         row_number() OVER (ORDER BY p.id) AS rn,
         count(*)      OVER ()            AS cnt
  FROM person p
  WHERE p.admin_id = (SELECT id FROM sa)
)
INSERT INTO vehicle (
    name,
    coordinates_x,
    coordinates_y,
    type,
    engine_power,
    number_of_wheels,
    capacity,
    distance_travelled,
    fuel_consumption,
    fuel_type,
    admin_id,
    owner_id
)
SELECT
    'Seed #' || gs::text,

    -- X <= 613 (1 знак после запятой)
    ROUND((random() * 613)::numeric, 1)::float8,

    -- Y <= 962 (1 знак после запятой)
    ROUND((random() * 962)::numeric, 1)::float4,

    -- type
    (ARRAY['CAR','HELICOPTER','MOTORCYCLE','CHOPPER'])[
      1 + floor(random()*4)::int
    ]::text,

    -- engine_power: 20% NULL, иначе 50..1049
    CASE WHEN random() < 0.20 THEN NULL
         ELSE (50 + floor(random()*1000))::int
    END,

    -- number_of_wheels: одно из {2,3,4,6}
    (ARRAY[2,3,4,6])[1 + floor(random()*4)::int],

    -- capacity: 30% NULL, иначе 1..6
    CASE WHEN random() < 0.30 THEN NULL
         ELSE (1 + floor(random()*6))::int
    END,

    -- distance_travelled: 25% NULL, иначе 500..100000
    CASE WHEN random() < 0.25 THEN NULL
         ELSE (500 + floor(random()*99501))::int
    END,

    -- fuel_consumption: 1..50 с 1 знаком после запятой
    ROUND((1 + random()*49)::numeric, 1)::float4,

    -- fuel_type
    (ARRAY['KEROSENE','MANPOWER','NUCLEAR'])[
      1 + floor(random()*3)::int
    ]::text,

    -- admin_id
    (SELECT id FROM sa),

    -- owner_id: равномерно по кругу
    (
      SELECT p2.id
      FROM pp p2
      WHERE p2.rn = ((gs - 1) % p2.cnt) + 1
      LIMIT 1
    )
FROM generate_series(1, :seed_count) AS gs;

COMMIT;
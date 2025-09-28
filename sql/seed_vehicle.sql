--  Usage: psql "postgresql://user:pass@host:port/db_name" -v seed_count=XXX -f sql/seed_vehicle.sql

\if :{?seed_count}
\else
\set seed_count 50
\endif

BEGIN;

-- гарантируем, что есть админ, от имени которого создаём записи
INSERT INTO admin (login, pass_hash, salt)
VALUES ('seed_admin', 'seed_pass_hash_for_demo', 'seed_salt_for_demo')
ON CONFLICT (login) DO NOTHING;

-- возьмём его id
WITH seed_admin AS (
  SELECT id FROM admin WHERE login = 'seed_admin' LIMIT 1
),
ins AS (
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
      admin_id
  )
  SELECT
      -- name
      'Seed #' || gs::text,

      -- coordinates_x <= 613 (1 знак после запятой)
      ROUND((random() * 613)::numeric, 1)::float8,

      -- coordinates_y <= 962 (1 знак после запятой)
      ROUND((random() * 962)::numeric, 1)::float4,

      -- type из допустимых
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

      -- fuel_type из допустимых
      (ARRAY['KEROSENE','MANPOWER','NUCLEAR'])[
          1 + floor(random()*3)::int
      ]::text,

      -- админ
      (SELECT id FROM seed_admin)
  FROM generate_series(1, :seed_count) AS gs
  RETURNING id
)
SELECT count(*) AS inserted_rows FROM ins;

COMMIT;

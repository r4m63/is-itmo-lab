CREATE TABLE IF NOT EXISTS vehicles (
    id                  BIGSERIAL PRIMARY KEY,
    name                TEXT NOT NULL CHECK (length(btrim(name)) > 0),
    coordinates_x       DOUBLE PRECISION NOT NULL CHECK (coordinates_x <= 613),
    coordinates_y       REAL NOT NULL CHECK (coordinates_y <= 962),
    creation_date       TIMESTAMP NOT NULL DEFAULT now(),
    type                TEXT NOT NULL CHECK (type IN ('CAR','HELICOPTER','MOTORCYCLE','CHOPPER')),
    engine_power        INTEGER CHECK (engine_power > 0),
    number_of_wheels    INTEGER NOT NULL CHECK (number_of_wheels > 0),
    capacity            INTEGER CHECK (capacity > 0),
    distance_travelled  INTEGER CHECK (distance_travelled > 0),
    fuel_consumption    REAL NOT NULL CHECK (fuel_consumption > 0),
    fuel_type           TEXT NOT NULL CHECK (fuel_type IN ('KEROSENE','MANPOWER','NUCLEAR'))
);

-- Функция 1: id объекта с минимальным distance_travelled (любой один)
CREATE OR REPLACE FUNCTION vehicle_min_distance_id()
RETURNS BIGINT LANGUAGE sql AS $$
  SELECT id FROM vehicle
  WHERE distance_travelled IS NOT NULL
  ORDER BY distance_travelled ASC, id ASC
  LIMIT 1;
$$;

-- Функция 2: количество объектов с fuel_consumption > t
CREATE OR REPLACE FUNCTION vehicle_count_fuel_gt(t REAL)
RETURNS BIGINT LANGUAGE sql AS $$
  SELECT COUNT(*) FROM vehicle WHERE fuel_consumption > t;
$$;

-- Функция 3: ids объектов с fuel_consumption > t (для выборки списком)
CREATE OR REPLACE FUNCTION vehicle_ids_fuel_gt(t REAL)
RETURNS SETOF BIGINT LANGUAGE sql AS $$
  SELECT id FROM vehicle WHERE fuel_consumption > t ORDER BY id;
$$;

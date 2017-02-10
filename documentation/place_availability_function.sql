CREATE OR REPLACE FUNCTION ixsi.place_availability(place_class text)
  RETURNS TABLE(
    place_id text,
    global_id text,
    capacity integer,
    on_premises_time_in_seconds integer,
    provider_id text,
    available_capacity integer,
    name text,
    gps_position text,
    available_vehicles bigint) AS

$BODY$

-- in order to have a clearly defined interface to get the availabilities from db,
-- we opt for a db function which can be called from within the code,
-- instead of using an ad-hoc query written in codebase.

WITH
    -- get the # of available vehicles at places with booking targets bound to places (stationsgebunden)
    available_place_bound AS (
    SELECT
      p3.place_id,
      p3.provider_id,
      count(av) AS available_vehicles
    FROM ixsi.place p3
      LEFT OUTER JOIN ixsi.booking_target bt2
        ON p3.place_id = bt2.exclusive_to_place_id
           AND bt2.status = 'ACTIVE'
      LEFT JOIN (
                  SELECT bt2.booking_target_id
                  FROM ixsi.booking_target bt2
                    LEFT JOIN ixsi.booking_target_status_inavailability btsi2
                      ON bt2.booking_target_id = btsi2.booking_target_id
                  WHERE btsi2.booking_target_id ISNULL
                ) AS av
        ON av.booking_target_id = bt2.booking_target_id
    WHERE bt2.exclusive_to_place_id NOTNULL
    GROUP BY p3.place_id
    ),

    -- get the # of available vehicles at places with flexible booking targets (stationsflexibel)
    available_place_flexible AS (
      SELECT
        p2.place_id,
        p2.provider_id,
        count(btsp.place_id) AS available_vehicles
      FROM ixsi.place p2
        JOIN ixsi.placegroup_place pgp
          ON p2.place_id = pgp.place_id
        LEFT OUTER JOIN ixsi.booking_target bt
          ON pgp.placegroup_id = bt.exclusive_to_placegroup_id
             AND bt.status = 'ACTIVE'
        LEFT OUTER JOIN ixsi.booking_target_status_place btsp
          ON bt.booking_target_id = btsp.booking_target_id AND bt.provider_id = btsp.provider_id
             AND bt.provider_id = btsp.provider_id
             AND p2.place_id = btsp.place_id
        LEFT OUTER JOIN ixsi.booking_target_status_inavailability btsi
          ON btsi.booking_target_id = btsp.booking_target_id
      WHERE btsi.booking_target_id ISNULL
      GROUP BY p2.place_id
    ),

    -- combine the two from above
    available AS (
      SELECT * FROM available_place_flexible
      UNION
      SELECT * FROM available_place_bound
    )

-- main query
SELECT
  p.place_id,
  p.global_id,
  p.capacity,
  p.on_premises_time_in_seconds,
  p.provider_id,
  p.available_capacity,
  pn.value AS name,
  CAST(p.gps_position AS VARCHAR(255)),
  available.available_vehicles
FROM ixsi.place p
  LEFT JOIN ixsi.place_attribute pa ON p.place_id = pa.place_id
  LEFT JOIN ixsi.attribute a ON pa.attribute_id = a.attribute_id
  LEFT JOIN ixsi.place_name pn ON p.place_id = pn.place_id
  LEFT JOIN available ON available.place_id = p.place_id AND available.provider_id = p.provider_id
WHERE a.class = $1

$BODY$
LANGUAGE sql;

INSERT INTO provider (provider_id, name, status)
VALUES ('Velocity', 'Velocity', 'ACTIVE');

INSERT INTO attribute (attribute_id, class, status)
VALUES ('VmVsb2NpdHlCaWtlU2hhcmluZw==', 'bike_sharing', 'ACTIVE');

INSERT INTO placegroup (placegroup_id, probability, status)
VALUES ('Velocity-Aachen', 100, 'ACTIVE');

INSERT INTO place (place_id, global_id, capacity, on_premises_time_in_seconds, provider_id, available_capacity, gps_position, status)
VALUES
  ('asdf1', NULL, 5, 2, 'Velocity', 2, '(6.060874099999978,50.7776886)', 'ACTIVE'),
  ('asdf2', NULL, 5, 2, 'Velocity', 2, '(6.075794,50.780254)', 'ACTIVE'),
  ('asdf3', NULL, 5, 2, 'Velocity', 2, '(6.047457,50.784148)', 'ACTIVE'),
  ('asdf4', NULL, 5, 2, 'Velocity', 2, '(6.06086,50.77832)', 'ACTIVE'),
  ('asdf5', NULL, 5, 2, 'Velocity', 2, '(6.070687,50.779775)', 'ACTIVE'),
  ('asdf6', NULL, 5, 2, 'Velocity', 2, '(6.079285,50.777942)', 'ACTIVE'),
  ('asdf7', NULL, 5, 2, 'Velocity', 2, '(6.06808,50.780492)', 'ACTIVE'),
  ('asdf8', NULL, 5, 2, 'Velocity', 2, '(6.086632,50.773716)', 'ACTIVE'),
  ('asdf9', NULL, 5, 2, 'Velocity', 2, '(6.048233,50.778987)', 'ACTIVE'),
  ('asdf10', NULL, 5, 2, 'Velocity', 2, '(6.095392,50.755531)', 'ACTIVE'),
  ('asdf11', NULL, 5, 2, 'Velocity', 2, '(6.080586,50.772292)', 'ACTIVE'),
  ('asdf12', NULL, 5, 2, 'Velocity', 2, '(6.07488,50.778293)', 'ACTIVE'),
  ('asdf13', NULL, 5, 2, 'Velocity', 2, '(6.081508,50.77842)', 'ACTIVE'),
  ('asdf14', NULL, 5, 2, 'Velocity', 2, '(6.093587,50.766139)', 'ACTIVE'),
  ('asdf15', NULL, 5, 2, 'Velocity', 2, '(6.051038,50.78924)', 'ACTIVE'),
  ('asdf16', NULL, 5, 2, 'Velocity', 2, '(6.044021,50.783504)', 'ACTIVE'),
  ('asdf17', NULL, 5, 2, 'Velocity', 2, '(6.050767,50.786014)', 'ACTIVE'),
  ('asdf18', NULL, 5, 2, 'Velocity', 2, '(6.105725,50.780189)', 'ACTIVE'),
  ('asdf19', NULL, 5, 2, 'Velocity', 2, '(6.09026,50.768087)', 'ACTIVE')
;

INSERT INTO place_attribute (place_id, attribute_id) VALUES
  ('asdf1','VmVsb2NpdHlCaWtlU2hhcmluZw=='),
  ('asdf2','VmVsb2NpdHlCaWtlU2hhcmluZw=='),
  ('asdf3','VmVsb2NpdHlCaWtlU2hhcmluZw=='),
  ('asdf4','VmVsb2NpdHlCaWtlU2hhcmluZw=='),
  ('asdf5','VmVsb2NpdHlCaWtlU2hhcmluZw=='),
  ('asdf6','VmVsb2NpdHlCaWtlU2hhcmluZw=='),
  ('asdf7','VmVsb2NpdHlCaWtlU2hhcmluZw=='),
  ('asdf8','VmVsb2NpdHlCaWtlU2hhcmluZw=='),
  ('asdf9','VmVsb2NpdHlCaWtlU2hhcmluZw=='),
  ('asdf10','VmVsb2NpdHlCaWtlU2hhcmluZw=='),
  ('asdf11','VmVsb2NpdHlCaWtlU2hhcmluZw=='),
  ('asdf12','VmVsb2NpdHlCaWtlU2hhcmluZw=='),
  ('asdf13','VmVsb2NpdHlCaWtlU2hhcmluZw=='),
  ('asdf14','VmVsb2NpdHlCaWtlU2hhcmluZw=='),
  ('asdf15','VmVsb2NpdHlCaWtlU2hhcmluZw=='),
  ('asdf16','VmVsb2NpdHlCaWtlU2hhcmluZw=='),
  ('asdf17','VmVsb2NpdHlCaWtlU2hhcmluZw=='),
  ('asdf18','VmVsb2NpdHlCaWtlU2hhcmluZw=='),
  ('asdf19','VmVsb2NpdHlCaWtlU2hhcmluZw==')
;


INSERT INTO placegroup_place (placegroup_id, place_id) VALUES
  ('Velocity-Aachen','asdf1'),
  ('Velocity-Aachen','asdf2'),
  ('Velocity-Aachen','asdf3'),
  ('Velocity-Aachen','asdf4'),
  ('Velocity-Aachen','asdf5'),
  ('Velocity-Aachen','asdf6'),
  ('Velocity-Aachen','asdf7'),
  ('Velocity-Aachen','asdf8'),
  ('Velocity-Aachen','asdf9'),
  ('Velocity-Aachen','asdf10'),
  ('Velocity-Aachen','asdf11'),
  ('Velocity-Aachen','asdf12'),
  ('Velocity-Aachen','asdf13'),
  ('Velocity-Aachen','asdf14'),
  ('Velocity-Aachen','asdf15'),
  ('Velocity-Aachen','asdf16'),
  ('Velocity-Aachen','asdf17'),
  ('Velocity-Aachen','asdf18'),
  ('Velocity-Aachen','asdf19')
;

INSERT INTO booking_target (booking_target_id, provider_id, global_id, class, booking_horizon_in_seconds,
                            booking_grid_in_minutes, opening_time_in_seconds, engine, co2_factor,
                            max_distance_in_meters, exclusive_to_floating_area_id, exclusive_to_place_id,
                            exclusive_to_placegroup_id, status)
VALUES
  ('dummy_bt_1', 'Velocity', NULL, 'bike', NULL, NULL, NULL, 'electric', NULL, NULL, NULL, NULL, 'Velocity-Aachen', 'ACTIVE'),
  ('dummy_bt_2', 'Velocity', NULL, 'bike', NULL, NULL, NULL, 'electric', NULL, NULL, NULL, NULL, 'Velocity-Aachen', 'ACTIVE'),
  ('dummy_bt_3', 'Velocity', NULL, 'bike', NULL, NULL, NULL, 'electric', NULL, NULL, NULL, NULL, 'Velocity-Aachen', 'ACTIVE'),
  ('dummy_bt_4', 'Velocity', NULL, 'bike', NULL, NULL, NULL, 'electric', NULL, NULL, NULL, NULL, 'Velocity-Aachen', 'ACTIVE'),
  ('dummy_bt_5', 'Velocity', NULL, 'bike', NULL, NULL, NULL, 'electric', NULL, NULL, NULL, NULL, 'Velocity-Aachen', 'ACTIVE'),
  ('dummy_bt_6', 'Velocity', NULL, 'bike', NULL, NULL, NULL, 'electric', NULL, NULL, NULL, NULL, 'Velocity-Aachen', 'ACTIVE'),
  ('dummy_bt_7', 'Velocity', NULL, 'bike', NULL, NULL, NULL, 'electric', NULL, NULL, NULL, NULL, 'Velocity-Aachen', 'ACTIVE'),
  ('dummy_bt_8', 'Velocity', NULL, 'bike', NULL, NULL, NULL, 'electric', NULL, NULL, NULL, NULL, 'Velocity-Aachen', 'ACTIVE'),
  ('dummy_bt_9', 'Velocity', NULL, 'bike', NULL, NULL, NULL, 'electric', NULL, NULL, NULL, NULL, 'Velocity-Aachen', 'ACTIVE'),
  ('dummy_bt_10', 'Velocity', NULL, 'bike', NULL, NULL, NULL, 'electric', NULL, NULL, NULL, NULL, 'Velocity-Aachen', 'ACTIVE'),
  ('dummy_bt_11', 'Velocity', NULL, 'bike', NULL, NULL, NULL, 'electric', NULL, NULL, NULL, NULL, 'Velocity-Aachen', 'ACTIVE'),
  ('dummy_bt_12', 'Velocity', NULL, 'bike', NULL, NULL, NULL, 'electric', NULL, NULL, NULL, NULL, 'Velocity-Aachen', 'ACTIVE'),
  ('dummy_bt_13', 'Velocity', NULL, 'bike', NULL, NULL, NULL, 'electric', NULL, NULL, NULL, NULL, 'Velocity-Aachen', 'ACTIVE'),
  ('dummy_bt_14', 'Velocity', NULL, 'bike', NULL, NULL, NULL, 'electric', NULL, NULL, NULL, NULL, 'Velocity-Aachen', 'ACTIVE'),
  ('dummy_bt_15', 'Velocity', NULL, 'bike', NULL, NULL, NULL, 'electric', NULL, NULL, NULL, NULL, 'Velocity-Aachen', 'ACTIVE'),
  ('dummy_bt_16', 'Velocity', NULL, 'bike', NULL, NULL, NULL, 'electric', NULL, NULL, NULL, NULL, 'Velocity-Aachen', 'ACTIVE'),
  ('dummy_bt_17', 'Velocity', NULL, 'bike', NULL, NULL, NULL, 'electric', NULL, NULL, NULL, NULL, 'Velocity-Aachen', 'ACTIVE'),
  ('dummy_bt_18', 'Velocity', NULL, 'bike', NULL, NULL, NULL, 'electric', NULL, NULL, NULL, NULL, 'Velocity-Aachen', 'ACTIVE'),
  ('dummy_bt_19', 'Velocity', NULL, 'bike', NULL, NULL, NULL, 'electric', NULL, NULL, NULL, NULL, 'Velocity-Aachen', 'ACTIVE'),
  ('dummy_bt_20', 'Velocity', NULL, 'bike', NULL, NULL, NULL, 'electric', NULL, NULL, NULL, NULL, 'Velocity-Aachen', 'ACTIVE')
;

INSERT INTO booking_target_status (booking_target_id, provider_id, gps_position, current_charge, current_driving_range_in_meters) VALUES
  ('dummy_bt_1', 'Velocity', NULL, 99, NULL),
  ('dummy_bt_2', 'Velocity', NULL, 99, NULL),
  ('dummy_bt_3', 'Velocity', NULL, 99, NULL),
  ('dummy_bt_4', 'Velocity', NULL, 99, NULL),
  ('dummy_bt_5', 'Velocity', NULL, 99, NULL),
  ('dummy_bt_6', 'Velocity', NULL, 99, NULL),
  ('dummy_bt_7', 'Velocity', NULL, 99, NULL),
  ('dummy_bt_8', 'Velocity', NULL, 99, NULL),
  ('dummy_bt_9', 'Velocity', NULL, 99, NULL),
  ('dummy_bt_10', 'Velocity', NULL, 99, NULL),
  ('dummy_bt_11', 'Velocity', NULL, 99, NULL),
  ('dummy_bt_12', 'Velocity', NULL, 99, NULL),
  ('dummy_bt_13', 'Velocity', NULL, 99, NULL),
  ('dummy_bt_14', 'Velocity', NULL, 99, NULL),
  ('dummy_bt_15', 'Velocity', NULL, 99, NULL),
  ('dummy_bt_16', 'Velocity', NULL, 99, NULL),
  ('dummy_bt_17', 'Velocity', NULL, 99, NULL),
  ('dummy_bt_18', 'Velocity', NULL, 99, NULL),
  ('dummy_bt_19', 'Velocity', NULL, 99, NULL),
  ('dummy_bt_20', 'Velocity', NULL, 99, NULL)
;

INSERT INTO booking_target_name (booking_target_id, provider_id, language, value) VALUES
  ('dummy_bt_1', 'Velocity', 'DE',  'dummy_bt_1'),
  ('dummy_bt_2', 'Velocity', 'DE',  'dummy_bt_2'),
  ('dummy_bt_3', 'Velocity', 'DE',  'dummy_bt_3'),
  ('dummy_bt_4', 'Velocity', 'DE',  'dummy_bt_4'),
  ('dummy_bt_5', 'Velocity', 'DE',  'dummy_bt_5'),
  ('dummy_bt_6', 'Velocity', 'DE',  'dummy_bt_6'),
  ('dummy_bt_7', 'Velocity', 'DE',  'dummy_bt_7'),
  ('dummy_bt_8', 'Velocity', 'DE',  'dummy_bt_8'),
  ('dummy_bt_9', 'Velocity', 'DE',  'dummy_bt_9'),
  ('dummy_bt_10', 'Velocity', 'DE',  'dummy_bt_10'),
  ('dummy_bt_11', 'Velocity', 'DE',  'dummy_bt_11'),
  ('dummy_bt_12', 'Velocity', 'DE',  'dummy_bt_12'),
  ('dummy_bt_13', 'Velocity', 'DE',  'dummy_bt_13'),
  ('dummy_bt_14', 'Velocity', 'DE',  'dummy_bt_14'),
  ('dummy_bt_15', 'Velocity', 'DE',  'dummy_bt_15'),
  ('dummy_bt_16', 'Velocity', 'DE',  'dummy_bt_16'),
  ('dummy_bt_17', 'Velocity', 'DE',  'dummy_bt_17'),
  ('dummy_bt_18', 'Velocity', 'DE',  'dummy_bt_18'),
  ('dummy_bt_19', 'Velocity', 'DE',  'dummy_bt_19'),
  ('dummy_bt_20', 'Velocity', 'DE',  'dummy_bt_20')
;

INSERT INTO booking_target_status_place (booking_target_id, provider_id, place_id) VALUES
  ('dummy_bt_1', 'Velocity', 'asdf1'),
  ('dummy_bt_2', 'Velocity', 'asdf2'),
  ('dummy_bt_3', 'Velocity', 'asdf3'),
  ('dummy_bt_4', 'Velocity', 'asdf4'),
  ('dummy_bt_5', 'Velocity', 'asdf5'),
  ('dummy_bt_6', 'Velocity', 'asdf6'),
  ('dummy_bt_7', 'Velocity', 'asdf7'),
  ('dummy_bt_8', 'Velocity', 'asdf8'),
  ('dummy_bt_9', 'Velocity', 'asdf9'),
  ('dummy_bt_10', 'Velocity', 'asdf10'),
  ('dummy_bt_11', 'Velocity', 'asdf11'),
  ('dummy_bt_12', 'Velocity', 'asdf12'),
  ('dummy_bt_13', 'Velocity', 'asdf13'),
  ('dummy_bt_14', 'Velocity', 'asdf14'),
  ('dummy_bt_15', 'Velocity', 'asdf15'),
  ('dummy_bt_16', 'Velocity', 'asdf16'),
  ('dummy_bt_17', 'Velocity', 'asdf17'),
  ('dummy_bt_18', 'Velocity', 'asdf18'),
  ('dummy_bt_19', 'Velocity', 'asdf19'),
  ('dummy_bt_20', 'Velocity', 'asdf1')
;
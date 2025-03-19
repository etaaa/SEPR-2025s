-- insert initial test data
-- the IDs are hardcoded to enable references between further test data
-- negative IDs are used to not interfere with user-entered data and allow clean deletion of test data

DELETE
FROM owner
WHERE id < 0;

INSERT INTO owner (id, first_name, last_name, description)
VALUES (-1, 'Wendy', 'Owner', 'Owner of multiple horses including Wendy.'),
       (-2, 'Not an', 'Owner', 'Owns no horses yet'),
       (-3, 'Third', 'Owner', 'Wait who is this?'),
       (-4, 'Fourth', 'Owner', 'Planning on buying a horse in the near future'),
       (-5, 'Fifth', 'Owner', 'I like horses lol');


DELETE
FROM horse
where id < 0;

INSERT INTO horse (id, name, description, date_of_birth, sex, owner_id, mother_id, father_id, image, mime_type)
VALUES (-1, 'Wendys Mother', 'The famous one!', '1970-01-01', 'FEMALE', null, null, null, null, null),
       (-2, 'Wendys Father', 'The cool one!', '1970-01-01', 'MALE', null, null, null, null, null),
       (-3, 'Wendy', 'The new one!', '2000-01-01', 'FEMALE', -1, -1, -2, null, null),
       (-4, 'Wendys first child', 'The first one!', '2020-01-01', 'MALE', -1, -3, null, null, null),
       (-5, 'Wendys second child', 'The youngest one!', '2025-01-01', 'FEMALE', -1, -3, null, null, null);
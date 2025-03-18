-- insert initial test data
-- the IDs are hardcoded to enable references between further test data
-- negative IDs are used to not interfere with user-entered data and allow clean deletion of test data

DELETE
FROM owner
WHERE id < 0;

INSERT INTO owner (id, first_name, last_name, description)
VALUES (-1, 'Wendy', 'Owner', 'Owner of multiple horses including Wendy.'),
       (-2, 'Not an', 'Owner', 'Owns no horses');


DELETE
FROM horse
where id < 0;

INSERT INTO horse (id, name, description, date_of_birth, sex, owner_id, mother_id, father_id, image, mime_type)
VALUES (-1, 'Wendys Mother', 'The famous one!', '1990-12-12', 'FEMALE', null, null, null, null, null),
       (-2, 'Wendys Father', 'The cool one!', '1990-12-12', 'MALE', null, null, null, null, null),
       (-3, 'Wendy', 'The new one!', '2010-12-12', 'FEMALE', -1, -1, -2, null, null);
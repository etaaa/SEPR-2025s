-- insert initial test data
-- the IDs are hardcoded to enable references between further test data
-- negative IDs are used to not interfere with user-entered data and allow clean deletion of test data

DELETE FROM owner WHERE id < 0;

INSERT INTO owner (id, name, description)
VALUES (-1, 'Ether', 'Owner of multiple horses');


DELETE FROM horse where id < 0;

INSERT INTO horse (id, name, description, date_of_birth, sex, image, mime_type)
VALUES (-1, 'Wendy', 'The famous one!', '2012-12-12', 'FEMALE', X'89504E470D0A1A0A', 'image/jpeg')
CREATE TABLE IF NOT EXISTS owner
(
    id          BIGINT auto_increment PRIMARY KEY,
    first_name  VARCHAR(255) NOT NULL,
    last_name   VARCHAR(255) NOT NULL,
    description VARCHAR(4095)
    );

CREATE TABLE IF NOT EXISTS horse
(
    id            BIGINT auto_increment PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    description   VARCHAR(4095),
    date_of_birth DATE NOT NULL,
    sex           ENUM('MALE', 'FEMALE') NOT NULL,
    owner_id      BIGINT,
    mother_id     BIGINT,
    father_id     BIGINT,
    image         BLOB,
    mime_type     VARCHAR(255),
    CONSTRAINT fk_owner FOREIGN KEY (owner_id) REFERENCES owner(id) ON DELETE SET NULL,
    CONSTRAINT fk_mother FOREIGN KEY (mother_id) REFERENCES horse(id) ON DELETE SET NULL,
    CONSTRAINT fk_father FOREIGN KEY (father_id) REFERENCES horse(id) ON DELETE SET NULL
    );
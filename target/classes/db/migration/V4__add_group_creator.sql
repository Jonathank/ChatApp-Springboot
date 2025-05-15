ALTER TABLE groups
ADD COLUMN creator_id BIGINT NOT NULL;

ALTER TABLE groups
ADD CONSTRAINT fk_group_creator
FOREIGN KEY (creator_id) REFERENCES users(id);

UPDATE groups
SET creator_id = 1
WHERE creator_id IS NULL;
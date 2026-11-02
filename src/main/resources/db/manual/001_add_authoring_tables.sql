CREATE TABLE story_draft (
    id BIGINT NOT NULL AUTO_INCREMENT,
    document_json LONGTEXT NOT NULL,
    revision BIGINT NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    published_story_id BIGINT NULL,
    published_revision BIGINT NULL,
    published_version INT NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE story_publication (
    id BIGINT NOT NULL AUTO_INCREMENT,
    draft_id BIGINT NOT NULL,
    source_revision BIGINT NOT NULL,
    story_id BIGINT NOT NULL,
    version_no INT NOT NULL,
    document_json LONGTEXT NOT NULL,
    published_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT ux_publication_revision UNIQUE (draft_id, source_revision),
    CONSTRAINT ux_publication_version UNIQUE (draft_id, version_no),
    CONSTRAINT ux_publication_story UNIQUE (story_id)
);

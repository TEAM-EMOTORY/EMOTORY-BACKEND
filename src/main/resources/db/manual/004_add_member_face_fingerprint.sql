-- 기존 회원은 NULL로 유지한다. 해시가 없는 과거 사진은 자동으로 동일 사진으로 간주하지 않는다.
ALTER TABLE member
    ADD COLUMN face_image_sha256 VARCHAR(64) NULL,
    ADD COLUMN face_snapshot_key VARCHAR(1024) NULL,
    ADD INDEX idx_member_name_face_sha256 (name, face_image_sha256);

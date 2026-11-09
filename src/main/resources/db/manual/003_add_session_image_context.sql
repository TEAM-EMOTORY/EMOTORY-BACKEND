-- 얼굴 원본과 첫 장면 참고 이미지 및 진행 중인 생성 작업을 세션별로 보관한다.
CREATE TABLE session_image_context (
    play_session_id BIGINT NOT NULL,
    source_face_key VARCHAR(1024) NOT NULL,
    face_snapshot_key VARCHAR(1024) NULL,
    reference_image_key VARCHAR(1024) NULL,
    generation_token VARCHAR(36) NULL,
    lease_until TIMESTAMP(6) NULL,
    PRIMARY KEY (play_session_id),
    CONSTRAINT fk_session_image_context_session FOREIGN KEY (play_session_id)
        REFERENCES play_session (play_session_id)
);

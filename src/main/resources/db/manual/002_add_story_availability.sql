-- 기존 동화는 계속 제공하며 이후 생성되는 동화도 기본 제공 상태로 유지한다.
ALTER TABLE story ADD COLUMN is_available BOOLEAN NOT NULL DEFAULT TRUE;

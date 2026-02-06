CREATE TABLE IF NOT EXISTS member_profile (
 id TEXT PRIMARY KEY,
 attributes JSONB NOT NULL,
 updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE IF NOT EXISTS enrollment (
 id UUID PRIMARY KEY,
 member_id TEXT NOT NULL REFERENCES member_profile(id),
 program_id TEXT NOT NULL,
 rule_version INTEGER NOT NULL,
 attribute_snapshot JSONB NOT NULL,
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 UNIQUE(member_id, program_id)
);
INSERT INTO member_profile(id, attributes) VALUES ('demo-member', '{"age":27,"residency":"NY","student":true}') ON CONFLICT DO NOTHING;

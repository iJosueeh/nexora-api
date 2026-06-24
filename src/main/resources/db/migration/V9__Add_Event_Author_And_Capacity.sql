-- V9: Add author_id to university_events for ownership validation
-- Also add capacity field for future RSVP limits

ALTER TABLE university_events
    ADD COLUMN IF NOT EXISTS author_id UUID REFERENCES usuarios(id),
    ADD COLUMN IF NOT EXISTS capacity INTEGER;

CREATE INDEX IF NOT EXISTS idx_university_events_author ON university_events(author_id);

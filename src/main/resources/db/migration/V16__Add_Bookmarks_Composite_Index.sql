-- V16: Add composite index for efficient bookmark queries with ordering
-- Optimizes: WHERE user_id = ? ORDER BY created_at DESC
CREATE INDEX IF NOT EXISTS idx_bookmarks_user_created ON bookmarks(user_id, created_at DESC);

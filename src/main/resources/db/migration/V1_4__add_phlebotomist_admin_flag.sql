-- Add isAdmin column to phlebotomist_profiles table
-- Migration for phlebotomist admin capability

ALTER TABLE phlebotomist_profiles
ADD COLUMN is_admin BOOLEAN NOT NULL DEFAULT FALSE;

-- Create index for faster queries on admin phlebotomists
CREATE INDEX idx_phlebotomist_is_admin ON phlebotomist_profiles(is_admin) WHERE is_admin = TRUE;

-- Add comment
COMMENT ON COLUMN phlebotomist_profiles.is_admin IS 'Indicates if phlebotomist has admin privileges for result uploads and admin operations';

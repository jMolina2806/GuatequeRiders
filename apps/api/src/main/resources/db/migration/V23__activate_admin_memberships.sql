UPDATE users
SET member_number = CONCAT('GR-ADM-', id)
WHERE role = 'ADMIN'
  AND member_number IS NULL;

UPDATE users
SET membership_active = TRUE,
    membership_version = membership_version + 1
WHERE role = 'ADMIN'
  AND membership_active = FALSE;

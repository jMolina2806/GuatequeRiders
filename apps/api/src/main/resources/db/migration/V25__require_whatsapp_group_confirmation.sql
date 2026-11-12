UPDATE users
SET membership_active = FALSE,
    membership_version = membership_version + 1
WHERE role = 'USER'
  AND membership_active = TRUE;

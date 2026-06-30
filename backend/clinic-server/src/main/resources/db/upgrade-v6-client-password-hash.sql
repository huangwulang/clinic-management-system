-- Store demo passwords as SHA-256(MD5(password)) so the login API can receive a client-side MD5 value.
SET @default_password_hash = 'cdf4a007e2b02a0c49fc9b7ccfbb8a10c644f635e1765dcf2a7ab794ddc7edac';
SET @legacy_plain_password = '123456';
SET @legacy_sha256_password = SHA2('123456', 256);

UPDATE sys_user_account
SET password = @default_password_hash,
    updated_at = NOW()
WHERE deleted = 0
  AND password IN (@legacy_plain_password, @legacy_sha256_password);

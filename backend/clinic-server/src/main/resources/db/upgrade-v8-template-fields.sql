DELIMITER //
DROP PROCEDURE IF EXISTS add_column_if_missing//
CREATE PROCEDURE add_column_if_missing(
  IN target_table VARCHAR(128),
  IN target_column VARCHAR(128),
  IN column_definition TEXT
)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = target_table
      AND column_name = target_column
  ) THEN
    SET @add_column_ddl = CONCAT(
      'ALTER TABLE `', target_table, '` ADD COLUMN `', target_column, '` ', column_definition
    );
    PREPARE add_column_statement FROM @add_column_ddl;
    EXECUTE add_column_statement;
    DEALLOCATE PREPARE add_column_statement;
  END IF;
END//

DROP PROCEDURE IF EXISTS drop_column_if_exists//
CREATE PROCEDURE drop_column_if_exists(
  IN target_table VARCHAR(128),
  IN target_column VARCHAR(128)
)
BEGIN
  IF EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = target_table
      AND column_name = target_column
  ) THEN
    SET @drop_column_ddl = CONCAT(
      'ALTER TABLE `', target_table, '` DROP COLUMN `', target_column, '`'
    );
    PREPARE drop_column_statement FROM @drop_column_ddl;
    EXECUTE drop_column_statement;
    DEALLOCATE PREPARE drop_column_statement;
  END IF;
END//
DELIMITER ;

CALL add_column_if_missing('clinic_medical_template', 'permission', 'VARCHAR(50) NULL COMMENT ''模板权限'' AFTER `name`');
CALL add_column_if_missing('clinic_medical_template', 'diagnosis', 'VARCHAR(255) NULL COMMENT ''诊断'' AFTER `permission`');
CALL add_column_if_missing('clinic_medical_template', 'description', 'VARCHAR(500) NULL COMMENT ''模板说明'' AFTER `diagnosis`');

UPDATE clinic_medical_template
SET permission = COALESCE(permission, '私人模板'),
    diagnosis = COALESCE(
      diagnosis,
      CASE
        WHEN JSON_VALID(content) THEN JSON_UNQUOTE(JSON_EXTRACT(content, '$.diagnosis'))
        ELSE NULL
      END
    ),
    description = COALESCE(description, name),
    updated_at = NOW()
WHERE deleted = 0;

CALL drop_column_if_exists('clinic_medical_template', 'department_name');

DROP PROCEDURE add_column_if_missing;
DROP PROCEDURE drop_column_if_exists;

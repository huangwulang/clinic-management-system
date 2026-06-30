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

CALL add_column_if_missing('clinic_supplier', 'creator', 'VARCHAR(100) NULL COMMENT ''创建人'' AFTER `status`');
CALL drop_column_if_exists('clinic_supplier', 'address');

CALL add_column_if_missing('clinic_check_project', 'cost_price', 'DECIMAL(12,2) NULL DEFAULT 0 COMMENT ''成本价'' AFTER `category`');
CALL add_column_if_missing('clinic_check_project', 'retail_price', 'DECIMAL(12,2) NULL DEFAULT 0 COMMENT ''零售价'' AFTER `cost_price`');
CALL add_column_if_missing('clinic_check_project', 'invoice_item', 'VARCHAR(100) NULL COMMENT ''发票项目'' AFTER `unit`');
CALL add_column_if_missing('clinic_check_project', 'body_part', 'VARCHAR(100) NULL COMMENT ''检查部位'' AFTER `invoice_item`');
CALL add_column_if_missing('clinic_check_project', 'allow_member_discount', 'TINYINT(1) NULL DEFAULT 1 COMMENT ''允许会员折扣'' AFTER `body_part`');
CALL add_column_if_missing('clinic_check_project', 'creator', 'VARCHAR(100) NULL COMMENT ''创建人'' AFTER `status`');

UPDATE clinic_check_project
SET retail_price = COALESCE(NULLIF(retail_price, 0), price),
    price = COALESCE(NULLIF(price, 0), retail_price),
    allow_member_discount = COALESCE(allow_member_discount, 1),
    updated_at = NOW()
WHERE deleted = 0;

DROP PROCEDURE add_column_if_missing;
DROP PROCEDURE drop_column_if_exists;

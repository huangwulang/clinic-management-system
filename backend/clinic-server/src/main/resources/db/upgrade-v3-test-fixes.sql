-- Test-result fixes: complete drug fields and add comments to every business table/column.
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
DELIMITER ;

CALL add_column_if_missing('clinic_drug', 'barcode', 'VARCHAR(100) NULL COMMENT ''药品条形码'' AFTER `drug_code`');
CALL add_column_if_missing('clinic_drug', 'pinyin', 'VARCHAR(100) NULL COMMENT ''药品拼音码'' AFTER `name`');
CALL add_column_if_missing('clinic_drug', 'otc', 'TINYINT(1) NULL DEFAULT 0 COMMENT ''是否OTC药品'' AFTER `dosage_form`');
CALL add_column_if_missing('clinic_drug', 'invoice_item', 'VARCHAR(100) NULL COMMENT ''发票项目'' AFTER `otc`');
CALL add_column_if_missing('clinic_drug', 'pack_unit', 'VARCHAR(50) NULL COMMENT ''包装单位'' AFTER `unit`');
CALL add_column_if_missing('clinic_drug', 'base_ratio', 'DECIMAL(12,4) NULL DEFAULT 0 COMMENT ''基本系数'' AFTER `pack_unit`');
CALL add_column_if_missing('clinic_drug', 'base_unit', 'VARCHAR(50) NULL COMMENT ''基本单位'' AFTER `base_ratio`');
CALL add_column_if_missing('clinic_drug', 'dose_ratio', 'DECIMAL(12,4) NULL DEFAULT 0 COMMENT ''剂量系数'' AFTER `base_unit`');
CALL add_column_if_missing('clinic_drug', 'dose_unit', 'VARCHAR(50) NULL COMMENT ''剂量单位'' AFTER `dose_ratio`');
CALL add_column_if_missing('clinic_drug', 'stock_min', 'INT NULL DEFAULT 0 COMMENT ''库存下限'' AFTER `warning_stock`');
CALL add_column_if_missing('clinic_drug', 'stock_max', 'INT NULL DEFAULT 0 COMMENT ''库存上限'' AFTER `stock_min`');
CALL add_column_if_missing('clinic_drug', 'location_no', 'VARCHAR(100) NULL COMMENT ''货位号'' AFTER `stock_max`');
CALL add_column_if_missing('clinic_drug', 'expiry_warning_days', 'INT NULL DEFAULT 0 COMMENT ''有效期预警天数'' AFTER `location_no`');
CALL add_column_if_missing('clinic_drug', 'allow_split', 'TINYINT(1) NULL DEFAULT 0 COMMENT ''是否允许拆零'' AFTER `expiry_warning_days`');
CALL add_column_if_missing('clinic_drug', 'allow_member_discount', 'TINYINT(1) NULL DEFAULT 0 COMMENT ''是否允许会员折扣'' AFTER `allow_split`');
CALL add_column_if_missing('clinic_drug', 'usage_method', 'VARCHAR(100) NULL COMMENT ''默认用法'' AFTER `allow_member_discount`');
CALL add_column_if_missing('clinic_drug', 'single_dose', 'DECIMAL(12,4) NULL DEFAULT 0 COMMENT ''默认单次用量'' AFTER `usage_method`');
CALL add_column_if_missing('clinic_drug', 'frequency', 'VARCHAR(100) NULL COMMENT ''默认频度'' AFTER `single_dose`');
CALL add_column_if_missing('clinic_drug', 'default_days', 'INT NULL DEFAULT 0 COMMENT ''默认用药天数'' AFTER `frequency`');
CALL add_column_if_missing('clinic_drug', 'default_total', 'DECIMAL(12,4) NULL DEFAULT 0 COMMENT ''默认总量'' AFTER `default_days`');
CALL add_column_if_missing('clinic_drug', 'total_unit', 'VARCHAR(50) NULL COMMENT ''默认总量单位'' AFTER `default_total`');
CALL add_column_if_missing('clinic_drug', 'description', 'VARCHAR(2000) NULL COMMENT ''药品说明'' AFTER `total_unit`');
CALL add_column_if_missing('clinic_drug', 'remark', 'VARCHAR(1000) NULL COMMENT ''备注'' AFTER `description`');
DROP PROCEDURE add_column_if_missing;

DELIMITER //
DROP FUNCTION IF EXISTS clinic_column_comment//
CREATE FUNCTION clinic_column_comment(target_table VARCHAR(128), target_column VARCHAR(128))
RETURNS VARCHAR(255)
DETERMINISTIC
BEGIN
  RETURN CASE CONCAT(target_table, '.', target_column)
    WHEN 'clinic_patient.name' THEN '患者姓名'
    WHEN 'clinic_drug.name' THEN '药品名称'
    WHEN 'clinic_drug.category' THEN '药品分类'
    WHEN 'clinic_drug.description' THEN '药品说明'
    WHEN 'clinic_drug.status' THEN '药品启用状态'
    WHEN 'clinic_drug.unit' THEN '药品计量单位'
    WHEN 'clinic_check_project.name' THEN '检查项目名称'
    WHEN 'clinic_check_project.category' THEN '检查项目分类'
    WHEN 'clinic_check_project.price' THEN '检查项目价格'
    WHEN 'clinic_check_project.status' THEN '检查项目状态'
    WHEN 'clinic_check_project.unit' THEN '检查项目计费单位'
    WHEN 'clinic_fee_item.name' THEN '收费项目名称'
    WHEN 'clinic_fee_item.category' THEN '收费项目分类'
    WHEN 'clinic_fee_item.price' THEN '收费项目单价'
    WHEN 'clinic_fee_item.status' THEN '收费项目状态'
    WHEN 'clinic_fee_item.unit' THEN '收费计量单位'
    WHEN 'clinic_family_member.name' THEN '家属姓名'
    WHEN 'clinic_medical_template.name' THEN '模板名称'
    WHEN 'clinic_medical_template.content' THEN '模板内容'
    WHEN 'clinic_medical_template.status' THEN '模板启用状态'
    WHEN 'clinic_supplier.name' THEN '供应商名称'
    WHEN 'clinic_supplier.status' THEN '供应商状态'
    WHEN 'clinic_inventory.category' THEN '药品分类'
    WHEN 'clinic_inventory.expire_date' THEN '药品有效期至'
    WHEN 'clinic_inventory.unit' THEN '库存计量单位'
    WHEN 'clinic_inventory_check.status' THEN '盘点单状态'
    WHEN 'clinic_member.card_no' THEN '会员卡号'
    WHEN 'clinic_member.expire_date' THEN '会员到期日期'
    WHEN 'clinic_member.points' THEN '当前积分'
    WHEN 'clinic_member.status' THEN '会员状态'
    WHEN 'clinic_charge_order.status' THEN '收费订单状态'
    WHEN 'clinic_consultation.status' THEN '接诊状态'
    WHEN 'clinic_registration.status' THEN '挂号状态'
    WHEN 'clinic_stock_order.status' THEN '出入库单状态'
    WHEN 'sys_message.content' THEN '消息内容'
    WHEN 'sys_department.name' THEN '科室名称'
    WHEN 'sys_department.description' THEN '科室描述'
    WHEN 'sys_dictionary_item.item_name' THEN '字典项名称'
    WHEN 'sys_role.name' THEN '角色名称'
    WHEN 'sys_role.description' THEN '角色描述'
    WHEN 'sys_staff.name' THEN '员工姓名'
    WHEN 'sys_user_account.name' THEN '用户姓名'
    ELSE CASE target_column
      WHEN 'id' THEN '主键编号'
      WHEN 'address' THEN '详细地址'
      WHEN 'age' THEN '年龄'
      WHEN 'allow_member_discount' THEN '是否允许会员折扣'
      WHEN 'allow_split' THEN '是否允许拆零'
      WHEN 'amount' THEN '交易金额'
      WHEN 'approval_no' THEN '药品批准文号'
      WHEN 'audit_at' THEN '审核时间'
      WHEN 'audit_status' THEN '审核状态'
      WHEN 'balance' THEN '账户余额'
      WHEN 'balance_after' THEN '变动后余额'
      WHEN 'barcode' THEN '药品条形码'
      WHEN 'base_ratio' THEN '基本单位换算系数'
      WHEN 'base_unit' THEN '基本单位'
      WHEN 'batch_no' THEN '药品批次号'
      WHEN 'birthday' THEN '出生日期'
      WHEN 'business_no' THEN '关联业务单号'
      WHEN 'business_type' THEN '库存变动业务类型'
      WHEN 'card_no' THEN '患者卡号'
      WHEN 'cashier' THEN '收银员姓名'
      WHEN 'category' THEN '业务分类'
      WHEN 'change_quantity' THEN '库存变动数量'
      WHEN 'charge_type' THEN '收费类型'
      WHEN 'check_date' THEN '盘点日期'
      WHEN 'check_items' THEN '检查项目明细'
      WHEN 'check_no' THEN '盘点单号'
      WHEN 'check_status' THEN '盘点状态'
      WHEN 'chief_complaint' THEN '患者主诉'
      WHEN 'clinic_code' THEN '诊所编码'
      WHEN 'clinic_name' THEN '诊所名称'
      WHEN 'company' THEN '工作单位'
      WHEN 'contact_name' THEN '联系人姓名'
      WHEN 'content' THEN '内容详情'
      WHEN 'created_at' THEN '创建时间'
      WHEN 'creator' THEN '创建人'
      WHEN 'default_days' THEN '默认用药天数'
      WHEN 'default_total' THEN '默认药品总量'
      WHEN 'deleted' THEN '逻辑删除标志'
      WHEN 'department_code' THEN '科室编码'
      WHEN 'department_name' THEN '科室名称'
      WHEN 'description' THEN '说明描述'
      WHEN 'diagnosis' THEN '诊断结果'
      WHEN 'diagnosis_fee' THEN '诊疗费用'
      WHEN 'dict_type' THEN '字典类型'
      WHEN 'discount_amount' THEN '优惠金额'
      WHEN 'doctor_advice' THEN '医生医嘱'
      WHEN 'doctor_id' THEN '接诊医生编号'
      WHEN 'doctor_name' THEN '接诊医生姓名'
      WHEN 'dosage_form' THEN '药品剂型'
      WHEN 'dose_ratio' THEN '剂量单位换算系数'
      WHEN 'dose_unit' THEN '剂量单位'
      WHEN 'drug_code' THEN '药品编码'
      WHEN 'drug_id' THEN '药品编号'
      WHEN 'drug_name' THEN '药品名称'
      WHEN 'duration' THEN '请求耗时毫秒数'
      WHEN 'education' THEN '文化程度'
      WHEN 'email' THEN '电子邮箱'
      WHEN 'enabled' THEN '是否启用'
      WHEN 'error_message' THEN '错误信息'
      WHEN 'expire_date' THEN '到期日期'
      WHEN 'expiry_warning_days' THEN '有效期预警天数'
      WHEN 'fee_code' THEN '收费项目编码'
      WHEN 'frequency' THEN '默认用药频度'
      WHEN 'gender' THEN '性别'
      WHEN 'id_card' THEN '身份证号码'
      WHEN 'introduction' THEN '诊所简介'
      WHEN 'inventory_id' THEN '库存记录编号'
      WHEN 'invoice_item' THEN '发票项目'
      WHEN 'ip' THEN '请求网络地址'
      WHEN 'item_code' THEN '字典项编码'
      WHEN 'job' THEN '职业'
      WHEN 'job_no' THEN '员工工号'
      WHEN 'level_name' THEN '会员等级名称'
      WHEN 'license_no' THEN '医疗机构许可证号'
      WHEN 'location_no' THEN '库位编号'
      WHEN 'maker' THEN '制单人'
      WHEN 'manufacturer' THEN '生产厂家'
      WHEN 'marriage' THEN '婚姻状况'
      WHEN 'medical_record' THEN '病历详细内容'
      WHEN 'member_expire_date' THEN '会员到期日期'
      WHEN 'member_id' THEN '会员编号'
      WHEN 'member_level' THEN '会员等级'
      WHEN 'member_type' THEN '会员类型'
      WHEN 'message_type' THEN '消息类型'
      WHEN 'module' THEN '操作模块'
      WHEN 'name' THEN '名称'
      WHEN 'nation' THEN '民族'
      WHEN 'new_purchase_price' THEN '调整后采购价'
      WHEN 'new_sell_price' THEN '调整后销售价'
      WHEN 'old_purchase_price' THEN '调整前采购价'
      WHEN 'old_sell_price' THEN '调整前销售价'
      WHEN 'open_date' THEN '会员开卡日期'
      WHEN 'operation' THEN '操作名称'
      WHEN 'operator' THEN '操作员姓名'
      WHEN 'order_no' THEN '业务订单号'
      WHEN 'otc' THEN '是否为非处方药'
      WHEN 'pack_unit' THEN '包装单位'
      WHEN 'paid_amount' THEN '实收金额'
      WHEN 'paid_at' THEN '支付时间'
      WHEN 'password' THEN '登录密码'
      WHEN 'patient_code' THEN '患者编码'
      WHEN 'patient_id' THEN '患者编号'
      WHEN 'patient_name' THEN '患者姓名'
      WHEN 'pay_method' THEN '支付方式'
      WHEN 'permissions' THEN '角色权限集合'
      WHEN 'phone' THEN '联系电话'
      WHEN 'pinyin' THEN '药品拼音码'
      WHEN 'points' THEN '积分变动数量'
      WHEN 'points_after' THEN '变动后积分'
      WHEN 'position_name' THEN '岗位名称'
      WHEN 'prescription' THEN '处方内容'
      WHEN 'price' THEN '项目单价'
      WHEN 'principal' THEN '诊所负责人'
      WHEN 'project_code' THEN '检查项目编码'
      WHEN 'province_city' THEN '所在省市'
      WHEN 'purchase_amount' THEN '采购金额'
      WHEN 'purchase_price' THEN '药品采购价'
      WHEN 'quantity' THEN '库存数量'
      WHEN 'quantity_after' THEN '变动后库存数量'
      WHEN 'quantity_before' THEN '变动前库存数量'
      WHEN 'read_at' THEN '消息阅读时间'
      WHEN 'read_status' THEN '消息已读标志'
      WHEN 'reason' THEN '调价原因'
      WHEN 'receivable_amount' THEN '应收金额'
      WHEN 'receiver_id' THEN '消息接收人编号'
      WHEN 'refund_amount' THEN '退款金额'
      WHEN 'refund_method' THEN '退款方式'
      WHEN 'refunded_at' THEN '退款时间'
      WHEN 'region' THEN '所在地区'
      WHEN 'registration_fee' THEN '挂号费用'
      WHEN 'registration_id' THEN '挂号记录编号'
      WHEN 'registration_no' THEN '挂号单号'
      WHEN 'relation' THEN '与患者关系'
      WHEN 'remark' THEN '备注'
      WHEN 'request_method' THEN '请求方式'
      WHEN 'request_params' THEN '请求参数'
      WHEN 'request_uri' THEN '请求地址'
      WHEN 'result' THEN '操作结果'
      WHEN 'retail_amount' THEN '零售金额'
      WHEN 'role_code' THEN '角色编码'
      WHEN 'role_name' THEN '角色名称'
      WHEN 'scale' THEN '诊所规模'
      WHEN 'sell_price' THEN '药品销售价'
      WHEN 'setting_key' THEN '配置项键名'
      WHEN 'setting_value' THEN '配置项值'
      WHEN 'single_dose' THEN '默认单次用量'
      WHEN 'sort_no' THEN '排序序号'
      WHEN 'source' THEN '患者来源'
      WHEN 'specification' THEN '药品规格'
      WHEN 'status' THEN '业务状态'
      WHEN 'stock_direction' THEN '出入库方向'
      WHEN 'stock_max' THEN '库存上限'
      WHEN 'stock_min' THEN '库存下限'
      WHEN 'stock_type' THEN '出入库类型'
      WHEN 'supplier_code' THEN '供应商编码'
      WHEN 'supplier_id' THEN '供应商编号'
      WHEN 'supplier_name' THEN '供应商名称'
      WHEN 'template_code' THEN '模板编码'
      WHEN 'template_type' THEN '模板类型'
      WHEN 'title' THEN '消息标题'
      WHEN 'total_consume' THEN '累计消费金额'
      WHEN 'total_stored' THEN '累计储值金额'
      WHEN 'total_unit' THEN '默认总量单位'
      WHEN 'transaction_type' THEN '会员交易类型'
      WHEN 'unit' THEN '计量单位'
      WHEN 'updated_at' THEN '更新时间'
      WHEN 'usage_method' THEN '默认用药方法'
      WHEN 'username' THEN '登录账号'
      WHEN 'visit_time' THEN '计划就诊时间'
      WHEN 'visit_type' THEN '接诊类型'
      WHEN 'vital_signs' THEN '生命体征数据'
      WHEN 'warning_stock' THEN '库存预警值'
      ELSE '业务字段'
    END
  END;
END//

DROP FUNCTION IF EXISTS clinic_table_comment//
CREATE FUNCTION clinic_table_comment(target_table VARCHAR(128))
RETURNS VARCHAR(255)
DETERMINISTIC
BEGIN
  RETURN CASE target_table
    WHEN 'clinic_charge_order' THEN '收费订单表'
    WHEN 'clinic_check_project' THEN '检查项目表'
    WHEN 'clinic_consultation' THEN '接诊记录表'
    WHEN 'clinic_drug' THEN '药品基础信息表'
    WHEN 'clinic_family_member' THEN '患者家属信息表'
    WHEN 'clinic_fee_item' THEN '收费项目表'
    WHEN 'clinic_info' THEN '诊所基础信息表'
    WHEN 'clinic_inventory' THEN '药品库存表'
    WHEN 'clinic_inventory_check' THEN '库存盘点单表'
    WHEN 'clinic_inventory_log' THEN '库存变动日志表'
    WHEN 'clinic_medical_template' THEN '病历及处方模板表'
    WHEN 'clinic_member' THEN '会员信息表'
    WHEN 'clinic_member_transaction' THEN '会员账户交易记录表'
    WHEN 'clinic_patient' THEN '患者基础信息表'
    WHEN 'clinic_price_adjustment' THEN '药品调价记录表'
    WHEN 'clinic_registration' THEN '挂号记录表'
    WHEN 'clinic_setting' THEN '诊所系统配置表'
    WHEN 'clinic_stock_order' THEN '药品出入库单表'
    WHEN 'clinic_supplier' THEN '药品供应商表'
    WHEN 'sys_department' THEN '系统科室表'
    WHEN 'sys_dictionary_item' THEN '系统字典项表'
    WHEN 'sys_message' THEN '系统消息表'
    WHEN 'sys_operation_log' THEN '系统操作日志表'
    WHEN 'sys_role' THEN '系统角色表'
    WHEN 'sys_staff' THEN '诊所员工表'
    WHEN 'sys_trial_application' THEN '系统试用申请表'
    WHEN 'sys_user_account' THEN '系统用户账号表'
    ELSE '系统业务表'
  END;
END//

DROP PROCEDURE IF EXISTS add_clinic_schema_comments//
CREATE PROCEDURE add_clinic_schema_comments()
BEGIN
  DECLARE finished INT DEFAULT 0;
  DECLARE current_table VARCHAR(128);
  DECLARE current_column VARCHAR(128);
  DECLARE current_type TEXT;
  DECLARE current_nullable VARCHAR(3);
  DECLARE current_default TEXT;
  DECLARE current_extra TEXT;
  DECLARE current_comment TEXT;
  DECLARE current_table_comment TEXT;
  DECLARE ddl LONGTEXT;

  DECLARE column_cursor CURSOR FOR
    SELECT table_name, column_name, column_type, is_nullable, column_default, extra,
           clinic_column_comment(table_name, column_name)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND (table_name LIKE 'clinic\_%' OR table_name LIKE 'sys\_%')
    ORDER BY table_name, ordinal_position;
  DECLARE table_cursor CURSOR FOR
    SELECT table_name, clinic_table_comment(table_name)
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_type = 'BASE TABLE'
      AND (table_name LIKE 'clinic\_%' OR table_name LIKE 'sys\_%')
    ORDER BY table_name;
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET finished = 1;

  OPEN column_cursor;
  comment_loop: LOOP
    FETCH column_cursor INTO current_table, current_column, current_type, current_nullable,
      current_default, current_extra, current_comment;
    IF finished = 1 THEN
      LEAVE comment_loop;
    END IF;

    SET ddl = CONCAT(
      'ALTER TABLE `', current_table, '` MODIFY COLUMN `', current_column, '` ', current_type,
      IF(current_nullable = 'YES', ' NULL', ' NOT NULL'),
      CASE
        WHEN current_default IS NULL THEN ''
        WHEN UPPER(current_default) LIKE 'CURRENT_TIMESTAMP%' THEN CONCAT(' DEFAULT ', current_default)
        ELSE CONCAT(' DEFAULT ', QUOTE(current_default))
      END,
      IF(TRIM(REPLACE(current_extra, 'DEFAULT_GENERATED', '')) = '', '',
        CONCAT(' ', TRIM(REPLACE(current_extra, 'DEFAULT_GENERATED', '')))),
      ' COMMENT ', QUOTE(current_comment)
    );
    SET @schema_comment_ddl = ddl;
    PREPARE schema_comment_statement FROM @schema_comment_ddl;
    EXECUTE schema_comment_statement;
    DEALLOCATE PREPARE schema_comment_statement;
  END LOOP;
  CLOSE column_cursor;

  SET finished = 0;
  OPEN table_cursor;
  table_comment_loop: LOOP
    FETCH table_cursor INTO current_table, current_table_comment;
    IF finished = 1 THEN
      LEAVE table_comment_loop;
    END IF;
    SET @schema_comment_ddl = CONCAT(
      'ALTER TABLE `', current_table, '` COMMENT = ', QUOTE(current_table_comment)
    );
    PREPARE schema_comment_statement FROM @schema_comment_ddl;
    EXECUTE schema_comment_statement;
    DEALLOCATE PREPARE schema_comment_statement;
  END LOOP;
  CLOSE table_cursor;
END//
DELIMITER ;

CALL add_clinic_schema_comments();
DROP PROCEDURE add_clinic_schema_comments;
DROP FUNCTION clinic_table_comment;
DROP FUNCTION clinic_column_comment;

-- Normalize the member name-to-level mapping used by patient management.
UPDATE sys_dictionary_item
SET item_name = CASE item_code
  WHEN 'VIP1' THEN '初级会员'
  WHEN 'VIP2' THEN '高级会员'
  WHEN 'VIP3' THEN '白银会员'
  WHEN 'VIP4' THEN '黄金会员'
  WHEN 'VIP5' THEN '钻石会员'
  ELSE item_name
END,
updated_at = NOW()
WHERE dict_type = 'MEMBER_LEVEL'
  AND item_code IN ('VIP1', 'VIP2', 'VIP3', 'VIP4', 'VIP5')
  AND deleted = 0;

UPDATE clinic_member
SET member_type = '初级会员'
WHERE member_type = '普通会员' AND deleted = 0;

UPDATE clinic_member
SET level_name = CASE member_type
  WHEN '初级会员' THEN 'VIP1'
  WHEN '高级会员' THEN 'VIP2'
  WHEN '白银会员' THEN 'VIP3'
  WHEN '黄金会员' THEN 'VIP4'
  WHEN '钻石会员' THEN 'VIP5'
  ELSE level_name
END
WHERE deleted = 0;

UPDATE clinic_patient p
LEFT JOIN clinic_member m ON m.patient_id = p.id AND m.deleted = 0
SET p.member_level = CASE COALESCE(m.member_type, p.member_level)
  WHEN '普通会员' THEN 'VIP1'
  WHEN '初级会员' THEN 'VIP1'
  WHEN '高级会员' THEN 'VIP2'
  WHEN '白银会员' THEN 'VIP3'
  WHEN '黄金会员' THEN 'VIP4'
  WHEN '钻石会员' THEN 'VIP5'
  ELSE p.member_level
END
WHERE p.deleted = 0;

-- Ensure every enabled department has at least one enabled doctor.
UPDATE sys_staff
SET job_no = COALESCE(job_no, 'ST001'),
    role_name = '医生',
    enabled = 1,
    updated_at = NOW()
WHERE id = 1
  AND department_name = '全科'
  AND deleted = 0;

INSERT INTO sys_staff
  (job_no, name, gender, age, phone, email, id_card, position_name, clinic_name,
   department_name, role_name, address, creator, enabled, deleted, created_at, updated_at)
VALUES
  ('ST009', '张医生', '男', 41, '13800001009', 'surgery@clinic.cn', '450100198501010099', '主治医师', '康宁诊所', '外科', '医生', '南宁市青秀区', '系统管理员', 1, 0, NOW(), NOW()),
  ('ST010', '刘医生', '女', 37, '13800001010', 'gynecology@clinic.cn', '450100198901010100', '主治医师', '康宁诊所', '妇科', '医生', '南宁市青秀区', '系统管理员', 1, 0, NOW(), NOW()),
  ('ST011', '黄医生', '男', 46, '13800001011', 'tcm@clinic.cn', '450100198001010111', '副主任医师', '康宁诊所', '中医科', '医生', '南宁市青秀区', '系统管理员', 1, 0, NOW(), NOW()),
  ('ST012', '何医生', '女', 34, '13800001012', 'dental@clinic.cn', '450100199201010122', '主治医师', '康宁诊所', '口腔科', '医生', '南宁市青秀区', '系统管理员', 1, 0, NOW(), NOW()),
  ('ST013', '孙医生', '男', 39, '13800001013', 'laboratory@clinic.cn', '450100198701010133', '主治医师', '康宁诊所', '检验科', '医生', '南宁市青秀区', '系统管理员', 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  position_name = VALUES(position_name),
  department_name = VALUES(department_name),
  role_name = VALUES(role_name),
  enabled = VALUES(enabled),
  updated_at = NOW(),
  deleted = 0;

-- Link charge orders to registrations so registration details can return payment data.
SET @registration_id_column_exists = (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'clinic_charge_order'
    AND COLUMN_NAME = 'registration_id'
);
SET @registration_id_ddl = IF(
  @registration_id_column_exists = 0,
  'ALTER TABLE clinic_charge_order ADD COLUMN registration_id BIGINT NULL COMMENT ''关联挂号记录编号'' AFTER order_no, ADD INDEX idx_charge_registration_id (registration_id)',
  'SELECT 1'
);
PREPARE registration_id_statement FROM @registration_id_ddl;
EXECUTE registration_id_statement;
DEALLOCATE PREPARE registration_id_statement;

UPDATE clinic_charge_order c
JOIN clinic_registration r ON r.id = c.id AND r.deleted = 0
SET c.registration_id = r.id
WHERE c.registration_id IS NULL
  AND c.deleted = 0;

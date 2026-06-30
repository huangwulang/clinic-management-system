SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- This script is idempotent for the demo keys below. It keeps user-created rows intact.

INSERT INTO sys_department
  (id, department_code, name, description, creator, enabled, deleted, created_at, updated_at)
VALUES
  (1, 'DEP001', '全科', '常见病综合诊疗', '系统管理员', 1, 0, NOW(), NOW()),
  (2, 'DEP002', '内科', '内科疾病诊疗', '系统管理员', 1, 0, NOW(), NOW()),
  (3, 'DEP003', '外科', '外科疾病诊疗', '系统管理员', 1, 0, NOW(), NOW()),
  (4, 'DEP004', '儿科', '儿童常见病诊疗', '系统管理员', 1, 0, NOW(), NOW()),
  (5, 'DEP005', '妇科', '妇科疾病诊疗', '系统管理员', 1, 0, NOW(), NOW()),
  (6, 'DEP006', '中医科', '中医辨证诊疗', '系统管理员', 1, 0, NOW(), NOW()),
  (7, 'DEP007', '口腔科', '口腔疾病诊疗', '系统管理员', 1, 0, NOW(), NOW()),
  (8, 'DEP008', '检验科', '医学检验与检查', '系统管理员', 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name), updated_at = NOW(), deleted = 0;

INSERT INTO sys_role
  (id, role_code, name, description, permissions, creator, enabled, deleted, created_at, updated_at)
VALUES
  (1, 'ADMIN', '系统管理员', '拥有全部系统权限', '["*"]', '系统管理员', 1, 0, NOW(), NOW()),
  (2, 'DOCTOR', '医生', '接诊和病历权限', '["consultation","patient","registration"]', '系统管理员', 1, 0, NOW(), NOW()),
  (3, 'NURSE', '护士', '护理与患者管理权限', '["patient","registration"]', '系统管理员', 1, 0, NOW(), NOW()),
  (4, 'CASHIER', '收费员', '收费退费权限', '["charge","registration"]', '系统管理员', 1, 0, NOW(), NOW()),
  (5, 'PHARMACIST', '药剂师', '药品和库存权限', '["drug","inventory"]', '系统管理员', 1, 0, NOW(), NOW()),
  (6, 'WAREHOUSE', '库管员', '出入库和盘点权限', '["stock","inventory"]', '系统管理员', 1, 0, NOW(), NOW()),
  (7, 'OPERATOR', '运营人员', '报表查看权限', '["statistics","dashboard"]', '系统管理员', 1, 0, NOW(), NOW()),
  (8, 'RECEPTION', '前台', '挂号和患者建档权限', '["registration","patient"]', '系统管理员', 1, 0, NOW(), NOW()),
  (9, 'USER', '普通用户', '默认可进入首页', '["dashboard"]', '系统管理员', 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name), permissions = VALUES(permissions), updated_at = NOW(), deleted = 0;

INSERT INTO sys_staff
  (id, job_no, name, gender, age, phone, email, id_card, position_name, clinic_name,
   department_name, role_name, address, creator, enabled, deleted, created_at, updated_at)
VALUES
  (1, 'ST001', '王医生', '男', 38, '13800001001', 'doctor1@clinic.cn', '450100198801010011', '主治医师', '康宁诊所', '全科', '医生', '南宁市青秀区', '系统管理员', 1, 0, NOW(), NOW()),
  (2, 'ST002', '林医生', '女', 35, '13800001002', 'doctor2@clinic.cn', '450100199101010022', '主治医师', '康宁诊所', '内科', '医生', '南宁市青秀区', '系统管理员', 1, 0, NOW(), NOW()),
  (3, 'ST003', '李医生', '男', 42, '13800001003', 'doctor3@clinic.cn', '450100198401010033', '副主任医师', '康宁诊所', '儿科', '医生', '南宁市西乡塘区', '系统管理员', 1, 0, NOW(), NOW()),
  (4, 'ST004', '陈护士', '女', 29, '13800001004', 'nurse@clinic.cn', '450100199701010044', '护士', '康宁诊所', '全科', '护士', '南宁市江南区', '系统管理员', 1, 0, NOW(), NOW()),
  (5, 'ST005', '赵收银', '女', 31, '13800001005', 'cashier@clinic.cn', '450100199501010055', '收费员', '康宁诊所', '全科', '收费员', '南宁市青秀区', '系统管理员', 1, 0, NOW(), NOW()),
  (6, 'ST006', '周药师', '男', 36, '13800001006', 'drug@clinic.cn', '450100199001010066', '药剂师', '康宁诊所', '检验科', '药剂师', '南宁市良庆区', '系统管理员', 1, 0, NOW(), NOW()),
  (7, 'ST007', '吴库管', '男', 40, '13800001007', 'stock@clinic.cn', '450100198601010077', '库管员', '康宁诊所', '检验科', '库管员', '南宁市兴宁区', '系统管理员', 1, 0, NOW(), NOW()),
  (8, 'ST008', '郑前台', '女', 27, '13800001008', 'front@clinic.cn', '450100199901010088', '前台', '康宁诊所', '全科', '前台', '南宁市青秀区', '系统管理员', 1, 0, NOW(), NOW()),
  (9, 'ST009', '张医生', '男', 41, '13800001009', 'surgery@clinic.cn', '450100198501010099', '主治医师', '康宁诊所', '外科', '医生', '南宁市青秀区', '系统管理员', 1, 0, NOW(), NOW()),
  (10, 'ST010', '刘医生', '女', 37, '13800001010', 'gynecology@clinic.cn', '450100198901010100', '主治医师', '康宁诊所', '妇科', '医生', '南宁市青秀区', '系统管理员', 1, 0, NOW(), NOW()),
  (11, 'ST011', '黄医生', '男', 46, '13800001011', 'tcm@clinic.cn', '450100198001010111', '副主任医师', '康宁诊所', '中医科', '医生', '南宁市青秀区', '系统管理员', 1, 0, NOW(), NOW()),
  (12, 'ST012', '何医生', '女', 34, '13800001012', 'dental@clinic.cn', '450100199201010122', '主治医师', '康宁诊所', '口腔科', '医生', '南宁市青秀区', '系统管理员', 1, 0, NOW(), NOW()),
  (13, 'ST013', '孙医生', '男', 39, '13800001013', 'laboratory@clinic.cn', '450100198701010133', '主治医师', '康宁诊所', '检验科', '医生', '南宁市青秀区', '系统管理员', 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  position_name = VALUES(position_name),
  department_name = VALUES(department_name),
  role_name = VALUES(role_name),
  enabled = VALUES(enabled),
  updated_at = NOW(),
  deleted = 0;

INSERT INTO sys_user_account
  (id, username, password, name, phone, role_name, enabled, deleted, created_at, updated_at)
VALUES
  (1, 'admin', 'cdf4a007e2b02a0c49fc9b7ccfbb8a10c644f635e1765dcf2a7ab794ddc7edac', '系统管理员', '13800000001', 'ADMIN', 1, 0, NOW(), NOW()),
  (2, 'doctor1', 'cdf4a007e2b02a0c49fc9b7ccfbb8a10c644f635e1765dcf2a7ab794ddc7edac', '王医生', '13800001001', 'DOCTOR', 1, 0, NOW(), NOW()),
  (3, 'doctor2', 'cdf4a007e2b02a0c49fc9b7ccfbb8a10c644f635e1765dcf2a7ab794ddc7edac', '林医生', '13800001002', 'DOCTOR', 1, 0, NOW(), NOW()),
  (4, 'nurse1', 'cdf4a007e2b02a0c49fc9b7ccfbb8a10c644f635e1765dcf2a7ab794ddc7edac', '陈护士', '13800001004', 'NURSE', 1, 0, NOW(), NOW()),
  (5, 'cashier1', 'cdf4a007e2b02a0c49fc9b7ccfbb8a10c644f635e1765dcf2a7ab794ddc7edac', '赵收银', '13800001005', 'CASHIER', 1, 0, NOW(), NOW()),
  (6, 'pharmacist1', 'cdf4a007e2b02a0c49fc9b7ccfbb8a10c644f635e1765dcf2a7ab794ddc7edac', '周药师', '13800001006', 'PHARMACIST', 1, 0, NOW(), NOW()),
  (7, 'warehouse1', 'cdf4a007e2b02a0c49fc9b7ccfbb8a10c644f635e1765dcf2a7ab794ddc7edac', '吴库管', '13800001007', 'WAREHOUSE', 1, 0, NOW(), NOW()),
  (8, 'reception1', 'cdf4a007e2b02a0c49fc9b7ccfbb8a10c644f635e1765dcf2a7ab794ddc7edac', '郑前台', '13800001008', 'RECEPTION', 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE password = VALUES(password), name = VALUES(name), role_name = VALUES(role_name), enabled = 1, updated_at = NOW(), deleted = 0;

INSERT INTO sys_dictionary_item
  (id, dict_type, item_code, item_name, sort_no, enabled, remark, deleted, created_at, updated_at)
VALUES
  (1, 'MEMBER_LEVEL', 'VIP1', '初级会员', 1, 1, '折扣9.8折', 0, NOW(), NOW()),
  (2, 'MEMBER_LEVEL', 'VIP2', '高级会员', 2, 1, '折扣9.5折', 0, NOW(), NOW()),
  (3, 'MEMBER_LEVEL', 'VIP3', '白银会员', 3, 1, '折扣9.0折', 0, NOW(), NOW()),
  (4, 'MEMBER_LEVEL', 'VIP4', '黄金会员', 4, 1, '折扣8.8折', 0, NOW(), NOW()),
  (5, 'MEMBER_LEVEL', 'VIP5', '钻石会员', 5, 1, '折扣8.5折', 0, NOW(), NOW()),
  (6, 'DRUG_CATEGORY', 'WESTERN', '西药', 1, 1, '药品分类', 0, NOW(), NOW()),
  (7, 'DRUG_CATEGORY', 'CHINESE', '中成药', 2, 1, '药品分类', 0, NOW(), NOW()),
  (8, 'VISIT_TYPE', 'FOLLOW_UP', '复诊', 1, 1, '就诊类型', 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE item_name = VALUES(item_name), updated_at = NOW(), deleted = 0;

INSERT INTO clinic_info
  (id, clinic_code, clinic_name, phone, email, region, address, license_no, principal, introduction, deleted, created_at, updated_at)
VALUES
  (1, 'CL001', '康宁社区诊所', '0771-5501001', 'clinic1@example.com', '广西南宁', '青秀区民族大道101号', 'YL001001', '李明', '社区综合门诊', 0, NOW(), NOW()),
  (2, 'CL002', '康宁内科门诊', '0771-5501002', 'clinic2@example.com', '广西南宁', '青秀区金湖路22号', 'YL001002', '王芳', '内科专科门诊', 0, NOW(), NOW()),
  (3, 'CL003', '康宁儿科门诊', '0771-5501003', 'clinic3@example.com', '广西南宁', '西乡塘区大学路33号', 'YL001003', '张华', '儿童健康门诊', 0, NOW(), NOW()),
  (4, 'CL004', '康宁中医门诊', '0771-5501004', 'clinic4@example.com', '广西南宁', '兴宁区朝阳路44号', 'YL001004', '陈杰', '中医综合门诊', 0, NOW(), NOW()),
  (5, 'CL005', '康宁口腔门诊', '0771-5501005', 'clinic5@example.com', '广西南宁', '江南区星光大道55号', 'YL001005', '赵敏', '口腔健康门诊', 0, NOW(), NOW()),
  (6, 'CL006', '康宁妇科门诊', '0771-5501006', 'clinic6@example.com', '广西南宁', '良庆区五象大道66号', 'YL001006', '周静', '妇女健康门诊', 0, NOW(), NOW()),
  (7, 'CL007', '康宁外科门诊', '0771-5501007', 'clinic7@example.com', '广西南宁', '邕宁区蒲津路77号', 'YL001007', '吴强', '外科门诊', 0, NOW(), NOW()),
  (8, 'CL008', '康宁健康中心', '0771-5501008', 'clinic8@example.com', '广西南宁', '青秀区凤岭路88号', 'YL001008', '郑洁', '健康管理中心', 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE clinic_name = VALUES(clinic_name), updated_at = NOW(), deleted = 0;

INSERT INTO clinic_setting
  (id, setting_key, setting_value, description, deleted, created_at, updated_at)
VALUES
  (1, 'clinic.businessHours', '08:00-20:00', '营业时间', 0, NOW(), NOW()),
  (2, 'registration.defaultFee', '20.00', '默认挂号费', 0, NOW(), NOW()),
  (3, 'registration.defaultDiagnosisFee', '30.00', '默认诊疗费', 0, NOW(), NOW()),
  (4, 'member.enabled', 'true', '是否启用会员功能', 0, NOW(), NOW()),
  (5, 'inventory.warningEnabled', 'true', '是否启用库存预警', 0, NOW(), NOW()),
  (6, 'sms.enabled', 'false', '是否启用短信通知', 0, NOW(), NOW()),
  (7, 'receipt.title', '康宁诊所收费票据', '票据标题', 0, NOW(), NOW()),
  (8, 'system.locale', 'zh-CN', '系统语言', 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value), updated_at = NOW(), deleted = 0;

INSERT INTO clinic_supplier
  (id, supplier_code, name, contact_name, phone, status, creator, remark, deleted, created_at, updated_at)
VALUES
  (1, 'SUP001', '广西医药有限公司', '黄经理', '13900002001', 'ENABLED', '系统管理员', '常用西药供应商', 0, NOW(), NOW()),
  (2, 'SUP002', '华南制药配送中心', '林经理', '13900002002', 'ENABLED', '系统管理员', '综合药品供应商', 0, NOW(), NOW()),
  (3, 'SUP003', '桂中药业有限公司', '韦经理', '13900002003', 'ENABLED', '系统管理员', '中成药供应商', 0, NOW(), NOW()),
  (4, 'SUP004', '康源医疗器械公司', '陈经理', '13900002004', 'ENABLED', '系统管理员', '耗材供应商', 0, NOW(), NOW()),
  (5, 'SUP005', '仁和医药配送公司', '赵经理', '13900002005', 'ENABLED', '系统管理员', '药品配送', 0, NOW(), NOW()),
  (6, 'SUP006', '同济药品批发公司', '周经理', '13900002006', 'ENABLED', '系统管理员', '常温药品', 0, NOW(), NOW()),
  (7, 'SUP007', '安康冷链医药公司', '吴经理', '13900002007', 'ENABLED', '系统管理员', '冷链药品', 0, NOW(), NOW()),
  (8, 'SUP008', '百草中药饮片公司', '郑经理', '13900002008', 'ENABLED', '系统管理员', '中药饮片', 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name), updated_at = NOW(), deleted = 0;

INSERT INTO clinic_drug
  (id, drug_code, name, specification, category, dosage_form, unit, purchase_price, sell_price,
   manufacturer, approval_no, status, warning_stock, deleted, created_at, updated_at)
VALUES
  (1, 'DRG001', '阿莫西林胶囊', '0.25g*24粒', '抗生素', '胶囊剂', '盒', 8.50, 15.00, '广西医药有限公司', '国药准字H45000001', 'ENABLED', 30, 0, NOW(), NOW()),
  (2, 'DRG002', '布洛芬缓释胶囊', '0.3g*20粒', '解热镇痛', '胶囊剂', '盒', 12.00, 22.00, '华南制药配送中心', '国药准字H45000002', 'ENABLED', 25, 0, NOW(), NOW()),
  (3, 'DRG003', '复方感冒灵颗粒', '10g*9袋', '感冒用药', '颗粒剂', '盒', 9.00, 18.00, '桂中药业有限公司', '国药准字Z45000003', 'ENABLED', 30, 0, NOW(), NOW()),
  (4, 'DRG004', '蒙脱石散', '3g*10袋', '消化系统', '散剂', '盒', 10.00, 19.50, '仁和医药配送公司', '国药准字H45000004', 'ENABLED', 20, 0, NOW(), NOW()),
  (5, 'DRG005', '氯雷他定片', '10mg*12片', '抗过敏', '片剂', '盒', 6.50, 13.00, '同济药品批发公司', '国药准字H45000005', 'ENABLED', 20, 0, NOW(), NOW()),
  (6, 'DRG006', '维生素C片', '0.1g*100片', '维生素', '片剂', '瓶', 5.00, 10.00, '华南制药配送中心', '国药准字H45000006', 'ENABLED', 40, 0, NOW(), NOW()),
  (7, 'DRG007', '板蓝根颗粒', '10g*20袋', '中成药', '颗粒剂', '包', 11.00, 21.00, '桂中药业有限公司', '国药准字Z45000007', 'ENABLED', 25, 0, NOW(), NOW()),
  (8, 'DRG008', '生理氯化钠注射液', '250ml', '注射用药', '注射剂', '瓶', 3.20, 8.00, '安康冷链医药公司', '国药准字H45000008', 'ENABLED', 50, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name), sell_price = VALUES(sell_price), updated_at = NOW(), deleted = 0;

INSERT INTO clinic_inventory
  (id, drug_id, drug_code, drug_name, location_no, batch_no, category, specification, dosage_form,
   manufacturer, quantity, warning_stock, unit, purchase_amount, retail_amount, expire_date, deleted, created_at, updated_at)
VALUES
  (1, 1, 'DRG001', '阿莫西林胶囊', 'A-01-01', 'B20260601', '抗生素', '0.25g*24粒', '胶囊剂', '广西医药有限公司', 120, 30, '盒', 1020.00, 1800.00, DATE_ADD(CURDATE(), INTERVAL 18 MONTH), 0, NOW(), NOW()),
  (2, 2, 'DRG002', '布洛芬缓释胶囊', 'A-01-02', 'B20260602', '解热镇痛', '0.3g*20粒', '胶囊剂', '华南制药配送中心', 86, 25, '盒', 1032.00, 1892.00, DATE_ADD(CURDATE(), INTERVAL 20 MONTH), 0, NOW(), NOW()),
  (3, 3, 'DRG003', '复方感冒灵颗粒', 'A-01-03', 'B20260603', '感冒用药', '10g*9袋', '颗粒剂', '桂中药业有限公司', 95, 30, '盒', 855.00, 1710.00, DATE_ADD(CURDATE(), INTERVAL 16 MONTH), 0, NOW(), NOW()),
  (4, 4, 'DRG004', '蒙脱石散', 'A-02-01', 'B20260604', '消化系统', '3g*10袋', '散剂', '仁和医药配送公司', 18, 20, '盒', 180.00, 351.00, DATE_ADD(CURDATE(), INTERVAL 14 MONTH), 0, NOW(), NOW()),
  (5, 5, 'DRG005', '氯雷他定片', 'A-02-02', 'B20260605', '抗过敏', '10mg*12片', '片剂', '同济药品批发公司', 72, 20, '盒', 468.00, 936.00, DATE_ADD(CURDATE(), INTERVAL 22 MONTH), 0, NOW(), NOW()),
  (6, 6, 'DRG006', '维生素C片', 'A-02-03', 'B20260606', '维生素', '0.1g*100片', '片剂', '华南制药配送中心', 150, 40, '瓶', 750.00, 1500.00, DATE_ADD(CURDATE(), INTERVAL 24 MONTH), 0, NOW(), NOW()),
  (7, 7, 'DRG007', '板蓝根颗粒', 'B-01-01', 'B20260607', '中成药', '10g*20袋', '颗粒剂', '桂中药业有限公司', 22, 25, '包', 242.00, 462.00, DATE_ADD(CURDATE(), INTERVAL 12 MONTH), 0, NOW(), NOW()),
  (8, 8, 'DRG008', '生理氯化钠注射液', 'C-01-01', 'B20260608', '注射用药', '250ml', '注射剂', '安康冷链医药公司', 200, 50, '瓶', 640.00, 1600.00, DATE_ADD(CURDATE(), INTERVAL 10 MONTH), 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE quantity = VALUES(quantity), warning_stock = VALUES(warning_stock), updated_at = NOW(), deleted = 0;

INSERT INTO clinic_patient
  (id, patient_code, name, card_no, age, birthday, gender, phone, id_card, source, member_level,
   member_expire_date, nation, marriage, education, province_city, address, job, company, remark,
   operator, deleted, created_at, updated_at)
VALUES
  (1, 'PAT001', '张伟', 'MC2026001', 32, '1994-03-12', '男', '13600003001', '450100199403120011', '朋友介绍', 'VIP1', DATE_ADD(CURDATE(), INTERVAL 1 YEAR), '汉族', '已婚', '本科', '广西南宁', '青秀区民族大道', '工程师', '广西科技公司', '无特殊病史', '郑前台', 0, NOW() - INTERVAL 7 DAY, NOW()),
  (2, 'PAT002', '李娜', 'MC2026002', 28, '1998-07-21', '女', '13600003002', '450100199807210022', '线上预约', 'VIP2', DATE_ADD(CURDATE(), INTERVAL 1 YEAR), '汉族', '未婚', '本科', '广西南宁', '西乡塘区大学路', '教师', '南宁第二中学', '青霉素过敏', '郑前台', 0, NOW() - INTERVAL 6 DAY, NOW()),
  (3, 'PAT003', '王强', 'MC2026003', 45, '1981-11-05', '男', '13600003003', '450100198111050033', '路过', 'VIP3', DATE_ADD(CURDATE(), INTERVAL 1 YEAR), '壮族', '已婚', '大专', '广西南宁', '江南区星光大道', '司机', '南宁运输公司', '高血压史', '郑前台', 0, NOW() - INTERVAL 5 DAY, NOW()),
  (4, 'PAT004', '陈敏', 'MC2026004', 36, '1990-02-18', '女', '13600003004', '450100199002180044', '广告', 'VIP1', DATE_ADD(CURDATE(), INTERVAL 1 YEAR), '汉族', '已婚', '本科', '广西南宁', '良庆区五象大道', '会计', '广西商贸公司', '无', '郑前台', 0, NOW() - INTERVAL 4 DAY, NOW()),
  (5, 'PAT005', '刘洋', 'MC2026005', 19, '2007-09-10', '男', '13600003005', '450100200709100055', '朋友介绍', 'VIP2', DATE_ADD(CURDATE(), INTERVAL 1 YEAR), '汉族', '未婚', '高中', '广西南宁', '兴宁区朝阳路', '学生', '广西大学', '无', '郑前台', 0, NOW() - INTERVAL 3 DAY, NOW()),
  (6, 'PAT006', '赵静', 'MC2026006', 52, '1974-05-26', '女', '13600003006', '450100197405260066', '线上预约', 'VIP4', DATE_ADD(CURDATE(), INTERVAL 1 YEAR), '汉族', '已婚', '高中', '广西南宁', '青秀区凤岭路', '个体经营', '凤岭商店', '糖尿病史', '郑前台', 0, NOW() - INTERVAL 2 DAY, NOW()),
  (7, 'PAT007', '周杰', 'MC2026007', 8, '2018-01-15', '男', '13600003007', '450100201801150077', '朋友介绍', 'VIP1', DATE_ADD(CURDATE(), INTERVAL 1 YEAR), '壮族', '未婚', '小学', '广西南宁', '邕宁区蒲津路', '学生', '蒲津小学', '由监护人陪同', '郑前台', 0, NOW() - INTERVAL 1 DAY, NOW()),
  (8, 'PAT008', '吴芳', 'MC2026008', 67, '1959-08-30', '女', '13600003008', '450100195908300088', '社区推荐', 'VIP5', DATE_ADD(CURDATE(), INTERVAL 1 YEAR), '汉族', '已婚', '初中', '广西南宁', '青秀区东葛路', '退休', '无', '冠心病史', '郑前台', 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name), phone = VALUES(phone), updated_at = NOW(), deleted = 0;

INSERT INTO clinic_family_member
  (id, patient_id, relation, name, gender, age, company, birthday, phone, deleted, created_at, updated_at)
VALUES
  (1, 1, '配偶', '孙丽', '女', 31, '南宁银行', '1995-04-11', '13700004001', 0, NOW(), NOW()),
  (2, 2, '父亲', '李建国', '男', 57, '退休', '1969-06-20', '13700004002', 0, NOW(), NOW()),
  (3, 3, '配偶', '何梅', '女', 43, '广西商贸公司', '1983-03-08', '13700004003', 0, NOW(), NOW()),
  (4, 4, '母亲', '陈秀兰', '女', 62, '退休', '1964-10-16', '13700004004', 0, NOW(), NOW()),
  (5, 5, '父亲', '刘建华', '男', 48, '南宁运输公司', '1978-12-02', '13700004005', 0, NOW(), NOW()),
  (6, 6, '女儿', '赵欣', '女', 25, '广西大学', '2001-02-14', '13700004006', 0, NOW(), NOW()),
  (7, 7, '母亲', '周敏', '女', 34, '南宁小学', '1992-07-07', '13700004007', 0, NOW(), NOW()),
  (8, 8, '儿子', '吴军', '男', 39, '广西电力公司', '1987-09-19', '13700004008', 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name), phone = VALUES(phone), updated_at = NOW(), deleted = 0;

INSERT INTO clinic_member
  (id, patient_id, card_no, patient_name, phone, member_type, level_name, total_consume, balance,
   total_stored, points, open_date, expire_date, status, deleted, created_at, updated_at)
VALUES
  (1, 1, 'MC2026001', '张伟', '13600003001', '初级会员', 'VIP1', 680.00, 320.00, 1000.00, 680, CURDATE() - INTERVAL 90 DAY, CURDATE() + INTERVAL 275 DAY, 'ENABLED', 0, NOW(), NOW()),
  (2, 2, 'MC2026002', '李娜', '13600003002', '初级会员', 'VIP1', 1280.00, 720.00, 2000.00, 1280, CURDATE() - INTERVAL 80 DAY, CURDATE() + INTERVAL 285 DAY, 'ENABLED', 0, NOW(), NOW()),
  (3, 3, 'MC2026003', '王强', '13600003003', '高级会员', 'VIP2', 3680.00, 1320.00, 5000.00, 3680, CURDATE() - INTERVAL 70 DAY, CURDATE() + INTERVAL 295 DAY, 'ENABLED', 0, NOW(), NOW()),
  (4, 4, 'MC2026004', '陈敏', '13600003004', '初级会员', 'VIP1', 860.00, 140.00, 1000.00, 860, CURDATE() - INTERVAL 60 DAY, CURDATE() + INTERVAL 305 DAY, 'ENABLED', 0, NOW(), NOW()),
  (5, 5, 'MC2026005', '刘洋', '13600003005', '初级会员', 'VIP1', 1540.00, 460.00, 2000.00, 1540, CURDATE() - INTERVAL 50 DAY, CURDATE() + INTERVAL 315 DAY, 'ENABLED', 0, NOW(), NOW()),
  (6, 6, 'MC2026006', '赵静', '13600003006', '白银会员', 'VIP3', 6250.00, 1750.00, 8000.00, 6250, CURDATE() - INTERVAL 40 DAY, CURDATE() + INTERVAL 325 DAY, 'ENABLED', 0, NOW(), NOW()),
  (7, 7, 'MC2026007', '周杰', '13600003007', '初级会员', 'VIP1', 420.00, 580.00, 1000.00, 420, CURDATE() - INTERVAL 30 DAY, CURDATE() + INTERVAL 335 DAY, 'ENABLED', 0, NOW(), NOW()),
  (8, 8, 'MC2026008', '吴芳', '13600003008', '黄金会员', 'VIP4', 9800.00, 2200.00, 12000.00, 9800, CURDATE(), CURDATE() + INTERVAL 365 DAY, 'ENABLED', 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE balance = VALUES(balance), points = VALUES(points), open_date = VALUES(open_date),
  expire_date = VALUES(expire_date), updated_at = NOW(), deleted = 0;

INSERT INTO clinic_member_transaction
  (id, member_id, transaction_type, amount, points, balance_after, points_after, business_no, remark, operator, deleted, created_at, updated_at)
VALUES
  (1, 1, 'RECHARGE', 500.00, 0, 320.00, 680, 'MT202606001', '会员充值', '赵收银', 0, NOW() - INTERVAL 7 DAY, NOW()),
  (2, 2, 'CONSUME', -120.00, 120, 720.00, 1280, 'MT202606002', '门诊消费', '赵收银', 0, NOW() - INTERVAL 6 DAY, NOW()),
  (3, 3, 'RECHARGE', 1000.00, 0, 1320.00, 3680, 'MT202606003', '会员充值', '赵收银', 0, NOW() - INTERVAL 5 DAY, NOW()),
  (4, 4, 'CONSUME', -86.00, 86, 140.00, 860, 'MT202606004', '药品消费', '赵收银', 0, NOW() - INTERVAL 4 DAY, NOW()),
  (5, 5, 'POINTS_ADJUST', 0.00, 100, 460.00, 1540, 'MT202606005', '积分奖励', '系统管理员', 0, NOW() - INTERVAL 3 DAY, NOW()),
  (6, 6, 'CONSUME', -250.00, 250, 1750.00, 6250, 'MT202606006', '检查消费', '赵收银', 0, NOW() - INTERVAL 2 DAY, NOW()),
  (7, 7, 'RECHARGE', 300.00, 0, 580.00, 420, 'MT202606007', '会员充值', '赵收银', 0, NOW() - INTERVAL 1 DAY, NOW()),
  (8, 8, 'REFUND', 80.00, -80, 2200.00, 9800, 'MT202606008', '门诊退费', '赵收银', 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE remark = VALUES(remark), updated_at = NOW(), deleted = 0;

INSERT INTO clinic_check_project
  (id, project_code, name, category, cost_price, retail_price, price, unit, invoice_item, body_part, allow_member_discount, status, creator, remark, deleted, created_at, updated_at)
VALUES
  (1, 'CHK001', '血常规', '检验', 12.00, 35.00, 35.00, '次', '检查费', '血液', 1, 'ENABLED', '系统管理员', '空腹非必需', 0, NOW(), NOW()),
  (2, 'CHK002', '尿常规', '检验', 8.00, 25.00, 25.00, '次', '检查费', '尿液', 1, 'ENABLED', '系统管理员', '留取中段尿', 0, NOW(), NOW()),
  (3, 'CHK003', '血糖检测', '检验', 6.00, 20.00, 20.00, '次', '检查费', '血液', 1, 'ENABLED', '系统管理员', '建议空腹', 0, NOW(), NOW()),
  (4, 'CHK004', '心电图', '功能检查', 18.00, 60.00, 60.00, '次', '检查费', '心脏', 1, 'ENABLED', '系统管理员', '静息心电图', 0, NOW(), NOW()),
  (5, 'CHK005', '腹部彩超', '超声检查', 70.00, 180.00, 180.00, '次', '检查费', '腹部', 1, 'ENABLED', '系统管理员', '需空腹', 0, NOW(), NOW()),
  (6, 'CHK006', '胸部DR', '影像检查', 45.00, 120.00, 120.00, '次', '检查费', '胸部', 1, 'ENABLED', '系统管理员', '去除金属物品', 0, NOW(), NOW()),
  (7, 'CHK007', 'C反应蛋白', '检验', 20.00, 50.00, 50.00, '次', '检查费', '血液', 1, 'ENABLED', '系统管理员', '炎症指标', 0, NOW(), NOW()),
  (8, 'CHK008', '肝功能八项', '检验', 35.00, 95.00, 95.00, '次', '检查费', '肝功能', 1, 'ENABLED', '系统管理员', '需空腹', 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name), retail_price = VALUES(retail_price), price = VALUES(price), updated_at = NOW(), deleted = 0;

INSERT INTO clinic_fee_item
  (id, fee_code, name, category, price, unit, status, remark, deleted, created_at, updated_at)
VALUES
  (1, 'FEE001', '普通挂号费', '挂号费', 20.00, '次', 'ENABLED', '普通门诊', 0, NOW(), NOW()),
  (2, 'FEE002', '专家挂号费', '挂号费', 50.00, '次', 'ENABLED', '专家门诊', 0, NOW(), NOW()),
  (3, 'FEE003', '诊疗费', '诊疗费', 30.00, '次', 'ENABLED', '基础诊疗', 0, NOW(), NOW()),
  (4, 'FEE004', '注射费', '治疗费', 15.00, '次', 'ENABLED', '肌肉注射', 0, NOW(), NOW()),
  (5, 'FEE005', '输液费', '治疗费', 35.00, '次', 'ENABLED', '静脉输液', 0, NOW(), NOW()),
  (6, 'FEE006', '换药费', '治疗费', 25.00, '次', 'ENABLED', '普通换药', 0, NOW(), NOW()),
  (7, 'FEE007', '材料费', '材料费', 10.00, '份', 'ENABLED', '一次性材料', 0, NOW(), NOW()),
  (8, 'FEE008', '雾化费', '治疗费', 40.00, '次', 'ENABLED', '雾化吸入', 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name), price = VALUES(price), updated_at = NOW(), deleted = 0;

INSERT INTO clinic_registration
  (id, registration_no, patient_id, patient_name, phone, gender, age, department_name, doctor_id,
   doctor_name, visit_type, visit_time, status, registration_fee, diagnosis_fee, operator, remark,
   deleted, created_at, updated_at)
VALUES
  (1, 'REG202606001', 1, '张伟', '13600003001', '男', 32, '全科', 1, '王医生', '初诊', NOW() - INTERVAL 7 DAY, 'COMPLETED', 20.00, 30.00, '郑前台', '咳嗽三天', 0, NOW() - INTERVAL 7 DAY, NOW()),
  (2, 'REG202606002', 2, '李娜', '13600003002', '女', 28, '内科', 2, '林医生', '复诊', NOW() - INTERVAL 6 DAY, 'COMPLETED', 20.00, 30.00, '郑前台', '头痛复诊', 0, NOW() - INTERVAL 6 DAY, NOW()),
  (3, 'REG202606003', 3, '王强', '13600003003', '男', 45, '内科', 2, '林医生', '初诊', NOW() - INTERVAL 5 DAY, 'COMPLETED', 20.00, 30.00, '郑前台', '血压偏高', 0, NOW() - INTERVAL 5 DAY, NOW()),
  (4, 'REG202606004', 4, '陈敏', '13600003004', '女', 36, '全科', 1, '王医生', '初诊', NOW() - INTERVAL 4 DAY, 'COMPLETED', 20.00, 30.00, '郑前台', '胃部不适', 0, NOW() - INTERVAL 4 DAY, NOW()),
  (5, 'REG202606005', 5, '刘洋', '13600003005', '男', 19, '全科', 1, '王医生', '初诊', NOW() - INTERVAL 3 DAY, 'COMPLETED', 20.00, 30.00, '郑前台', '发热', 0, NOW() - INTERVAL 3 DAY, NOW()),
  (6, 'REG202606006', 6, '赵静', '13600003006', '女', 52, '内科', 2, '林医生', '复诊', NOW() - INTERVAL 2 DAY, 'COMPLETED', 20.00, 30.00, '郑前台', '血糖复查', 0, NOW() - INTERVAL 2 DAY, NOW()),
  (7, 'REG202606007', 7, '周杰', '13600003007', '男', 8, '儿科', 3, '李医生', '初诊', NOW() - INTERVAL 1 DAY, 'WAITING', 20.00, 30.00, '郑前台', '咽痛发热', 0, NOW() - INTERVAL 1 DAY, NOW()),
  (8, 'REG202606008', 8, '吴芳', '13600003008', '女', 67, '全科', 1, '王医生', '复诊', NOW(), 'WAITING', 20.00, 30.00, '郑前台', '胸闷复诊', 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE status = VALUES(status), visit_time = VALUES(visit_time), updated_at = NOW(), deleted = 0;

INSERT INTO clinic_consultation
  (id, registration_id, patient_id, patient_name, doctor_name, department_name, visit_type,
   chief_complaint, diagnosis, doctor_advice, vital_signs, medical_record, prescription, check_items,
   status, deleted, created_at, updated_at)
VALUES
  (1, 1, 1, '张伟', '王医生', '全科', '初诊', '咳嗽伴咽痛三天', '急性上呼吸道感染', '多饮水，注意休息', JSON_OBJECT('temperature', 37.2, 'bloodPressure', '120/80'), JSON_OBJECT('history', '无慢性病史'), '阿莫西林胶囊，每日三次', '血常规', 'FINISHED', 0, NOW() - INTERVAL 7 DAY, NOW()),
  (2, 2, 2, '李娜', '林医生', '内科', '复诊', '间断头痛一周', '紧张性头痛', '规律作息，减少熬夜', JSON_OBJECT('temperature', 36.6, 'bloodPressure', '110/72'), JSON_OBJECT('history', '青霉素过敏'), '布洛芬缓释胶囊，必要时服用', '血常规', 'FINISHED', 0, NOW() - INTERVAL 6 DAY, NOW()),
  (3, 3, 3, '王强', '林医生', '内科', '初诊', '头晕伴血压升高', '原发性高血压', '低盐饮食，监测血压', JSON_OBJECT('temperature', 36.5, 'bloodPressure', '152/96'), JSON_OBJECT('history', '高血压家族史'), '继续原降压方案', '心电图', 'FINISHED', 0, NOW() - INTERVAL 5 DAY, NOW()),
  (4, 4, 4, '陈敏', '王医生', '全科', '初诊', '上腹隐痛两天', '急性胃炎', '清淡饮食，避免刺激食物', JSON_OBJECT('temperature', 36.8, 'bloodPressure', '118/76'), JSON_OBJECT('history', '无'), '蒙脱石散，每日三次', '腹部彩超', 'FINISHED', 0, NOW() - INTERVAL 4 DAY, NOW()),
  (5, 5, 5, '刘洋', '王医生', '全科', '初诊', '发热伴乏力一天', '病毒性感冒', '休息并观察体温', JSON_OBJECT('temperature', 38.3, 'bloodPressure', '116/70'), JSON_OBJECT('history', '无'), '复方感冒灵颗粒，每日三次', '血常规,C反应蛋白', 'FINISHED', 0, NOW() - INTERVAL 3 DAY, NOW()),
  (6, 6, 6, '赵静', '林医生', '内科', '复诊', '糖尿病复查', '2型糖尿病', '控制饮食，规律用药', JSON_OBJECT('temperature', 36.4, 'bloodPressure', '132/82'), JSON_OBJECT('history', '糖尿病五年'), '按原方案用药', '血糖检测,肝功能八项', 'FINISHED', 0, NOW() - INTERVAL 2 DAY, NOW()),
  (7, 7, 7, '周杰', '李医生', '儿科', '初诊', '咽痛伴低热', '急性咽炎', '温水漱口，清淡饮食', JSON_OBJECT('temperature', 37.8, 'weight', 26), JSON_OBJECT('guardian', '母亲陪同'), '板蓝根颗粒，每日三次', '血常规', 'IN_PROGRESS', 0, NOW() - INTERVAL 1 DAY, NOW()),
  (8, 8, 8, '吴芳', '王医生', '全科', '复诊', '活动后胸闷', '冠心病待查', '避免剧烈活动，及时复诊', JSON_OBJECT('temperature', 36.5, 'bloodPressure', '138/85'), JSON_OBJECT('history', '冠心病史'), '暂按原方案用药', '心电图,胸部DR', 'IN_PROGRESS', 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE diagnosis = VALUES(diagnosis), status = VALUES(status), updated_at = NOW(), deleted = 0;

INSERT INTO clinic_charge_order
  (id, order_no, registration_id, patient_id, patient_name, charge_type, department_name, doctor_name, receivable_amount,
   discount_amount, paid_amount, refund_amount, pay_method, refund_method, status, cashier, paid_at,
   refunded_at, remark, deleted, created_at, updated_at)
VALUES
  (1, 'CHG202606001', 1, 1, '张伟', '门诊收费', '全科', '王医生', 100.00, 2.00, 98.00, 0.00, '微信', NULL, 'PAID', '赵收银', NOW() - INTERVAL 7 DAY, NULL, '挂号及药品', 0, NOW() - INTERVAL 7 DAY, NOW()),
  (2, 'CHG202606002', 2, 2, '李娜', '门诊收费', '内科', '林医生', 122.00, 5.00, 117.00, 0.00, '支付宝', NULL, 'PAID', '赵收银', NOW() - INTERVAL 6 DAY, NULL, '挂号及检查', 0, NOW() - INTERVAL 6 DAY, NOW()),
  (3, 'CHG202606003', 3, 3, '王强', '门诊收费', '内科', '林医生', 110.00, 10.00, 100.00, 0.00, '会员卡', NULL, 'PAID', '赵收银', NOW() - INTERVAL 5 DAY, NULL, '挂号及心电图', 0, NOW() - INTERVAL 5 DAY, NOW()),
  (4, 'CHG202606004', 4, 4, '陈敏', '门诊收费', '全科', '王医生', 249.50, 9.50, 240.00, 0.00, '银行卡', NULL, 'PAID', '赵收银', NOW() - INTERVAL 4 DAY, NULL, '挂号、药品及彩超', 0, NOW() - INTERVAL 4 DAY, NOW()),
  (5, 'CHG202606005', 5, 5, '刘洋', '门诊收费', '全科', '王医生', 168.00, 8.00, 160.00, 0.00, '现金', NULL, 'PAID', '赵收银', NOW() - INTERVAL 3 DAY, NULL, '挂号及检验', 0, NOW() - INTERVAL 3 DAY, NOW()),
  (6, 'CHG202606006', 6, 6, '赵静', '门诊收费', '内科', '林医生', 165.00, 15.00, 150.00, 0.00, '会员卡', NULL, 'PAID', '赵收银', NOW() - INTERVAL 2 DAY, NULL, '复诊及检验', 0, NOW() - INTERVAL 2 DAY, NOW()),
  (7, 'CHG202606007', 7, 7, '周杰', '门诊收费', '儿科', '李医生', 106.00, 0.00, 0.00, 0.00, NULL, NULL, 'PENDING', '赵收银', NULL, NULL, '待收费', 0, NOW() - INTERVAL 1 DAY, NOW()),
  (8, 'CHG202606008', 8, 8, '吴芳', '门诊收费', '全科', '王医生', 230.00, 20.00, 210.00, 80.00, '微信', '微信', 'REFUNDED', '赵收银', NOW(), NOW(), '部分退费', 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE registration_id = VALUES(registration_id), status = VALUES(status), paid_amount = VALUES(paid_amount), updated_at = NOW(), deleted = 0;

INSERT INTO clinic_stock_order
  (id, order_no, stock_direction, stock_type, supplier_id, supplier_name, maker, purchase_amount,
   retail_amount, operator, audit_status, audit_at, remark, deleted, created_at, updated_at)
VALUES
  (1, 'STK202606001', 'IN', '采购入库', 1, '广西医药有限公司', '吴库管', 1020.00, 1800.00, '吴库管', 'APPROVED', NOW() - INTERVAL 7 DAY, '阿莫西林入库', 0, NOW() - INTERVAL 7 DAY, NOW()),
  (2, 'STK202606002', 'IN', '采购入库', 2, '华南制药配送中心', '吴库管', 1032.00, 1892.00, '吴库管', 'APPROVED', NOW() - INTERVAL 6 DAY, '布洛芬入库', 0, NOW() - INTERVAL 6 DAY, NOW()),
  (3, 'STK202606003', 'IN', '采购入库', 3, '桂中药业有限公司', '吴库管', 855.00, 1710.00, '吴库管', 'APPROVED', NOW() - INTERVAL 5 DAY, '感冒灵入库', 0, NOW() - INTERVAL 5 DAY, NOW()),
  (4, 'STK202606004', 'IN', '采购入库', 5, '仁和医药配送公司', '吴库管', 500.00, 975.00, '吴库管', 'PENDING', NULL, '蒙脱石散待审核', 0, NOW() - INTERVAL 4 DAY, NOW()),
  (5, 'STK202606005', 'OUT', '门诊领用', 1, '广西医药有限公司', '周药师', 85.00, 150.00, '周药师', 'APPROVED', NOW() - INTERVAL 3 DAY, '门诊药房领用', 0, NOW() - INTERVAL 3 DAY, NOW()),
  (6, 'STK202606006', 'OUT', '报损出库', 3, '桂中药业有限公司', '周药师', 45.00, 90.00, '周药师', 'APPROVED', NOW() - INTERVAL 2 DAY, '包装破损', 0, NOW() - INTERVAL 2 DAY, NOW()),
  (7, 'STK202606007', 'OUT', '门诊领用', 2, '华南制药配送中心', '周药师', 120.00, 220.00, '周药师', 'PENDING', NULL, '待审核领用', 0, NOW() - INTERVAL 1 DAY, NOW()),
  (8, 'STK202606008', 'IN', '退货入库', 7, '安康冷链医药公司', '吴库管', 320.00, 800.00, '吴库管', 'APPROVED', NOW(), '退货重新入库', 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE audit_status = VALUES(audit_status), updated_at = NOW(), deleted = 0;

INSERT INTO clinic_inventory_check
  (id, check_no, check_date, maker, check_status, remark, deleted, created_at, updated_at)
VALUES
  (1, 'ICK202606001', NOW() - INTERVAL 7 DAY, '吴库管', 'CONFIRMED', '月度盘点', 0, NOW() - INTERVAL 7 DAY, NOW()),
  (2, 'ICK202606002', NOW() - INTERVAL 6 DAY, '吴库管', 'CONFIRMED', '西药区盘点', 0, NOW() - INTERVAL 6 DAY, NOW()),
  (3, 'ICK202606003', NOW() - INTERVAL 5 DAY, '周药师', 'CONFIRMED', '中成药区盘点', 0, NOW() - INTERVAL 5 DAY, NOW()),
  (4, 'ICK202606004', NOW() - INTERVAL 4 DAY, '吴库管', 'DRAFT', '临期药品盘点', 0, NOW() - INTERVAL 4 DAY, NOW()),
  (5, 'ICK202606005', NOW() - INTERVAL 3 DAY, '周药师', 'CONFIRMED', '注射剂盘点', 0, NOW() - INTERVAL 3 DAY, NOW()),
  (6, 'ICK202606006', NOW() - INTERVAL 2 DAY, '吴库管', 'DRAFT', '低库存盘点', 0, NOW() - INTERVAL 2 DAY, NOW()),
  (7, 'ICK202606007', NOW() - INTERVAL 1 DAY, '周药师', 'CONFIRMED', '药房盘点', 0, NOW() - INTERVAL 1 DAY, NOW()),
  (8, 'ICK202606008', NOW(), '吴库管', 'DRAFT', '当日抽盘', 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE check_status = VALUES(check_status), updated_at = NOW(), deleted = 0;

INSERT INTO clinic_inventory_log
  (id, inventory_id, drug_id, business_type, business_no, change_quantity, quantity_before,
   quantity_after, operator, remark, deleted, created_at, updated_at)
VALUES
  (1, 1, 1, 'STOCK_IN', 'STK202606001', 120, 0, 120, '吴库管', '采购入库', 0, NOW() - INTERVAL 7 DAY, NOW()),
  (2, 2, 2, 'STOCK_IN', 'STK202606002', 100, 0, 100, '吴库管', '采购入库', 0, NOW() - INTERVAL 6 DAY, NOW()),
  (3, 3, 3, 'STOCK_IN', 'STK202606003', 100, 0, 100, '吴库管', '采购入库', 0, NOW() - INTERVAL 5 DAY, NOW()),
  (4, 4, 4, 'SALE', 'CHG202606004', -2, 20, 18, '周药师', '门诊发药', 0, NOW() - INTERVAL 4 DAY, NOW()),
  (5, 5, 5, 'STOCK_IN', 'STK202606004', 80, 0, 80, '吴库管', '采购入库', 0, NOW() - INTERVAL 3 DAY, NOW()),
  (6, 6, 6, 'SALE', 'CHG202606005', -5, 155, 150, '周药师', '门诊发药', 0, NOW() - INTERVAL 2 DAY, NOW()),
  (7, 7, 7, 'CHECK_ADJUST', 'ICK202606007', 2, 20, 22, '吴库管', '盘盈调整', 0, NOW() - INTERVAL 1 DAY, NOW()),
  (8, 8, 8, 'STOCK_IN', 'STK202606008', 200, 0, 200, '吴库管', '退货入库', 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE remark = VALUES(remark), updated_at = NOW(), deleted = 0;

INSERT INTO clinic_price_adjustment
  (id, drug_id, drug_code, drug_name, old_purchase_price, new_purchase_price, old_sell_price,
   new_sell_price, operator, reason, deleted, created_at, updated_at)
VALUES
  (1, 1, 'DRG001', '阿莫西林胶囊', 8.00, 8.50, 14.00, 15.00, '周药师', '采购价调整', 0, NOW() - INTERVAL 7 DAY, NOW()),
  (2, 2, 'DRG002', '布洛芬缓释胶囊', 11.50, 12.00, 21.00, 22.00, '周药师', '厂家调价', 0, NOW() - INTERVAL 6 DAY, NOW()),
  (3, 3, 'DRG003', '复方感冒灵颗粒', 8.50, 9.00, 17.00, 18.00, '周药师', '采购成本上涨', 0, NOW() - INTERVAL 5 DAY, NOW()),
  (4, 4, 'DRG004', '蒙脱石散', 9.50, 10.00, 18.00, 19.50, '周药师', '统一零售价', 0, NOW() - INTERVAL 4 DAY, NOW()),
  (5, 5, 'DRG005', '氯雷他定片', 6.00, 6.50, 12.00, 13.00, '周药师', '厂家调价', 0, NOW() - INTERVAL 3 DAY, NOW()),
  (6, 6, 'DRG006', '维生素C片', 4.80, 5.00, 9.50, 10.00, '周药师', '成本调整', 0, NOW() - INTERVAL 2 DAY, NOW()),
  (7, 7, 'DRG007', '板蓝根颗粒', 10.00, 11.00, 20.00, 21.00, '周药师', '采购成本上涨', 0, NOW() - INTERVAL 1 DAY, NOW()),
  (8, 8, 'DRG008', '生理氯化钠注射液', 3.00, 3.20, 7.50, 8.00, '周药师', '冷链成本调整', 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE new_sell_price = VALUES(new_sell_price), updated_at = NOW(), deleted = 0;

INSERT INTO clinic_medical_template
  (id, template_code, template_type, name, permission, diagnosis, description, content, creator, status, deleted, created_at, updated_at)
VALUES
  (1, 'TPL001', 'MEDICAL', '上呼吸道感染病历', '公共模板', '急性上呼吸道感染', '常见上呼吸道感染病历', '{"chiefComplaint":"咳嗽、咽痛","diagnosis":"急性上呼吸道感染"}', '王医生', 'ENABLED', 0, NOW(), NOW()),
  (2, 'TPL002', 'MEDICAL', '高血压复诊病历', '公共模板', '原发性高血压', '高血压复诊病历', '{"chiefComplaint":"血压复查","diagnosis":"原发性高血压"}', '林医生', 'ENABLED', 0, NOW(), NOW()),
  (3, 'TPL003', 'MEDICAL', '糖尿病复诊病历', '公共模板', '2型糖尿病', '糖尿病复诊病历', '{"chiefComplaint":"血糖复查","diagnosis":"2型糖尿病"}', '林医生', 'ENABLED', 0, NOW(), NOW()),
  (4, 'TPL004', 'MEDICAL', '儿童发热病历', '公共模板', '急性咽炎', '儿童发热病历', '{"chiefComplaint":"发热","diagnosis":"急性咽炎"}', '李医生', 'ENABLED', 0, NOW(), NOW()),
  (5, 'TPL005', 'PRESCRIPTION', '成人感冒处方', '公共模板', '感冒', '成人感冒处方', '{"templateType":"西/成药处方","diagnosis":"感冒","drugs":["复方感冒灵颗粒","维生素C片"]}', '王医生', 'ENABLED', 0, NOW(), NOW()),
  (6, 'TPL006', 'PRESCRIPTION', '儿童咽炎处方', '公共模板', '急性咽炎', '儿童咽炎处方', '{"templateType":"西/成药处方","diagnosis":"急性咽炎","drugs":["板蓝根颗粒"]}', '李医生', 'ENABLED', 0, NOW(), NOW()),
  (7, 'TPL007', 'PRESCRIPTION', '过敏处方', '公共模板', '过敏性鼻炎', '过敏处方', '{"templateType":"西/成药处方","diagnosis":"过敏性鼻炎","drugs":["氯雷他定片"]}', '王医生', 'ENABLED', 0, NOW(), NOW()),
  (8, 'TPL008', 'PRESCRIPTION', '胃肠不适处方', '公共模板', '胃肠功能紊乱', '胃肠不适处方', '{"templateType":"西/成药处方","diagnosis":"胃肠功能紊乱","drugs":["蒙脱石散"]}', '王医生', 'ENABLED', 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE content = VALUES(content), permission = VALUES(permission), diagnosis = VALUES(diagnosis), description = VALUES(description), updated_at = NOW(), deleted = 0;

INSERT INTO sys_message
  (id, receiver_id, title, content, message_type, read_status, read_at, deleted, created_at, updated_at)
VALUES
  (1, 1, '库存预警', '蒙脱石散库存低于预警值，请及时补货。', 'INVENTORY', 0, NULL, 0, NOW() - INTERVAL 7 HOUR, NOW()),
  (2, 1, '库存预警', '板蓝根颗粒库存低于预警值，请及时补货。', 'INVENTORY', 0, NULL, 0, NOW() - INTERVAL 6 HOUR, NOW()),
  (3, 1, '待审核入库单', '入库单 STK202606004 等待审核。', 'STOCK', 1, NOW() - INTERVAL 4 HOUR, 0, NOW() - INTERVAL 5 HOUR, NOW()),
  (4, 1, '待审核出库单', '出库单 STK202606007 等待审核。', 'STOCK', 0, NULL, 0, NOW() - INTERVAL 4 HOUR, NOW()),
  (5, 1, '今日待就诊', '当前有2位患者等待就诊。', 'REGISTRATION', 0, NULL, 0, NOW() - INTERVAL 3 HOUR, NOW()),
  (6, 1, '待收费订单', '收费单 CHG202606007 尚未支付。', 'CHARGE', 1, NOW() - INTERVAL 1 HOUR, 0, NOW() - INTERVAL 2 HOUR, NOW()),
  (7, 1, '系统通知', '演示数据已更新完成。', 'SYSTEM', 0, NULL, 0, NOW() - INTERVAL 1 HOUR, NOW()),
  (8, 1, '备份提醒', '请在营业结束后执行数据库备份。', 'SYSTEM', 0, NULL, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE content = VALUES(content), updated_at = NOW(), deleted = 0;

INSERT INTO sys_operation_log
  (id, operator, module, operation, request_uri, request_method, request_params, ip, duration,
   result, error_message, deleted, created_at, updated_at)
VALUES
  (1, '郑前台', '患者管理', '新增患者', '/api/v1/patients', 'POST', '{"patientCode":"PAT008"}', '127.0.0.1', 35, 'SUCCESS', NULL, 0, NOW() - INTERVAL 7 HOUR, NOW()),
  (2, '郑前台', '挂号管理', '新增挂号', '/api/v1/registrations', 'POST', '{"registrationNo":"REG202606008"}', '127.0.0.1', 42, 'SUCCESS', NULL, 0, NOW() - INTERVAL 6 HOUR, NOW()),
  (3, '王医生', '接诊管理', '保存病历', '/api/v1/visits/8/medical-record', 'PUT', '{"diagnosis":"冠心病待查"}', '127.0.0.1', 58, 'SUCCESS', NULL, 0, NOW() - INTERVAL 5 HOUR, NOW()),
  (4, '赵收银', '收费管理', '收费', '/api/v1/charge-orders/6/pay', 'POST', '{"payMethod":"会员卡"}', '127.0.0.1', 31, 'SUCCESS', NULL, 0, NOW() - INTERVAL 4 HOUR, NOW()),
  (5, '周药师', '药品管理', '药品调价', '/api/v1/price-adjust-orders', 'POST', '{"drugId":8}', '127.0.0.1', 46, 'SUCCESS', NULL, 0, NOW() - INTERVAL 3 HOUR, NOW()),
  (6, '吴库管', '库存管理', '库存盘点', '/api/v1/stock-check-orders/7/confirm', 'POST', '{}', '127.0.0.1', 63, 'SUCCESS', NULL, 0, NOW() - INTERVAL 2 HOUR, NOW()),
  (7, '系统管理员', '员工管理', '修改角色', '/api/v1/employees/8', 'PUT', '{"roleName":"前台"}', '127.0.0.1', 29, 'SUCCESS', NULL, 0, NOW() - INTERVAL 1 HOUR, NOW()),
  (8, '系统管理员', '系统设置', '修改配置', '/api/v1/system/settings', 'PUT', '{"sms.enabled":"false"}', '127.0.0.1', 24, 'SUCCESS', NULL, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE result = VALUES(result), updated_at = NOW(), deleted = 0;

INSERT INTO sys_trial_application
  (id, clinic_name, contact_name, phone, region, scale, remark, status, deleted, created_at, updated_at)
VALUES
  (1, '安康诊所', '黄先生', '13500005001', '广西南宁', '1-5人', '希望试用患者管理', 'PENDING', 0, NOW() - INTERVAL 7 DAY, NOW()),
  (2, '仁爱门诊', '林女士', '13500005002', '广西柳州', '6-10人', '关注药品库存功能', 'APPROVED', 0, NOW() - INTERVAL 6 DAY, NOW()),
  (3, '春晖诊所', '韦医生', '13500005003', '广西桂林', '1-5人', '需要收费管理', 'PENDING', 0, NOW() - INTERVAL 5 DAY, NOW()),
  (4, '惠民门诊', '陈先生', '13500005004', '广西北海', '11-20人', '连锁门诊评估', 'FOLLOWING', 0, NOW() - INTERVAL 4 DAY, NOW()),
  (5, '同康诊所', '赵医生', '13500005005', '广西梧州', '6-10人', '需要电子病历', 'APPROVED', 0, NOW() - INTERVAL 3 DAY, NOW()),
  (6, '百姓门诊', '周女士', '13500005006', '广西玉林', '1-5人', '希望尽快联系', 'PENDING', 0, NOW() - INTERVAL 2 DAY, NOW()),
  (7, '健康之家', '吴先生', '13500005007', '广西钦州', '6-10人', '关注统计报表', 'REJECTED', 0, NOW() - INTERVAL 1 DAY, NOW()),
  (8, '益民诊所', '郑医生', '13500005008', '广西贵港', '1-5人', '申请完整功能试用', 'PENDING', 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE status = VALUES(status), updated_at = NOW(), deleted = 0;

SET FOREIGN_KEY_CHECKS = 1;

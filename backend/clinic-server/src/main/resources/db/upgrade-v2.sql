-- Clinic management system V2 incremental schema.
ALTER TABLE clinic_consultation ADD COLUMN department_name VARCHAR(100) NULL AFTER doctor_name;
ALTER TABLE clinic_consultation ADD COLUMN visit_type VARCHAR(50) NULL AFTER department_name;
ALTER TABLE clinic_consultation ADD COLUMN doctor_advice VARCHAR(1000) NULL AFTER diagnosis;
ALTER TABLE clinic_consultation ADD COLUMN vital_signs JSON NULL AFTER doctor_advice;
ALTER TABLE clinic_consultation ADD COLUMN medical_record JSON NULL AFTER vital_signs;

ALTER TABLE clinic_charge_order ADD COLUMN department_name VARCHAR(100) NULL AFTER charge_type;
ALTER TABLE clinic_charge_order ADD COLUMN doctor_name VARCHAR(100) NULL AFTER department_name;
ALTER TABLE clinic_charge_order ADD COLUMN refund_amount DECIMAL(12,2) NULL DEFAULT 0 AFTER paid_amount;
ALTER TABLE clinic_charge_order ADD COLUMN refund_method VARCHAR(50) NULL AFTER pay_method;
ALTER TABLE clinic_charge_order ADD COLUMN refunded_at DATETIME NULL AFTER paid_at;
ALTER TABLE clinic_charge_order ADD COLUMN remark VARCHAR(1000) NULL AFTER refunded_at;

ALTER TABLE clinic_info ADD COLUMN clinic_code VARCHAR(40) NULL AFTER id;
ALTER TABLE clinic_info ADD COLUMN email VARCHAR(100) NULL AFTER phone;
ALTER TABLE clinic_info ADD COLUMN region VARCHAR(100) NULL AFTER email;
ALTER TABLE clinic_inventory ADD COLUMN warning_stock INT NULL DEFAULT 0 AFTER quantity;
ALTER TABLE clinic_member ADD COLUMN card_no VARCHAR(60) NULL AFTER patient_id;
ALTER TABLE clinic_member ADD COLUMN member_type VARCHAR(100) NULL AFTER phone;
ALTER TABLE clinic_member ADD COLUMN total_consume DECIMAL(12,2) NULL DEFAULT 0 AFTER level_name;
ALTER TABLE clinic_member ADD COLUMN total_stored DECIMAL(12,2) NULL DEFAULT 0 AFTER balance;
ALTER TABLE clinic_member ADD COLUMN open_date DATE NULL AFTER points;
ALTER TABLE clinic_registration ADD COLUMN visit_type VARCHAR(50) NULL AFTER doctor_name;
ALTER TABLE clinic_registration ADD COLUMN diagnosis_fee DECIMAL(12,2) NULL DEFAULT 0 AFTER registration_fee;
ALTER TABLE clinic_registration ADD COLUMN remark VARCHAR(1000) NULL AFTER operator;
ALTER TABLE clinic_stock_order ADD COLUMN audit_at DATETIME NULL AFTER audit_status;

CREATE TABLE IF NOT EXISTS sys_trial_application (
  id BIGINT PRIMARY KEY AUTO_INCREMENT, clinic_name VARCHAR(150) NOT NULL, contact_name VARCHAR(100) NOT NULL,
  phone VARCHAR(30) NOT NULL, region VARCHAR(100) NOT NULL, scale VARCHAR(50) NOT NULL, remark VARCHAR(1000),
  status VARCHAR(30) NOT NULL DEFAULT 'PENDING', created_at DATETIME NOT NULL, updated_at DATETIME NOT NULL,
  deleted TINYINT(1) NOT NULL DEFAULT 0, INDEX idx_trial_phone(phone), INDEX idx_trial_status(status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT, receiver_id BIGINT NULL, title VARCHAR(200) NOT NULL, content VARCHAR(2000) NOT NULL,
  message_type VARCHAR(50), read_status TINYINT(1) NOT NULL DEFAULT 0, read_at DATETIME NULL,
  created_at DATETIME NOT NULL, updated_at DATETIME NOT NULL, deleted TINYINT(1) NOT NULL DEFAULT 0,
  INDEX idx_message_receiver(receiver_id, read_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_operation_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT, operator VARCHAR(100), module VARCHAR(100), operation VARCHAR(100),
  request_uri VARCHAR(500), request_method VARCHAR(20), request_params TEXT, ip VARCHAR(64), duration BIGINT,
  result VARCHAR(30), error_message TEXT, created_at DATETIME NOT NULL, updated_at DATETIME NOT NULL,
  deleted TINYINT(1) NOT NULL DEFAULT 0, INDEX idx_log_module(module), INDEX idx_log_created(created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS clinic_member_transaction (
  id BIGINT PRIMARY KEY AUTO_INCREMENT, member_id BIGINT NOT NULL, transaction_type VARCHAR(50) NOT NULL,
  amount DECIMAL(12,2) NULL, points INT NULL, balance_after DECIMAL(12,2) NULL, points_after INT NULL,
  business_no VARCHAR(80), remark VARCHAR(1000), operator VARCHAR(100), created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL, deleted TINYINT(1) NOT NULL DEFAULT 0,
  INDEX idx_member_tx(member_id, transaction_type, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS clinic_setting (
  id BIGINT PRIMARY KEY AUTO_INCREMENT, setting_key VARCHAR(100) NOT NULL UNIQUE, setting_value TEXT,
  description VARCHAR(500), created_at DATETIME NOT NULL, updated_at DATETIME NOT NULL,
  deleted TINYINT(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE IF NOT EXISTS clinic_inventory_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT, inventory_id BIGINT NOT NULL, drug_id BIGINT NULL,
  business_type VARCHAR(50) NOT NULL, business_no VARCHAR(80) NULL, change_quantity INT NOT NULL,
  quantity_before INT NOT NULL, quantity_after INT NOT NULL, operator VARCHAR(100) NULL, remark VARCHAR(1000) NULL,
  created_at DATETIME NOT NULL, updated_at DATETIME NOT NULL, deleted TINYINT(1) NOT NULL DEFAULT 0,
  INDEX idx_inventory_log(inventory_id, created_at), INDEX idx_inventory_business(business_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

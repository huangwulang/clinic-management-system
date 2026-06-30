-- Store generated charge-order detail payloads for dynamic payment pages.
ALTER TABLE clinic_charge_order
  MODIFY COLUMN remark TEXT NULL COMMENT '备注/收费明细JSON';

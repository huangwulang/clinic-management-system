import fs from "node:fs";
import path from "node:path";

const resources = [
  ["患者管理", "patients", "Patient", "患者"],
  ["挂号管理", "registrations", "Registration", "挂号"],
  ["接诊病历", "consultations", "Consultation", "接诊病历"],
  ["收费管理", "charge-orders", "ChargeOrder", "收费订单"],
  ["药品库存", "drugs", "Drug", "药品"],
  ["药品库存", "stock-orders", "StockOrder", "入出库单"],
  ["药品库存", "inventories", "Inventory", "库存"],
  ["药品库存", "inventory-checks", "InventoryCheck", "库存盘点"],
  ["药品库存", "price-adjustments", "PriceAdjustment", "调价记录"],
  ["会员管理", "members", "Member", "会员"],
  ["系统设置", "departments", "Department", "科室"],
  ["系统设置", "roles", "Role", "角色"],
  ["系统设置", "staff", "Staff", "员工"],
  ["系统设置", "dictionary-items", "DictionaryItem", "字典项"],
  ["系统设置", "suppliers", "Supplier", "供应商"],
  ["系统设置", "check-projects", "CheckProject", "检查项目"],
  ["系统设置", "medical-templates", "MedicalTemplate", "病历模板"],
  ["系统设置", "fee-items", "FeeItem", "收费项目"],
  ["系统设置", "clinic-info", "ClinicInfo", "诊所信息"],
  ["系统设置", "user-accounts", "UserAccount", "用户账号"],
  ["患者管理", "family-members", "FamilyMember", "家庭成员"]
];

const fields = {
  Patient: {
    patientCode: ["string", "P202605260001"],
    name: ["string", "张三"],
    cardNo: ["string", "JZK0001"],
    age: ["integer", 28],
    birthday: ["string", "1998-01-01", "date"],
    gender: ["string", "男"],
    phone: ["string", "13800000000"],
    idCard: ["string", "450000199801010000"],
    source: ["string", "门诊"],
    memberLevel: ["string", "普通会员"],
    memberExpireDate: ["string", "2027-05-26", "date"],
    nation: ["string", "汉族"],
    marriage: ["string", "未婚"],
    education: ["string", "本科"],
    provinceCity: ["string", "广西桂林"],
    address: ["string", "桂林市七星区"],
    job: ["string", "职员"],
    company: ["string", "某公司"],
    remark: ["string", "无"],
    operator: ["string", "管理员"]
  },
  Registration: {
    registrationNo: ["string", "GH202605260001"],
    patientId: ["integer", 1, "int64"],
    patientName: ["string", "张三"],
    phone: ["string", "13800000000"],
    gender: ["string", "男"],
    age: ["integer", 28],
    departmentName: ["string", "内科"],
    doctorId: ["integer", 2, "int64"],
    doctorName: ["string", "李医生"],
    visitType: ["string", "初诊"],
    visitTime: ["string", "2026-05-26T10:30:00", "date-time"],
    status: ["string", "待就诊"],
    registrationFee: ["number", 10, "double"],
    diagnosisFee: ["number", 20, "double"],
    operator: ["string", "管理员"],
    remark: ["string", "无"]
  },
  Consultation: {
    registrationId: ["integer", 1, "int64"],
    patientId: ["integer", 1, "int64"],
    patientName: ["string", "张三"],
    doctorName: ["string", "李医生"],
    departmentName: ["string", "内科"],
    visitType: ["string", "初诊"],
    chiefComplaint: ["string", "头痛两天"],
    diagnosis: ["string", "上呼吸道感染"],
    doctorAdvice: ["string", "多喝水，按时服药"],
    vitalSigns: ["string", "体温 37.2"],
    medicalRecord: ["string", "患者自述头痛"],
    prescription: ["string", "感冒灵颗粒"],
    checkItems: ["string", "血常规"],
    status: ["string", "已完成"]
  },
  ChargeOrder: {
    orderNo: ["string", "SF202605260001"],
    patientId: ["integer", 1, "int64"],
    patientName: ["string", "张三"],
    chargeType: ["string", "门诊收费"],
    departmentName: ["string", "内科"],
    doctorName: ["string", "李医生"],
    receivableAmount: ["number", 120, "double"],
    discountAmount: ["number", 0, "double"],
    paidAmount: ["number", 0, "double"],
    refundAmount: ["number", 0, "double"],
    payMethod: ["string", "微信"],
    refundMethod: ["string", "原路退回"],
    status: ["string", "待支付"],
    cashier: ["string", "管理员"],
    paidAt: ["string", "2026-05-26T10:30:00", "date-time"],
    refundedAt: ["string", "2026-05-26T10:40:00", "date-time"],
    remark: ["string", "无"]
  },
  Drug: {
    drugCode: ["string", "YP0001"],
    name: ["string", "感冒灵颗粒"],
    specification: ["string", "10g*9袋"],
    category: ["string", "中成药"],
    dosageForm: ["string", "颗粒剂"],
    unit: ["string", "盒"],
    purchasePrice: ["number", 8.5, "double"],
    sellPrice: ["number", 12, "double"],
    manufacturer: ["string", "某制药厂"],
    approvalNo: ["string", "国药准字Z00000000"],
    status: ["string", "启用"],
    warningStock: ["integer", 20]
  },
  Inventory: {
    drugId: ["integer", 1, "int64"],
    drugCode: ["string", "YP0001"],
    drugName: ["string", "感冒灵颗粒"],
    locationNo: ["string", "A-01"],
    batchNo: ["string", "B202605260001"],
    category: ["string", "中成药"],
    specification: ["string", "10g*9袋"],
    dosageForm: ["string", "颗粒剂"],
    manufacturer: ["string", "某制药厂"],
    quantity: ["integer", 100],
    unit: ["string", "盒"],
    purchaseAmount: ["number", 850, "double"],
    retailAmount: ["number", 1200, "double"],
    expireDate: ["string", "2028-05-26", "date"],
    warningStock: ["integer", 20]
  },
  Member: {
    patientId: ["integer", 1, "int64"],
    cardNo: ["string", "HY0001"],
    patientName: ["string", "张三"],
    phone: ["string", "13800000000"],
    memberType: ["string", "储值会员"],
    levelName: ["string", "普通会员"],
    totalConsume: ["number", 0, "double"],
    balance: ["number", 100, "double"],
    totalStored: ["number", 100, "double"],
    points: ["integer", 0],
    openDate: ["string", "2026-05-26", "date"],
    expireDate: ["string", "2027-05-26", "date"],
    status: ["string", "正常"]
  },
  StockOrder: {
    orderNo: ["string", "RK202605260001"],
    stockDirection: ["string", "入库"],
    stockType: ["string", "采购入库"],
    supplierId: ["integer", 1, "int64"],
    supplierName: ["string", "默认供应商"],
    maker: ["string", "管理员"],
    purchaseAmount: ["number", 850, "double"],
    retailAmount: ["number", 1200, "double"],
    operator: ["string", "管理员"],
    auditStatus: ["string", "待审核"],
    auditAt: ["string", "2026-05-26T10:00:00", "date-time"],
    remark: ["string", "无"]
  },
  InventoryCheck: {
    checkNo: ["string", "PD202605260001"],
    checkDate: ["string", "2026-05-26T10:00:00", "date-time"],
    maker: ["string", "管理员"],
    checkStatus: ["string", "待盘点"],
    remark: ["string", "无"]
  },
  PriceAdjustment: {
    drugId: ["integer", 1, "int64"],
    drugCode: ["string", "YP0001"],
    drugName: ["string", "感冒灵颗粒"],
    oldPurchasePrice: ["number", 8.5, "double"],
    newPurchasePrice: ["number", 9, "double"],
    oldSellPrice: ["number", 12, "double"],
    newSellPrice: ["number", 13, "double"],
    operator: ["string", "管理员"],
    reason: ["string", "采购成本上涨"]
  },
  FamilyMember: {
    patientId: ["integer", 1, "int64"],
    relation: ["string", "父亲"],
    name: ["string", "张父"],
    gender: ["string", "男"],
    age: ["integer", 55],
    company: ["string", "无"],
    birthday: ["string", "1971-01-01", "date"],
    phone: ["string", "13800000003"]
  },
  Department: {
    departmentCode: ["string", "KS001"],
    name: ["string", "内科"],
    description: ["string", "内科门诊"],
    creator: ["string", "管理员"],
    enabled: ["boolean", true]
  },
  Role: {
    roleCode: ["string", "ROLE_ADMIN"],
    name: ["string", "管理员"],
    description: ["string", "系统管理员"],
    permissions: ["string", "all"],
    creator: ["string", "管理员"],
    enabled: ["boolean", true]
  },
  Staff: {
    jobNo: ["string", "YG001"],
    name: ["string", "李医生"],
    gender: ["string", "男"],
    age: ["integer", 35],
    phone: ["string", "13800000001"],
    email: ["string", "doctor@example.com"],
    idCard: ["string", "450000199001010000"],
    positionName: ["string", "医生"],
    clinicName: ["string", "示例诊所"],
    departmentName: ["string", "内科"],
    roleName: ["string", "医生"],
    address: ["string", "桂林市"],
    creator: ["string", "管理员"],
    enabled: ["boolean", true]
  },
  DictionaryItem: {
    dictType: ["string", "gender"],
    itemCode: ["string", "male"],
    itemName: ["string", "男"],
    sortNo: ["integer", 1],
    enabled: ["boolean", true],
    remark: ["string", "性别字典"]
  },
  Supplier: {
    supplierCode: ["string", "GYS001"],
    name: ["string", "默认供应商"],
    contactName: ["string", "王经理"],
    phone: ["string", "13800000002"],
    address: ["string", "桂林市"],
    status: ["string", "启用"],
    remark: ["string", "无"]
  },
  CheckProject: {
    projectCode: ["string", "JC001"],
    name: ["string", "血常规"],
    category: ["string", "检验"],
    price: ["number", 30, "double"],
    unit: ["string", "次"],
    status: ["string", "启用"],
    remark: ["string", "无"]
  },
  MedicalTemplate: {
    templateCode: ["string", "MB001"],
    templateType: ["string", "门诊病历"],
    name: ["string", "感冒模板"],
    departmentName: ["string", "内科"],
    content: ["string", "主诉：发热、咳嗽。"],
    creator: ["string", "管理员"],
    status: ["string", "启用"]
  },
  FeeItem: {
    feeCode: ["string", "SF001"],
    name: ["string", "诊疗费"],
    category: ["string", "门诊收费"],
    price: ["number", 20, "double"],
    unit: ["string", "次"],
    status: ["string", "启用"],
    remark: ["string", "无"]
  },
  ClinicInfo: {
    clinicCode: ["string", "CLINIC001"],
    clinicName: ["string", "示例诊所"],
    principal: ["string", "负责人"],
    phone: ["string", "0773-0000000"],
    email: ["string", "clinic@example.com"],
    region: ["string", "广西桂林"],
    address: ["string", "桂林市七星区"],
    licenseNo: ["string", "LICENSE001"],
    introduction: ["string", "诊所简介"]
  },
  UserAccount: {
    username: ["string", "admin"],
    password: ["string", "123456"],
    name: ["string", "管理员"],
    phone: ["string", "13800000000"],
    roleName: ["string", "超级管理员"],
    enabled: ["boolean", true]
  }
};

const baseProps = {
  id: { type: "integer", format: "int64", example: 1 },
  createdAt: { type: "string", format: "date-time", example: "2026-05-26T10:00:00" },
  updatedAt: { type: "string", format: "date-time", example: "2026-05-26T10:00:00" },
  deleted: { type: "boolean", example: false }
};

function schemaProps(model) {
  return Object.fromEntries(
    Object.entries(fields[model]).map(([name, [type, example, format]]) => [
      name,
      { type, ...(format ? { format } : {}), example }
    ])
  );
}

function example(model) {
  return Object.fromEntries(Object.entries(fields[model]).map(([name, [, value]]) => [name, value]));
}

function apiResponse(dataSchema) {
  return {
    type: "object",
    properties: {
      code: { type: "integer", example: 200 },
      message: { type: "string", example: "success" },
      data: dataSchema,
      timestamp: { type: "string", format: "date-time", example: "2026-05-26T10:00:00" }
    }
  };
}

function response(schema) {
  return {
    description: "成功",
    content: {
      "application/json": {
        schema
      }
    }
  };
}

const params = {
  id: { name: "id", in: "path", required: true, schema: { type: "integer", format: "int64" }, description: "数据 ID" },
  page: { name: "page", in: "query", required: false, schema: { type: "integer", default: 1 }, description: "页码" },
  size: { name: "size", in: "query", required: false, schema: { type: "integer", default: 10 }, description: "每页条数" },
  keyword: { name: "keyword", in: "query", required: false, schema: { type: "string" }, description: "关键字" }
};

function body(model) {
  return {
    required: true,
    content: {
      "application/json": {
        schema: { $ref: `#/components/schemas/${model}` },
        example: example(model)
      }
    }
  };
}

function crudPaths(tag, resource, model, label) {
  return {
    [`/${resource}`]: {
      get: {
        tags: [tag],
        summary: `${label}分页查询`,
        operationId: `page${model}`,
        parameters: [params.page, params.size, params.keyword],
        responses: {
          200: response(apiResponse({
            type: "object",
            properties: {
              records: { type: "array", items: { $ref: `#/components/schemas/${model}` } },
              total: { type: "integer", format: "int64", example: 100 },
              page: { type: "integer", example: 1 },
              size: { type: "integer", example: 10 }
            }
          }))
        }
      },
      post: {
        tags: [tag],
        summary: `新增${label}`,
        operationId: `create${model}`,
        requestBody: body(model),
        responses: { 200: response(apiResponse({ $ref: `#/components/schemas/${model}` })) }
      }
    },
    [`/${resource}/list`]: {
      get: {
        tags: [tag],
        summary: `${label}列表查询`,
        operationId: `list${model}`,
        parameters: [params.keyword],
        responses: { 200: response(apiResponse({ type: "array", items: { $ref: `#/components/schemas/${model}` } })) }
      }
    },
    [`/${resource}/{id}`]: {
      get: {
        tags: [tag],
        summary: `根据 ID 查询${label}`,
        operationId: `get${model}`,
        parameters: [params.id],
        responses: { 200: response(apiResponse({ $ref: `#/components/schemas/${model}` })) }
      },
      put: {
        tags: [tag],
        summary: `修改${label}`,
        operationId: `update${model}`,
        parameters: [params.id],
        requestBody: body(model),
        responses: { 200: response(apiResponse({ $ref: `#/components/schemas/${model}` })) }
      },
      delete: {
        tags: [tag],
        summary: `删除${label}`,
        operationId: `delete${model}`,
        parameters: [params.id],
        responses: { 200: response(apiResponse({ nullable: true })) }
      }
    }
  };
}

const paths = {
  "/auth/login": {
    post: {
      tags: ["认证"],
      summary: "用户登录",
      operationId: "login",
      requestBody: {
        required: true,
        content: {
          "application/json": {
            schema: { $ref: "#/components/schemas/LoginRequest" },
            example: { username: "admin", password: "123456" }
          }
        }
      },
      responses: {
        200: response(apiResponse({
          type: "object",
          properties: {
            token: { type: "string", example: "b3a1f0b8-xxxx-xxxx-xxxx-xxxxxxxxxxxx" },
            user: { $ref: "#/components/schemas/UserAccount" }
          }
        }))
      }
    }
  }
};

for (const item of resources) {
  Object.assign(paths, crudPaths(...item));
}

paths["/charge-orders/{id}/pay"] = {
  post: {
    tags: ["收费管理"],
    summary: "收费订单支付",
    operationId: "payChargeOrder",
    parameters: [params.id],
    requestBody: {
      required: true,
      content: {
        "application/json": {
          schema: { $ref: "#/components/schemas/PayRequest" },
          example: { paidAmount: 120, payMethod: "微信", cashier: "管理员" }
        }
      }
    },
    responses: { 200: response(apiResponse({ $ref: "#/components/schemas/ChargeOrder" })) }
  }
};

paths["/charge-orders/{id}/refund"] = {
  post: {
    tags: ["收费管理"],
    summary: "收费订单退款",
    operationId: "refundChargeOrder",
    parameters: [params.id],
    requestBody: {
      required: true,
      content: {
        "application/json": {
          schema: { $ref: "#/components/schemas/RefundRequest" },
          example: { refundAmount: 120, refundMethod: "原路退回" }
        }
      }
    },
    responses: { 200: response(apiResponse({ $ref: "#/components/schemas/ChargeOrder" })) }
  }
};

paths["/inventories/warnings"] = {
  get: {
    tags: ["药品库存"],
    summary: "库存预警列表",
    operationId: "inventoryWarnings",
    responses: { 200: response(apiResponse({ type: "array", items: { $ref: "#/components/schemas/Inventory" } })) }
  }
};

paths["/statistics/dashboard"] = {
  get: {
    tags: ["统计分析"],
    summary: "首页统计",
    operationId: "dashboardStatistics",
    responses: {
      200: response(apiResponse({
        type: "object",
        properties: {
          patientCount: { type: "integer", format: "int64", example: 100 },
          registrationCount: { type: "integer", format: "int64", example: 20 },
          drugCount: { type: "integer", format: "int64", example: 300 },
          memberCount: { type: "integer", format: "int64", example: 50 },
          income: { type: "number", format: "double", example: 6888.5 }
        }
      }))
    }
  }
};

const schemas = {
  LoginRequest: {
    type: "object",
    required: ["username", "password"],
    properties: {
      username: { type: "string", example: "admin" },
      password: { type: "string", example: "123456" }
    }
  },
  PayRequest: {
    type: "object",
    properties: {
      paidAmount: { type: "number", format: "double", example: 120 },
      payMethod: { type: "string", example: "微信" },
      cashier: { type: "string", example: "管理员" }
    }
  },
  RefundRequest: {
    type: "object",
    properties: {
      refundAmount: { type: "number", format: "double", example: 120 },
      refundMethod: { type: "string", example: "原路退回" }
    }
  }
};

for (const [, , model] of resources) {
  schemas[model] = {
    type: "object",
    properties: {
      ...baseProps,
      ...schemaProps(model)
    }
  };
}

const openapi = {
  openapi: "3.0.3",
  info: {
    title: "诊所管理系统 API",
    description: "可直接导入 Apifox 的 OpenAPI 3.0 接口数据。",
    version: "1.0.0"
  },
  servers: [
    { url: "http://localhost:8080", description: "本地后端服务" },
    { url: "/api", description: "前端开发代理" }
  ],
  tags: [
    { name: "认证" },
    { name: "患者管理" },
    { name: "挂号管理" },
    { name: "接诊病历" },
    { name: "收费管理" },
    { name: "药品库存" },
    { name: "会员管理" },
    { name: "系统设置" },
    { name: "统计分析" }
  ],
  paths,
  components: {
    securitySchemes: {
      bearerAuth: { type: "http", scheme: "bearer", bearerFormat: "UUID" }
    },
    schemas
  },
  security: [{ bearerAuth: [] }]
};

fs.mkdirSync("docs", { recursive: true });
fs.writeFileSync(path.join("docs", "openapi.json"), `${JSON.stringify(openapi, null, 2)}\n`);

import fs from "node:fs";
import path from "node:path";

const root = "D:\\idea_code\\clinic-menagement-system";
const common = path.join(root, "clinic-common");
const pojo = path.join(root, "clinic-pojo");
const server = path.join(root, "clinic-server");
const pojoJava = path.join(pojo, "src/main/java/com/guet/clinic/pojo");
const entityDir = path.join(pojoJava, "entity");
const serverJava = path.join(server, "src/main/java/com/guet/clinic/server");

const entities = [
  ["UserAccount", "sys_user_account", "user-accounts", ["username", "name", "phone", "roleName"], [["String", "username"], ["String", "password"], ["String", "name"], ["String", "phone"], ["String", "roleName"], ["Boolean", "enabled"]], ["username", "password"]],
  ["Patient", "clinic_patient", "patients", ["patientCode", "name", "phone", "cardNo", "idCard"], [["String", "patientCode"], ["String", "name"], ["String", "cardNo"], ["Integer", "age"], ["LocalDate", "birthday"], ["String", "gender"], ["String", "phone"], ["String", "idCard"], ["String", "source"], ["String", "memberLevel"], ["LocalDate", "memberExpireDate"], ["String", "nation"], ["String", "marriage"], ["String", "education"], ["String", "provinceCity"], ["String", "address"], ["String", "job"], ["String", "company"], ["String", "remark"], ["String", "operator"]], ["name"]],
  ["FamilyMember", "clinic_family_member", "family-members", ["name", "phone", "relation"], [["Long", "patientId"], ["String", "relation"], ["String", "name"], ["String", "gender"], ["Integer", "age"], ["String", "company"], ["LocalDate", "birthday"], ["String", "phone"]], []],
  ["Member", "clinic_member", "members", ["patientName", "phone", "levelName", "cardNo"], [["Long", "patientId"], ["String", "cardNo"], ["String", "patientName"], ["String", "phone"], ["String", "memberType"], ["String", "levelName"], ["BigDecimal", "totalConsume"], ["BigDecimal", "balance"], ["BigDecimal", "totalStored"], ["Integer", "points"], ["LocalDate", "openDate"], ["LocalDate", "expireDate"], ["String", "status"]], []],
  ["Registration", "clinic_registration", "registrations", ["registrationNo", "patientName", "phone", "departmentName", "doctorName"], [["String", "registrationNo"], ["Long", "patientId"], ["String", "patientName"], ["String", "phone"], ["String", "gender"], ["Integer", "age"], ["String", "departmentName"], ["Long", "doctorId"], ["String", "doctorName"], ["String", "visitType"], ["LocalDateTime", "visitTime"], ["String", "status"], ["BigDecimal", "registrationFee"], ["BigDecimal", "diagnosisFee"], ["String", "operator"], ["String", "remark"]], ["patientName"]],
  ["Consultation", "clinic_consultation", "consultations", ["patientName", "doctorName", "diagnosis"], [["Long", "registrationId"], ["Long", "patientId"], ["String", "patientName"], ["String", "doctorName"], ["String", "departmentName"], ["String", "visitType"], ["String", "chiefComplaint"], ["String", "diagnosis"], ["String", "doctorAdvice"], ["String", "vitalSigns"], ["String", "medicalRecord"], ["String", "prescription"], ["String", "checkItems"], ["String", "status"]], []],
  ["ChargeOrder", "clinic_charge_order", "charge-orders", ["orderNo", "patientName", "chargeType", "status"], [["String", "orderNo"], ["Long", "patientId"], ["String", "patientName"], ["String", "chargeType"], ["String", "departmentName"], ["String", "doctorName"], ["BigDecimal", "receivableAmount"], ["BigDecimal", "discountAmount"], ["BigDecimal", "paidAmount"], ["BigDecimal", "refundAmount"], ["String", "payMethod"], ["String", "refundMethod"], ["String", "status"], ["String", "cashier"], ["LocalDateTime", "paidAt"], ["LocalDateTime", "refundedAt"], ["String", "remark"]], ["orderNo"]],
  ["Drug", "clinic_drug", "drugs", ["drugCode", "name", "manufacturer", "category"], [["String", "drugCode"], ["String", "name"], ["String", "specification"], ["String", "category"], ["String", "dosageForm"], ["String", "unit"], ["BigDecimal", "purchasePrice"], ["BigDecimal", "sellPrice"], ["String", "manufacturer"], ["String", "approvalNo"], ["String", "status"], ["Integer", "warningStock"]], ["name"]],
  ["StockOrder", "clinic_stock_order", "stock-orders", ["orderNo", "stockType", "supplierName", "auditStatus"], [["String", "orderNo"], ["String", "stockDirection"], ["String", "stockType"], ["Long", "supplierId"], ["String", "supplierName"], ["String", "maker"], ["BigDecimal", "purchaseAmount"], ["BigDecimal", "retailAmount"], ["String", "operator"], ["String", "auditStatus"], ["LocalDateTime", "auditAt"], ["String", "remark"]], ["orderNo"]],
  ["Inventory", "clinic_inventory", "inventories", ["drugCode", "drugName", "manufacturer", "batchNo", "locationNo"], [["Long", "drugId"], ["String", "drugCode"], ["String", "drugName"], ["String", "locationNo"], ["String", "batchNo"], ["String", "category"], ["String", "specification"], ["String", "dosageForm"], ["String", "manufacturer"], ["Integer", "quantity"], ["String", "unit"], ["BigDecimal", "purchaseAmount"], ["BigDecimal", "retailAmount"], ["LocalDate", "expireDate"], ["Integer", "warningStock"]], []],
  ["InventoryCheck", "clinic_inventory_check", "inventory-checks", ["checkNo", "maker", "checkStatus"], [["String", "checkNo"], ["LocalDateTime", "checkDate"], ["String", "maker"], ["String", "checkStatus"], ["String", "remark"]], ["checkNo"]],
  ["PriceAdjustment", "clinic_price_adjustment", "price-adjustments", ["drugCode", "drugName", "operator"], [["Long", "drugId"], ["String", "drugCode"], ["String", "drugName"], ["BigDecimal", "oldPurchasePrice"], ["BigDecimal", "newPurchasePrice"], ["BigDecimal", "oldSellPrice"], ["BigDecimal", "newSellPrice"], ["String", "operator"], ["String", "reason"]], []],
  ["Department", "sys_department", "departments", ["departmentCode", "name"], [["String", "departmentCode"], ["String", "name"], ["String", "description"], ["String", "creator"], ["Boolean", "enabled"]], ["name"]],
  ["Role", "sys_role", "roles", ["roleCode", "name"], [["String", "roleCode"], ["String", "name"], ["String", "description"], ["String", "permissions"], ["String", "creator"], ["Boolean", "enabled"]], ["name"]],
  ["Staff", "sys_staff", "staff", ["jobNo", "name", "phone", "departmentName", "roleName"], [["String", "jobNo"], ["String", "name"], ["String", "gender"], ["Integer", "age"], ["String", "phone"], ["String", "email"], ["String", "idCard"], ["String", "positionName"], ["String", "clinicName"], ["String", "departmentName"], ["String", "roleName"], ["String", "address"], ["String", "creator"], ["Boolean", "enabled"]], ["name"]],
  ["DictionaryItem", "sys_dictionary_item", "dictionary-items", ["dictType", "itemCode", "itemName"], [["String", "dictType"], ["String", "itemCode"], ["String", "itemName"], ["Integer", "sortNo"], ["Boolean", "enabled"], ["String", "remark"]], []],
  ["Supplier", "clinic_supplier", "suppliers", ["supplierCode", "name", "contactName", "phone"], [["String", "supplierCode"], ["String", "name"], ["String", "contactName"], ["String", "phone"], ["String", "address"], ["String", "status"], ["String", "remark"]], ["name"]],
  ["CheckProject", "clinic_check_project", "check-projects", ["projectCode", "name", "category"], [["String", "projectCode"], ["String", "name"], ["String", "category"], ["BigDecimal", "price"], ["String", "unit"], ["String", "status"], ["String", "remark"]], ["name"]],
  ["MedicalTemplate", "clinic_medical_template", "medical-templates", ["templateCode", "name", "templateType", "departmentName"], [["String", "templateCode"], ["String", "templateType"], ["String", "name"], ["String", "departmentName"], ["String", "content"], ["String", "creator"], ["String", "status"]], ["name"]],
  ["FeeItem", "clinic_fee_item", "fee-items", ["feeCode", "name", "category"], [["String", "feeCode"], ["String", "name"], ["String", "category"], ["BigDecimal", "price"], ["String", "unit"], ["String", "status"], ["String", "remark"]], ["name"]],
  ["ClinicInfo", "clinic_info", "clinic-info", ["clinicName", "phone", "principal"], [["String", "clinicCode"], ["String", "clinicName"], ["String", "principal"], ["String", "phone"], ["String", "email"], ["String", "region"], ["String", "address"], ["String", "licenseNo"], ["String", "introduction"]], []],
];

function mkdirp(dir) {
  fs.mkdirSync(dir, { recursive: true });
}

function write(file, content) {
  mkdirp(path.dirname(file));
  fs.writeFileSync(file, content.replace(/^\n/, ""), "utf8");
}

function camelToSnake(value) {
  return value.replace(/[A-Z]/g, (m, offset) => `${offset ? "_" : ""}${m.toLowerCase()}`);
}

function javaImports(fields, required) {
  const imports = new Set([
    "com.guet.clinic.pojo.BaseEntity",
    "lombok.EqualsAndHashCode",
    "lombok.Getter",
    "lombok.Setter",
  ]);
  if (required.length) imports.add("jakarta.validation.constraints.NotBlank");
  if (fields.some(([type]) => type === "BigDecimal")) imports.add("java.math.BigDecimal");
  if (fields.some(([type]) => type === "LocalDate")) imports.add("java.time.LocalDate");
  if (fields.some(([type]) => type === "LocalDateTime")) imports.add("java.time.LocalDateTime");
  return [...imports].sort().map((item) => `import ${item};`).join("\n");
}

function writePoms() {
  write(path.join(root, "pom.xml"), `<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <parent><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-parent</artifactId><version>3.3.5</version><relativePath/></parent>
  <groupId>com.guet</groupId><artifactId>clinic-menagement-system</artifactId><version>1.0.0</version><packaging>pom</packaging>
  <modules><module>clinic-common</module><module>clinic-pojo</module><module>clinic-server</module></modules>
  <properties><java.version>17</java.version><project.build.sourceEncoding>UTF-8</project.build.sourceEncoding><mybatis.spring.boot.version>3.0.4</mybatis.spring.boot.version></properties>
  <dependencyManagement><dependencies>
    <dependency><groupId>com.guet</groupId><artifactId>clinic-common</artifactId><version>\${project.version}</version></dependency>
    <dependency><groupId>com.guet</groupId><artifactId>clinic-pojo</artifactId><version>\${project.version}</version></dependency>
    <dependency><groupId>org.mybatis.spring.boot</groupId><artifactId>mybatis-spring-boot-starter</artifactId><version>\${mybatis.spring.boot.version}</version></dependency>
  </dependencies></dependencyManagement>
  <build><pluginManagement><plugins><plugin><groupId>org.apache.maven.plugins</groupId><artifactId>maven-compiler-plugin</artifactId><version>3.8.1</version><configuration><release>17</release></configuration></plugin><plugin><groupId>org.apache.maven.plugins</groupId><artifactId>maven-surefire-plugin</artifactId><version>2.22.2</version></plugin><plugin><groupId>org.apache.maven.plugins</groupId><artifactId>maven-jar-plugin</artifactId><version>3.2.2</version></plugin></plugins></pluginManagement></build>
</project>
`);
  write(path.join(common, "pom.xml"), `<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <parent><groupId>com.guet</groupId><artifactId>clinic-menagement-system</artifactId><version>1.0.0</version></parent>
  <artifactId>clinic-common</artifactId>
  <dependencies>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-validation</artifactId></dependency>
  </dependencies>
</project>
`);
  write(path.join(pojo, "pom.xml"), `<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <parent><groupId>com.guet</groupId><artifactId>clinic-menagement-system</artifactId><version>1.0.0</version></parent>
  <artifactId>clinic-pojo</artifactId>
  <dependencies>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-validation</artifactId></dependency>
    <dependency><groupId>org.projectlombok</groupId><artifactId>lombok</artifactId><optional>true</optional></dependency>
  </dependencies>
</project>
`);
  write(path.join(server, "pom.xml"), `<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <parent><groupId>com.guet</groupId><artifactId>clinic-menagement-system</artifactId><version>1.0.0</version></parent>
  <artifactId>clinic-server</artifactId>
  <dependencies>
    <dependency><groupId>com.guet</groupId><artifactId>clinic-common</artifactId></dependency>
    <dependency><groupId>com.guet</groupId><artifactId>clinic-pojo</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-validation</artifactId></dependency>
    <dependency><groupId>org.mybatis.spring.boot</groupId><artifactId>mybatis-spring-boot-starter</artifactId></dependency>
    <dependency><groupId>com.mysql</groupId><artifactId>mysql-connector-j</artifactId><scope>runtime</scope></dependency>
    <dependency><groupId>org.projectlombok</groupId><artifactId>lombok</artifactId><optional>true</optional></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-test</artifactId><scope>test</scope></dependency>
  </dependencies>
  <build><plugins><plugin><groupId>org.springframework.boot</groupId><artifactId>spring-boot-maven-plugin</artifactId></plugin></plugins></build>
</project>
`);
}

function writeCommonAndEntities() {
  write(path.join(common, "src/main/java/com/guet/clinic/common/ApiResponse.java"), `package com.guet.clinic.common;

import java.time.LocalDateTime;

public record ApiResponse<T>(Integer code, String message, T data, LocalDateTime timestamp) {
    public static <T> ApiResponse<T> ok(T data) { return new ApiResponse<>(200, "success", data, LocalDateTime.now()); }
    public static ApiResponse<Void> ok() { return ok(null); }
    public static ApiResponse<Void> fail(Integer code, String message) { return new ApiResponse<>(code, message, null, LocalDateTime.now()); }
}
`);
  write(path.join(common, "src/main/java/com/guet/clinic/common/PageResponse.java"), `package com.guet.clinic.common;

import java.util.List;

public record PageResponse<T>(List<T> records, long total, int page, int size) {
    public static <T> PageResponse<T> of(List<T> records, long total, int page, int size) {
        return new PageResponse<>(records, total, page, size);
    }
}
`);
  write(path.join(common, "src/main/java/com/guet/clinic/common/BusinessException.java"), `package com.guet.clinic.common;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) { super(message); }
}
`);
  write(path.join(pojoJava, "BaseEntity.java"), `package com.guet.clinic.pojo;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public abstract class BaseEntity {
    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean deleted = false;
}
`);
  for (const [className, , , , fields, required] of entities) {
    const body = fields.flatMap(([type, name]) => {
      const lines = [];
      if (required.includes(name) && type === "String") lines.push("    @NotBlank");
      lines.push(`    private ${type} ${name};`);
      return lines;
    }).join("\n");
    write(path.join(entityDir, `${className}.java`), `package com.guet.clinic.pojo.entity;

${javaImports(fields, required)}

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class ${className} extends BaseEntity {
${body}
}
`);
  }
}

function writeMappers() {
  for (const stale of ["ColumnMeta.java", "TableMeta.java", "TableMetaRegistry.java", "CrudSqlProvider.java"]) {
    const file = path.join(serverJava, "mapper", stale);
    if (fs.existsSync(file)) fs.rmSync(file, { force: true });
  }
  write(path.join(serverJava, "mapper/CrudMapper.java"), `package com.guet.clinic.server.mapper;

import com.guet.clinic.pojo.BaseEntity;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface CrudMapper<T extends BaseEntity> {
    List<T> selectPage(@Param("offset") int offset, @Param("size") int size, @Param("keyword") String keyword);

    long count(@Param("keyword") String keyword);

    T selectById(@Param("id") Long id);

    int insert(T entity);

    int update(T entity);

    int softDelete(@Param("id") Long id);
}
`);
  mkdirp(path.join(server, "src/main/resources/mapper"));
  for (const [className] of entities) {
    write(path.join(serverJava, "mapper", `${className}Mapper.java`), `package com.guet.clinic.server.mapper;

import com.guet.clinic.pojo.entity.${className};
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ${className}Mapper extends CrudMapper<${className}> {
}
`);
  }
  for (const [className, table, , keywords, fields] of entities) {
    const allFields = [["Long", "id"], ...fields, ["LocalDateTime", "createdAt"], ["LocalDateTime", "updatedAt"], ["Boolean", "deleted"]];
    const baseColumns = allFields.map(([, name]) => camelToSnake(name)).join(", ");
    const insertFields = [...fields, ["LocalDateTime", "createdAt"], ["LocalDateTime", "updatedAt"], ["Boolean", "deleted"]];
    const insertColumns = insertFields.map(([, name]) => camelToSnake(name)).join(", ");
    const insertValues = insertFields.map(([, name]) => `#{${name}}`).join(", ");
    const updateSet = [...fields, ["LocalDateTime", "updatedAt"], ["Boolean", "deleted"]]
      .map(([, name]) => `      <if test="${name} != null">${camelToSnake(name)} = #{${name}},</if>`)
      .join("\n");
    const keywordSql = keywords.length
      ? `\n      <if test="keyword != null and keyword != ''">\n        AND (${keywords.map((name) => `${camelToSnake(name)} LIKE CONCAT('%', #{keyword}, '%')`).join(" OR ")})\n      </if>`
      : "";
    write(path.join(server, "src/main/resources/mapper", `${className}Mapper.xml`), `<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.guet.clinic.server.mapper.${className}Mapper">
  <select id="selectPage" resultType="com.guet.clinic.pojo.entity.${className}">
    SELECT ${baseColumns}
    FROM ${table}
    WHERE deleted = 0${keywordSql}
    ORDER BY id DESC
    LIMIT #{size} OFFSET #{offset}
  </select>

  <select id="count" resultType="long">
    SELECT COUNT(1)
    FROM ${table}
    WHERE deleted = 0${keywordSql}
  </select>

  <select id="selectById" resultType="com.guet.clinic.pojo.entity.${className}">
    SELECT ${baseColumns}
    FROM ${table}
    WHERE id = #{id} AND deleted = 0
  </select>

  <insert id="insert" useGeneratedKeys="true" keyProperty="id">
    INSERT INTO ${table} (${insertColumns})
    VALUES (${insertValues})
  </insert>

  <update id="update">
    UPDATE ${table}
    <set>
${updateSet}
    </set>
    WHERE id = #{id} AND deleted = 0
  </update>

  <update id="softDelete">
    UPDATE ${table}
    SET deleted = 1, updated_at = NOW()
    WHERE id = #{id}
  </update>
</mapper>
`);
  }
}

function writeServicesAndControllers() {
  write(path.join(serverJava, "service/CrudService.java"), `package com.guet.clinic.server.service;

import com.guet.clinic.common.PageResponse;
import com.guet.clinic.pojo.BaseEntity;
import java.util.List;

public interface CrudService<T extends BaseEntity> {
    PageResponse<T> page(int page, int size, String keyword);
    List<T> list(String keyword);
    long count(String keyword);
    T get(Long id);
    T save(T entity);
    T update(Long id, T entity);
    void delete(Long id);
}
`);
  mkdirp(path.join(serverJava, "service/impl"));
  write(path.join(serverJava, "service/impl/AbstractCrudService.java"), `package com.guet.clinic.server.service.impl;

import com.guet.clinic.common.BusinessException;
import com.guet.clinic.common.PageResponse;
import com.guet.clinic.pojo.BaseEntity;
import com.guet.clinic.server.mapper.CrudMapper;
import com.guet.clinic.server.service.CrudService;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

public abstract class AbstractCrudService<T extends BaseEntity> implements CrudService<T> {
    protected abstract CrudMapper<T> mapper();

    @Override
    public PageResponse<T> page(int page, int size, String keyword) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 200);
        int offset = (safePage - 1) * safeSize;
        return PageResponse.of(mapper().selectPage(offset, safeSize, keyword), mapper().count(keyword), safePage, safeSize);
    }

    @Override
    public List<T> list(String keyword) { return mapper().selectPage(0, 200, keyword); }

    @Override
    public long count(String keyword) { return mapper().count(keyword); }

    @Override
    public T get(Long id) {
        T entity = mapper().selectById(id);
        if (entity == null) throw new BusinessException("Data not found");
        return entity;
    }

    @Override
    @Transactional
    public T save(T entity) {
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setDeleted(false);
        mapper().insert(entity);
        return entity;
    }

    @Override
    @Transactional
    public T update(Long id, T entity) {
        get(id);
        entity.setId(id);
        entity.setUpdatedAt(LocalDateTime.now());
        mapper().update(entity);
        return get(id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        get(id);
        mapper().softDelete(id);
    }
}
`);
  for (const [className] of entities) {
    const extraImports = className === "ChargeOrder"
      ? "import java.math.BigDecimal;\nimport java.time.LocalDateTime;\nimport org.springframework.transaction.annotation.Transactional;\n"
      : "";
    const extraMethods = className === "ChargeOrder"
      ? `
    @Transactional
    public ChargeOrder pay(Long id, BigDecimal paidAmount, String payMethod, String cashier) {
        ChargeOrder order = get(id);
        order.setPaidAmount(paidAmount == null ? order.getReceivableAmount() : paidAmount);
        order.setPayMethod(payMethod);
        order.setCashier(cashier);
        order.setStatus("PAID");
        order.setPaidAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        mapper.update(order);
        return get(id);
    }

    @Transactional
    public ChargeOrder refund(Long id, BigDecimal refundAmount, String refundMethod) {
        ChargeOrder order = get(id);
        order.setRefundAmount(refundAmount == null ? order.getPaidAmount() : refundAmount);
        order.setRefundMethod(refundMethod);
        order.setStatus("REFUNDED");
        order.setRefundedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        mapper.update(order);
        return get(id);
    }
`
      : className === "Inventory"
        ? `
    public java.util.List<Inventory> warnings() {
        return list(null).stream()
                .filter(item -> (item.getWarningStock() != null && item.getQuantity() != null && item.getQuantity() <= item.getWarningStock())
                        || (item.getExpireDate() != null && item.getExpireDate().isBefore(java.time.LocalDate.now().plusDays(30))))
                .toList();
    }
`
        : "";
    const interfaceExtra = className === "ChargeOrder"
      ? `\n    ${className} pay(Long id, java.math.BigDecimal paidAmount, String payMethod, String cashier);\n    ${className} refund(Long id, java.math.BigDecimal refundAmount, String refundMethod);\n`
      : className === "Inventory"
        ? `\n    java.util.List<${className}> warnings();\n`
        : "";
    write(path.join(serverJava, "service", `${className}Service.java`), `package com.guet.clinic.server.service;

import com.guet.clinic.pojo.entity.${className};

public interface ${className}Service extends CrudService<${className}> {${interfaceExtra}}
`);
    write(path.join(serverJava, "service/impl", `${className}ServiceImpl.java`), `package com.guet.clinic.server.service.impl;

import com.guet.clinic.pojo.entity.${className};
import com.guet.clinic.server.mapper.CrudMapper;
import com.guet.clinic.server.mapper.${className}Mapper;
import com.guet.clinic.server.service.${className}Service;
import org.springframework.stereotype.Service;
${extraImports}
@Service
public class ${className}ServiceImpl extends AbstractCrudService<${className}> implements ${className}Service {
    private final ${className}Mapper mapper;

    public ${className}ServiceImpl(${className}Mapper mapper) { this.mapper = mapper; }

    @Override
    protected CrudMapper<${className}> mapper() { return mapper; }
${extraMethods}}
`);
  }

  write(path.join(serverJava, "controller/CrudController.java"), `package com.guet.clinic.server.controller;

import com.guet.clinic.common.ApiResponse;
import com.guet.clinic.common.PageResponse;
import com.guet.clinic.pojo.BaseEntity;
import com.guet.clinic.server.service.CrudService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

public abstract class CrudController<T extends BaseEntity> {
            protected abstract CrudService<T> service();

    @GetMapping
    public ApiResponse<PageResponse<T>> page(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(required = false) String keyword) {
        return ApiResponse.ok(service().page(page, size, keyword));
    }

    @GetMapping("/list")
    public ApiResponse<List<T>> list(@RequestParam(required = false) String keyword) { return ApiResponse.ok(service().list(keyword)); }

    @GetMapping("/{id}")
    public ApiResponse<T> get(@PathVariable Long id) { return ApiResponse.ok(service().get(id)); }

    @PostMapping
    public ApiResponse<T> create(@Valid @RequestBody T entity) { return ApiResponse.ok(service().save(entity)); }

    @PutMapping("/{id}")
    public ApiResponse<T> update(@PathVariable Long id, @Valid @RequestBody T entity) { return ApiResponse.ok(service().update(id, entity)); }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) { service().delete(id); return ApiResponse.ok(); }
}
`);
  for (const [className, , route] of entities) {
    if (className === "ChargeOrder") {
      write(path.join(serverJava, "controller/ChargeOrderController.java"), `package com.guet.clinic.server.controller;

import com.guet.clinic.common.ApiResponse;
import com.guet.clinic.pojo.entity.ChargeOrder;
import com.guet.clinic.server.service.ChargeOrderService;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("/charge-orders")
public class ChargeOrderController extends CrudController<ChargeOrder> {
    private final ChargeOrderService service;

    public ChargeOrderController(ChargeOrderService service) { this.service = service; }

    @Override
    protected ChargeOrderService service() { return service; }

    @PostMapping("/{id}/pay")
    public ApiResponse<ChargeOrder> pay(@PathVariable Long id, @RequestBody PayRequest request) {
        return ApiResponse.ok(service.pay(id, request.paidAmount(), request.payMethod(), request.cashier()));
    }

    @PostMapping("/{id}/refund")
    public ApiResponse<ChargeOrder> refund(@PathVariable Long id, @RequestBody RefundRequest request) {
        return ApiResponse.ok(service.refund(id, request.refundAmount(), request.refundMethod()));
    }

    public record PayRequest(BigDecimal paidAmount, String payMethod, String cashier) {}
    public record RefundRequest(BigDecimal refundAmount, String refundMethod) {}
}
`);
    } else if (className === "Inventory") {
      write(path.join(serverJava, "controller/InventoryController.java"), `package com.guet.clinic.server.controller;

import com.guet.clinic.common.ApiResponse;
import com.guet.clinic.pojo.entity.Inventory;
import com.guet.clinic.server.service.InventoryService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/inventories")
public class InventoryController extends CrudController<Inventory> {
    private final InventoryService service;

    public InventoryController(InventoryService service) { this.service = service; }

    @Override
    protected InventoryService service() { return service; }

    @GetMapping("/warnings")
    public ApiResponse<List<Inventory>> warnings() { return ApiResponse.ok(service.warnings()); }
}
`);
    } else {
      write(path.join(serverJava, "controller", `${className}Controller.java`), `package com.guet.clinic.server.controller;

import com.guet.clinic.pojo.entity.${className};
import com.guet.clinic.server.service.${className}Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/${route}")
public class ${className}Controller extends CrudController<${className}> {
    private final ${className}Service service;

    public ${className}Controller(${className}Service service) { this.service = service; }

    @Override
    protected ${className}Service service() { return service; }
}
`);
    }
  }
}

function writeAppConfig() {
  write(path.join(serverJava, "ClinicServerApplication.java"), `package com.guet.clinic.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.guet.clinic.server.mapper")
@SpringBootApplication(scanBasePackages = "com.guet.clinic")
public class ClinicServerApplication {
    public static void main(String[] args) { SpringApplication.run(ClinicServerApplication.class, args); }
}
`);
  write(path.join(serverJava, "controller/AuthController.java"), `package com.guet.clinic.server.controller;

import com.guet.clinic.common.ApiResponse;
import com.guet.clinic.common.BusinessException;
import com.guet.clinic.pojo.entity.UserAccount;
import com.guet.clinic.server.service.UserAccountService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserAccountService userAccountService;

    public AuthController(UserAccountService userAccountService) { this.userAccountService = userAccountService; }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody LoginRequest request) {
        UserAccount user = userAccountService.list(request.username()).stream()
                .filter(item -> request.username().equals(item.getUsername()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("Invalid username or password"));
        if (!request.password().equals(user.getPassword())) throw new BusinessException("Invalid username or password");
        return ApiResponse.ok(Map.of("token", UUID.randomUUID().toString(), "user", user));
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
}
`);
  write(path.join(serverJava, "controller/StatisticsController.java"), `package com.guet.clinic.server.controller;

import com.guet.clinic.common.ApiResponse;
import com.guet.clinic.pojo.entity.ChargeOrder;
import com.guet.clinic.server.service.*;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/statistics")
public class StatisticsController {
    private final PatientService patientService;
    private final RegistrationService registrationService;
    private final ChargeOrderService chargeOrderService;
    private final DrugService drugService;
    private final MemberService memberService;

    public StatisticsController(PatientService patientService, RegistrationService registrationService, ChargeOrderService chargeOrderService, DrugService drugService, MemberService memberService) {
        this.patientService = patientService;
        this.registrationService = registrationService;
        this.chargeOrderService = chargeOrderService;
        this.drugService = drugService;
        this.memberService = memberService;
    }

    @GetMapping("/dashboard")
    public ApiResponse<Map<String, Object>> dashboard() {
        BigDecimal income = chargeOrderService.list(null).stream().map(ChargeOrder::getPaidAmount).filter(amount -> amount != null).reduce(BigDecimal.ZERO, BigDecimal::add);
        return ApiResponse.ok(Map.of("patientCount", patientService.count(null), "registrationCount", registrationService.count(null), "drugCount", drugService.count(null), "memberCount", memberService.count(null), "income", income));
    }
}
`);
  write(path.join(serverJava, "config/WebConfig.java"), `package com.guet.clinic.server.config;

import org.springframework.core.env.Environment;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final Environment environment;

    public WebConfig(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] allowedOrigins = environment.getProperty(
                "clinic.cors.allowed-origins",
                String[].class,
                new String[]{"http://localhost:5173", "http://127.0.0.1:5173"}
        );

        registry.addMapping("/**").allowedOrigins(allowedOrigins).allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS").allowedHeaders("*").allowCredentials(true);
    }
}
`);
  write(path.join(serverJava, "config/DataInitializer.java"), `package com.guet.clinic.server.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {
    // Initial data is managed by db/schema.sql and optional migration scripts.
}
`);
  write(path.join(serverJava, "exception/GlobalExceptionHandler.java"), `package com.guet.clinic.server.exception;

import com.guet.clinic.common.ApiResponse;
import com.guet.clinic.common.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> business(BusinessException exception) { return ApiResponse.fail(400, exception.getMessage()); }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> validation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream().findFirst().map(error -> error.getField() + " " + error.getDefaultMessage()).orElse("Validation failed");
        return ApiResponse.fail(400, message);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> unknown(Exception exception) { return ApiResponse.fail(500, exception.getMessage()); }
}
`);
}

function writeResources() {
  write(path.join(server, "src/main/resources/application.yml"), `server:
  port: \${clinic.server.port}
  servlet:
    context-path: \${clinic.server.context-path}

spring:
  profiles:
    active: dev
  application:
    name: \${clinic.application.name}
  datasource:
    driver-class-name: \${clinic.datasource.driver-class-name}
    url: jdbc:mysql://\${clinic.datasource.host}:\${clinic.datasource.port}/\${clinic.datasource.database}?useUnicode=true&characterEncoding=utf8&serverTimezone=\${clinic.datasource.server-timezone}&useSSL=false&allowPublicKeyRetrieval=true
    username: \${clinic.datasource.username}
    password: \${clinic.datasource.password}
  jackson:
    time-zone: \${clinic.jackson.time-zone}
    date-format: \${clinic.jackson.date-format}

mybatis:
  mapper-locations: classpath:mapper/*.xml
  configuration:
    map-underscore-to-camel-case: true
  type-aliases-package: com.guet.clinic.pojo.entity

`);
  write(path.join(server, "src/main/resources/application-dev.yml"), `clinic:
  server:
    port: 8080
    context-path: /api
  application:
    name: clinic-server
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    host: localhost
    port: 3306
    database: clinic_management
    username: root
    password: 1234
    server-timezone: Asia/Shanghai
  jackson:
    time-zone: Asia/Shanghai
    date-format: yyyy-MM-dd HH:mm:ss
  cors:
    allowed-origins:
      - http://localhost:5173
      - http://127.0.0.1:5173
`);

  const typeMap = {
    String: "VARCHAR(255)",
    Integer: "INT",
    Long: "BIGINT",
    Boolean: "TINYINT(1)",
    BigDecimal: "DECIMAL(12,2)",
    LocalDate: "DATE",
    LocalDateTime: "DATETIME",
  };
  const longText = new Set(["remark", "content", "medicalRecord", "prescription", "checkItems", "doctorAdvice", "chiefComplaint", "diagnosis", "introduction", "permissions"]);
  const shortString = new Set(["phone", "status", "gender", "roleName", "category", "unit", "payMethod", "refundMethod"]);
  const nameString = new Set(["name", "patientName", "doctorName", "departmentName", "drugName", "manufacturer", "supplierName", "clinicName", "principal"]);
  const sqlType = (type, name) => {
    if (longText.has(name)) return "TEXT";
    if (type === "String" && (name.endsWith("Code") || name.endsWith("No") || shortString.has(name))) return "VARCHAR(80)";
    if (type === "String" && nameString.has(name)) return "VARCHAR(150)";
    return typeMap[type];
  };
  const schema = [
    "CREATE DATABASE IF NOT EXISTS clinic_management DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;",
    "USE clinic_management;",
    "",
  ];
  for (const [, table, , , fields, required] of entities) {
    schema.push(`CREATE TABLE IF NOT EXISTS ${table} (`);
    schema.push("  id BIGINT PRIMARY KEY AUTO_INCREMENT,");
    for (const [type, name] of fields) {
      const notNull = required.includes(name) ? " NOT NULL" : "";
      const defaultValue = type === "Boolean" && name === "enabled" ? " DEFAULT 1" : "";
      schema.push(`  ${camelToSnake(name)} ${sqlType(type, name)}${notNull}${defaultValue},`);
    }
    schema.push("  created_at DATETIME NOT NULL,");
    schema.push("  updated_at DATETIME NOT NULL,");
    schema.push("  deleted TINYINT(1) DEFAULT 0,");
    schema.push("  INDEX idx_deleted (deleted)");
    schema.push(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;");
    schema.push("");
  }
  schema.push("INSERT INTO sys_user_account (username, password, name, phone, role_name, enabled, created_at, updated_at, deleted) SELECT 'admin', '123456', 'Administrator', '13800000000', 'ADMIN', 1, NOW(), NOW(), 0 WHERE NOT EXISTS (SELECT 1 FROM sys_user_account WHERE username = 'admin');");
  write(path.join(server, "src/main/resources/db/schema.sql"), `${schema.join("\n")}\n`);
}

function main() {
  if (!fs.existsSync(root) || path.basename(root) !== "clinic-menagement-system") {
    throw new Error(`Unexpected target path: ${root}`);
  }
  for (const dir of [pojoJava, entityDir, path.join(serverJava, "mapper"), path.join(serverJava, "service"), path.join(serverJava, "controller"), path.join(serverJava, "config"), path.join(serverJava, "exception")]) mkdirp(dir);
  const repo = path.join(serverJava, "repository");
  if (fs.existsSync(repo)) fs.rmSync(repo, { recursive: true, force: true });
  writePoms();
  writeCommonAndEntities();
  writeMappers();
  writeServicesAndControllers();
  writeAppConfig();
  writeResources();
  console.log(`Generated MyBatis backend in ${root}`);
}

main();

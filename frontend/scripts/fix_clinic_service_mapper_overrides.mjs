import fs from "node:fs";
import path from "node:path";

const implDir = "D:\\idea_code\\clinic-menagement-system\\clinic-server\\src\\main\\java\\com\\guet\\clinic\\server\\service\\impl";

for (const fileName of fs.readdirSync(implDir)) {
  if (!fileName.endsWith("ServiceImpl.java")) {
    continue;
  }
  const file = path.join(implDir, fileName);
  const before = fs.readFileSync(file, "utf8");
  if (!before.includes("extends AbstractCrudService<") || before.includes("protected CrudMapper<")) {
    continue;
  }
  const entity = before.match(/extends AbstractCrudService<([A-Za-z0-9_]+)>/);
  const mapperField = before.match(/private ([A-Za-z0-9_]+Mapper) mapper;/);
  if (!entity || !mapperField) {
    continue;
  }
  const entityType = entity[1];
  const after = before.replace(
    /private [A-Za-z0-9_]+Mapper mapper;\r?\n/,
    `private ${mapperField[1]} mapper;\r\n\r\n    @Override\r\n    protected CrudMapper<${entityType}> mapper() {\r\n        return mapper;\r\n    }\r\n`
  );
  fs.writeFileSync(file, after, "utf8");
}

import fs from "node:fs";
import path from "node:path";

const controllerDir = "D:\\idea_code\\clinic-menagement-system\\clinic-server\\src\\main\\java\\com\\guet\\clinic\\server\\controller";

for (const fileName of fs.readdirSync(controllerDir)) {
  if (!fileName.endsWith("Controller.java") || fileName === "CrudController.java") {
    continue;
  }
  const file = path.join(controllerDir, fileName);
  const before = fs.readFileSync(file, "utf8");
  if (!before.includes("extends CrudController<") || before.includes("protected ") && before.includes(" service()")) {
    continue;
  }
  const serviceField = before.match(/private ([A-Za-z0-9_]+Service) service;/);
  if (!serviceField) {
    continue;
  }
  const serviceType = serviceField[1];
  const after = before.replace(
    /private [A-Za-z0-9_]+Service service;\r?\n/,
    `private ${serviceType} service;\r\n\r\n    @Override\r\n    protected ${serviceType} service() {\r\n        return service;\r\n    }\r\n`
  );
  fs.writeFileSync(file, after, "utf8");
}

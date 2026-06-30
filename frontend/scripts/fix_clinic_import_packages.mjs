import fs from "node:fs";
import path from "node:path";

const root = "D:\\idea_code\\clinic-menagement-system\\clinic-server\\src\\main\\java";

function walk(dir) {
  const result = [];
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const full = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      result.push(...walk(full));
    } else if (entry.name.endsWith(".java")) {
      result.push(full);
    }
  }
  return result;
}

for (const file of walk(root)) {
  let content = fs.readFileSync(file, "utf8");
  const next = content
    .replace(/com\.guet\.clinic\.server\.[A-Za-z0-9_]+Service\./g, "com.guet.clinic.server.service.")
    .replace(/com\.guet\.clinic\.server\.[A-Za-z0-9_]+Mapper\./g, "com.guet.clinic.server.mapper.");

  if (next !== content) {
    fs.writeFileSync(file, next, "utf8");
  }
}

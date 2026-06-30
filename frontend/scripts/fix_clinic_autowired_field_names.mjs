import fs from "node:fs";
import path from "node:path";

const roots = [
  "D:\\idea_code\\clinic-menagement-system\\clinic-server\\src\\main\\java\\com\\guet\\clinic\\server\\controller",
  "D:\\idea_code\\clinic-menagement-system\\clinic-server\\src\\main\\java\\com\\guet\\clinic\\server\\service\\impl",
];

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

function lowerCamel(typeName) {
  return typeName.charAt(0).toLowerCase() + typeName.slice(1);
}

function escapeRegExp(value) {
  return value.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
}

function replaceFieldUsages(content, from, to) {
  const escaped = escapeRegExp(from);
  return content
    .replace(new RegExp(`\\breturn\\s+${escaped}\\s*;`, "g"), `return ${to};`)
    .replace(new RegExp(`\\b${escaped}\\.`, "g"), `${to}.`);
}

for (const root of roots) {
  for (const file of walk(root)) {
    let content = fs.readFileSync(file, "utf8");
    const matches = [...content.matchAll(/@Autowired\s+private\s+([A-Za-z0-9_]+)\s+([A-Za-z0-9_]+);/g)];
    for (const match of matches) {
      const typeName = match[1];
      const oldName = match[2];
      const newName = lowerCamel(typeName);
      if (oldName === newName) {
        continue;
      }
      content = content.replace(match[0], `@Autowired\r\n    private ${typeName} ${newName};`);
      content = replaceFieldUsages(content, oldName, newName);
    }
    fs.writeFileSync(file, content, "utf8");
  }
}

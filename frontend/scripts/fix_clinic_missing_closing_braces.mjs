import fs from "node:fs";
import path from "node:path";

const dirs = [
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

function count(content, char) {
  return [...content].filter((item) => item === char).length;
}

for (const dir of dirs) {
  for (const file of walk(dir)) {
    const before = fs.readFileSync(file, "utf8");
    const missing = count(before, "{") - count(before, "}");
    if (missing <= 0) {
      continue;
    }
    fs.writeFileSync(file, `${before.trimEnd()}\r\n${"}\r\n".repeat(missing)}`, "utf8");
  }
}

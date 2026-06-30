import fs from "node:fs";
import path from "node:path";

const serverRoot = "D:\\idea_code\\clinic-menagement-system\\clinic-server\\src\\main\\java\\com\\guet\\clinic\\server";
const targets = [
  path.join(serverRoot, "controller"),
  path.join(serverRoot, "service", "impl"),
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

function addAutowiredImport(content) {
  if (!content.includes("private final ") || content.includes("import org.springframework.beans.factory.annotation.Autowired;")) {
    return content;
  }
  return content.replace(/(package [^;]+;\r?\n\r?\n)/, "$1import org.springframework.beans.factory.annotation.Autowired;\r\n");
}

function removeConstructor(content, className) {
  const constructorPattern = new RegExp(`\\r?\\n\\s*public ${className}\\([^)]*\\)\\s*\\{[\\s\\S]*?\\r?\\n\\s*\\}`, "g");
  let next = content.replace(constructorPattern, "");
  const oneLinePattern = new RegExp(`\\r?\\n\\s*public ${className}\\([^)]*\\)\\s*\\{[^{}]*\\}`, "g");
  next = next.replace(oneLinePattern, "");
  return next;
}

function convertFields(content) {
  return content.replace(/^(\s*)private final ([^;]+);/gm, "$1@Autowired\r\n$1private $2;");
}

for (const dir of targets) {
  for (const file of walk(dir)) {
    const className = path.basename(file, ".java");
    const before = fs.readFileSync(file, "utf8");
    let after = addAutowiredImport(before);
    after = convertFields(after);
    after = removeConstructor(after, className);
    after = after.replace(/\r?\n{3,}/g, "\r\n\r\n");
    if (after !== before) {
      fs.writeFileSync(file, after, "utf8");
    }
  }
}

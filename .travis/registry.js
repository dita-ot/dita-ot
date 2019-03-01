const util = require("util");
const fs = require("fs");

const readdir = util.promisify(fs.readdir);
const readFile = util.promisify(fs.readFile);
const writeFile = util.promisify(fs.writeFile);

const distDir = process.argv[2] || "build/distributions";
const metadataDir = process.argv[3] || "registry";

async function update() {
  const files = await readdir(distDir);
  files.filter(file => file.endsWith(".sha256")).forEach(async checksumFile => {
    const [pluginName, version] = checksumFile
      .replace(/\.zip\.sha256$/, "")
      .split("-");
    const minorVersion = version
      .split(".")
      .slice(0, 2)
      .join(".");
    const checksum = (await readFile(
      `${distDir}/${checksumFile}`,
      "utf8"
    )).trim();
    const metadataFile = `${metadataDir}/${pluginName}.json`;
    let metadata = await readMetadata(metadataFile);
    if (metadata) {
      const last = metadata[metadata.length - 1];
      metadata.push({
        ...last,
        vers: version,
        deps: last.deps.map(dep => {
          return {
            ...dep,
            req: dep.name === "org.dita.base" ? `>=${version}` : dep.req
          };
        }),
        url: `https://github.com/dita-ot/dita-ot/releases/download/${version.endsWith('.0') ? minorVersion : version}/${pluginName}-${version}.zip`,
        cksum: checksum
      });
    } else {
      metadata = [
        {
          name: pluginName,
          description: "",
          keywords: [],
          homepage: "https://github.com/dita-ot/dita-ot/",
          vers: version,
          license: "Apache-2.0",
          deps: [
            {
              name: "org.dita.base",
              req: `>=${version}`
            }
          ],
          url: `https://github.com/dita-ot/dita-ot/releases/download/${version.endsWith('.0') ? minorVersion : version}/${pluginName}-${version}.zip`,
          cksum: checksum
        }
      ];
    }
    console.log("Write", metadataFile);
    await writeFile(metadataFile, JSON.stringify(metadata, null, 2));
  });
}

async function readMetadata(metadataFile) {
  try {
    let data = await readFile(metadataFile);
    return JSON.parse(data);
  } catch (e) {
    return null;
  }
}

update().catch(err => {
  throw err;
});

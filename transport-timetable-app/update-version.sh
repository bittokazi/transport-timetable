#!/bin/bash

# Define paths
APP_JSON="app.json"
VERSION_JS="common/version.js"

if [ ! -f "$APP_JSON" ]; then
  echo "Error: $APP_JSON not found!"
  exit 1
fi

# Extract version using Node.js (handles both app.version and root version fields)
eval "$(node -e '
  const fs = require("fs");
  try {
    const raw = fs.readFileSync("./../info.json", "utf8");
    const data = JSON.parse(raw);
    const version = data.appVersion.name;
    if (version) {
      console.log(`VERSION="${version}"`);
    } else {
      console.error("Version field not found in info.json");
      process.exit(1);
    }
    const buildVersion = data.appVersion.buildVersion;
    if (buildVersion) {
      console.log(`BUILD_VERSION="${buildVersion}"`);
    }
  } catch (e) {
    console.error("Failed to parse info.json:", e.message);
    process.exit(1);
  }
')"

if [ $? -ne 0 ]; then
  exit 1
fi

# Create directory if it doesn't exist
mkdir -p "$(dirname "$VERSION_JS")"

# Write out the new version file
cat << EOF > "$VERSION_JS"
// This file is auto-generated. Do not edit manually.
export const APP_VERSION = "$VERSION";
export const BUILD_VERSION = "$BUILD_VERSION";
EOF

echo "Successfully updated $VERSION_JS to version v$VERSION (build $BUILD_VERSION)"

#!/usr/bin/env bash
set -euo pipefail
MC="26.3"
LOADER="0.19.5"
INSTALLER="https://maven.fabricmc.net/net/fabricmc/fabric-installer/1.0.0/fabric-installer-1.0.0.jar"

if [[ ! -f fabric-installer.jar ]]; then
  curl -fLo fabric-installer.jar "$INSTALLER"
fi

java -jar fabric-installer.jar server -mcversion "$MC" -loader "$LOADER" -dir .

echo "Fabric server ready. Put mods into mods/ and configs as described in README.md."

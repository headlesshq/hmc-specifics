#!/bin/bash

set -eo pipefail

sudo chown "$(id -u):$(id -g)" "$GRADLE_USER_HOME"

if ! command -v jq > /dev/null; then
  sudo apt-get -o Acquire::Retries=5 update
  sudo apt-get -o Acquire::Retries=5 install -y --no-install-recommends jq
fi

# versions/1_17 builds with Java 16 in CI. SDKMAN has no Java 16 at all, so
# install the final Temurin 16 release from Adoptium next to the SDKMAN JDKs.
jdk16="/usr/local/sdkman/candidates/java/16.0.2-tem"
if [ ! -x "$jdk16/bin/java" ]; then
  case "$(uname -m)" in
    x86_64)
      arch=x64
      sha=323d6d7474a359a28eff7ddd0df8e65bd61554a8ed12ef42fd9365349e573c2c
      ;;
    aarch64)
      arch=aarch64
      sha=cb77d9d126f97898dfdc8b5fb694d1e0e5d93d13a0a6cb2aeda76f8635384340
      ;;
    *)
      arch=
      echo "No Temurin 16 for $(uname -m), skipping" >&2
      ;;
  esac
fi

if [ ! -x "$jdk16/bin/java" ] && [ -n "$arch" ]; then
  tmp=$(mktemp -d)
  trap 'rm -rf "$tmp"' EXIT
  curl -fsSL --retry 5 --retry-delay 5 --retry-all-errors --connect-timeout 10 \
    -o "$tmp/jdk16.tar.gz" \
    "https://github.com/adoptium/temurin16-binaries/releases/download/jdk-16.0.2%2B7/OpenJDK16U-jdk_${arch}_linux_hotspot_16.0.2_7.tar.gz"
  echo "$sha  $tmp/jdk16.tar.gz" | sha256sum -c -
  sudo mkdir -p "$jdk16"
  sudo tar -xzf "$tmp/jdk16.tar.gz" -C "$jdk16" --strip-components=1
fi

# Stable per-major JDK paths (/opt/java/<major>) for the VS Code settings in
# devcontainer.json, independent of the exact SDKMAN version directories.
sudo mkdir -p /opt/java
for major in 8 16 17 21 25; do
  jdk=$(find /usr/local/sdkman/candidates/java -mindepth 1 -maxdepth 1 \
    -name "$major.*" -printf '%f\n' | sort -V | tail -n 1)
  [ -z "$jdk" ] || sudo ln -sfn "/usr/local/sdkman/candidates/java/$jdk" "/opt/java/$major"
done

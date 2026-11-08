#!/bin/bash
# Builds one Gradle build of this repo with the JDK that CI uses for it.
#
#   .devcontainer/use-java.sh <dir|mc-version> [gradle args...]
#
# <dir> is a directory from ci-data.json, e.g. hmc-specifics-api, versions/1_12
# or just 1_12; it builds with the defaults from its gradle.properties.
# <mc-version>, e.g. 1.21.4, picks the matching ci-data.json entry and passes
# its minecraft/lexforge/neoforge versions like CI does.
# Gradle args default to "build".

set -eo pipefail

if [ $# -lt 1 ]; then
  sed -n '2,10s/^# \{0,1\}//p' "$0" >&2
  exit 2
fi

root=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
target=$1
shift
[ $# -gt 0 ] || set -- build

entry=$(jq -c --arg t "$target" '
  [.build_data[] | select(.mc == $t)][0]
  // [.build_data[] | select(.dir == $t or .dir == "versions/" + $t)
                    | del(.mc, .lex, .neo)][0]
  // empty' "$root/ci-data.json")
if [ -z "$entry" ]; then
  echo "No entry for '$target' in ci-data.json" >&2
  exit 1
fi

dir=$(jq -r .dir <<< "$entry")
java=$(jq -r .java <<< "$entry")
props=()
for p in mc:minecraft_version lex:lexforge_version neo:neoforge_version; do
  value=$(jq -r --arg k "${p%%:*}" '.[$k] // empty' <<< "$entry")
  [ -z "$value" ] || props+=("-P${p#*:}=$value")
done

candidates=${SDKMAN_DIR:-/usr/local/sdkman}/candidates/java
jdk=$(find "$candidates" -mindepth 1 -maxdepth 1 -name "$java.*" -printf '%f\n' | sort -V | tail -n 1)
if [ -z "$jdk" ]; then
  echo "No Java $java in $candidates" >&2
  exit 1
fi

export JAVA_HOME="$candidates/$jdk"
export PATH="$JAVA_HOME/bin:$PATH"
echo "Building $dir with Java $java ($JAVA_HOME): ${props[*]} $*" >&2
cd "$root/$dir"
exec sh ./gradlew "${props[@]}" "$@"

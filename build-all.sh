#!/usr/bin/env bash
# Builds GenCount for every version and drops all jars into ./artifacts
#   JAVA_HOME         -> JDK 25 (26.x builds)
#   LEGACY_JAVA_HOME  -> JDK 8 or 17 (1.8.9 Forge build; Gradle 7 can't run on 25)
set -euo pipefail
cd "$(dirname "$0")"

: "${LEGACY_JAVA_HOME:?Set LEGACY_JAVA_HOME to a JDK 8 or 17 for the 1.8.9 build}"

rm -rf artifacts
mkdir -p artifacts

echo "== 26.x (Fabric) =="
chmod +x gradlew
./gradlew buildAll
cp build/libs/*.jar artifacts/

echo "== 1.8.9 (Forge) =="
(
  cd legacy-1.8.9
  chmod +x gradlew
  JAVA_HOME="$LEGACY_JAVA_HOME" ./gradlew build
  for f in build/libs/gencount-1.8.9-*.jar; do
    case "$f" in *-dev.jar|*-sources*.jar) ;; *) cp "$f" ../artifacts/ ;; esac
  done
)

echo
echo "All jars:"
ls -1 artifacts

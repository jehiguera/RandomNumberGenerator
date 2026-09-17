#!/bin/sh

# Lightweight bootstrap wrapper for CI. Downloads a pinned Gradle distribution
# and delegates all arguments to it. No binary wrapper JAR is stored in the repo.
set -eu
GRADLE_VERSION=8.10.2
BASE_DIR="${HOME}/.gradle/manual-wrapper/gradle-${GRADLE_VERSION}"
if [ ! -x "${BASE_DIR}/bin/gradle" ]; then
  TMP="${RUNNER_TEMP:-/tmp}/gradle-${GRADLE_VERSION}.zip"
  curl -fsSL "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" -o "$TMP"
  mkdir -p "$(dirname "$BASE_DIR")"
  unzip -q -o "$TMP" -d "$(dirname "$BASE_DIR")"
fi
exec "${BASE_DIR}/bin/gradle" "$@"

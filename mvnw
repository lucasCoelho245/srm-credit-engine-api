#!/bin/sh
# Maven Wrapper script
# Baixa Maven se não estiver disponível localmente
set -e

MAVEN_VERSION="3.9.6"
MAVEN_HOME="$HOME/.m2/wrapper/dists/apache-maven-${MAVEN_VERSION}"
MVN_CMD="$MAVEN_HOME/bin/mvn"

if [ ! -f "$MVN_CMD" ]; then
  echo "Downloading Maven ${MAVEN_VERSION}..."
  mkdir -p "$MAVEN_HOME"
  curl -fsSL "https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz" \
    | tar -xz -C "$HOME/.m2/wrapper/dists/" 2>/dev/null || true
fi

if command -v mvn > /dev/null 2>&1; then
  exec mvn "$@"
elif [ -f "$MVN_CMD" ]; then
  exec "$MVN_CMD" "$@"
else
  echo "ERROR: Maven not found. Install Maven 3.9+ or run: sudo apt-get install maven"
  exit 1
fi

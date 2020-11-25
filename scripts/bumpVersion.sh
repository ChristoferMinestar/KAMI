#!/bin/bash

# Created by l1ving on 17/02/20
#
# ONLY USED IN AUTOMATED BUILDS
#
# Usage: "./bumpVersion.sh <major or empty>"
# First argument can be empty or "major"

# Get version numbers
__dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/version.sh"
VERSION=$("$__dir" "$1") || exit $?
VERSION_SIMPLE=$("$__dir" "$1" "simple") || exit $?
VERSION_MAJOR=$("$__dir" "major") || exit $?

# Get and parse build number
BUILD_NUMBER_PREVIOUS=$(curl -s https://kamiblue.org/api/v1/builds)
BUILD_NUMBER=$((BUILD_NUMBER_PREVIOUS + 1))

if [ "$BUILD_NUMBER" == "$BUILD_NUMBER_PREVIOUS" ]; then
  echo "[bumpBuildNumber] Failed to bump build number, exiting."
  exit 1
fi

if [[ ! "$BUILD_NUMBER" =~ ^-?[0-9]+$ ]]; then
  echo "[bumpBuildNumber] Could not parse '$BUILD_NUMBER' as an Int, exiting."
  exit 1
fi

# Set above information
sed -i "s/modVersion=.*/modVersion=$VERSION/" gradle.properties
sed -i "s/VERSION = \".*\";/VERSION = \"$VERSION\";/" src/main/java/me/zeroeightsix/kami/KamiMod.java
sed -i "s/VERSION_SIMPLE = \".*\";/VERSION_SIMPLE = \"$VERSION_SIMPLE\";/" src/main/java/me/zeroeightsix/kami/KamiMod.java
sed -i "s/VERSION_MAJOR = \".*\";/VERSION_MAJOR = \"$VERSION_MAJOR\";/" src/main/java/me/zeroeightsix/kami/KamiMod.java
sed -i "s/BUILD_NUMBER = .*;/BUILD_NUMBER = $BUILD_NUMBER;/" src/main/java/me/zeroeightsix/kami/KamiMod.java
sed -i "s/\"version\": \".*\",/\"version\": \"$VERSION\",/" src/main/resources/mcmod.info
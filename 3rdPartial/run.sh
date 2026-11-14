#!/usr/bin/env bash

set -e

# Run from the directory where this script is located.
cd "$(dirname "$0")"

PRACTICE="${1:?Usage: ./run.sh <practice>}"

BUILD_DIR="build/$PRACTICE"

# Paths to the shared and current engines.
SECOND_ENGINE="../2ndPartial/engine/src"
THIRD_ENGINE="engine/src"

# Validate the practice directory.
if [[ ! -d "$PRACTICE" ]]; then
    echo "Error: Practice '$PRACTICE' does not exist."
    exit 1
fi

# Validate the second partial engine.
if [[ ! -d "$SECOND_ENGINE" ]]; then
    echo "Error: Second partial engine not found."
    exit 1
fi

# Recreate the output directory.
rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR"

echo "Compiling $PRACTICE..."

# Collect all Java source files.
mapfile -t SOURCES < <(
    find "$SECOND_ENGINE" "$THIRD_ENGINE" "$PRACTICE" \
        -name "*.java"
)

# Compile both engines and the selected practice.
javac -d "$BUILD_DIR" "${SOURCES[@]}"

echo "Running $PRACTICE..."

java -cp "$BUILD_DIR" Main

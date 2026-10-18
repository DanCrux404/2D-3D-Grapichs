
#!/usr/bin/env bash

set -e

# Run from the directory where this script is located.
cd "$(dirname "$0")"

PRACTICE="${1:?Usage: ./run.sh <practice>}"

BUILD_DIR="build/$PRACTICE"

# Validate that the practice directory exists.
if [[ ! -d "$PRACTICE" ]]; then
    echo "Error: Practice '$PRACTICE' does not exist."
    exit 1
fi

# Recreate the output directory.
rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR"

echo "Compiling $PRACTICE..."

# Compile the engine and the selected practice.
javac -d "$BUILD_DIR" \
    $(find engine/src -name "*.java") \
    "$PRACTICE"/*.java

echo "Running $PRACTICE..."

java -cp "$BUILD_DIR" Main


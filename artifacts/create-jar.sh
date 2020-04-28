#!/usr/bin/env bash

# Author: Vladislav Yaroshchuk
# Website: github.com/shchuko
# First arg - main class name (can be empty)
# Other args - path to search for sources

if [[ $# -lt 2 ]]; then
  echo "Not enough arguments";
  exit 1;
fi;

MANIFEST_FILE_PATH=$(mktemp -t MANIFEST-XXXXXXXXXXXX);
JAVAC_OUTPUT_PATH=$(mktemp -d -t javac-out-XXXXXXXXXXXX);

MAIN_CLASS=$1;
for SOURCE_SEARCH_PATH in "${@:2}"; do
  SOURCE_SEARCH_PATH="$(readlink -f "$SOURCE_SEARCH_PATH")";
  JAVA_SOURCES_LIST="$(find "$SOURCE_SEARCH_PATH" -name "*.java" 2>/dev/null) $JAVA_SOURCES_LIST";
done

MANIFEST="Manifest-Version: 1.0"$'\n'
if [[ -n $MAIN_CLASS ]]; then
  JAR_NAME=$(echo "$MAIN_CLASS" | sed -e 's/.*\.//g')".jar"
  MANIFEST="$MANIFEST""Main-Class: $MAIN_CLASS"$'\n';
else
  JAR_NAME="JarRes-"$(date "+%Y-%m-%d_%H-%M-%S_%3N");
fi
echo "$MANIFEST" >"$MANIFEST_FILE_PATH";

echo "MAIN CLASS: $MAIN_CLASS";
echo "JAR TO BE CREATED: $PWD/$JAR_NAME";

SAVED_PWD=$PWD;
# shellcheck disable=SC2086
javac -d "$JAVAC_OUTPUT_PATH" $JAVA_SOURCES_LIST &&
cd "$JAVAC_OUTPUT_PATH" &&
COMPILED_FILES="$(find . -name '*.class')" &&
jar cfm "$SAVED_PWD/$JAR_NAME" "$MANIFEST_FILE_PATH" $COMPILED_FILES &&
RETURN_CODE=0 ||
RETURN_CODE=1;

if [[ $RETURN_CODE -eq 0 ]]; then
  echo "JAR CREATION SUCCEED";
else
  echo "JAR CREATION ERRORED";
fi;

rm -rf JAVAC_OUTPUT_PATH;
rm -f MANIFEST_FILE_PATH;

exit $RETURN_CODE;

#!/bin/env bash

# Author: Vladislav Yaroshchuk
# Website: github.com/shchuko

TEST_NAME="$1";
TASK_SRC_DIRS="$2";
BIN_ROOT="$3";
JAVAC_COMPILE_FLAGS="$4";
RUN_TEST_COMMAND="$5";
PROJECT_ROOT_DIR="$PWD";

echo "######################################";
echo "Exec test: $TEST_NAME";

if [[ -z $TASK_SRC_DIRS ]]; then
    echo "Task sources path is empty";
    exit 1;
else
    echo "Task sources dirs: $TASK_SRC_DIRS";
fi;

if [[ -z $BIN_ROOT ]]; then
    echo "Task binaries out path is empty";
    exit 1;
else
    echo "Task binaries out path: $BIN_ROOT";
fi;


echo "-------------------------";
echo "Compiling, javac flags: $JAVAC_COMPILE_FLAGS";

SRC_DIRS_ARRAY=$(echo "$TASK_SRC_DIRS" |  tr "," " ");
for SOURCE_DIR in $SRC_DIRS_ARRAY; do
  JAVA_SOURCES_LIST="$(find "$SOURCE_DIR/" -name "*.java") $JAVA_SOURCES_LIST";
done;

if [[ -z $JAVAC_COMPILE_FLAGS ]]; then
  # shellcheck disable=SC2086
  javac -d $BIN_ROOT $JAVA_SOURCES_LIST;
else
  # shellcheck disable=SC2086
  javac -d $BIN_ROOT $JAVAC_COMPILE_FLAGS $JAVA_SOURCES_LIST;
fi;

echo "-------------------------";
echo "Starting tests, command: $RUN_TEST_COMMAND";
cd "$BIN_ROOT" || (echo "['cd' ERR] Bin root not found" && exit 1);
eval "$RUN_TEST_COMMAND";

echo "-------------------------";
CLEAN_COMMAND="git clean -f -d";
echo "Cleaning ($CLEAN_COMMAND)...";
cd "$PROJECT_ROOT_DIR" || (echo "['cd' ERR] Couldn't return to project root" && exit 1);
eval "$CLEAN_COMMAND";

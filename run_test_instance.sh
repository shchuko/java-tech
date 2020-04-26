#!/bin/env bash

TASK_SRC_DIR="$1";
BIN_ROOT="$2";
JAVAC_COMPILE_FLAGS="$3";
RUN_TEST_COMMAND="$4";
TEST_NAME="$5";
PROJECT_ROOT_DIR="$PWD";

echo;
echo "######################################";
echo "Exec test: $TEST_NAME";

if [[ -z $TASK_SRC_DIR ]]; then
    echo "Task sources path is empty";
    exit 1;
else
    echo "Task sources path: $TASK_SRC_DIR";
fi;

if [[ -z $BIN_ROOT ]]; then
    echo "Task binaries out path is empty";
    exit 1;
else
    echo "Task binaries out path: $BIN_ROOT";
fi;


echo "-------------------------";
echo "Compiling, javac flags: $JAVAC_COMPILE_FLAGS";
if [[ -z $JAVAC_COMPILE_FLAGS ]]; then
  javac -d "$BIN_ROOT" "$(find "$TASK_SRC_DIR/"  -name "*.java")";
else
  javac -d "$BIN_ROOT" "$JAVAC_COMPILE_FLAGS" "$(find "$TASK_SRC_DIR/"  -name "*.java")";
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

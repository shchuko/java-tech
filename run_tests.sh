#!/usr/bin/env bash

TESTS_INFO_PATH="./test_info.txt";

# SOURCES_DIR:BUILD_OUT_DIR:JAVAC_FLAGS:RUN_TESTING_COMMAND:TEST_NAME
awk -F ':' 'NF == 5 {
    system("bash ./run_test_instance.sh " "\"" $1 "\" \"" $2 "\" \"" $3 "\" \"" $4 "\" \"" $5 "\"");
  }' "$TESTS_INFO_PATH";

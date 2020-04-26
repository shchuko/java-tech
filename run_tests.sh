#!/usr/bin/env bash

TESTS_INFO_PATH="./test_info.txt";

awk -F ':' '{
    system("bash ./run_test_instance.sh " "\"" $1 "\" \"" $2 "\" \"" $3 "\" \"" $4 "\"");
  }' "$TESTS_INFO_PATH"

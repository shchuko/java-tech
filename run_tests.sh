#!/usr/bin/env bash

TESTS_INFO_PATH="./test_info.txt";

sed "s/#.*$//g" "$TESTS_INFO_PATH" |
awk -F ':' 'NF == 5 {
    system("bash ./run_test_instance.sh " "\"" $1 "\" \"" $2 "\" \"" $3 "\" \"" $4 "\" \"" $5 "\"");
    system("echo;");
  }';

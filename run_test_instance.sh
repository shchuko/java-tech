TASK_SRC_DIR="$1"
BIN_ROOT="$2"
JAVAC_COMPILE_FLAGS="$3"
RUN_TEST_COMMAND="$4"

echo "New target: ${TASK_SRC_DIR}"

echo "Compiling, javac flags: ${JAVAC_COMPILE_FLAGS}"
if [[ -z $JAVAC_COMPILE_FLAGS ]]; then
  javac -d "$BIN_ROOT" "$(find "${TASK_SRC_DIR}/"  -name "*.java")";
else
  javac -d "$BIN_ROOT" "$JAVAC_COMPILE_FLAGS" "$(find "${TASK_SRC_DIR}/"  -name "*.java")";
fi;

echo "Starting tests, command: ${RUN_TEST_COMMAND}"
cd "$BIN_ROOT" || (echo "Bin root not found" && exit 1);
eval "$RUN_TEST_COMMAND"

git clean -f -d;

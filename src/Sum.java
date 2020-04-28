/**
 * CLI Sum calculator
 */
public class Sum {

    /**
     * Prints sum of passed arguments
     * @param args List of numbers
     */
    public static void main(String[] args) {
        int sum = 0;
        for (String arg : args) {
            int numBegin = -1;
            for (int i = 0; i < arg.length(); ++i) {
                if (Character.isWhitespace(arg.charAt(i))) {
                    if (numBegin != -1) {
                        sum += Long.parseLong(arg.substring(numBegin, i));
                        numBegin = -1;
                    }
                } else {
                    if (numBegin == -1) {
                        numBegin = i;
                    }
                }
            }

            if (numBegin != -1) {
                sum += Long.parseLong(arg.substring(numBegin));
            }

        }

        System.out.println(sum);
    }

}
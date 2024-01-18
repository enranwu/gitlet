package gitlet;

import java.io.IOException;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Enran Wu
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                Commands.init();
                break;
            case "add":
                validateNumArgs("add", args, 2);
                Commands.add(args[1]);
                break;
            case "commit":
                validateNumArgs("commit", args, 2);
                Commands.commit(args[1]);
                break;
            case "rm":
                validateNumArgs("rm", args, 2);
                Commands.rm(args[1]);
                break;
            case "log":
                validateNumArgs("log", args, 1);
                Commands.log();
                break;
            case "global-log":
                validateNumArgs("global-log", args, 1);
                Commands.globalLog();
                break;
            case "find":
                validateNumArgs("find", args, 2);
                Commands.find(args[1]);
                break;
            case "status":
                validateNumArgs("status", args, 1);
                Commands.status();
                break;
            case "checkout":
                if (args.length == 3 && args[1].equals("--")) {
                    Commands.checkout(args[2]);
                    break;
                } else if (args.length == 4 && args[2].equals("--")) {
                    Commands.checkout(args[1], args[3]);
                    break;
                } else if (args.length == 2){
                    Commands.checkoutBranch(args[1]);
                    break;
                } else {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
            case "branch":
                validateNumArgs("branch", args, 2);
                Commands.branch(args[1]);
                break;
            case "rm-branch":
                validateNumArgs("rm-branch", args, 2);
                Commands.rmBranch(args[1]);
                break;
            case "reset":
                validateNumArgs("reset", args, 2);
                Commands.reset(args[1]);
                break;
            case "merge":
                validateNumArgs("merge", args, 2);
                Commands.merge(args[1]);
            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
        }
    }

    /**
     * Checks the number of arguments versus the expected number,
     * throws a RuntimeException if they do not match.
     *
     * @param cmd Name of command you are validating
     * @param args Argument array from command line
     * @param n Number of expected arguments
     * NOTE: this method is taking directly from lab6 but with added init checker
     */
    public static void validateNumArgs(String cmd, String[] args, int n) {
        if (args.length != n) {
            throw new RuntimeException(
                    String.format("Invalid number of arguments for: %s.", cmd));
        }
        if (!Repository.GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }
}

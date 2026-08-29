import java.util.Scanner;

/**
 * Handles Plana's console input and user-facing interface formatting.
 */
public class Ui implements AutoCloseable {
    private static final String BORDER_LINE = "____________________________________________________________";

    private final Scanner scanner;

    /**
     * Creates a console user interface backed by standard input.
     */
    public Ui() {
        scanner = new Scanner(System.in);
    }

    /**
     * Displays Plana's startup banner and greeting.
     *
     * @param banner the text banner to display
     * @param bannerArt the decorative banner art to display
     */
    public void showWelcome(String banner, String bannerArt) {
        showLine();
        System.out.print(banner);
        System.out.print(bannerArt);
        System.out.println("Hi hi! I'm Plana.");
        System.out.println("What shall we get done today?");
        showLine();
    }

    /**
     * Checks whether another command is available from the user.
     *
     * @return true when another input line is available
     */
    public boolean hasNextCommand() {
        return scanner.hasNextLine();
    }

    /**
     * Reads and echoes one command, including the separator printed after it.
     *
     * @return the command entered by the user
     */
    public String readCommand() {
        String command = scanner.nextLine();
        System.out.println(command);
        showLine();
        return command;
    }

    /**
     * Displays the available commands.
     */
    public void showHelp() {
        System.out.println("Don't worry! I am always here to help :>");
        System.out.println("Here's what I can do:");
        System.out.println("  todo <description>                          add a task");
        System.out.println("  deadline <description> /by <date>           add a deadline");
        System.out.println("  event <description> /from <start> /to <end> add an event");
        System.out.println("  on <date>                                    show deadlines/events on a date");
        System.out.println("  list                                        show all tasks");
        System.out.println("  delete <number>                             delete a task");
        System.out.println("  mark <number>                               mark a task as done");
        System.out.println("  unmark <number>                             mark a task as not done");
        System.out.println("  help or ?                                   show this help");
        System.out.println("  bye                                         say goodbye");
    }

    /**
     * Displays Plana's goodbye message.
     */
    public void showGoodbye() {
        System.out.println("Bye-bye! See you next time, okay?");
        showLine();
    }

    /**
     * Displays an error message followed by the standard separator.
     *
     * @param message the user-facing error message
     */
    public void showError(String message) {
        System.out.println(message);
        showLine();
    }

    /**
     * Displays the standard separator used between console interactions.
     */
    public void showLine() {
        System.out.println(BORDER_LINE);
    }

    /**
     * Closes the input scanner when the application exits.
     */
    @Override
    public void close() {
        scanner.close();
    }
}

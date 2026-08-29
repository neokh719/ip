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
     * Displays every task in its current order.
     *
     * @param tasks the tasks to display
     */
    public void showTaskList(TaskList tasks) {
        System.out.println(" Here are your tasks:");
        for (int i = 0; i < tasks.size(); i++) {
            showTask(i, tasks.get(i));
        }
        showLine();
    }

    /**
     * Displays a task using the one-based number shown to the user.
     *
     * @param zeroBasedIndex the task's zero-based index
     * @param task the task to display
     */
    public void showTask(int zeroBasedIndex, Task task) {
        System.out.println(" " + (zeroBasedIndex + 1) + "." + task);
    }

    /**
     * Displays the heading for a date-filtered task query.
     *
     * @param displayDate the date formatted for display
     */
    public void showTasksOnDateHeader(String displayDate) {
        System.out.println(" Here are the deadlines and events on " + displayDate + ":");
    }

    /**
     * Displays the empty result message for a date-filtered task query.
     *
     * @param displayDate the date formatted for display
     */
    public void showNoTasksOnDate(String displayDate) {
        System.out.println(" No deadlines or events found on " + displayDate + ".");
    }

    /**
     * Displays the confirmation for adding a task.
     *
     * @param task the newly added task
     * @param taskCount the number of tasks after adding
     */
    public void showTaskAdded(Task task, int taskCount) {
        System.out.println("Yay, I've added this task:");
        System.out.println("  " + task);
        System.out.println("Now you have " + taskCount + (taskCount == 1 ? " task" : " tasks")
                + " in your list!");
        showLine();
    }

    /**
     * Displays the confirmation for deleting a task.
     *
     * @param task the deleted task
     * @param remainingTaskCount the number of tasks after deletion
     */
    public void showTaskDeleted(Task task, int remainingTaskCount) {
        System.out.println("Noted. I've removed this task:");
        System.out.println("  " + task);
        System.out.println("Now you have " + remainingTaskCount
                + (remainingTaskCount == 1 ? " task" : " tasks") + " in the list.");
        showLine();
    }

    /**
     * Displays the confirmation for marking a task as done.
     *
     * @param task the completed task
     */
    public void showTaskMarkedDone(Task task) {
        System.out.println("Yay! I've marked this task as done:");
        System.out.println("  " + task);
        showLine();
    }

    /**
     * Displays the confirmation for marking a task as not done.
     *
     * @param task the task marked as not done
     */
    public void showTaskMarkedNotDone(Task task) {
        System.out.println("No worries! I've marked this task as not done:");
        System.out.println("  " + task);
        showLine();
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

package plana.ui;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Objects;
import java.util.Scanner;

import plana.task.Task;
import plana.task.TaskList;

/**
 * Handles Plana's console input and user-facing interface formatting.
 */
public class Ui implements AutoCloseable {
    private static final String BORDER_LINE = "____________________________________________________________";

    private final Scanner scanner;
    private final PrintStream output;
    private final boolean showSeparators;

    /**
     * Creates a console user interface backed by standard input.
     */
    public Ui() {
        this(new Scanner(System.in), System.out, true);
    }

    /**
     * Creates a response-only interface that writes to the supplied stream.
     * This mode is used by the JavaFX interface to reuse Plana's existing
     * command response formatting without reading from standard input.
     *
     * @param output the stream that receives user-facing responses.
     */
    public Ui(PrintStream output) {
        this(new Scanner(InputStream.nullInputStream()), output, false);
    }

    private Ui(Scanner scanner, PrintStream output, boolean showSeparators) {
        this.scanner = scanner;
        this.output = Objects.requireNonNull(output);
        this.showSeparators = showSeparators;
    }

    /**
     * Displays Plana's startup banner and greeting.
     *
     * @param banner the text banner to display.
     * @param bannerArt the decorative banner art to display.
     */
    public void showWelcome(String banner, String bannerArt) {
        showLine();
        output.print(banner);
        output.print(bannerArt);
        output.println("Hi hi! I'm Plana.");
        output.println("What shall we get done today?");
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
        output.println(command);
        showLine();
        return command;
    }

    /**
     * Displays the available commands.
     */
    public void showHelp() {
        output.println("Don't worry! I am always here to help :>");
        output.println("Here's what I can do:");
        output.println("  todo <description>                          add a task");
        output.println("  deadline <description> /by <date>           add a deadline");
        output.println("  event <description> /from <start> /to <end> add an event");
        output.println("  on <date>                                    show deadlines/events on a date");
        output.println("  find <keyword>                                find tasks by description");
        output.println("  list                                        show all tasks");
        output.println("  delete <number>                             delete a task");
        output.println("  mark <number>                               mark a task as done");
        output.println("  unmark <number>                             mark a task as not done");
        output.println("  help or ?                                   show this help");
        output.println("  bye                                         say goodbye");
    }

    /**
     * Displays every task in its current order.
     *
     * @param tasks the tasks to display.
     */
    public void showTaskList(TaskList tasks) {
        output.println(" Here are your tasks:");
        for (int i = 0; i < tasks.size(); i++) {
            showTask(i, tasks.get(i));
        }
        showLine();
    }

    /**
     * Displays the heading for a keyword-filtered task query.
     */
    public void showMatchingTasksHeader() {
        output.println(" Here are the matching tasks in your list:");
    }

    /**
     * Displays the empty result message for a keyword-filtered task query.
     */
    public void showNoMatchingTasks() {
        output.println(" No matching tasks found.");
    }

    /**
     * Displays a task using the one-based number shown to the user.
     *
     * @param zeroBasedIndex the task's zero-based index.
     * @param task the task to display.
     */
    public void showTask(int zeroBasedIndex, Task task) {
        output.println(" " + (zeroBasedIndex + 1) + "." + task);
    }

    /**
     * Displays the heading for a date-filtered task query.
     *
     * @param displayDate the date formatted for display.
     */
    public void showTasksOnDateHeader(String displayDate) {
        output.println(" Here are the deadlines and events on " + displayDate + ":");
    }

    /**
     * Displays the empty result message for a date-filtered task query.
     *
     * @param displayDate the date formatted for display.
     */
    public void showNoTasksOnDate(String displayDate) {
        output.println(" No deadlines or events found on " + displayDate + ".");
    }

    /**
     * Displays the confirmation for adding a task.
     *
     * @param task the newly added task.
     * @param taskCount the number of tasks after adding.
     */
    public void showTaskAdded(Task task, int taskCount) {
        output.println("Yay, I've added this task:");
        output.println("  " + task);
        output.println("Now you have " + taskCount + (taskCount == 1 ? " task" : " tasks")
                + " in your list!");
        showLine();
    }

    /**
     * Displays the confirmation for deleting a task.
     *
     * @param task the deleted task.
     * @param remainingTaskCount the number of tasks after deletion.
     */
    public void showTaskDeleted(Task task, int remainingTaskCount) {
        output.println("Noted. I've removed this task:");
        output.println("  " + task);
        output.println("Now you have " + remainingTaskCount
                + (remainingTaskCount == 1 ? " task" : " tasks") + " in the list.");
        showLine();
    }

    /**
     * Displays the confirmation for marking a task as done.
     *
     * @param task the completed task.
     */
    public void showTaskMarkedDone(Task task) {
        output.println("Yay! I've marked this task as done:");
        output.println("  " + task);
        showLine();
    }

    /**
     * Displays the confirmation for marking a task as not done.
     *
     * @param task the task marked as not done.
     */
    public void showTaskMarkedNotDone(Task task) {
        output.println("No worries! I've marked this task as not done:");
        output.println("  " + task);
        showLine();
    }

    /**
     * Displays Plana's goodbye message.
     */
    public void showGoodbye() {
        output.println("Bye-bye! See you next time, okay?");
        showLine();
    }

    /**
     * Displays an error message followed by the standard separator.
     *
     * @param message the user-facing error message.
     */
    public void showError(String message) {
        output.println(message);
        showLine();
    }

    /**
     * Displays the standard separator used between console interactions.
     */
    public void showLine() {
        if (showSeparators) {
            output.println(BORDER_LINE);
        }
    }

    /**
     * Closes the input scanner when the application exits.
     */
    @Override
    public void close() {
        scanner.close();
    }
}

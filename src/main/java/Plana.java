import java.util.ArrayList;
import java.util.Scanner;

/**
 * Runs Plana's command-line task manager and responds in Plana's friendly voice.
 */
public class Plana {
    private static final String TODO_DESCRIPTION_ERROR = "Oops, a ToDo description can't be empty."
            + " Try: todo <description>.";
    private static final String EMPTY_COMMAND_ERROR = "Oops, I didn't catch a command."
            + " Type help to see what I can do.";
    private static final String DEADLINE_USAGE = "Try: deadline <description> /by <date>.";
    private static final String EVENT_USAGE = "Try: event <description> /from <start> /to <end>.";

    /**
     * Converts a user-entered task number into a zero-based list index.
     *
     * @param action the command being performed, such as mark or unmark
     * @param taskNumber the task number entered by the user
     * @param taskCount the number of tasks currently stored
     * @return the zero-based index of the selected task
     * @throws PlanaException if the task number is not a valid existing task
     */
    private static int getTaskIndex(TaskAction action, String taskNumber, int taskCount) throws PlanaException {
        if (taskNumber.isBlank()) {
            throw new PlanaException("Oops, " + action.getCommandText() + " needs a task number."
                    + " Try: " + action.getCommandText() + " <number>.");
        }

        final int taskIndex;
        try {
            taskIndex = Integer.parseInt(taskNumber) - 1;
        } catch (NumberFormatException exception) {
            throw new PlanaException("Oops, '" + taskNumber + "' isn't a valid task number."
                    + " Use a positive whole number, like " + action.getCommandText() + " 1.");
        }

        if (taskIndex < 0) {
            throw new PlanaException("Oops, task numbers start at 1."
                    + " Try " + action.getCommandText() + " 1 or another number from list.");
        }
        if (taskIndex >= taskCount) {
            throw new PlanaException("Oops, task " + (taskIndex + 1) + " doesn't exist yet."
                    + " Type list to check the task numbers you have.");
        }
        return taskIndex;
    }

    /**
     * Creates an error for a deadline whose syntax is missing one specific part.
     *
     * @param problem the part of the deadline that needs correcting
     * @return a user-friendly deadline error
     */
    private static PlanaException deadlineError(String problem) {
        return new PlanaException("Oops, " + problem + " " + DEADLINE_USAGE);
    }

    /**
     * Creates an error for an event whose syntax is missing one specific part.
     *
     * @param problem the part of the event that needs correcting
     * @return a user-friendly event error
     */
    private static PlanaException eventError(String problem) {
        return new PlanaException("Oops, " + problem + " " + EVENT_USAGE);
    }

    /**
     * Prints the commands and features that Plana supports.
     */
    private static void printHelp() {
        System.out.println("Don't worry! I am always here to help :>");
        System.out.println("Here's what I can do:");
        System.out.println("  todo <description>                          add a task");
        System.out.println("  deadline <description> /by <date>           add a deadline");
        System.out.println("  event <description> /from <start> /to <end> add an event");
        System.out.println("  list                                        show all tasks");
        System.out.println("  delete <number>                             delete a task");
        System.out.println("  mark <number>                               mark a task as done");
        System.out.println("  unmark <number>                             mark a task as not done");
        System.out.println("  help or ?                                   show this help");
        System.out.println("  bye                                         say goodbye");
    }

    public static void main(String[] args) {
        String banner = " ____  _                  \n"
                + "|  _ \\| | __ _ _ __   __ _ \n"
                + "| |_) | |/ _` | '_ \\ / _` |\n"
                + "|  __/| | (_| | | | | (_| |\n"
                + "|_|   |_|\\__,_|_| |_|\\__,_|\n";
        String banner_art = """
                ⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡿⠿⡛⡫⢋⠍⠍⢅⢍⢑⠩⡉⢍⠫⢛⠻⡻⡿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿
                ⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠿⡻⢿⠑⢌⢌⣔⣴⣵⢾⣾⣾⣾⣾⣾⡾⠶⡳⠵⢬⣢⢊⠌⠝⡛⢿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿
                ⣿⣿⣿⣿⣿⣿⣿⣿⣿⡿⠫⠡⡑⡰⠴⢿⣿⣿⠟⡑⣌⣂⢊⢻⣿⣿⠏⠪⣨⣦⣥⡑⡸⣿⣷⣧⣊⠔⡨⢛⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿
                ⣿⣿⣿⣿⣿⣿⣿⡟⡣⠊⣌⢎⠎⠪⡘⣾⣿⠣⢨⣺⣿⣿⡢⠡⣿⡇⢅⢽⣿⣿⣿⣗⠌⣺⣿⣿⡇⢕⢐⠡⡘⠿⣿⣿⣿⣿⣿⣿⣿⣿
                ⣿⣿⣿⣿⣿⡿⠋⢔⣰⣽⣿⡧⡊⢌⣺⣿⡇⢅⣽⣿⣿⣿⠪⢨⣿⡪⢐⣽⣿⣿⣿⠣⢊⢼⣿⡿⡟⠆⡢⢑⣴⡡⠂⠝⢿⣿⣿⣿⣿⣿
                ⣿⣿⣿⣿⠏⡢⣱⣵⣿⣿⣿⠣⡊⢴⣿⣿⢇⢂⢿⣿⢟⠏⣊⣾⣿⣧⡅⢝⠩⢋⢂⢕⣼⣿⣿⣧⠪⡐⢌⢘⠍⡢⢑⠡⡑⢜⣿⣿⣿⣿
                ⣿⣿⣿⠣⢑⣴⣿⣿⣿⣿⡏⡊⡲⣿⣿⣿⣷⣐⢌⢂⡢⣱⣾⣿⣿⣿⣿⣷⣷⣶⣷⣿⣿⣿⣿⣷⣧⣶⣗⢐⠅⠢⣱⣮⡆⢅⠚⣿⣿⣿
                ⣿⣿⢃⠪⣸⣿⣿⣿⣿⣿⡐⢌⢼⣿⣿⣿⡿⠿⢟⢓⣛⣙⣭⡮⠾⡚⣛⣍⣝⣬⣵⣭⣮⡭⣏⣻⣟⣛⠿⢦⣾⣼⣾⣿⣿⡦⡑⡘⣿⣿
                ⣿⢇⢑⢸⣿⣿⣿⣿⣿⡿⢿⠻⡛⡙⣅⣥⣪⣮⣾⡿⠻⣙⣕⣼⣾⣾⣿⣿⣿⣿⣿⡿⡫⣸⣿⣿⣿⣿⣿⣾⣬⣙⢟⢿⣿⣿⡆⠪⡸⣿
                ⡿⢐⠡⣺⣿⣿⡟⢭⡑⣌⣦⣵⣾⣿⢿⡻⣛⣫⣵⣼⢞⣻⣿⣿⣿⣿⣿⣿⣿⢿⠫⢨⢰⣿⣿⣿⣿⣿⣿⣿⣿⣿⣷⣧⣙⠿⣯⠊⠌⣿
                ⡏⡢⢑⣿⣿⣿⣿⣿⢙⣙⣍⣵⣥⣾⣶⣿⣿⡿⡛⣼⣾⣿⣿⣿⣿⡿⡿⢋⠣⠡⣊⣴⣿⣿⣿⡿⢿⣿⣿⣿⣿⣿⣿⣿⣿⣮⣪⠣⡑⡸
                ⡇⡊⢼⣿⣿⣿⣿⣿⣤⢍⢛⠻⡛⢟⠻⣫⡧⡱⣞⠻⢛⢛⠝⡩⢃⠣⡨⣂⣵⣷⣿⣿⣿⣿⣿⡿⣸⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡌⢔⢘
                ⡌⡂⡊⡃⣻⣿⣿⣿⣿⣇⠢⡑⡐⡡⠿⡛⣴⣿⣿⣿⣿⠢⢗⢂⣓⣃⠪⣿⣿⣿⣿⠏⣽⣿⣿⠣⣾⣿⣿⣿⣿⣿⢹⣿⣿⣿⣿⡯⡐⠌
                ⣇⢊⠔⡁⢎⢛⢙⢽⣿⣿⣿⣯⣦⡦⡑⣴⣿⣿⣿⣿⢣⢳⣱⣿⣿⣿⡎⣽⣿⣿⣿⢨⣿⡿⢣⢡⠩⡙⡻⣿⣿⣿⠸⣿⣿⣿⣿⡗⡨⢨
                ⣿⡐⠬⡎⢔⢐⠡⣙⣿⣿⡏⡙⡣⠑⢬⣾⣿⣿⣿⡿⣨⢾⠻⢛⢛⠻⢿⡆⢿⣿⡯⢪⡟⢅⣿⣷⠡⣻⣦⣝⣿⣟⢬⣿⣿⣿⣿⢇⠺⣿
                ⣿⡆⠅⢍⠢⡘⢾⣿⣿⣿⣮⡐⠔⡡⣳⣿⡿⣻⣿⠂⢕⣠⣵⣶⣦⣅⢕⣿⣧⠹⡯⣂⣾⠟⢟⠻⡳⢨⢻⣿⣿⡇⣺⣿⣿⣿⡿⡂⢝⣿
                ⣿⣮⠊⠔⡁⣢⣿⣿⣿⣿⣿⣬⠎⢄⢿⣿⡗⣺⡇⣵⠿⡿⣻⢿⣿⣿⣿⣿⣿⣿⣬⣼⣧⣼⣴⣥⣌⢂⢊⢝⢿⠌⣾⣿⣿⣿⡓⢌⣺⣿
                ⣿⣿⡎⠌⠜⣾⣿⣿⣿⣿⣿⠇⠕⡁⣿⣿⡧⢺⡇⣺⣼⣇⣟⣼⣿⡿⣿⣿⣿⣿⣿⣿⣿⣿⢫⡿⢿⣷⣕⢐⠔⣹⣿⣿⣿⡿⡈⢦⣿⣿
                ⣿⣿⣿⣎⠌⢜⢻⣿⣿⣿⠣⡑⡡⠢⢹⣿⢑⣥⡢⣹⣿⣿⣿⣿⣗⠅⣂⣪⣬⣙⡝⡻⢿⣯⣢⣟⣼⡢⣿⡇⡢⣿⣿⣿⠫⡑⢼⣿⣿⣿
                ⣿⣿⣿⣿⣮⢐⠡⠻⣿⣿⣷⡟⡢⣱⣇⠣⡸⣿⣿⣿⣿⣿⣿⣿⣇⣺⣿⣿⣿⣿⣿⣷⡩⣿⣿⣿⣿⣿⢏⢢⡿⣏⡿⠡⡑⣼⣿⣿⣿⣿
                ⣿⣿⣿⣿⣿⣷⡅⠕⡩⠻⡏⡢⡢⣿⣿⡇⣚⣿⣿⣿⣿⣿⣿⣿⡧⣺⣿⣿⣿⣿⣿⣿⢪⣿⣿⣿⡿⢫⡼⡛⡱⢑⢐⣱⣾⣿⣿⣿⣿⣿
                ⣿⣿⣿⣿⣿⣿⣿⣷⣔⠡⢒⠫⢸⣿⣿⢇⡢⡙⢿⣿⣿⣿⣿⣿⣯⢜⢿⣿⣿⣿⣿⠏⣼⣿⣯⣣⣪⣱⢔⠌⡂⣕⣾⣿⣿⣿⣿⣿⣿⣿
                ⣿⣿⣿⣿⣿⣿⣿⣿⣿⣷⣥⡊⠔⡩⢙⢂⢿⠠⠡⡙⡻⣿⣿⣿⣿⣷⣍⠻⡻⡻⣱⣽⣿⣿⠿⡫⢃⢅⢢⣪⣾⣿⣿⣿⣿⣿⣿⣿⣿⣿
                ⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣷⣬⣂⢅⠢⣡⢑⢐⠔⡨⢋⠿⠿⠿⠿⠿⡻⡚⢏⠫⡑⡡⣊⣤⣷⣽⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿
                ⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣷⣦⣥⣢⣡⣂⣅⣅⣣⣑⣔⣬⣶⣿⣾⣾⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿
                """;
        String border_line = "____________________________________________________________";
        System.out.println(border_line);
        System.out.print(banner);
        System.out.print(banner_art);
        System.out.println("Hi hi! I'm Plana.");
        System.out.println("What shall we get done today?");
        System.out.println(border_line);

        try (Scanner scanner = new Scanner(System.in)) {
            ArrayList<Task> tasks = new ArrayList<>();

            commandLoop:
            while (scanner.hasNextLine()) {
                String command = scanner.nextLine();
                System.out.println(command);
                System.out.println(border_line);

                try {
                    CommandType commandType = CommandType.fromInput(command);
                    if (commandType == CommandType.BYE) {
                        System.out.println("Bye-bye! See you next time, okay?");
                        System.out.println(border_line);
                        break commandLoop;
                    }
                    switch (commandType) {
                    case HELP -> {
                        printHelp();
                        System.out.println(border_line);
                    }
                    case LIST -> {
                        System.out.println(" Here are your tasks:");
                        for (int i = 0; i < tasks.size(); i++) {
                            System.out.println(" " + (i + 1) + "." + tasks.get(i));
                        }
                        System.out.println(border_line);
                    }
                    case DELETE -> {
                        String taskNumber = command.substring(CommandType.DELETE.getCommandText().length()).trim();
                        int taskIndex = getTaskIndex(TaskAction.DELETE, taskNumber, tasks.size());
                        Task deletedTask = tasks.remove(taskIndex);
                        Storage.saveTasks(tasks);
                        System.out.println("Noted. I've removed this task:");
                        System.out.println("  " + deletedTask);
                        int taskCount = tasks.size();
                        System.out.println("Now you have " + taskCount + (taskCount == 1 ? " task" : " tasks") + " in the list.");
                        System.out.println(border_line);
                    }
                    case MARK -> {
                        String taskNumber = command.substring(CommandType.MARK.getCommandText().length()).trim();
                        int taskIndex = getTaskIndex(TaskAction.MARK, taskNumber, tasks.size());
                        tasks.get(taskIndex).markAsDone();
                        Storage.saveTasks(tasks);
                        System.out.println("Yay! I've marked this task as done:");
                        System.out.println("  " + tasks.get(taskIndex));
                        System.out.println(border_line);
                    }
                    case UNMARK -> {
                        String taskNumber = command.substring(CommandType.UNMARK.getCommandText().length()).trim();
                        int taskIndex = getTaskIndex(TaskAction.UNMARK, taskNumber, tasks.size());
                        tasks.get(taskIndex).markAsNotDone();
                        Storage.saveTasks(tasks);
                        System.out.println("No worries! I've marked this task as not done:");
                        System.out.println("  " + tasks.get(taskIndex));
                        System.out.println(border_line);
                    }
                    case DEADLINE -> {
                        String arguments = command.substring(CommandType.DEADLINE.getCommandText().length()).trim();
                        if (arguments.isBlank()) {
                            throw deadlineError("a deadline needs a description and a due date.");
                        }
                        int bySeparatorIndex = arguments.indexOf("/by");
                        if (bySeparatorIndex < 0) {
                            throw deadlineError("I couldn't find /by and a due date is missing.");
                        }
                        String description = arguments.substring(0, bySeparatorIndex).trim();
                        String dueDate = arguments.substring(bySeparatorIndex + "/by".length()).trim();
                        if (description.isBlank()) {
                            throw deadlineError("that deadline is missing its description.");
                        }
                        if (dueDate.isBlank()) {
                            throw deadlineError("that deadline is missing its due date.");
                        }
                        tasks.add(new Deadline(description, dueDate));
                        Storage.saveTasks(tasks);
                        int taskCount = tasks.size();
                        System.out.println("Yay, I've added this task:");
                        System.out.println("  " + tasks.get(taskCount - 1));
                        System.out.println("Now you have " + taskCount + (taskCount == 1 ? " task" : " tasks") + " in your list!");
                        System.out.println(border_line);
                    }
                    case EVENT -> {
                        String arguments = command.substring(CommandType.EVENT.getCommandText().length()).trim();
                        if (arguments.isBlank()) {
                            throw eventError("an event needs a description, a start, and an end.");
                        }
                        int fromSeparatorIndex = arguments.indexOf("/from");
                        int toSeparatorIndex = arguments.indexOf("/to");
                        if (fromSeparatorIndex < 0) {
                            throw eventError("that event is missing its start marker /from.");
                        }
                        if (toSeparatorIndex < 0) {
                            throw eventError("that event is missing its end marker /to.");
                        }
                        if (toSeparatorIndex < fromSeparatorIndex) {
                            throw eventError("use /from before /to in an event.");
                        }
                        String description = arguments.substring(0, fromSeparatorIndex).trim();
                        String from = arguments.substring(fromSeparatorIndex + "/from".length(), toSeparatorIndex).trim();
                        String to = arguments.substring(toSeparatorIndex + "/to".length()).trim();
                        if (description.isBlank()) {
                            throw eventError("that event is missing its description.");
                        }
                        if (from.isBlank()) {
                            throw eventError("that event is missing its start time.");
                        }
                        if (to.isBlank()) {
                            throw eventError("that event is missing its end time.");
                        }
                        tasks.add(new Event(description, from, to));
                        Storage.saveTasks(tasks);
                        int taskCount = tasks.size();
                        System.out.println("Yay, I've added this task:");
                        System.out.println("  " + tasks.get(taskCount - 1));
                        System.out.println("Now you have " + taskCount + (taskCount == 1 ? " task" : " tasks") + " in your list!");
                        System.out.println(border_line);
                    }
                    case TODO -> {
                        String description = command.substring(CommandType.TODO.getCommandText().length()).trim();
                        if (description.isBlank()) {
                            throw new PlanaException(TODO_DESCRIPTION_ERROR);
                        }
                        tasks.add(new ToDo(description));
                        Storage.saveTasks(tasks);
                        int taskCount = tasks.size();
                        System.out.println("Yay, I've added this task:");
                        System.out.println("  " + tasks.get(taskCount - 1));
                        System.out.println("Now you have " + taskCount + (taskCount == 1 ? " task" : " tasks") + " in your list!");
                        System.out.println(border_line);
                    }
                    case UNKNOWN -> {
                        if (command.isBlank()) {
                            throw new PlanaException(EMPTY_COMMAND_ERROR);
                        }
                        throw new PlanaException("Oops, I don't recognize '" + command + "'."
                                + " Type help to see the commands I know.");
                    }
                    }
                } catch (PlanaException exception) {
                    System.out.println(exception.getMessage());
                    System.out.println(border_line);
                }
            }
        }
    }
}

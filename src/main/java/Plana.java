import java.util.Scanner;

/**
 * Runs Plana's command-line task manager and responds in Plana's friendly voice.
 */
public class Plana {
    private static final int MAX_TASKS = 100;
    private static final String TODO_DESCRIPTION_ERROR = "Oops, a ToDo needs a description.";
    private static final String UNKNOWN_COMMAND_ERROR = "Oops, I don't know what that means.";
    private static final String EMPTY_COMMAND_ERROR = "Oops, please enter a command.";
    private static final String INVALID_TASK_NUMBER_ERROR = "Oops, please enter a valid task number.";
    private static final String MISSING_TASK_ERROR = "Oops, that task number doesn't exist.";
    private static final String TASK_LIST_FULL_ERROR = "Oops, your task list is full for now.";
    private static final String DEADLINE_FORMAT_ERROR = "Oops, a deadline needs a description and a due date."
            + " Try: deadline <description> /by <date>.";
    private static final String EVENT_FORMAT_ERROR = "Oops, an event needs a description, a start, and an end."
            + " Try: event <description> /from <start> /to <end>.";

    /**
     * Converts a user-entered task number into a zero-based array index.
     *
     * @param taskNumber the task number entered by the user
     * @param taskCount the number of tasks currently stored
     * @return the zero-based index of the selected task
     * @throws PlanaException if the task number is not a valid existing task
     */
    private static int getTaskIndex(String taskNumber, int taskCount) throws PlanaException {
        final int taskIndex;
        try {
            taskIndex = Integer.parseInt(taskNumber) - 1;
        } catch (NumberFormatException exception) {
            throw new PlanaException(INVALID_TASK_NUMBER_ERROR);
        }

        if (taskIndex < 0 || taskIndex >= taskCount) {
            throw new PlanaException(MISSING_TASK_ERROR);
        }
        return taskIndex;
    }

    /**
     * Ensures that another task can be stored in the task array.
     *
     * @param taskCount the number of tasks currently stored
     * @throws PlanaException if the task list has reached its capacity
     */
    private static void ensureTaskCapacity(int taskCount) throws PlanaException {
        if (taskCount >= MAX_TASKS) {
            throw new PlanaException(TASK_LIST_FULL_ERROR);
        }
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
            Task[] tasks = new Task[MAX_TASKS];
            int taskCount = 0;

            while (scanner.hasNextLine()) {
                String command = scanner.nextLine();
                System.out.println(command);
                System.out.println(border_line);

                try {
                    if (command.equals("bye")) {
                        System.out.println("Bye-bye! See you next time, okay?");
                        System.out.println(border_line);
                        break;
                    } else if (command.equals("list")) {
                        System.out.println(" Here are your tasks:");
                        for (int i = 0; i < taskCount; i++) {
                            System.out.println(" " + (i + 1) + "." + tasks[i]);
                        }
                        System.out.println(border_line);
                    } else if (command.equals("mark") || command.startsWith("mark ")) {
                        String taskNumber = command.substring("mark".length()).trim();
                        int taskIndex = getTaskIndex(taskNumber, taskCount);
                        tasks[taskIndex].markAsDone();
                        System.out.println("Yay! I've marked this task as done:");
                        System.out.println("  " + tasks[taskIndex]);
                        System.out.println(border_line);
                    } else if (command.equals("unmark") || command.startsWith("unmark ")) {
                        String taskNumber = command.substring("unmark".length()).trim();
                        int taskIndex = getTaskIndex(taskNumber, taskCount);
                        tasks[taskIndex].markAsNotDone();
                        System.out.println("No worries! I've marked this task as not done:");
                        System.out.println("  " + tasks[taskIndex]);
                        System.out.println(border_line);
                } else if (command.equals("deadline") || command.startsWith("deadline ")) {
                    String arguments = command.substring("deadline".length()).trim();
                    int bySeparatorIndex = arguments.indexOf(" /by ");
                    if (bySeparatorIndex <= 0 || bySeparatorIndex + " /by ".length() >= arguments.length()) {
                        throw new PlanaException(DEADLINE_FORMAT_ERROR);
                    }
                    String description = arguments.substring(0, bySeparatorIndex).trim();
                    String dueDate = arguments.substring(bySeparatorIndex + " /by ".length()).trim();
                    if (description.isBlank() || dueDate.isBlank()) {
                        throw new PlanaException(DEADLINE_FORMAT_ERROR);
                    }
                    ensureTaskCapacity(taskCount);
                    tasks[taskCount] = new Deadline(description, dueDate);
                    taskCount++;
                    System.out.println("Yay, I've added this task:");
                    System.out.println("  " + tasks[taskCount - 1]);
                    System.out.println("Now you have " + taskCount + (taskCount == 1 ? " task" : " tasks") + " in your list!");
                    System.out.println(border_line);
                } else if (command.equals("event") || command.startsWith("event ")) {
                    String arguments = command.substring("event".length()).trim();
                    int fromSeparatorIndex = arguments.indexOf(" /from ");
                    int toSeparatorIndex = arguments.indexOf(" /to ", fromSeparatorIndex + " /from ".length());
                    if (fromSeparatorIndex <= 0 || toSeparatorIndex <= fromSeparatorIndex + " /from ".length()
                            || toSeparatorIndex + " /to ".length() >= arguments.length()) {
                        throw new PlanaException(EVENT_FORMAT_ERROR);
                    }
                    String description = arguments.substring(0, fromSeparatorIndex).trim();
                    String from = arguments.substring(fromSeparatorIndex + " /from ".length(), toSeparatorIndex).trim();
                    String to = arguments.substring(toSeparatorIndex + " /to ".length()).trim();
                    if (description.isBlank() || from.isBlank() || to.isBlank()) {
                        throw new PlanaException(EVENT_FORMAT_ERROR);
                    }
                    ensureTaskCapacity(taskCount);
                    tasks[taskCount] = new Event(description, from, to);
                    taskCount++;
                    System.out.println("Yay, I've added this task:");
                    System.out.println("  " + tasks[taskCount - 1]);
                    System.out.println("Now you have " + taskCount + (taskCount == 1 ? " task" : " tasks") + " in your list!");
                    System.out.println(border_line);
                } else if (command.equals("todo") || command.startsWith("todo ")) {
                    String description = command.substring("todo".length()).trim();
                    if (description.isBlank()) {
                        throw new PlanaException(TODO_DESCRIPTION_ERROR);
                    }
                    ensureTaskCapacity(taskCount);
                    tasks[taskCount] = new ToDo(description);
                    taskCount++;
                    System.out.println("Yay, I've added this task:");
                    System.out.println("  " + tasks[taskCount - 1]);
                    System.out.println("Now you have " + taskCount + (taskCount == 1 ? " task" : " tasks") + " in your list!");
                    System.out.println(border_line);
                } else if (command.isBlank()) {
                    throw new PlanaException(EMPTY_COMMAND_ERROR);
                } else {
                    throw new PlanaException(UNKNOWN_COMMAND_ERROR);
                    }
                } catch (PlanaException exception) {
                    System.out.println(exception.getMessage());
                    System.out.println(border_line);
                }
            }
        }
    }
}

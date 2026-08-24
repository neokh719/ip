import java.util.Scanner;

public class Plana {
    private static final int MAX_TASKS = 100;

    public static void main(String[] args) {
        String banner = " ____  _                  \n"
                + "|  _ \\| | __ _ _ __   __ _ \n"
                + "| |_) | |/ _` | '_ \\ / _` |\n"
                + "|  __/| | (_| | | | | (_| |\n"
                + "|_|   |_|\\__,_|_| |_|\\__,_|\n";
        String border_line = "____________________________________________________________";
        System.out.println(border_line);
        System.out.print(banner);
        System.out.println("Hello! I'm Plana.");
        System.out.println("What can I do for you?");
        System.out.println(border_line);

        try (Scanner scanner = new Scanner(System.in)) {
            String[] tasks = new String[MAX_TASKS];
            boolean[] completed = new boolean[MAX_TASKS];
            int taskCount = 0;

            while (scanner.hasNextLine()) {
                String command = scanner.nextLine();
                System.out.println(command);
                System.out.println(border_line);

                if (command.equals("bye")) {
                    System.out.println("Bye. Hope to see you again soon!");
                    System.out.println(border_line);
                    break;
                } else if (command.equals("list")) {
                    System.out.println(" Here are the tasks in your list:");
                    for (int i = 0; i < taskCount; i++) {
                        String status = completed[i] ? "X" : " ";
                        System.out.println(" " + (i + 1) + ".[" + status + "] " + tasks[i]);
                    }
                    System.out.println(border_line);
                } else if (command.startsWith("mark ")) {
                    String taskNumber = command.substring("mark ".length()).trim();
                    try {
                        int taskIndex = Integer.parseInt(taskNumber) - 1;
                        if (taskIndex >= 0 && taskIndex < taskCount) {
                            completed[taskIndex] = true;
                            System.out.println("Nice! I've marked this task as done:");
                            System.out.println("  [X] " + tasks[taskIndex]);
                        } else {
                            System.out.println("Sorry, that task number does not exist.");
                        }
                    } catch (NumberFormatException exception) {
                        System.out.println("Sorry, please provide a valid task number.");
                    }
                    System.out.println(border_line);
                } else if (command.startsWith("unmark ")) {
                    String taskNumber = command.substring("unmark ".length()).trim();
                    try {
                        int taskIndex = Integer.parseInt(taskNumber) - 1;
                        if (taskIndex >= 0 && taskIndex < taskCount) {
                            completed[taskIndex] = false;
                            System.out.println("OK, I've marked this task as not done yet:");
                            System.out.println("  [ ] " + tasks[taskIndex]);
                        } else {
                            System.out.println("Sorry, that task number does not exist.");
                        }
                    } catch (NumberFormatException exception) {
                        System.out.println("Sorry, please provide a valid task number.");
                    }
                    System.out.println(border_line);
                } else if (!command.isBlank()) {
                    if (taskCount < MAX_TASKS) {
                        tasks[taskCount] = command;
                        taskCount++;
                        System.out.println("added: " + command);
                    } else {
                        System.out.println("Sorry, your task list is full.");
                    }
                    System.out.println(border_line);
                }
            }
        }
    }
}

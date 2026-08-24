import java.util.Scanner;

public class Plana {
    private static final int MAX_TASKS = 100;

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
        System.out.println("Hello! I'm Plana.");
        System.out.println("What can I do for you?");
        System.out.println(border_line);

        try (Scanner scanner = new Scanner(System.in)) {
            ToDo[] tasks = new ToDo[MAX_TASKS];
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
                        System.out.println(" " + (i + 1) + "." + tasks[i]);
                    }
                    System.out.println(border_line);
                } else if (command.startsWith("mark ")) {
                    String taskNumber = command.substring("mark ".length()).trim();
                    try {
                        int taskIndex = Integer.parseInt(taskNumber) - 1;
                        if (taskIndex >= 0 && taskIndex < taskCount) {
                            tasks[taskIndex].markAsDone();
                            System.out.println("Nice! I've marked this task as done:");
                            System.out.println("  " + tasks[taskIndex]);
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
                            tasks[taskIndex].markAsNotDone();
                            System.out.println("OK, I've marked this task as not done yet:");
                            System.out.println("  " + tasks[taskIndex]);
                        } else {
                            System.out.println("Sorry, that task number does not exist.");
                        }
                    } catch (NumberFormatException exception) {
                        System.out.println("Sorry, please provide a valid task number.");
                    }
                    System.out.println(border_line);
                } else if (command.equals("todo") || command.startsWith("todo ")) {
                    String description = command.substring("todo".length()).trim();
                    if (description.isBlank()) {
                        System.out.println("Sorry, a ToDo must have a description.");
                    } else if (taskCount < MAX_TASKS) {
                        tasks[taskCount] = new ToDo(description);
                        taskCount++;
                        System.out.println("Got it. I've added this task:");
                        System.out.println("  " + tasks[taskCount - 1]);
                        System.out.println("Now you have " + taskCount + " tasks in the list.");
                    } else {
                        System.out.println("Sorry, your task list is full.");
                    }
                    System.out.println(border_line);
                } else if (!command.isBlank()) {
                    if (taskCount < MAX_TASKS) {
                        tasks[taskCount] = new ToDo(command);
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

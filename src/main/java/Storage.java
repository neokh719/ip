import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Saves and loads Plana's task list using a file relative to the project root.
 */
public class Storage {
    private static final Path DATA_FILE = Path.of("data", "plana.txt");

    /**
     * Writes the current task list to disk, creating the data directory when necessary.
     *
     * @param tasks the tasks that should be saved
     */
    public static void saveTasks(List<Task> tasks) {
        try {
            Path parentDirectory = DATA_FILE.getParent();
            if (parentDirectory != null) {
                Files.createDirectories(parentDirectory);
            }

            String fileContents = tasks.stream()
                    .map(Task::toStorageString)
                    .collect(Collectors.joining(System.lineSeparator()));
            if (!fileContents.isEmpty()) {
                fileContents += System.lineSeparator();
            }
            Files.writeString(DATA_FILE, fileContents, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            // Saving is best-effort for now; loading and user-facing error handling can be added later.
            System.err.println("Unable to save tasks: " + exception.getMessage());
        }
    }

    /**
     * Loads the saved tasks, returning an empty list when no save file exists.
     * Malformed records are skipped so one corrupted record does not prevent the
     * remaining valid tasks from being loaded.
     *
     * @return the tasks loaded from disk
     */
    public static ArrayList<Task> loadTasks() {
        ArrayList<Task> tasks = new ArrayList<>();
        if (Files.notExists(DATA_FILE)) {
            return tasks;
        }

        try {
            for (String line : Files.readAllLines(DATA_FILE, StandardCharsets.UTF_8)) {
                Task task = parseTask(line);
                if (task != null) {
                    tasks.add(task);
                }
            }
        } catch (IOException exception) {
            System.err.println("Unable to load tasks: " + exception.getMessage());
        }
        return tasks;
    }

    /**
     * Converts one saved line into a task.
     *
     * @param line a record from the save file
     * @return the parsed task, or {@code null} when the record is malformed
     */
    private static Task parseTask(String line) {
        if (line.isBlank()) {
            return null;
        }

        String[] parts = line.split("\\s*\\|\\s*", -1);
        if (parts.length < 3 || (!parts[1].equals("0") && !parts[1].equals("1"))) {
            return null;
        }

        String type = parts[0].trim();
        String description = parts[2].trim();
        if (description.isBlank()) {
            return null;
        }

        Task task;
        switch (type) {
        case "T" -> {
            if (parts.length != 3) {
                return null;
            }
            task = new ToDo(description);
        }
        case "D" -> {
            if (parts.length != 4 || parts[3].trim().isBlank()) {
                return null;
            }
            task = new Deadline(description, parts[3].trim());
        }
        case "E" -> {
            if (parts.length != 5 || parts[3].trim().isBlank() || parts[4].trim().isBlank()) {
                return null;
            }
            task = new Event(description, parts[3].trim(), parts[4].trim());
        }
        default -> {
            return null;
        }
        }

        if (parts[1].equals("1")) {
            task.markAsDone();
        }
        return task;
    }
}

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Saves Plana's task list to a file relative to the project root.
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
}

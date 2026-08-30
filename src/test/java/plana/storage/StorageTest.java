package plana.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import plana.task.Deadline;
import plana.task.Event;
import plana.task.TaskList;
import plana.task.ToDo;

/**
 * Tests persistence and storage-field escaping behavior.
 */
public class StorageTest {
    @TempDir
    private Path temporaryDirectory;

    /**
     * Verifies that saving and loading preserves task types, dates,
     * descriptions, escaping, and completion status.
     */
    @Test
    public void saveTasksAndLoadTasks_mixedTaskList_roundTripPreservesTasks() {
        Path dataFile = temporaryDirectory.resolve("tasks.txt");
        Storage storage = new Storage(dataFile.toString());

        ToDo todo = new ToDo("review | draft\\final\nnotes");
        Deadline deadline = new Deadline("submit report", LocalDate.of(2026, 9, 3));
        deadline.markAsDone();
        Event event = new Event("team meeting", LocalDate.of(2026, 9, 4), LocalDate.of(2026, 9, 5));
        TaskList originalTasks = new TaskList(List.of(todo, deadline, event));

        storage.saveTasks(originalTasks);
        TaskList loadedTasks = storage.loadTasks();

        assertEquals(3, loadedTasks.size());
        assertEquals(todo.toStorageString(), loadedTasks.get(0).toStorageString());
        assertEquals(deadline.toStorageString(), loadedTasks.get(1).toStorageString());
        assertEquals(event.toStorageString(), loadedTasks.get(2).toStorageString());
        assertEquals(todo.toString(), loadedTasks.get(0).toString());
        assertEquals(deadline.toString(), loadedTasks.get(1).toString());
        assertEquals(event.toString(), loadedTasks.get(2).toString());
    }

    /**
     * Verifies that storage escaping protects separators, backslashes, and
     * line-break characters.
     */
    @Test
    public void escapeField_specialCharacters_escapedWithoutDataLoss() {
        assertEquals("a\\\\b\\|c\\nd\\r", Storage.escapeField("a\\b|c\nd\r"));
        assertEquals("", Storage.escapeField(null));
    }

    /**
     * Verifies that loading a missing save file produces an empty task list.
     */
    @Test
    public void loadTasks_missingFile_emptyTaskListReturned() {
        Storage storage = new Storage(temporaryDirectory.resolve("missing.txt").toString());

        TaskList loadedTasks = storage.loadTasks();

        assertTrue(loadedTasks.isEmpty());
    }

    /**
     * Verifies that malformed records are skipped while valid records remain
     * loadable, including completed records.
     */
    @Test
    public void loadTasks_malformedRecords_validRecordsPreserved() throws IOException {
        Path dataFile = temporaryDirectory.resolve("mixed-records.txt");
        Files.writeString(dataFile, String.join(System.lineSeparator(),
                "T | 0 | valid task",
                "not a storage record",
                "D | 0 | missing date |",
                "E | 2 | invalid status | 2026-09-04 | 2026-09-05",
                "X | 0 | unknown type",
                "T | 1 | completed task",
                "T | 0 | incomplete escape\\"));

        TaskList loadedTasks = new Storage(dataFile.toString()).loadTasks();

        assertEquals(2, loadedTasks.size());
        assertEquals("T | 0 | valid task", loadedTasks.get(0).toStorageString());
        assertEquals("T | 1 | completed task", loadedTasks.get(1).toStorageString());
    }
}

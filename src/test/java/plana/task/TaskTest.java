package plana.task;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TaskTest {

    @Test
    void task_newTask_isIncompleteByDefault() {
        Task task = new Task("read book");

        assertEquals(" ", task.getStatusIcon());
        assertEquals("[ ] read book", task.toString());
        assertEquals("T | 0 | read book", task.toStorageString());
    }

    @Test
    void markAsDone_thenMarkAsNotDone_updatesStatusAndStorage() {
        Task task = new Task("read book");

        task.markAsDone();
        assertEquals("X", task.getStatusIcon());
        assertEquals("T | 1 | read book", task.toStorageString());

        task.markAsNotDone();
        assertEquals(" ", task.getStatusIcon());
        assertEquals("T | 0 | read book", task.toStorageString());
    }
}

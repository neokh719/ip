/**
 * Represents a ToDo task without an attached date or time.
 *
 * <p>This class uses composition instead of inheritance: the existing
 * {@link Task} stores the completion state, while this class adds the ToDo
 * type marker used when displaying the task.</p>
 */
public class ToDo {
    private final Task task;

    /**
     * Creates an incomplete ToDo with the given description.
     *
     * @param description the task description
     */
    public ToDo(String description) {
        task = new Task(description);
    }

    /**
     * Marks this ToDo as done.
     */
    public void markAsDone() {
        task.markAsDone();
    }

    /**
     * Marks this ToDo as not done.
     */
    public void markAsNotDone() {
        task.markAsNotDone();
    }

    /**
     * Returns the ToDo in the format used when displaying the task list.
     *
     * @return the ToDo type, completion status, and description
     */
    @Override
    public String toString() {
        return "[T]" + task;
    }
}

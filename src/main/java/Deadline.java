/**
 * Represents a task that must be completed by a specified date or time.
 *
 * <p>This class uses composition instead of inheritance: the existing
 * {@link Task} stores the completion state, while this class stores the
 * deadline-specific due-date text and type marker.</p>
 */
public class Deadline {
    private final Task task;
    private final String dueDate;

    /**
     * Creates an incomplete Deadline.
     *
     * @param description the task description
     * @param dueDate the due date or time, kept as entered by the user
     */
    public Deadline(String description, String dueDate) {
        task = new Task(description);
        this.dueDate = dueDate;
    }

    /**
     * Marks this Deadline as done.
     */
    public void markAsDone() {
        task.markAsDone();
    }

    /**
     * Marks this Deadline as not done.
     */
    public void markAsNotDone() {
        task.markAsNotDone();
    }

    /**
     * Returns the Deadline in the format used when displaying the task list.
     *
     * @return the Deadline type, completion status, description, and due date
     */
    @Override
    public String toString() {
        return "[D]" + task + " (by: " + dueDate + ")";
    }
}

/**
 * Represents a task that must be completed by a specified date or time.
 */
public class Deadline extends Task {
    private final String dueDate;

    /**
     * Creates an incomplete Deadline.
     *
     * @param description the task description
     * @param dueDate the due date or time, kept as entered by the user
     */
    public Deadline(String description, String dueDate) {
        super(description);
        this.dueDate = dueDate;
    }

    /**
     * Returns the Deadline in the format used by the storage file.
     *
     * @return the Deadline type, completion status, description, and due date
     */
    @Override
    public String toStorageString() {
        String status = completionStatus == CompletionStatus.DONE ? "1" : "0";
        return "D | " + status + " | " + description + " | " + dueDate;
    }

    /**
     * Returns the Deadline in the format used when displaying the task list.
     *
     * @return the Deadline type, completion status, description, and due date
     */
    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + dueDate + ")";
    }
}

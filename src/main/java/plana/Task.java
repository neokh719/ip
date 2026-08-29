package plana;

/**
 * Represents a task in Plana's task list.
 */
public class Task {
    protected String description;
    protected CompletionStatus completionStatus;

    /**
     * Creates a task that is initially not done.
     *
     * @param description the task description
     */
    public Task(String description) {
        this.description = description;
        this.completionStatus = CompletionStatus.NOT_DONE;
    }

    /**
     * Returns the icon representing this task's completion status.
     *
     * @return {@code X} if the task is done, or a space otherwise
     */
    public String getStatusIcon() {
        return completionStatus.getStatusIcon();
    }

    /**
     * Marks this task as done.
     */
    public void markAsDone() {
        completionStatus = CompletionStatus.DONE;
    }

    /**
     * Marks this task as not done.
     */
    public void markAsNotDone() {
        completionStatus = CompletionStatus.NOT_DONE;
    }

    /**
     * Returns the task in the format used by the storage file.
     *
     * @return the task type, completion status, and description
     */
    public String toStorageString() {
        return "T | " + getStorageStatus() + " | " + Storage.escapeField(description);
    }

    /**
     * Returns this task's completion status in the storage format.
     *
     * @return {@code 1} for done, or {@code 0} otherwise
     */
    protected String getStorageStatus() {
        return completionStatus == CompletionStatus.DONE ? "1" : "0";
    }

    /**
     * Returns the task in the format used when displaying the task list.
     *
     * @return the task status and description
     */
    @Override
    public String toString() {
        return "[" + getStatusIcon() + "] " + description;
    }
}

/**
 * Represents a task that starts and ends at specified date or time text.
 */
public class Event extends Task {
    private final String from;
    private final String to;

    /**
     * Creates an incomplete Event.
     *
     * @param description the task description
     * @param from the start date or time, kept as entered by the user
     * @param to the end date or time, kept as entered by the user
     */
    public Event(String description, String from, String to) {
        super(description);
        this.from = from;
        this.to = to;
    }

    /**
     * Returns the Event in the format used by the storage file.
     *
     * @return the Event type, completion status, description, start, and end
     */
    @Override
    public String toStorageString() {
        String status = completionStatus == CompletionStatus.DONE ? "1" : "0";
        return "E | " + status + " | " + description + " | " + from + " | " + to;
    }

    /**
     * Returns the Event in the format used when displaying the task list.
     *
     * @return the Event type, completion status, description, start, and end
     */
    @Override
    public String toString() {
        return "[E]" + super.toString() + " (from: " + from + " to: " + to + ")";
    }
}

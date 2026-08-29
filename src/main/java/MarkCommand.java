/**
 * Marks a selected task as done and persists the updated task list.
 */
public class MarkCommand extends Command {
    private final String taskNumber;

    /**
     * Creates a mark command for a user-facing task number.
     *
     * @param taskNumber the one-based task number entered by the user
     */
    public MarkCommand(String taskNumber) {
        this.taskNumber = taskNumber;
    }

    /**
     * Marks the selected task, saves the list, and displays the result.
     *
     * @param tasks the task list to update
     * @param ui the user interface used for the response
     * @param storage the storage collaborator used for persistence
     * @throws PlanaException if the task number is invalid
     */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws PlanaException {
        Task markedTask = tasks.mark(taskNumber);
        storage.saveTasks(tasks);
        ui.showTaskMarkedDone(markedTask);
    }
}

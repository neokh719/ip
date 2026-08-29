package plana;

/**
 * Deletes a task and persists the updated task list.
 */
public class DeleteCommand extends Command {
    private final String taskNumber;

    /**
     * Creates a delete command for a user-facing task number.
     *
     * @param taskNumber the one-based task number entered by the user
     */
    public DeleteCommand(String taskNumber) {
        this.taskNumber = taskNumber;
    }

    /**
     * Deletes the selected task, saves the list, and displays the result.
     *
     * @param tasks the task list to update
     * @param ui the user interface used for the response
     * @param storage the storage collaborator used for persistence
     * @throws PlanaException if the task number is invalid
     */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws PlanaException {
        Task deletedTask = tasks.delete(taskNumber);
        storage.saveTasks(tasks);
        ui.showTaskDeleted(deletedTask, tasks.size());
    }
}

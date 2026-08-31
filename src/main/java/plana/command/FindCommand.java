package plana.command;

import plana.storage.Storage;
import plana.task.Task;
import plana.task.TaskList;
import plana.ui.Ui;

/**
 * Displays tasks whose descriptions contain a supplied keyword.
 */
public class FindCommand extends Command {
    private final String keyword;

    /**
     * Creates a find command for a keyword.
     *
     * @param keyword the keyword to search for.
     */
    public FindCommand(String keyword) {
        this.keyword = keyword;
    }

    /**
     * Displays matching tasks in their original task-list order and numbers.
     *
     * @param tasks the task list to search.
     * @param ui the user interface used for the search response.
     * @param storage unused by this command.
     */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        boolean hasMatchingTask = false;
        ui.showMatchingTasksHeader();
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            if (task.matchesKeyword(keyword)) {
                ui.showTask(i, task);
                hasMatchingTask = true;
            }
        }
        if (!hasMatchingTask) {
            ui.showNoMatchingTasks();
        }
        ui.showLine();
    }
}

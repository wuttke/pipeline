package eu.wuttke.pipeline.task.local;

import eu.wuttke.pipeline.task.Task;
import eu.wuttke.pipeline.task.TaskExecutor;
import eu.wuttke.pipeline.task.TaskStatus;
import eu.wuttke.pipeline.task.TaskStatusChangeListener;

public class LocalTaskExecutor implements TaskExecutor {

	@Override
	public void executeTask(Task task, int actualCores) {
	}

	@Override
	public TaskStatus getTaskStatus(Task task) {
		return null;
	}

	@Override
	public void addStatusChangeListener(TaskStatusChangeListener listener) {
	}


}

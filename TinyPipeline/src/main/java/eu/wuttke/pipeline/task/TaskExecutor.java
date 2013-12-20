package eu.wuttke.pipeline.task;

public interface TaskExecutor {

	public void executeTask(Task task, int actualCores);
	public TaskStatus getTaskStatus(Task task);
	public void addStatusChangeListener(TaskStatusChangeListener listener);
	
}

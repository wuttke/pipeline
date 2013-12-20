package eu.wuttke.pipeline.task;

public interface TaskStatusChangeListener {

	public void taskStatusChanged(Task task, TaskStatus newStatus);
	
}

package eu.wuttke.pipeline.task.local;

import eu.wuttke.pipeline.task.Task;
import eu.wuttke.pipeline.task.TaskStatus;

public class LocalTask {

	private Task task;
	private Process process;
	private TaskStatus status;
	
	public Task getTask() {
		return task;
	}
	
	public void setTask(Task task) {
		this.task = task;
	}
	
	public Process getProcess() {
		return process;
	}
	
	public void setProcess(Process process) {
		this.process = process;
	}
	
	public TaskStatus getStatus() {
		return status;
	}
	
	public void setStatus(TaskStatus status) {
		this.status = status;
	}
	
}

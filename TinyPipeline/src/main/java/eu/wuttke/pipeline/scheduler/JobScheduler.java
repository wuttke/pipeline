package eu.wuttke.pipeline.scheduler;

import eu.wuttke.pipeline.task.Task;
import eu.wuttke.pipeline.task.TaskExecutor;
import eu.wuttke.pipeline.task.TaskStatus;
import eu.wuttke.pipeline.task.TaskStatusChangeListener;

public class JobScheduler
implements TaskStatusChangeListener {

	//private Jobs jobs;
	private TaskExecutor taskExecutor;
	
	public void init() {
		taskExecutor.addStatusChangeListener(this);
	}
	
	public void writeJobsFile() {
		
	}
	
	public void readJobsFile() {
		
	}

	@Override
	public void taskStatusChanged(Task task, TaskStatus newStatus) {		
	}
	
}

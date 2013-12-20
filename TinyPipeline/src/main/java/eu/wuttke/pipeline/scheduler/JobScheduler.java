package eu.wuttke.pipeline.scheduler;

import eu.wuttke.pipeline.task.Task;
import eu.wuttke.pipeline.task.TaskStatus;
import eu.wuttke.pipeline.task.TaskStatusChangeListener;

public class JobScheduler
implements TaskStatusChangeListener {

	private Jobs jobs;

	@Override
	public void taskStatusChanged(Task task, TaskStatus newStatus) {
		// TODO Auto-generated method stub
		
	}
	
}

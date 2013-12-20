package eu.wuttke.pipeline.scheduler;

import java.util.LinkedList;
import java.util.List;

import eu.wuttke.pipeline.task.Task;

public class Job {

	private String id;
	private Task task;
	private List<Job> requirements;
	private JobStatus status;
	
	public Job(String id, Task task, List<Job> requirements) {
		this.id = id;
		this.task = task;
		this.requirements = requirements;
		setInitialStatusByRequirements();
	}

	public Job(String id, Task task, Job requirement) {
		this.id = id;
		this.task = task;
		this.requirements = new LinkedList<Job>();
		requirements.add(requirement);
		setInitialStatusByRequirements();
	}

	public Job(String id, Task task) {
		this.id = id;
		this.task = task;
		this.requirements = new LinkedList<Job>();
		setInitialStatusByRequirements();
	}

	public boolean isJobRequiredByJob(Job job) {
		return requirements.contains(job);
	}

	public boolean areRequirementsMet() {
		for (Job requirement : requirements)
			if (requirement.getStatus() != JobStatus.SUCCESSFUL)
				return false;
		return true;
	}
	
	private void setInitialStatusByRequirements() {
		if (requirements.size() == 0)
			status = JobStatus.PENDING;
		else
			status = JobStatus.WAITING;
	}

	public JobStatus getStatus() {
		return status;
	}

	public void setStatus(JobStatus status) {
		this.status = status;
	}

	public String getId() {
		return id;
	}

	public Task getTask() {
		return task;
	}

	public List<Job> getRequirements() {
		return requirements;
	}
	
}

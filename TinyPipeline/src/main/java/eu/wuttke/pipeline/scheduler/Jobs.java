package eu.wuttke.pipeline.scheduler;

import java.util.ArrayList;
import java.util.List;

public class Jobs {

	private List<Job> jobs = new ArrayList<Job>();
	
	public void addJob(Job job) {
		jobs.add(job);
	}
	
	public List<Job> findJobSuccessors(Job job) {
		List<Job> successors = new ArrayList<Job>();
		for (Job candidate : jobs)
			if (candidate.isJobRequiredByJob(job))
				successors.add(candidate);
		return successors;
	}
	
	public void markJobDone(Job job) {
		job.setStatus(JobStatus.SUCCESSFUL);
		for (Job successor : findJobSuccessors(job)) 
			if (job.areRequirementsMet())
				successor.setStatus(JobStatus.PENDING);
	}
	
	public List<Job> getJobs() {
		return jobs;
	}
	
}

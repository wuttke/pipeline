package eu.wuttke.pipeline.pipeline;

import eu.wuttke.pipeline.scheduler.Jobs;

public class Pipeline {

	private String name;
	private Jobs jobs;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Jobs getJobs() {
		return jobs;
	}
	
	public void setJobs(Jobs jobs) {
		this.jobs = jobs;
	}
	
}

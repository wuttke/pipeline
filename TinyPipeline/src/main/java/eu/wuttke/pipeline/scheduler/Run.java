package eu.wuttke.pipeline.scheduler;

import java.util.Date;

public class Run {

	private Date startDateTime;
	private Date finishDateTime;
	private String host;
	private Job job;
	
	public Date getStartDateTime() {
		return startDateTime;
	}
	
	public void setStartDateTime(Date startDateTime) {
		this.startDateTime = startDateTime;
	}
	
	public Date getFinishDateTime() {
		return finishDateTime;
	}
	
	public void setFinishDateTime(Date finishDateTime) {
		this.finishDateTime = finishDateTime;
	}
	
	public String getHost() {
		return host;
	}
	
	public void setHost(String host) {
		this.host = host;
	}
	
	public Job getJob() {
		return job;
	}
	
	public void setJob(Job job) {
		this.job = job;
	}
	
}

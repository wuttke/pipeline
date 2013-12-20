package eu.wuttke.pipeline.scheduler;

public enum JobStatus {

	// not all requirements have been met, waiting for requirements
	WAITING,
	
	// all requirements are fulfilled, can be executed
	PENDING,
	
	// job is being executed
	RUNNING,
	
	// job finished successfully
	SUCCESSFUL,
	
	// job termined with failure
	FAILURE
	
}

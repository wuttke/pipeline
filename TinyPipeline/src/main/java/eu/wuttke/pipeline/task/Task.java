package eu.wuttke.pipeline.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Task {

	private String executable;
	private List<String> arguments = new ArrayList<String>();
	private int minCores = 1;
	private int maxCores = 1;
	private int returnCode;
	private Map<String, String> environment = new HashMap<String, String>();
	
	public void buildCommandLine(int actualCores, List<String> commandLineArguments) {
		commandLineArguments.addAll(arguments);
	}

	public String getExecutable() {
		return executable;
	}

	public void setExecutable(String executable) {
		this.executable = executable;
	}

	public List<String> getArguments() {
		return arguments;
	}

	public void setArguments(List<String> arguments) {
		this.arguments = arguments;
	}

	public int getMinCores() {
		return minCores;
	}

	public void setMinCores(int minCores) {
		this.minCores = minCores;
	}

	public int getMaxCores() {
		return maxCores;
	}

	public void setMaxCores(int maxCores) {
		this.maxCores = maxCores;
	}
	
	public int getReturnCode() {
		return returnCode;
	}
	
	public void setReturnCode(int returnCode) {
		this.returnCode = returnCode;
	}
	
	public Map<String, String> getEnvironment() {
		return environment;
	}
	
	public void setEnvironment(Map<String, String> environment) {
		this.environment = environment;
	}
	
}

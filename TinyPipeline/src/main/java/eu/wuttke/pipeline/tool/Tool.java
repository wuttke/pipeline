package eu.wuttke.pipeline.tool;

public class Tool {

	private String key;
	private String version;
	private String executable;
	
	public Tool(String key, String version, String executable) {
		this.key = key;
		this.version = version;
		this.executable = executable;
	}

	public String getKey() {
		return key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	public String getVersion() {
		return version;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}
	
	public String getExecutable() {
		return executable;
	}
	
	public void setExecutable(String executable) {
		this.executable = executable;
	}
	
}

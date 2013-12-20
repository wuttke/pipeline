package eu.wuttke.pipeline.tool;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import eu.wuttke.pipeline.util.TabFileReader;

public class Tools {

	private Map<String, Tool> tools = new HashMap<String, Tool>();
	
	public void readToolsFile(File f) {
		try {
			TabFileReader reader = new TabFileReader(f);
			String[] fields;
			while ((fields = reader.readLine()) != null) {
				Tool tool = new Tool(fields[0], fields[1], fields[2]);
				tools.put(tool.getKey(), tool);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public Tool getTool(String key) {
		Tool tool = tools.get(key);
		if (tool == null)
			throw new RuntimeException("tool not found: " + key);
		return tool;
	}
	
}

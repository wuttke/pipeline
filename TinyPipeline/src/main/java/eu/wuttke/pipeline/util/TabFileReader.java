package eu.wuttke.pipeline.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class TabFileReader {

	private BufferedReader reader;
	
	public TabFileReader(File file) throws FileNotFoundException {
		init(new FileInputStream(file), "ISO-8859-1");
	}
	
	public TabFileReader(String fileName) throws FileNotFoundException {
		init(new FileInputStream(fileName), "ISO-8859-1");
	}
	
	public TabFileReader(String fileName, String encoding) throws FileNotFoundException {
		init(new FileInputStream(fileName), encoding);
	}

	public TabFileReader(InputStream stream, String encoding) {
		init(stream, encoding);
	}
	
	public String[] readLine() throws IOException {
		String line;
		do { 
			line= reader.readLine();
			if (line == null)
				return null;
		} while (line.startsWith("#"));
		
		return line.split("\t");
	}

	private void init(InputStream stream, String encoding) {
		try {
			reader = new BufferedReader(new InputStreamReader(stream, encoding));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
}

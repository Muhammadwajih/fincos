package pt.uc.dei.fincos.data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import pt.uc.dei.fincos.basic.Globals;


public class CSVReader {
	private BufferedReader reader;	
	private String separator;
	private String filePath;
	
	public CSVReader(String path) throws FileNotFoundException {
		this(path, Globals.CSV_SEPARATOR);
	}
	
	public CSVReader(String path, String separator) throws FileNotFoundException {
		this.filePath = path;
		reader = new BufferedReader(new FileReader(path));			
		this.separator = separator;
	}
	
	public String[] getNextRecord() throws IOException {
		String [] cols = null;
		String line = reader.readLine();
		
		if (line != null)			
			cols = split(line, this.separator);
			
		return cols; 
	}
	
	public String getNextLine() throws IOException {		
		return reader.readLine();		
	}
	
	/**
	 * More efficient implementation of split method for string
	 * 
	 * @param line			The record to be split
	 * @param separator		The character used to split record's fields
	 * @return
	 */
	public static String[] split(String line, String separator) {
		String[] ret = null;
		ArrayList<String> cols = new ArrayList<String>();
		int commaIndex;
		while (true) {
			commaIndex = line.lastIndexOf(separator);
			if(commaIndex != -1) {
				cols.add(line.substring(commaIndex+1));
				line = line.substring(0, commaIndex);
			}
			else {
				cols.add(line);
				break;
			}
		}
		ret = new String[cols.size()];
		
		int i = ret.length-1;
		for (String c : cols) {
			ret[i] = c;
			i--;
		}
		return ret;
	}
	
	
	public void closeFile() throws IOException{
		reader.close();
	}
	
	public void reopen() throws IOException {
		this.closeFile();
		reader = new BufferedReader(new FileReader(this.filePath));
	}
}

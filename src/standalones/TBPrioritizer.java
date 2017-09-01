package standalones;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import bean.TBSourceTO;
import manager.DBManager;
import manager.PSRCATManager;

public class TBPrioritizer {

	public static void main(String[] args) throws IOException {
		
		System.err.println(PSRCATManager.loadDB());
		

	}
}

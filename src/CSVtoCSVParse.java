import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class CSVtoCSVParse {

	public static double[] percentileList = { 20, 30, 40, 50, 60, 70, 80, 90, 100 };

	public static void main(String args[]) throws IOException {
		System.out.println("Insert the CSV File");
		Scanner in = new Scanner(System.in);
		String line = null;
		String file_path = in.nextLine();
		Map<String, Integer> MapColumn=null;
		try {
			String csvSplitter = ",";
			String columnSplit = " ";
			BufferedReader br = new BufferedReader(new FileReader(file_path));
			/*
			 * Create a Map in which will Map score and percentile relevance
			 * value which we intend to set in
			 */
			String[] split_line=br.readLine().split(",");
			MapColumn=Find_Columns(split_line);
			Map<Integer, Double> k_Map = CalculateRelevence(br,MapColumn.get("Relevance"),MapColumn.get("Event"));
			br.close();
			br = new BufferedReader(new FileReader(file_path));
			String f_Path = "E:\\CSV.2.csv";
			File ff = new File(f_Path);
			PrintWriter output = new PrintWriter(ff, "UTF-8");
			int i = 0;
			while ((line = br.readLine()) != null) {
				String[] cols = line.split(csvSplitter);
				String checkbloom=cols[MapColumn.get("Event")];
				String to_check="Bloomberg";
				if (i == 0) {
					
					String[] date_field1 = cols[MapColumn.get("Date Time")].split(columnSplit);
					output.println(date_field1[0] + csvSplitter + date_field1[1] + csvSplitter + cols[MapColumn.get("Event")] + csvSplitter
							+cols[MapColumn.get("Period")] + csvSplitter+ cols[MapColumn.get("Survey")] + csvSplitter + cols[MapColumn.get("Actual")] + csvSplitter + cols[MapColumn.get("Prior")] + csvSplitter 
							+ cols[MapColumn.get("Revised")]+ csvSplitter  + "Importance");
					i++;
				}
				else if(checkbloom.toLowerCase().contains(to_check.toLowerCase())){
					continue;
				}
				else if(cols.length<8){
					continue;
				}
				else if (isNumeric(cols[MapColumn.get("Relevance")]) && i != 0) {
					double v_original = Double.parseDouble(cols[MapColumn.get("Relevance")]);
					double calculated_relavance = -1;
					/*
					 * take consideration of the relevance value score to set on
					 * that column
					 */
					for (Map.Entry<Integer, Double> entry : k_Map.entrySet()) {
						if (entry.getValue() < v_original) {
							continue;
						} else {
							calculated_relavance = entry.getKey();
							break;
						}
					}
					/*
					 * date and time split to set new column
					 */
					String[] date_field = cols[MapColumn.get("Date Time")].split(columnSplit);
					if (date_field.length < 2) {
						output.println(date_field[0] + csvSplitter + null + csvSplitter + cols[MapColumn.get("Event")] + csvSplitter
								+cols[MapColumn.get("Period")] + csvSplitter+ cols[MapColumn.get("Survey")] + csvSplitter + cols[MapColumn.get("Actual")] + csvSplitter + cols[MapColumn.get("Prior")] + csvSplitter 
								+ cols[MapColumn.get("Revised")]+ csvSplitter  + calculated_relavance);
					} else {
						output.println(date_field[0] + csvSplitter + date_field[1] + csvSplitter + cols[MapColumn.get("Event")] + csvSplitter+
								cols[MapColumn.get("Period")] + csvSplitter+ cols[MapColumn.get("Survey")] + csvSplitter + cols[MapColumn.get("Actual")] + csvSplitter + cols[MapColumn.get("Prior")] + csvSplitter 
								+ cols[MapColumn.get("Revised")]+ csvSplitter  + calculated_relavance);
					}
				}
			}
			br.close();
			output.close();
			System.out.println("the file path is : " + ff.getAbsolutePath());
		} catch (IOException e) {

			System.out.println("exception at reading: " + e);
		}

	}

	public static  Map<String, Integer> Find_Columns(String[] cols) {
		// TODO Auto-generated method stub
		String date_time="Date Time";
		String event="Event";
		String survey="Survey";
		String actual="Actual";
		String period="Period";
		String prior="Prior";
		String revised="Revised";
		String relevance = "Relevance";
		Map<String, Integer> MapColumn = new HashMap<String, Integer>();
		for (int i = 0; i < cols.length; i++) {
			if (cols[i].toLowerCase().contains(date_time.toLowerCase())) {
				MapColumn.put(date_time, i);
			} else if (cols[i].toLowerCase().contains(event.toLowerCase())) {
				MapColumn.put(event, i);
			} else if (cols[i].toLowerCase().contains(survey.toLowerCase())) {
				MapColumn.put(survey, i);
			} else if (cols[i].toLowerCase().contains(actual.toLowerCase())) {
				MapColumn.put(actual, i);
			} else if (cols[i].toLowerCase().contains(prior.toLowerCase())) {
				MapColumn.put(prior, i);
			} else if (cols[i].toLowerCase().contains(revised.toLowerCase())) {
				MapColumn.put(revised, i);
			} else if (cols[i].toLowerCase().contains(relevance.toLowerCase())) {
				MapColumn.put(relevance, i);
			}
			else if(cols[i].toLowerCase().contains(period.toLowerCase())){
				MapColumn.put(period, i);
			}
		}
		return MapColumn;
	}

	// public static double[] percentiles(ArrayList<Double> numbers, double...
	// percentiles) {
	// Collections.sort(numbers);
	// double[] values = new double[percentiles.length];
	// for (int i = 0; i < percentiles.length; i++) {
	// int index = (int) (percentiles[i] * numbers.size());
	// values[i] = numbers.get(index);
	// System.out.println("Value: " + values[i]);
	// }
	// return values;
	// }

	public static Map<Integer, Double> CalculateRelevence(BufferedReader br,int relevanceIndex,int eventIndex) {
		// TODO Auto-generated method stub
		String to_check="Bloomberg";
		// ArrayList<Integer> values_relevence = new ArrayList<Integer>();
		ArrayList<Double> percentiles_Values = new ArrayList<Double>();
		Map<Integer, Double> k_Map = new HashMap<Integer, Double>();
		try {
			String line = null;
			int i = 0;
			while ((line = br.readLine()) != null) {
				String[] cols = line.split(",");
				if (i == 0) {
					i++;
					continue;
				}
				else if(cols[eventIndex].toLowerCase().contains(to_check.toLowerCase())){
					continue;
					
				}
				else if (isNumeric(cols[relevanceIndex])) {
					percentiles_Values.add(Double.parseDouble(cols[relevanceIndex]));
				}

			}
			int score = 1;
			for (int j = 0; j < percentileList.length; j++) {
				double v_relevance = Percentile(percentiles_Values, percentileList[j]);
				k_Map.put(score, v_relevance);
				score++;
			}
			// br.close();
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println(e);
		}
		return k_Map;
	}

	public static double Percentile(ArrayList<Double> latencies, double Percentile) {
		Collections.sort(latencies);
		int Index = (int) Math.ceil(((double) Percentile / (double) 100) * (double) latencies.size());
		System.out.println(latencies.get(Index - 1));
		return latencies.get(Index - 1);
	}

	public static boolean isNumeric(String str) {
		try {
			double d = Double.parseDouble(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}
}

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.github.opendevl.JFlat;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ConverterClass {

	public static void main(String args[]) throws IOException{
//		BufferedReader reader=null;
//        StringBuilder content=null;
//        String result=null;
//
//            reader = new BufferedReader(new FileReader("json_files/test.postman_test_run.json"));
//
//            String line = null;
//            content= new StringBuilder();
//
//            while ((line = reader.readLine()) != null) {
//            content.append(line);
//            }
//            reader.close();
//            result= content.toString();
//
//            JsonElement jelement = new JsonParser().parse(result);
//
//            printJsonRecursive(jelement);
		
		String str = new String(Files.readAllBytes(Paths.get("json_files/test.postman_test_run.json")));

		JFlat flatMe = new JFlat(str);

		//directly write the JSON document to CSV
		//flatMe.json2Sheet().write2csv("json_files/test.postman_test_run.json");
		List<Object[]> json2csv = flatMe.json2Sheet().getJsonAsSheet();
		//directly write the JSON document to CSV but with delimiter
//		flatMe.json2Sheet().write2csv("json_files/test.postman_test_run.json", '|');

	}
	public static void printJsonRecursive(JsonElement jelement){


        if(jelement.isJsonPrimitive()){

            System.out.println(jelement.getAsString());
            return;
        }
        if(jelement.isJsonArray()){

            JsonArray jarray= jelement.getAsJsonArray();
            for(int i=0;i<jarray.size();i++){
                JsonElement element= jarray.get(i);
                printJsonRecursive(element);
            }
            return;

        }
        JsonObject  jobject= jelement.getAsJsonObject();

        Set<Entry<String, JsonElement>> set= jobject.entrySet();

        for (Entry<String, JsonElement> s : set) {
        	System.out.println(s.getKey()); 
            printJsonRecursive(s.getValue());


        }

    }

}

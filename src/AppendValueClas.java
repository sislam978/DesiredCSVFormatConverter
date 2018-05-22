import java.lang.reflect.Field;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AppendValueClas {
    /**
    *
    * @param c The class to get fields from
    * @param rootPackage The root package to compare(we only get fields from the same package to exclude other java class)
    * @param target the target object to get value from
    * @param sb the string builder to append values with comma delimited
    *
    * @throws IllegalAccessException This function will search recursively and append all the values of the 'target' Object.
    */
   void appendDeclaredFieldValues(Class c, String rootPackage, Object target, StringBuilder sb) throws IllegalAccessException
   {
       Field[] fields = c.getDeclaredFields();
       for (Field field : fields)
       {
           Class filedClass = field.getType();
           field.setAccessible(true);
           Object childObject = null;
           try
           {
               //try to get the object value from the 'target' Object
               childObject = field.get(target);
           }
           catch (Exception e)
           {
               //do nothing, just a try to get value, exception is expected with empty columns
           }
           //if we have declaredfileds and the filed is in in the same package  as root and filed is not Enum, we continue search
           if (filedClass.getDeclaredFields().length > 0 && filedClass.getPackage().getName().contains(rootPackage) && !filedClass.isEnum())
           {
               appendDeclaredFieldValues(filedClass, rootPackage, childObject, sb);
           }
           //If it is plain fields like String/int/bigDecimal, we append the filed value.
           else
           {
               //Since this is served as CSV, we do not want the object value contains comma which would break the formatting.
               sb.append(",").append(String.valueOf(childObject).replaceAll(",", "").replaceAll("(\r\n|\n)", ""));
           }

       }
   }
   
   public static void main(String [] args) throws Exception{
	   String value="0.554688";
	   //String vv=String.format("%.5g%n", value);
	   DecimalFormat df = new DecimalFormat("#.#####");
	   df.setRoundingMode(RoundingMode.CEILING);
	   
	   //System.out.println(df.format(Double.parseDouble("0.554688")).toString());
	   
	   SimpleDateFormat input_format = new SimpleDateFormat("MM/DD/YY");
		// Date date= new SimpleDateFormat(dateToformat);
		String formatedDate = null;
		// String toformat="12/31/2017";

		Date date;
		Date cur_date=new Date();
		DateFormat dateFormat = new SimpleDateFormat("MMMM d, yy");
		//Date cc_date=dateFormat.parse("05/22/2018");
		
		long end_dates = cur_date.getTime()/1000 + 86400;
		String dd=stringTodate("May 21, 2018","MMMM d, yy","yyyy-MM-dd");
		System.out.println(dd);
		try {
			date = input_format.parse("05/01/18");
			
			SimpleDateFormat output_format = new SimpleDateFormat("yyyy-MM-dd");
		
			
			formatedDate = output_format.format(date);
			 System.out.println("printed date: "+formatedDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
   }
   
	public static String stringTodate(String date, String formatter, String format) throws Exception {
		// System.out.println( "String "+ date+ " formatter "+ formatter+"
		// format "+ format);
		SimpleDateFormat desiredFormat = new SimpleDateFormat(format);
		SimpleDateFormat dateFormatter = new SimpleDateFormat(formatter);

		Date newdate = null;
		String newDateString = null;
		try {
			newdate = dateFormatter.parse(date);
			newDateString = desiredFormat.format(newdate);
			// System.out.println(newDateString);
		} catch (ParseException e) {
			e.printStackTrace();
			throw e;
		}
		// System.out.println("newDateString : "+newDateString);
		return newDateString;

	}
   
	public static String dateFormation(String toformat) {

		SimpleDateFormat input_format = new SimpleDateFormat("mm/dd/yy");
		// Date date= new SimpleDateFormat(dateToformat);
		String formatedDate = null;
		// String toformat="12/31/2017";

		Date date;
		
		try {
			date = input_format.parse(toformat);
			SimpleDateFormat output_format = new SimpleDateFormat("yyyy-MM-dd");
			formatedDate = output_format.format(date);
			// System.out.println("printed date: "+formatedDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return formatedDate;
	}

}

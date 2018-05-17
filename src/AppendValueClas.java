import java.lang.reflect.Field;

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

}

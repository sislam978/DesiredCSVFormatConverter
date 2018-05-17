import java.lang.reflect.Field;

public class AppendFieldClass {
	//StringBuilder sb = new StringBuilder();
    //appendAllDeclaredFields(YourClass.class, YourClass.class.getPackage().getName(), "", sb);
    /**
     *
     * @param c The class to get fields from
     * @param rootPackage The root package to compare(we only get fields from the same package to exclude other java class)
     * @param parentName The parent class name so that we could combine all its path.
     * @param sb the string builder to append values with comma delimited
     * <p/>
     * This function will search recursively and append all the fields
     * MyObject{int fileld1, OtherObject, other}, OtherObject{int filed2, String filed3}  --> filed1,other_filed2,otherfield3
     */
    void appendAllDeclaredFields(Class c, String rootPackage, String parentName, StringBuilder sb)
    {
        Field[] fields = c.getDeclaredFields();
        for (Field field : fields)
        {
            Class filedClass = field.getType();
            String fieldName = field.getName();
            //if we have declaredfileds and the filed is in in the same package  as root and filed is not Enum, we continue search
            if (filedClass.getDeclaredFields().length > 0 && filedClass.getPackage().getName().contains(rootPackage) && !filedClass.isEnum())
            {
                appendAllDeclaredFields(filedClass, rootPackage, getCombinedName(parentName, fieldName), sb);
            }
            //If it is plain fields like String/int/bigDecimal, we append the filed name.
            else
            {
                sb.append(",").append(getCombinedName(parentName, fieldName));
            }
        }
    }

    private String getCombinedName(String parentName, String fieldName)
    {
        return "".equals(parentName) ? fieldName : parentName + "_" + fieldName;
    }

}

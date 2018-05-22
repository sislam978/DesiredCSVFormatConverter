import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class UpdateVolatilityIndex {
	public static final SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
	private static Connection connectLocal() throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.jdbc.Driver");
		Connection con = (Connection) DriverManager
				.getConnection("jdbc:mysql://localhost:3306/new_database?rewriteBatchedStatements=true", "root", "");
		con.setAutoCommit(true);
		return con;
	}
	
	public static void SpikeCalculation_UpdateVolatilityRows(String desired_date) throws ClassNotFoundException, SQLException{
		
		Connection con=connectLocal();
		Calendar instance=Calendar.getInstance();
		instance.add(Calendar.DATE, -22);
		String considered_date=dateformat.format(instance.getTime());
		String SQL_QUERY = "SELECT * FROM Volatility_Index where history_date>='"+considered_date+"' and history_date<='"+desired_date+"'";
		
		Statement statement = con.createStatement();
		ResultSet rs = statement.executeQuery(SQL_QUERY);
		ArrayList<Double> adjList=new ArrayList<Double>();
		while(rs.next()){
			double adjClose=rs.getDouble(7);
			adjList.add(adjClose);
			System.out.println(adjClose);
		}
		Collections.sort(adjList);
		double dividend=adjList.get(adjList.size()-1);
		double divisor=adjList.get(0);
		double spike_Value=(dividend/divisor)-1;
		
		String query_str="UPDATE volatility_index SET spike='"+spike_Value+"' WHERE history_date='"+desired_date+"'";
		Statement update_statement=con.createStatement();
		update_statement.executeUpdate(query_str);
	}
	
	public static void returnCalculate_updatetableData(String considered_date,String desired_date) throws ClassNotFoundException, SQLException{
		Connection con=connectLocal();
		Calendar instance=Calendar.getInstance();
		instance.add(Calendar.DATE, -22);
		//String considered_date=dateformat.format(instance.getTime());
		String SQL_QUERY = "SELECT * FROM Volatility_Index where history_date>='"+considered_date+"' and history_date<='"+desired_date+"' order by history_date asc";
		
		Statement statement = con.createStatement();
		ResultSet rs = statement.executeQuery(SQL_QUERY);
		Map<String,Double> adjList=new HashMap<String,Double>();
		int i=0;
		double prev_value=-1;
		while(rs.next()){
			
			double adjClose=rs.getDouble(7);
			String dates=rs.getString(2);
			if(i==0){
				String query_str="UPDATE volatility_index SET return_val='"+0+"' WHERE history_date='"+dates+"'";
				Statement update_statement=con.createStatement();
				update_statement.executeUpdate(query_str);
				prev_value=adjClose;
				i++;
				continue;
			}
			double returnValue=adjClose/prev_value;
			String query_str="UPDATE volatility_index SET return_val='"+returnValue+"' WHERE history_date='"+dates+"'";
			Statement update_statement=con.createStatement();
			update_statement.executeUpdate(query_str);
			prev_value=adjClose;
		}
	}
	
	public static void main(String [] args) throws ClassNotFoundException, SQLException{
		Scanner in=new Scanner(System.in);
		
		String dates=in.nextLine();
		String end_date=in.nextLine();
		//SpikeCalculation_UpdateVolatilityRows(end_date);
		returnCalculate_updatetableData(dates,end_date);
	}


}

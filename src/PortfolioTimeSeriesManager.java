import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.print.attribute.HashAttributeSet;

public class PortfolioTimeSeriesManager {

	public static final int Y1 = 122;
	public static final int Y2 = 123;
	public static final int Y3 = 124;
	public static final int Y5 = 125;
	public static final int Y7 = 126;
	public static final int Y10 = 127;
	public static final int Y20 = 128;
	public static final int Y30 = 129;

	public static final Map<Integer, Integer> percentIndexMap = new HashMap<Integer, Integer>();
	public static ArrayList<Integer> YieldIndex = new ArrayList<Integer>();

	private static Connection connectLocal() throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.jdbc.Driver");
		Connection con = (Connection) DriverManager
				.getConnection("jdbc:mysql://localhost:3306/new_database?rewriteBatchedStatements=true", "root", "");
		con.setAutoCommit(true);
		return con;
	}

	private static Connection connectKkrClient() throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.jdbc.Driver");
		Connection con = (Connection) DriverManager
				.getConnection("jdbc:mysql://localhost:3306/kkrclient?rewriteBatchedStatements=true", "root", "");
		con.setAutoCommit(true);
		return con;
	}

	public static void insertData(String start_date, String end_date)
			throws ClassNotFoundException, SQLException, ParseException {

		Connection conL = connectLocal();
		Connection conKkr = connectKkrClient();

		
		String SQL_query = "SELECT * FROM `treasury_yield_curve_rates` WHERE rates_date>='" + start_date
				+ "' AND rates_date<='" + end_date + "' ORDER BY rates_date ASC";

		Statement locStat = conL.createStatement();

		ResultSet rsL = locStat.executeQuery(SQL_query);
		/*
		 * Yr1,Yr2,Yr3,Yr5,Yr7,Yr10,Yr20,Yr30 index value from
		 * user_saved_portfolio table write as a constant because it would never
		 * change. create a arraylist from the constant further convenience
		 */

		int mm = 0;
		while (rsL.next()) {
			/*
			 * values of Yr1, Yr2, Yr3,Yr5,Yr7,Yr10,Yr20,Yr30
			 */
			ArrayList<Double> YieldValues = new ArrayList<Double>();
			ArrayList<Double> Yield_return = new ArrayList<Double>();
			YieldValues.add(rsL.getDouble(6));
			YieldValues.add(rsL.getDouble(7));
			YieldValues.add(rsL.getDouble(8));
			YieldValues.add(rsL.getDouble(9));
			YieldValues.add(rsL.getDouble(10));
			YieldValues.add(rsL.getDouble(11));
			YieldValues.add(rsL.getDouble(12));
			YieldValues.add(rsL.getDouble(13));

			for (int i = 0; i < YieldValues.size(); i++) {
				if (!YieldValues.get(i).isNaN()) {
					double vv = (1 + (YieldValues.get(i) / 100));
					double p = 1.00 / 252;
					double pow_val = Math.pow(vv, p);
					double f_val = pow_val - 1;
					Yield_return.add(f_val);
				} else {
					Yield_return.add(0.0);
				}
			}
			/*
			 * insert satement create
			 */
			PreparedStatement pSLocal = conKkr.prepareStatement(
					"insert into  user_saved_portfolio_timeseries (user_saved_portfolio_id,timeseries_date,returns) values (?,?,?)");
			for (int j = 0; j < Yield_return.size(); j++) {

				pSLocal.setInt(1, YieldIndex.get(j));
				pSLocal.setString(2, rsL.getString(2));
				pSLocal.setDouble(3, Yield_return.get(j));

				pSLocal.execute();
				System.out.println(mm++);
			}
		}

	}

	private static void inserDataForPercents( String start_date, String end_date)
			throws ParseException, SQLException, ClassNotFoundException {
		// TODO Auto-generated method stub
		
		Connection conKkr=connectKkrClient();
		SimpleDateFormat input_format = new SimpleDateFormat("yyyy-MM-dd");
		Date dateStart = input_format.parse(start_date);
		Date EndDate = input_format.parse(end_date);

		Calendar start = Calendar.getInstance();
		start.setTime(dateStart);
		Calendar end = Calendar.getInstance();

		end.setTime(EndDate);
		end.add(Calendar.DATE, 1);

		PreparedStatement pSLocal = conKkr.prepareStatement(
				"insert into  user_saved_portfolio_timeseries (user_saved_portfolio_id,timeseries_date,returns) values (?,?,?)");
		int mm = 0;
		for (Date date = start.getTime(); start.before(end); start.add(Calendar.DATE, 1), date = start.getTime()) {
			String desired_date = input_format.format(date);
			for (Map.Entry<Integer, Integer> entry : percentIndexMap.entrySet()) {
				double vv = 1 + ((entry.getValue() * 1.00) / 100);
				double p = 1.00 / 252;
				double pow_val = Math.pow(vv, p);
				double return_val = pow_val - 1;

				pSLocal.setInt(1, entry.getKey());
				pSLocal.setString(2, desired_date);
				pSLocal.setDouble(3, return_val);

				pSLocal.execute();
				System.out.println(mm++);

			}
		}

	}

	public static void updatePortfolioTimeSeries(String start_date, String end_date)
			throws ClassNotFoundException, SQLException {
		Connection conKkr = connectKkrClient();

		for (int k = 0; k < YieldIndex.size(); k++) {

			String SQL_query = "SELECT * FROM user_saved_portfolio_timeseries WHERE timeseries_date>='" + start_date
					+ "' AND timeseries_date<='" + end_date + "' and user_saved_portfolio_id=" + YieldIndex.get(k)
					+ " ORDER BY timeseries_date ASC";

			Statement kkrStat = conKkr.createStatement();

			ResultSet rsKkr = kkrStat.executeQuery(SQL_query);
			int i = 0;
			double prev_close = -1;
			int mm = 0;
			while (rsKkr.next()) {
				if (i == 0) {
					prev_close = rsKkr.getDouble(5);
					i++;
					continue;
				}
				double calculated_close = prev_close * (rsKkr.getDouble(4) + 1);
				String time_seriesdate = rsKkr.getString(3);
				String query_str = "UPDATE user_saved_portfolio_timeseries SET close='" + calculated_close
						+ "' WHERE timeseries_date='" + time_seriesdate + "'";
				Statement update_statement = conKkr.createStatement();
				update_statement.executeUpdate(query_str);
				prev_close = calculated_close;
				System.out.println(mm++);
			}
		}

	}

	public static void main(String[] args) throws ClassNotFoundException, SQLException, ParseException {

		Scanner in = new Scanner(System.in);
		System.out.println("enter the start date:");
		String start_date = in.nextLine();
		System.out.println("enter the end date:");
		String end_date = in.nextLine();
		// insertData(start_date, end_date);
		YieldIndex.add(Y1);
		YieldIndex.add(Y2);
		YieldIndex.add(Y3);
		YieldIndex.add(Y5);
		YieldIndex.add(Y7);
		YieldIndex.add(Y10);
		YieldIndex.add(Y20);
		YieldIndex.add(Y30);

		percentIndexMap.put(131, 5);
		percentIndexMap.put(132, 10);
		percentIndexMap.put(133, 15);
		percentIndexMap.put(134, 20);

		//updatePortfolioTimeSeries(start_date, end_date);
		//inserDataForPercents(start_date, end_date);
		updatePortfolioTimeSeriePercents(start_date,end_date);
	}
	
	public static void updatePortfolioTimeSeriePercents(String start_date, String end_date)
			throws ClassNotFoundException, SQLException {
		Connection conKkr = connectKkrClient();

		for (Map.Entry<Integer, Integer> entry : percentIndexMap.entrySet()) {

			String SQL_query = "SELECT * FROM user_saved_portfolio_timeseries WHERE timeseries_date>='" + start_date
					+ "' AND timeseries_date<='" + end_date + "' and user_saved_portfolio_id=" + entry.getKey()
					+ " ORDER BY timeseries_date ASC";

			Statement kkrStat = conKkr.createStatement();

			ResultSet rsKkr = kkrStat.executeQuery(SQL_query);
			int i = 0;
			double prev_close = -1;
			int mm = 0;
			while (rsKkr.next()) {
				if (i == 0) {
					prev_close = rsKkr.getDouble(5);
					i++;
					continue;
				}
				double calculated_close = prev_close * (rsKkr.getDouble(4) + 1);
				String time_seriesdate = rsKkr.getString(3);
				String query_str = "UPDATE user_saved_portfolio_timeseries SET close='" + calculated_close
						+ "' WHERE timeseries_date='" + time_seriesdate + "'";
				Statement update_statement = conKkr.createStatement();
				update_statement.executeUpdate(query_str);
				prev_close = calculated_close;
				System.out.println(mm++);
			}
		}

	}

}

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class webScraping {
	
	
	public static void main(String args[]) throws Exception{
		Scanner input=new Scanner(System.in);
		System.out.println("Enter year if we want to fetch historical data");
		String year=input.nextLine();
		treasuryWebsiteRead(year);
	}

	private static Connection connectLocal() throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.jdbc.Driver");
		Connection con = (Connection) DriverManager
				.getConnection("jdbc:mysql://localhost:3306/new_database?rewriteBatchedStatements=true", "root", "");
		con.setAutoCommit(true);
		return con;
	}

	public static String stringTodate(String date, String formatter, String format) throws Exception
	{
		//System.out.println(  "String   "+   date+ " formatter  "+ formatter+" format   "+ format);
		SimpleDateFormat desiredFormat = new SimpleDateFormat(format);
		SimpleDateFormat dateFormatter = new SimpleDateFormat(formatter); 

		Date newdate = null;
		String newDateString = null;
	    try {
	        newdate = dateFormatter.parse(date);
	        newDateString = desiredFormat.format(newdate);
	        //System.out.println(newDateString);
	    } catch (ParseException e) {
	        e.printStackTrace();
	        throw e;
	    }
		//System.out.println("newDateString : "+newDateString);
		return newDateString;
		
	}

	public static void treasuryWebsiteRead(String year) throws Exception {
		try {
			// fetch the document over HTTP
			Document doc = null;
			
			if (year.equals("")) {
				doc = Jsoup
						.connect(
								"https://www.treasury.gov/resource-center/data-chart-center/interest-rates/Pages/TextView.aspx?data=yield")
						.get();
			}
			else{
				doc = Jsoup 
						.connect(
								"https://www.treasury.gov/resource-center/data-chart-center/interest-rates/Pages/TextView.aspx?data=yieldYear&year="+year+"")
						.get();
			}
					

			// get the page title
			String title = doc.title();
			System.out.println("title: " + title);

			Elements table_tag = doc.select("table");
			// get all links in page
			Elements links = doc.select("a[href]");
			// for (Element link : links) {
			// // get the value from the href attribute
			// System.out.println("\nlink: " + link.attr("href"));
			// System.out.println("text: " + link.text());
			// }

			ArrayList<TreasureyYieldCurveRate> downServers = new ArrayList<TreasureyYieldCurveRate>();
			Element table = doc.select("table").get(0); // select the first
														// table.
			Elements rows = table.select("tr");

			for (int i = 1; i < rows.size(); i++) { // first row is the col
													// names so skip it.
				Element row = rows.get(i);
				Elements cols = row.select("td");
				TreasureyYieldCurveRate tycr = new TreasureyYieldCurveRate();
				if (cols.size() == 12) {
					// System.out.println(cols.get(0).text());
					String dates = stringTodate(cols.get(0).text(),"MM/dd/yy","yyyy-MM-dd");
					Double Mo1 = (cols.get(1).text().toString().equals("N/A"))? null:Double.parseDouble(cols.get(1).text());
					Double Mo3 = (cols.get(2).text().toString().equals("N/A"))? null:Double.parseDouble(cols.get(2).text());
					Double Mo6 = (cols.get(3).text().toString().equals("N/A"))? null:Double.parseDouble(cols.get(3).text());
					Double Yr1 = (cols.get(4).text().toString().equals("N/A"))? null:Double.parseDouble(cols.get(4).text());
					Double Yr2 = (cols.get(5).text().toString().equals("N/A"))? null:Double.parseDouble(cols.get(5).text());
					Double Yr3 = (cols.get(6).text().toString().equals("N/A"))? null:Double.parseDouble(cols.get(6).text());
					Double Yr5 = (cols.get(7).text().toString().equals("N/A"))? null:Double.parseDouble(cols.get(7).text());
					Double Yr7 = (cols.get(8).text().toString().equals("N/A"))? null:Double.parseDouble(cols.get(8).text());
					Double Yr10 = (cols.get(9).text().toString().equals("N/A"))? null:Double.parseDouble(cols.get(9).text());
					Double Yr20 = (cols.get(10).text().toString().equals("N/A"))? null:Double.parseDouble(cols.get(10).text());
					Double Yr30 = (cols.get(11).text().toString().equals("N/A"))? null:Double.parseDouble(cols.get(11).text());

					tycr.setDates(dates);
					
					tycr.setMo1(Mo1);
					tycr.setMo3(Mo3);
					tycr.setMo6(Mo6);
					tycr.setYr1(Yr1);
					tycr.setYr2(Yr2);
					tycr.setYr3(Yr3);
					tycr.setYr5(Yr5);
					tycr.setYr7(Yr7);
					tycr.setYr10(Yr10);
					tycr.setYr20(Yr20);
					tycr.setYr30(Yr30);

					downServers.add(tycr);
				}

				// tycr.setRate_date(cols.);
				// System.out.println(cols);
			}

			InsertDataIntotreasury(downServers);
			// for(int i=0;i<downServers.size();i++){
			// System.out.println(downServers.get(i));
			// }
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void InsertDataIntotreasury(ArrayList<TreasureyYieldCurveRate> downServers)
			throws ClassNotFoundException, SQLException {
		// TODO Auto-generated method stub
		Connection con = connectLocal();

		for (int i = 0; i < downServers.size(); i++) {
			
			String SQL_QUERY = "SELECT * FROM treasury_yield_curve_rates WHERE rates_date ='"
					+ downServers.get(i).getDates() + "'";
			Statement checkstatement = con.createStatement();
			ResultSet checkSet = checkstatement.executeQuery(SQL_QUERY);
			int size = 0;
			if (checkSet != null) {
				checkSet.beforeFirst();
				checkSet.last();
				size = checkSet.getRow();
			}

			if (size < 1) {
				System.out.println(SQL_QUERY);
				PreparedStatement pSLocal = con.prepareStatement(
						"insert into treasury_yield_curve_rates (rates_date,Mo1,Mo3,Mo6,Yr1,Yr2,Yr3,"
								+ "Yr5,Yr7,Yr10,Yr20,Yr30) values (?,?,?,?,?,?,?,?,?,?,?,?)");

				pSLocal.setString(1, downServers.get(i).getDates());
				if(downServers.get(i).getMo1()==null){
					pSLocal.setNull(2, java.sql.Types.DOUBLE);
				}
				else{
					pSLocal.setDouble(2, downServers.get(i).getMo1());
				}
				if(downServers.get(i).getMo3()==null){
					pSLocal.setNull(3, java.sql.Types.DOUBLE);
				}
				else{
					pSLocal.setDouble(3, downServers.get(i).getMo3());
				}
				if(downServers.get(i).getMo6()==null){
					pSLocal.setNull(4, java.sql.Types.DOUBLE);
				}
				else{
					pSLocal.setDouble(4, downServers.get(i).getMo6());
				}
				if(downServers.get(i).getYr1()==null){
					pSLocal.setNull(5, java.sql.Types.DOUBLE);
				}
				else{
					pSLocal.setDouble(5, downServers.get(i).getYr1());
				}
				if(downServers.get(i).getYr2()==null){
					pSLocal.setNull(6, java.sql.Types.DOUBLE);
				}
				else{
					pSLocal.setDouble(6, downServers.get(i).getYr2());
				}
				if(downServers.get(i).getYr3()==null){
					pSLocal.setNull(7, java.sql.Types.DOUBLE);
				}
				else{
					pSLocal.setDouble(7, downServers.get(i).getYr3());
				}
				if(downServers.get(i).getYr5()==null){
					pSLocal.setNull(8, java.sql.Types.DOUBLE);
				}
				else{
					pSLocal.setDouble(8, downServers.get(i).getYr5());
				}
				if(downServers.get(i).getYr7()==null){
					pSLocal.setNull(9, java.sql.Types.DOUBLE);
				}
				else{
					pSLocal.setDouble(9, downServers.get(i).getYr7());
				}
				if(downServers.get(i).getYr10()==null){
					pSLocal.setNull(10, java.sql.Types.DOUBLE);
				}
				else{
					pSLocal.setDouble(10, downServers.get(i).getYr10());
				}
				if(downServers.get(i).getYr20()==null){
					pSLocal.setNull(11, java.sql.Types.DOUBLE);
				}
				else{
					pSLocal.setDouble(11, downServers.get(i).getYr20());
				}
				if(downServers.get(i).getYr30()==null){
					pSLocal.setNull(12, java.sql.Types.DOUBLE);
				}
				else{
					pSLocal.setDouble(12, downServers.get(i).getYr30());
				}
				
				

				pSLocal.execute();
			}
		}

	}

}

/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 * William Sio
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.sql.Timestamp;
import java.text.*;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.*;
/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class Cafe {

   //login info for later use
   private static String authorisedUser = null;

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of Cafe
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public Cafe (String dbname, String dbport) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://127.0.0.1:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end Cafe

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
	 if(outputHeader){
	    for(int i = 1; i <= numCol; i++){
		System.out.print(rsmd.getColumnName(i) + "\t");
	    }
	    System.out.println();
	    outputHeader = false;
	 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close ();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
      // creates a statement object 
      Statement stmt = this._connection.createStatement (); 
 
      // issues the query instruction 
      ResultSet rs = stmt.executeQuery (query); 
 
      /* 
       ** obtains the metadata object for the returned result set.  The metadata 
       ** contains row and column info. 
       */ 
      ResultSetMetaData rsmd = rs.getMetaData (); 
      int numCol = rsmd.getColumnCount (); 
      int rowCount = 0; 
 
      // iterates through the result set and saves the data returned by the query. 
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>(); 
      while (rs.next()){
          List<String> record = new ArrayList<String>(); 
         for (int i=1; i<=numCol; ++i) 
            record.add(rs.getString (i)); 
         result.add(record); 
      }//end while 
      stmt.close (); 
      return result; 
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       if(rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current 
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();
	
	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 2) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            Cafe.class.getName () +
            " <dbname> <port>");
         return;
      }//end if

      Greeting();
      Cafe esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the Cafe object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         esql = new Cafe (dbname, dbport);

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              String user_type = find_type(esql);
	      switch (user_type){
		case "Customer": 
		  while(usermenu) {
                    System.out.println("Customer-MAIN MENU");
                    System.out.println("---------");
                    System.out.println("1. Browse Menu by ItemName");
                    System.out.println("2. Browse Menu by Type");
                    System.out.println("3. Add Order");
                    System.out.println("4. Update Order");
                    System.out.println("5. View Order History");
                    System.out.println("6. View Order Status");
                    System.out.println("7. Update User Info");
                    System.out.println(".........................");
                    System.out.println("9. Log out");
                      switch (readChoice()){
                       case 1: BrowseMenuName(esql); break;
                       case 2: BrowseMenuType(esql); break;
                       case 3: AddOrder(esql); break;
                       case 4: UpdateOrder(esql); break;
                       case 5: ViewOrderHistory(esql); break;
                       case 6: ViewOrderStatus(esql); break;
                       case 7: UpdateUserInfo(esql); break;
                       case 9: usermenu = false; break;
                       default : System.out.println("Unrecognized choice!"); break;
		      }//end switch
		  } break;
		case "Employee": 
		  while(usermenu) {
                    System.out.println("Employee-MAIN MENU");
                    System.out.println("---------");
                    System.out.println("1. Browse Menu by ItemName");
                    System.out.println("2. Browse Menu by Type");
                    System.out.println("3. Add Order");
                    System.out.println("4. Update Order");
                    System.out.println("5. View Current Orders");
                    System.out.println("6. View Order Status");
                    System.out.println("7. Update User Info");
                    System.out.println(".........................");
                    System.out.println("9. Log out");
                      switch (readChoice()){
                       case 1: BrowseMenuName(esql); break;
                       case 2: BrowseMenuType(esql); break;
                       case 3: AddOrder(esql); break;
                       case 4: EmployeeUpdateOrder(esql); break;
                       case 5: ViewCurrentOrder(esql); break;
                       case 6: ViewOrderStatus(esql); break;
                       case 7: UpdateUserInfo(esql); break;
                       case 9: usermenu = false; break;
                       default : System.out.println("Unrecognized choice!"); break;
		      }//end switch
		  } break;
		case "Manager ": 
		  while(usermenu) {
                    System.out.println("Manager-MAIN MENU");
                    System.out.println("---------");
                    System.out.println("1. Browse Menu by ItemName");
                    System.out.println("2. Browse Menu by Type");
                    System.out.println("3. Add Order");
                    System.out.println("4. Update Order");
                    System.out.println("5. View Current Orders");
                    System.out.println("6. View Order Status");
                    System.out.println("7. Update User Info");
                    System.out.println("8. Update Menu");
                    System.out.println(".........................");
                    System.out.println("9. Log out");
                      switch (readChoice()){
                       case 1: BrowseMenuName(esql); break;
                       case 2: BrowseMenuType(esql); break;
                       case 3: AddOrder(esql); break;
                       case 4: EmployeeUpdateOrder(esql); break;
                       case 5: ViewCurrentOrder(esql); break;
                       case 6: ViewOrderStatus(esql); break;
                       case 7: ManagerUpdateUserInfo(esql); break;
                       case 8: UpdateMenu(esql); break;
		       case 9: usermenu = false;break;
                       default : System.out.println("Unrecognized choice!"); break;
		      }//end switch
		  } break;
	      }//end switch
            }//end if
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface                         \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user with privided login, passowrd and phoneNum
    **/
   public static void CreateUser(Cafe esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();
         System.out.print("\tEnter user phone: ");
         String phone = in.readLine();
         
	 String type="Customer";
	 String favItems="";

	 String query = String.format("INSERT INTO USERS (phoneNum, login, password, favItems, type) VALUES ('%s','%s','%s','%s','%s')", phone, login, password, favItems, type);

         esql.executeUpdate(query);
         System.out.println ("User successfully created!");
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end
   
   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(Cafe esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();

         String query = String.format("SELECT * FROM Users WHERE login = '%s' AND password = '%s'", login, password);
         int userNum = esql.executeQuery(query);
	 if (userNum > 0)
		return login;
         return null;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end

   public static String find_type(Cafe esql){
      try{
		String query = String.format("SELECT Type FROM Users WHERE login = '%s'", authorisedUser);
		List <List<String>> Result = esql.executeQueryAndReturnResult(query);
		String Resultstring = (Result.get(0)).get(0);
		return Resultstring;
	}catch(Exception e){
		System.err.println (e.getMessage ());
		return null;
	}
  	//return "Employee";
   }

   public static void BrowseMenuName(Cafe esql){
      try{
		System.out.print("\tEnter Search name: ");
		String searchword = in.readLine();
	
		String query = String.format("SELECT * FROM Menu M WHERE M.ItemName LIKE '");
		query += "%";
		query += searchword;
		query += "%'";
	//	System.out.println(query);
		int rowcount = esql.executeQueryAndPrintResult(query);
		System.out.println("Total Row(s): " + rowcount);
		return;
	}catch(Exception e){
		System.err.println (e.getMessage ());
		return;
	}	 
		
   }//end

   public static void BrowseMenuType(Cafe esql){
     try{
		System.out.print("\tEnter Search type: ");
		String searchword = in.readLine();
	
		String query = String.format("SELECT * FROM Menu M WHERE M.Type LIKE '");
		query += "%";
		query += searchword;
		query += "%'";
	//	System.out.println(query);
		int rowcount = esql.executeQueryAndPrintResult(query);
		System.out.println("Total Row(s): " + rowcount);
		return;
	}catch(Exception e){
		System.err.println (e.getMessage ());
		return;
	}	
   }//end

   public static Integer AddOrder(Cafe esql){
     try{
		List<Double> Total_amount = new ArrayList<Double>();
		int order_repeat = 0; //counter for repeating order	
		String order = "";
		int rowcount_find = 0;
		Map<String, Integer> itemANDamount = new HashMap<String, Integer>();
		Map<String, String> itemANDcomment = new HashMap<String, String>();
		Set<String>all_order = new HashSet<String>();
		do{
			//check if the item user wants to order is valid
			do{
				System.out.print("\tEnter the name of the item: ");
				order = in.readLine();
				String query_findname = String.format("SELECT * FROM Menu M WHERE M.ItemName = '%s'", order);
				rowcount_find = esql.executeQuery(query_findname);
			if (rowcount_find == 0){
				System.out.println("\tSorry, we can't match the name of the item that you want to buy");
				order_repeat = 1;
				}
			else{
				order_repeat = 0;
				}

			}while(order_repeat == 1);

				//check if the user enter in the valid amount number
			do{
				System.out.print("\tEnter the amount of order (cannont be negative): ");
				String Str_amount_order = in.readLine();
		      		int amount_order = Integer.parseInt(Str_amount_order); //changing the string to integer.			
				if (amount_order < 0){
					System.out.println("\tSorry, we do not accept negative number here, please re-enter a 0 or positive number");					
					order_repeat = 1;//ask for re-ordering
					}
				else if (amount_order >= 1){ //if the user enters in a valid number, store the order(s) price in the list
				
					int temp_ao = amount_order; //store the amount order the number would be lost
					if (itemANDamount.containsKey(order)){
						itemANDamount.put(order,itemANDamount.get(order) + temp_ao);
						}
					else{
						itemANDamount.put(order,temp_ao);
						}
					
					while(amount_order != 0){
						String find_price_query = String.format("SELECT M.price FROM Menu M WHERE M.ItemName = '%s'", order);
						List <List<String>> each_price  = esql.executeQueryAndReturnResult(find_price_query);	
						String Resultstring = (each_price.get(0)).get(0);
						Double resultprice = Double.parseDouble(Resultstring);
						Total_amount.add(resultprice);
						amount_order--;
							}
						order_repeat = 0;

						System.out.print("Have any comment on this item?(type 'null' if you have no comment): ");
						String comment = in.readLine();
						
						if (itemANDcomment.containsKey(order)){
							itemANDcomment.put(order,itemANDcomment.get(order) + "\\" + comment);
						}
						else{
							itemANDcomment.put(order,comment);
						}//if the item name exist, then comment can just be added after the first comment with '\\' serve as seperator.
						all_order.add(order);	
					}	
					else if (amount_order == 0){// if the use enters in 0, cancel the order (which means do nothing)
						System.out.println("Order Cancelled");
						order_repeat = 0;
						}
				}while(order_repeat == 1);
	
				System.out.print("\tDo you want anything else?(yes/no): ");
				String continue_order = in.readLine();

				if (continue_order.equals("no")){
		//			System.out.println("Continue_ORDER: " + continue_order);
					order_repeat = 0;
					}
				else if (continue_order.equals("yes")){
		//			System.out.println("Continue_ORDER: " + continue_order);
					order_repeat = 1; //anything beside yes will consider as not continuing.
					}
		}while(order_repeat == 1);//Check if user wants to keep ordering, if yes, continue, if no, jump out
			
		Double final_total = 0.0;
		if (Total_amount.isEmpty()){
			return 0;
		}
		else{
			for(Double d:Total_amount)
				final_total += d;
			}//Sum the prices of each order  in the list
 		//System.out.println(final_total); TEST CORRECTNESS, Good
 		
		// now that we get the total amount of the price, we can insert the query

		String query = String.format("INSERT INTO Orders (login, paid, timeStampRecieved, total) VALUES ('%s', false, NOW(), '%s')", authorisedUser,final_total);
		//System.out.println(query);
		esql.executeUpdate(query);
		System.out.println("Order has been successfully created.");
		
		String select_query = String.format("SELECT orderid, timeStampRecieved FROM Orders O WHERE O.timeStampRecieved = (SELECT MAX(O2.timeStampRecieved) FROM Orders O2 WHERE O2.login = '%s')",authorisedUser); 
      		//System.out.println(select_query);
		List <List<String>> Result_id  = esql.executeQueryAndReturnResult(select_query);	
		String Resultstring_id = (Result_id.get(0)).get(0);
		Integer orderid = Integer.parseInt(Resultstring_id);
		String timeRecieved = (Result_id.get(0)).get(1);
		Timestamp s = Timestamp.valueOf(timeRecieved);
		for (Iterator<String> it = all_order.iterator(); it.hasNext();){
			String a = it.next();
			String item_status_query = String.format("INSERT INTO ItemStatus (orderid, itemName, amount,lastUpdated, status, comments) VALUES ('%s', '%s', '%s', '%s', 'Has Not Started', '%s')" ,orderid, a,itemANDamount.get(a), s,itemANDcomment.get(a));
			esql.executeUpdate(item_status_query);
		}
		System.out.println("Orderid is " + orderid);
		return orderid;
	}catch(Exception e){
		System.err.println (e.getMessage ());
		return 0;
	}
   }//end 

   public static void UpdateOrder(Cafe esql){
	List<List<String>> result_storage  = new ArrayList<List<String>>(); 
	try{//check user type to see what he/she can update
		int repeat_prompt = 0; //counter to repeat the prompt
		do{
			System.out.print("Enter in the order ID: ");
			String orderid = in.readLine();
			String match_query = String.format("SELECT paid FROM Orders O WHERE O.login = '%s' AND O.orderid = '%s'",authorisedUser, orderid);//use "select paid" becasue so that we can reuse this string
			int rowcount = esql.executeQuery(match_query);
			//check if the orderid he enters is made under his name
			if (rowcount == 0){//orderid cant be find under user's name	
				System.out.println("Sorry, we cannot find your order, please re-enter the orderid.");
				repeat_prompt = 1;
			}
			else {//orderid found, then check if the order has been paid.
				result_storage = esql.executeQueryAndReturnResult(match_query);
				String paidornot = (result_storage.get(0)).get(0);
				if (paidornot.equals("true")){
					System.out.println("Sorry, this order can't be change because it has been paid.");
					repeat_prompt = 1;
				}//if it is paid, reprompt the user to enter in new orderid.
				else{
					repeat_prompt = 0;
					List<Double> Total_amount = new ArrayList<Double>();
					int order_repeat = 0; //counter for repeating order	
					String order = "";
					int rowcount_find = 0;
					Map<String, Integer> itemANDamount = new HashMap<String, Integer>();
					Map<String, String> itemANDcomment = new HashMap<String, String>();
					Set<String>all_order = new HashSet<String>();
					do{
						//check if the item user wants to order is valid
						do{
							System.out.print("\tEnter the name of the item: ");
							order = in.readLine();
							String query_findname = String.format("SELECT * FROM Menu M WHERE M.ItemName = '%s'", order);
							rowcount_find = esql.executeQuery(query_findname);
						if (rowcount_find == 0){
							System.out.println("\tSorry, we can't match the name of the item that you want to buy");
							order_repeat = 1;
							}
						else{
							order_repeat = 0;
							}

						}while(order_repeat == 1);

							//check if the user enter in the valid amount number
						do{
							System.out.print("\tEnter the amount of order (enter negative if you want to lower the amount): ");
							String Str_amount_order = in.readLine();
							int amount_order = Integer.parseInt(Str_amount_order); //changing the string to integer.			
							
							if (amount_order < 0){
								int temp_ao = amount_order;
								if (itemANDamount.containsKey(order)){
									itemANDamount.put(order,itemANDamount.get(order) + temp_ao);
								}
								else{ 
									itemANDamount.put(order,temp_ao);
								}
								
								while(amount_order != 0){
									String find_price_query = String.format("SELECT M.price FROM Menu M WHERE M.ItemName = '%s'", order);
									List <List<String>> each_price  = esql.executeQueryAndReturnResult(find_price_query);	
									String Resultstring = (each_price.get(0)).get(0);
									Double resultprice = Double.parseDouble(Resultstring);
									resultprice = resultprice * -1;
									Total_amount.add(resultprice);
									amount_order++;
										}
									order_repeat = 0;

									System.out.print("Have any comment on this item?(type 'null' if you have no comment): ");
									String comment = in.readLine();
									
									if (itemANDcomment.containsKey(order)){
										itemANDcomment.put(order,itemANDcomment.get(order) + "\\" + comment);
									}
									else{
										itemANDcomment.put(order,comment);
									}//if the item name exist, then comment can just be added after the first comment with '\\' serve as seperator.
									all_order.add(order);	
								}	
							else if (amount_order >= 1){ //if the user enters in a valid number, store the order(s) price in the list
							
								int temp_ao = amount_order; //store the amount order the number would be lost
								if (itemANDamount.containsKey(order)){
									itemANDamount.put(order,itemANDamount.get(order) + temp_ao);
									}
								else{
									itemANDamount.put(order,temp_ao);
									}
								
								while(amount_order != 0){
									String find_price_query = String.format("SELECT M.price FROM Menu M WHERE M.ItemName = '%s'", order);
									List <List<String>> each_price  = esql.executeQueryAndReturnResult(find_price_query);	
									String Resultstring = (each_price.get(0)).get(0);
									Double resultprice = Double.parseDouble(Resultstring);
									Total_amount.add(resultprice);
									amount_order--;
										}
									order_repeat = 0;

									System.out.print("Have any comment on this item?(type 'null' if you have no comment): ");
									String comment = in.readLine();
									
									if (itemANDcomment.containsKey(order)){
										itemANDcomment.put(order,itemANDcomment.get(order) + "\\" + comment);
									}
									else{
										itemANDcomment.put(order,comment);
									}//if the item name exist, then comment can just be added after the first comment with '\\' serve as seperator.
									all_order.add(order);	
								}	
								else if (amount_order == 0){// if the use enters in 0, cancel the order (which means do nothing)
									System.out.println("Update Cancelled");
									order_repeat = 0;
									}
							}while(order_repeat == 1);
				
							System.out.print("\tDo you want anything else?(yes/no): ");
							String continue_order = in.readLine();

							if (continue_order.equals("no")){
					//			System.out.println("Continue_ORDER: " + continue_order);
								order_repeat = 0;
								}
							else if (continue_order.equals("yes")){
					//			System.out.println("Continue_ORDER: " + continue_order);
								order_repeat = 1; //anything beside yes will consider as not continuing.
								}
					}while(order_repeat == 1);//Check if user wants to keep ordering, if yes, continue, if no, jump out
					Double update_total = 0.0;
					if (Total_amount.isEmpty()){
						return;
					}
					else{
						for(Double d:Total_amount)
							update_total += d;
						}//Sum the prices of each order  in the list that need to update to order
					//System.out.println(final_total); TEST CORRECTNESS, Good
					
					// now that we get the total amount of the price, we can insert the query

					String query = String.format("UPDATE Orders SET total = total+'%s' WHERE orderid = '%s'",update_total, orderid);
					//System.out.println(query);
					esql.executeUpdate(query);
					System.out.println("Order has been successfully updated.");
					
					for (Iterator<String> it = all_order.iterator(); it.hasNext();){
						String a = it.next();
						String test = String.format("SELECT * FROM ItemStatus I WHERE I.orderid = '%s' AND itemName = '%s'", orderid, a);
						int test_rownum = esql.executeQuery(test);
						if (test_rownum == 0){
						String item_status_query = String.format("INSERT INTO ItemStatus (orderid, itemName, amount,lastUpdated, status, comments) VALUES ('%s', '%s', '%s', NOW(), 'Has Not Started', '%s')" ,orderid, a,itemANDamount.get(a), itemANDcomment.get(a));
						esql.executeUpdate(item_status_query);
						}
						else{
						String item_status_query = String.format("UPDATE ItemStatus SET amount = amount+'%s', lastUpdated = NOW(), comments = '%s' WHERE orderid = '%s' AND itemName = '%s'", itemANDamount.get(a), itemANDcomment.get(a), orderid,a); 
						esql.executeUpdate(item_status_query);
						}
					} 
				}
			}
		}while(repeat_prompt == 1);
	}catch(Exception e){
		System.err.println (e.getMessage ());
		return;
	}
   }//end

   public static void EmployeeUpdateOrder(Cafe esql){
      // Your code goes here.
      // ...
      // ...
   }//end

   public static void ViewOrderHistory(Cafe esql){
      // Your code goes here.
      // ...
      // ...
   }//end

   public static void UpdateUserInfo(Cafe esql){
      // Your code goes here.
      // ...
      // ...
   }//end

   public static void ManagerUpdateUserInfo(Cafe esql){
      // Your code goes here.
      // ...
      // ...
   }//end

   public static void UpdateMenu(Cafe esql){
      // Your code goes here.
      // ...
      // ...
   }//end

   public static void ViewOrderStatus(Cafe esql){
      // Your code goes here.
      // ...
      // ...
   }//end

   public static void ViewCurrentOrder(Cafe esql){
      // Your code goes here.
      // ...
      // ...
   }//end

   public static void Query6(Cafe esql){
      // Your code goes here.
      // ...
      // ...
   }//end Query6

}//end Cafe

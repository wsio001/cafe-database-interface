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
	 String login = "";
	 String password = "";
	 String phone = "";
	 int prompt_r = 0;
	 do{
         	System.out.print("\tEnter user login: ");
         	login = in.readLine();
		if (login.isEmpty() || login.trim().isEmpty()){
			System.out.println("login cant be empty");
			prompt_r = 1;
		}
		else{
			prompt_r = 0;
		}
	}while(prompt_r == 1);
	do{
         	System.out.print("\tEnter user password: ");
         	password = in.readLine();
		if (password.isEmpty() || password.trim().isEmpty()){
			System.out.println("password cant be empty");
			prompt_r = 1;
		}
		else{
			prompt_r =0;
		}
	}while(prompt_r == 1);
         System.out.print("\tEnter user phone: ");
         phone = in.readLine();
		if (phone.isEmpty() || phone.trim().isEmpty()){
			phone = "";
		}
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

						System.out.print("\tHave any comment on this item?(type 'null' if you have no comment): ");
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
				
				int prompt_r = 0;

				do{
					prompt_r = 0;
					System.out.print("\tDo you want anything else?(yes/no): ");
					String continue_order = in.readLine();

					if (continue_order.equals("no")){
		//				System.out.println("Continue_ORDER: " + continue_order);
						order_repeat = 0;
						}
					else if (continue_order.equals("yes")){
		//				System.out.println("Continue_ORDER: " + continue_order);
						order_repeat = 1; //anything beside yes will consider as not continuing.
					}
					else {
						System.out.println("Unrecgonized Choice!!");
						prompt_r = 1;
					}
				}while(prompt_r == 1);
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
			System.out.print("\tEnter in the order ID: ");
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

									System.out.print("\tHave any comment on this item?(type 'null' if you have no comment): ");
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

									System.out.print("\tHave any comment on this item?(type 'null' if you have no comment): ");
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
							int prompt_r = 0;

							do {
								prompt_r = 0;
								if (continue_order.equals("no")){
					//			System.out.println("Continue_ORDER: " + continue_order);
								order_repeat = 0;
								}
								else if (continue_order.equals("yes")){
					//			System.out.println("Continue_ORDER: " + continue_order);
								order_repeat = 1; //anything beside yes will consider as not continuing.
								}
								else{
									System.out.println("Unrecgonized Choice!!");
									prompt_r = 1;
								}
							}while(prompt_r == 1);
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
	String delete_query = String.format("DELETE * FROM ItemStatus WHERE amount =< 0");
	//delete ItemStatus that has the amount of 0 or negative
	}catch(Exception e){
		System.err.println (e.getMessage ());
		return;
	}
   }//end

   public static void EmployeeUpdateOrder(Cafe esql){
	List<List<String>> result_storage  = new ArrayList<List<String>>(); 
	try{//check user type to see what he/she can update
		int repeat_prompt = 0; //counter to repeat the prompt
		String status_change = "";
		System.out.print("\tDo you want to update your own order or someone else's (1=yourself,2=others)?  ");
		String choice_1 = in.readLine();
		if (choice_1.equals("1")){
			UpdateOrder(esql);
			return;
		}
		else if(choice_1.equals("2")){
		do{
			System.out.print("\tEnter in the order ID: ");
			String orderid = in.readLine();
			String match_query = String.format("SELECT paid FROM Orders O WHERE O.login = '%s' AND O.orderid = '%s'",authorisedUser, orderid);//use "select paid" becasue so that we can reuse this string
			int rowcount = esql.executeQuery(match_query);
			//check if the orderid he enters is made under his name
			if (rowcount == 0){//orderid cant be find under user's name	
				System.out.println("Sorry, we cannot find your order, please re-enter the orderid.");
				repeat_prompt = 1;
			}
			else{
				result_storage = esql.executeQueryAndReturnResult(match_query);
				String paidornot = (result_storage.get(0)).get(0);
				if (paidornot.equals("false")){
					int prompt_r = 0;

					do{
					prompt_r = 0;
					System.out.print("\tThis order has not been paid yet, would you want to change it to paid?(y/n) ");
					String paid_change = in.readLine();
					if(paid_change.equals("y")){
						paid_change = String.format("UPDATE Orders SET paid = 'true' WHERE orderid = '%s'",orderid);	
						esql.executeUpdate(paid_change);
						}
					else if (paid_change.equals("n")){
						return;
						}
					else{
						System.out.println("Unrecgonized choice!!");
						prompt_r = 1;
					}
					}while(prompt_r == 1);
				}	
				String Select_query = String.format("SELECT * FROM ItemStatus I WHERE I.orderid = '%s'", orderid);
				rowcount = esql.executeQueryAndPrintResult(Select_query); 
				System.out.println("\tWhich item do you want to update? ");
				String choice = in.readLine();
				Select_query = String.format("Select status FROM ItemStatus I WHERE I.orderid = '%s' AND I.itemName = '%s'",orderid, choice);
				result_storage = esql.executeQueryAndReturnResult(Select_query);
				String status =  (result_storage.get(0)).get(0);
				if (status.equals("Has Not Started")){
					System.out.print("\tIt has not started yet, want to change it to Started?(y/n) ");
					choice = in.readLine();
					int prompt_r = 0;

					do{
					prompt_r = 0;
					if (choice.equals("y")){
						status_change = String.format("UPDATE itemStatus SET status = 'Started' WHERE orderid = '%s' AND itemName = '%s'", orderid, choice);
						esql.executeUpdate(status_change);
					}
					else if (choice.equals("n")){
						return;
					}
					else{
						System.out.println("Unrecgonized Choice!!");
						prompt_r = 1;
					}
					}while(prompt_r == 1);
				}
				else if (status.equals("Started")){
					System.out.print("\tIt has started, want to Change it to Finished?(y/n) ");
					choice = in.readLine();
					int prompt_r = 0;

					do{
					prompt_r = 0;
					if (choice.equals("y")){
						status_change = String.format("UPDATE itemStatus SET status = 'Finished' WHERE orderid = '%s' AND itemName = '%s'", orderid, choice);
						esql.executeUpdate(status_change);
					}
					else if (choice.equals("n")){
						return;
					}
					else{
					System.out.println("Unrecgonized choice");
					prompt_r = 1;
					}
					}while(prompt_r == 1);
				}
				else if (status.equals("Finished")){
					System.out.println("This order has been finished, you cannot change it anymore");
					return;
				}
			}
		}while(repeat_prompt == 1);
		}
	}
	catch(Exception e){
		System.err.println (e.getMessage ());
		return;
	}
			
   }//end

   public static void ViewOrderHistory(Cafe esql){
	try{
		List<List<String>> result_storage = new ArrayList<List<String>>();
		String Select_query = String.format("SELECT orderid FROM Orders WHERE login = '%s' ORDER BY timeStampRecieved DESC LIMIT 5", authorisedUser);
		int rowcount = esql.executeQueryAndPrintResult(Select_query);
		System.out.println("Total row(s): " + rowcount);
		return;
	}
	catch(Exception e){
		System.err.println (e.getMessage());
		return;
	}
 
   }//end

   public static void UpdateUserInfo(Cafe esql){
      	try{
		String update_query = "";
		System.out.println("\tWhat do you want to update?");
		System.out.println("\t1.password");
		System.out.println("\t2.phone number");
		System.out.println("\t3.favorite item");
		int prompt_r = 0;
		String np = "";
		String nnum = "";
		String nf = "";
		do{
	 	int input = readChoice();
		prompt_r = 0;
		if (input == 1){
			do{
				System.out.print("\tEnter the new password: ");
				np = in.readLine();
				if (np.isEmpty() || np.trim().isEmpty()){
					System.out.println("new password cannot be empty");
					prompt_r = 1;
				}
				else{
					prompt_r = 0;
				}
			}while(prompt_r ==1);
				update_query = String.format("UPDATE Users SET password = '%s' WHERE login = '%s'",np,authorisedUser);
				esql.executeUpdate(update_query);
		}
		else if (input == 2){
			System.out.print("\tEnter the new phone number: ");
			nnum = in.readLine();
				if (nnum.isEmpty() || nnum.trim().isEmpty()){
					nnum = "";
				}
			update_query = String.format("UPDATE Users SET phoneNum = '%s' WHERE login = '%s'",nnum,authorisedUser);
			esql.executeUpdate(update_query);
		}
		else if (input == 3){
			System.out.print("\tEnter your favorite item: ");
			 nf = in.readLine();
				if (nf.isEmpty()|| nf.trim().isEmpty()){
					nf ="";
				}
			update_query = String.format("UPDATE Users SET favItems = favItems ||' / '||'%s' WHERE login = '%s'",nf,authorisedUser);
			esql.executeUpdate(update_query);
		}
		else{
			System.out.println("Unrecgonized Choice!!");
			prompt_r = 1;
		}
		}while(prompt_r == 1);
		System.out.println("Update Successfully");
		return;	
	}
	catch(Exception e){
		System.err.println (e.getMessage());
		return;
	}
   }//end

   public static void ManagerUpdateUserInfo(Cafe esql){
	try{
		System.out.println("\tDo you want to update your own info or other users' type?");	
		System.out.println("\t1. Own info");
		System.out.println("\t2. Other's type");
		switch(readChoice()){
			case 1: UpdateUserInfo(esql);
				break;
			case 2: 
				String update_query = "";
				String login = "";
				int prompt_r = 0;
				do{
					prompt_r = 0;
					do{
						System.out.print("\tEnter the loggin name that you want to update: ");
						login = in.readLine();
						String select_query = String.format("SELECT * FROM Users U WHERE U.login = '%s'", login);
						int rowcount = esql.executeQuery(select_query);
						if (rowcount == 0){
							System.out.println("Sorry we cannot find the username you are looking for.");
							prompt_r = 1;
						}
						else{
							prompt_r = 0;
						}
					}while(prompt_r == 1);
					System.out.print("\tWhat do you want to update that user's type to?");
					String newtype = in.readLine();
					if (newtype.equals("Manager")){
						update_query = String.format("UPDATE Users SET type = '%s' WHERE login = '%s'", newtype, login);
						esql.executeUpdate(update_query);	
					}
					else if (newtype.equals("Employee")){	
						update_query = String.format("UPDATE Users SET type = '%s' WHERE login = '%s'", newtype, login);
						esql.executeUpdate(update_query);	
					}
					else if (newtype.equals("Customer")){	
						update_query = String.format("UPDATE Users SET type = '%s' WHERE login = '%s'", newtype, login);
						esql.executeUpdate(update_query);		
					}
					else{
						System.out.println("Unrecognized type!!");
						prompt_r = 1;
					}
				}while(prompt_r == 1);
				System.out.println("Update Successfully");		
				break;
			default: System.out.println("Unrecognized Choice!!");
				break;
		}//end switch
		return;
	}
	catch(Exception e){
		System.err.println (e.getMessage());
		return;
	}
   }//end

   public static void UpdateMenu(Cafe esql){
	try{
		System.out.println("\tWhat action do you want to take?: ");
		System.out.println("\t1.add");
		System.out.println("\t2.update");
		System.out.println("\t3.delete");
		String itemName = "";
		String type = "";
		double price = 0.0;
		String desrcb = "";
		String URL = "";
		String update_query = "";
		String select_query = "";
		int prompt_r = 0;
			String S_itemName = "";
		switch(readChoice()){
			case 1: 
				do{
					System.out.print("\tEnter the itemName: ");
					itemName = in.readLine();
					if (itemName.isEmpty() || (itemName.trim()).isEmpty()){
						System.out.println("itemName cant be empty");
						prompt_r = 1;
					}
					else{
						prompt_r = 0;
					}
				}while(prompt_r == 0);
				do{
					System.out.print("\tEnter the type for this item: ");
					type = in.readLine();
					if (type.isEmpty() || (type.trim()).isEmpty()){
						System.out.println("type cant be empty");
						prompt_r = 1;
					}
					else{
						prompt_r = 0;
					}
				}while(prompt_r == 0);
				do{
					try{
						do{
							System.out.print("\tEnter the price for this item: ");
							String temp_price = in.readLine();
							price = Double.parseDouble(temp_price);
							if (price <= 0)
							{
								System.out.println("You can only enter positive numbers");
								prompt_r = 1;
							}
							else {
								prompt_r = 0;
							}
						}while(prompt_r == 1);
						break;
					}
					catch(NumberFormatException e){
						System.out.println("You can only enter positive numbers");
						continue;
					}
				}while(true);
				System.out.print("\tEnter the description");
				desrcb = in.readLine();
				if(desrcb.isEmpty()||desrcb.trim().isEmpty()){
					desrcb = "";
				}
				System.out.println("\tEnter the URL");
				URL = in.readLine();
				if (URL.isEmpty()||URL.trim().isEmpty()){
					URL = "";
				}
				update_query = String.format("INSERT INTO Menu (itemName, type, price, description, imageURL) VALUES ( '%s', '%s', '%s', '%s', '%s')", itemName,type,price,desrcb,URL);
				esql.executeUpdate(update_query);
	 			System.out.println("Added Succesfully");
				break;
			case 2:
				String np = "";
				String nnum = "";
				int rowcount = 0;
				do{
					System.out.print("\tWhat is the name of the item you want to update");
					S_itemName = in.readLine();
					select_query = String.format("SELECT * FROM Menu WHERE itemName = '%s'",S_itemName);
					rowcount = esql.executeQuery(select_query);
					if(rowcount == 0){
						System.out.println("Sorry, we cannot find the item that you are looking for.");
						prompt_r = 1;
					}
					else{
						prompt_r = 0;
					}
				}while(prompt_r == 1);

				System.out.println("\tWhat do you want to update?");
				System.out.println("\t1.itemName");
				System.out.println("\t2.type");
				System.out.println("\t3.price");
				System.out.println("\t4.description");
				System.out.println("\t5.imageURL");
				do{
	 				int input = readChoice();
					prompt_r = 0;
					if (input == 1){
						do{
							System.out.print("\tEnter the new itemName: ");
							np = in.readLine();
							if (np.isEmpty() || np.trim().isEmpty()){
								System.out.println("new itemName cannot be empty");
								prompt_r = 1;
							}	
							else{
								prompt_r = 0;
							}
						}while(prompt_r ==1);
						update_query = String.format("UPDATE Menu SET itemName = '%s' WHERE itemName = '%s'",np,S_itemName);
						esql.executeUpdate(update_query);
					}
					else if (input == 2){
						System.out.print("\tEnter the new type for the item: ");
						nnum = in.readLine();
						do{
							if (nnum.isEmpty() || nnum.trim().isEmpty()){
								System.out.println("new type for the item cannot be empty");
								prompt_r = 1;
							}
							else{
								prompt_r = 0;
							}
						}while(prompt_r == 1);
						update_query = String.format("UPDATE Menu SET type  = '%s' WHERE itemName = '%s'",nnum,S_itemName);
						esql.executeUpdate(update_query);
					}
					else if (input == 3){
						do{
							try{
								do{
									System.out.print("\tEnter the new price for this item: ");
									String temp_price = in.readLine();
									price = Double.parseDouble(temp_price);
									if (price <= 0)
									{
										System.out.println("You can only enter positive numbers");
										prompt_r = 1;
									}
									else {
										prompt_r = 0;
									}
								}while(prompt_r == 1);
							break;
							}
							catch(NumberFormatException e){
								System.out.println("You can only enter positive numbers");
								continue;
							}
						}while(true);
		
						update_query = String.format("UPDATE Menu SET price = '%s' WHERE itemName = '%s'",price,S_itemName);
						esql.executeUpdate(update_query);
					}
					else if (input == 4){
						System.out.print("\tEnter the description");
						desrcb = in.readLine();
						if(desrcb.isEmpty()||desrcb.trim().isEmpty()){
							desrcb = "";
						}
						update_query = String.format("UPDATE Menu SET description = '%s' WHERE itemName = '%s'",desrcb,S_itemName);
						esql.executeUpdate(update_query);
					}
					else if (input == 5){
						System.out.print("\tEnter the URL");
						URL = in.readLine();
						if(URL.isEmpty()||URL.trim().isEmpty()){
							URL = "";
						}
						update_query = String.format("UPDATE Menu SET URL = '%s' WHERE itemName = '%s'",URL,S_itemName);
						esql.executeUpdate(update_query);
					}
					else{
						System.out.println("Unrecgonized Choice!!");
						prompt_r = 1;
					}	
				}while(prompt_r == 1);
				System.out.println("Update Successfully");
				break;
			case 3:
				System.out.print("\tWhat is the name of the item you want to delete");
				S_itemName = in.readLine();
				select_query = String.format("DELETE * FROM Menu WHERE itemName = '%s'",S_itemName);
				break;
 			default: 
				System.out.println("\t Unrecgonized Choice!!");
				break;
		}//end switch		
	}
	catch(Exception e){
		System.err.println (e.getMessage());
		return;
	}
   }//end

   public static void ViewOrderStatus(Cafe esql){
	try{
		String user_type = find_type(esql);
		String select_query = "";
		String orderid = "";
		int rowcount = 0;
		int prompt_r = 0;
		switch(user_type){
		case "Employee":
			System.out.print("\tEnter the orderid: ");
			orderid = in.readLine();
			select_query = String.format("SELECT * FROM ItemStatus I WHERE I.orderid = '%s'",orderid);
			rowcount = esql.executeQueryAndPrintResult(select_query);
			System.out.println("Total row(s): " + rowcount);
			break;
		case "Manager ":
			System.out.print("\tEnter the orderid: ");
			orderid = in.readLine();
			select_query = String.format("SELECT * FROM ItemStatus I WHERE I.orderid = '%s'",orderid);
			rowcount = esql.executeQueryAndPrintResult(select_query);
			System.out.println("Total row(s): " + rowcount);
			break;
		case "Customer":
			do{
				System.out.print("\tEnter the orderid: ");
				orderid = in.readLine();
				select_query = String.format("SELECT * FROM Orders O WHERE O.orderid = '%s' AND O.login = '%s'", orderid, authorisedUser);
				rowcount = esql.executeQuery(select_query);
				if (rowcount == 0){
					System.out.println("Sorry, we cannot find the order that you are looking for under your login");
					prompt_r = 1;
				}
				else{
					prompt_r = 0;
				}
			}while(prompt_r == 1);
			select_query = String.format("SELECT * FROM ItemStatus I WHERE I.orderid = '%s'", orderid);
			rowcount = esql.executeQueryAndPrintResult(select_query);
			System.out.println("Total row(s): " + rowcount);
			break;
		}//end switch
		return;
	}
	catch(Exception e){
		System.err.println (e.getMessage());
		return;
	}
   }//end

   public static void ViewCurrentOrder(Cafe esql){
	try{
		String select_query = String.format("SELECT orderid, login, timeStampRecieved, total FROM Orders WHERE paid = 'false' AND timeStampRecieved >= NOW() - '1 day'::INTERVAL");
		int rowcount = esql.executeQueryAndPrintResult(select_query);
		System.out.println("Total row(s): " + rowcount);
		return;
	}
	catch(Exception e){
		System.err.println (e.getMessage());
		return;
	}
   }//end

   public static void Query6(Cafe esql){
      // Your code goes here.
      // ...
      // ...
   }//end Query6

}//end Cafe

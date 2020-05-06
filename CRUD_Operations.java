//Contains logic for create, display, update and delete record from table.
import java.util.Scanner;
import java.lang.System;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.DriverManager;
class CRUD_Operations
{
	Connection connection;
	String tableName = "BankCustomer";
	String insertQuery;
	int countOfColumns; 
	String id;
	ArrayList<String> columnNames = new ArrayList<String>();
	Scanner scanner = new Scanner(System.in);

	public CRUD_Operations(String dataBase, String userName, String password)
	{
		String url = "jdbc:mysql://165.22.14.77/" + dataBase + "?AautoReconnect=true&useSSL=false";
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection(url, userName, password);
			getColumnNames();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	void getColumnNames()
	{
		String query = "select * from " + tableName;
		try
		{
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);
			ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
			countOfColumns = resultSetMetaData.getColumnCount();
			for(int counter = 1; counter <= countOfColumns; counter++)
			{
				columnNames.add(resultSetMetaData.getColumnName(counter));
			}
			if(columnNames.get(0).toLowerCase().equals("status") && countOfColumns > 2)
			{
				generateInsertQuery();
			}
			else
			{
				System.out.println("To run this framework a table with at-least three columns are required in which first one should be \"status\".");
				System.exit(1);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	void generateInsertQuery()
	{
		String placeHolders = "";
		String parameters = "";
		int countOfColumns = columnNames.size();
		for(int counter = 2; counter < countOfColumns - 1; counter++)
		{
			parameters = parameters + columnNames.get(counter) + ", ";
			placeHolders = placeHolders + "?, ";
		}	
	
		insertQuery = "insert into " + tableName + "(" + parameters + columnNames.get(countOfColumns-1) + ") values (" + placeHolders +"?);";
	}

	void insert()
	{
		String[] returnId = { columnNames.get(1) };
		try
		{
			PreparedStatement preparedStatement = connection.prepareStatement(insertQuery, returnId);
			for(int counter = 2; counter < countOfColumns; counter++)
			{
				String parameter = columnNames.get(counter);
				System.out.print("Enter " + parameter + ": ");
				String value = scanner.next();
				preparedStatement.setString(counter-1, value);
			}
			int rowsEffected = preparedStatement.executeUpdate();
			ResultSet resultSet = preparedStatement.getGeneratedKeys();
			if(resultSet.next() && rowsEffected > 0)
			{
				System.out.println("Record inserted succesfully.");
				System.out.println("Your " + columnNames.get(1) + " is " + resultSet.getInt(1) + ".");
			}
			else
			{
				System.out.println("Failed to insert try again.");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	void display()
	{
		String query = "select * from " + tableName + " where " + columnNames.get(0) + " = 1";
		try
		{
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);
			if(resultSet.next())
			{
				resultSet.previous();
				for(int counter = 1; counter < countOfColumns; counter++)
				{
					System.out.print(String.format("%15s", columnNames.get(counter)));
				}
				System.out.println("\n");
				while(resultSet.next())
				{
					for(int counter = 2; counter <= countOfColumns; counter++)
					{
						System.out.print(String.format("%15s", resultSet.getString(counter)));
					}
					System.out.println();
				}
			}
			else
			{
				System.out.println("No records available.");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	void update()
	{
		readId();
		for(int counter = 2; counter < countOfColumns; counter++)
		{
			System.out.println(counter-1 + ". " + columnNames.get(counter));
		}
		System.out.print("Choose any field to update: ");
		try
		{
			int choice = scanner.nextInt();
			if(choice >=1 && choice <= countOfColumns)
			{
				System.out.print("Enter " + columnNames.get(choice+1) + ": ");
				String newValue = scanner.next();
				String query = "update " + tableName + " set " + columnNames.get(choice+1) + " = ? " + "where " + columnNames.get(0) + " = 1 and " + columnNames.get(1) + " = ?";
				PreparedStatement preparedStatement = connection.prepareStatement(query);
				preparedStatement.setString(1, newValue);
				preparedStatement.setString(2, id);
				int rowsEffected = preparedStatement.executeUpdate();
				if(rowsEffected > 0)
				{
					System.out.println("Record updated sucessfully.");
				}
				else
				{
					System.out.println("Record with " + columnNames.get(1) + " = " + id + " does not exists.");
				}
			}
			else
			{
				printInvalidChoice();
			}
		}
		catch(InputMismatchException i)
		{
			scanner.next();
			printInvalidChoice();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	void delete()
	{
		String query = "update " + tableName + " set " + columnNames.get(0) + " = 0 " + "where " + columnNames.get(0) + " = 1 and " + columnNames.get(1) + " = ?";
		try
		{
			readId();
			PreparedStatement preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, id);
			int rowsEffected = preparedStatement.executeUpdate();
			if(rowsEffected > 0)
			{
				System.out.println("Record deleted successfully.");
			}
			else
			{
				System.out.println("Record with " + columnNames.get(1) + " = " + id + " does not exists.");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();	
		}
	}

	void readId()
	{
		System.out.print("Enter " + columnNames.get(1) + ": ");
	 	id = scanner.next();
	}

	void printInvalidChoice()
	{
		System.out.println("Invalid choice.");
	}
}
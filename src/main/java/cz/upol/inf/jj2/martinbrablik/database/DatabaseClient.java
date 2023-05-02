package cz.upol.inf.jj2.martinbrablik.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import cz.upol.inf.jj2.martinbrablik.database.models.Item;
import cz.upol.inf.jj2.martinbrablik.database.models.Receipt;
import cz.upol.inf.jj2.martinbrablik.database.ui.MainWindow;

public class DatabaseClient {
	
	public static List<Receipt> allReceipts = new ArrayList<Receipt>();
	public static DatabaseClient dbClient;
	
	public static final String USERNAME = "remote";
	public static final String PASSWORD = "zp4jv";
	public static final String connectionURL = "jdbc:mysql://localhost:3306/ShopDB";
	public static final String TABLE_RECEIPTS = "receipts";
	public static final String TABLE_ITEMS = "items";
	public static final String TABLE_ITEMS_DATA = "items_data";
	
	public static void main(String[] args) throws SQLException {
		try (Connection con = DriverManager.getConnection(connectionURL, USERNAME, PASSWORD)) {
			System.setProperty("derby.language.sequence.preallocator", "1");
			if (!isReady(con))
				initializeTables(con);
			try {
				dbClient = new DatabaseClient();
				dbClient.listReceipts(con);
				dbClient.listItems(con);
				con.close();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		MainWindow mainWindow = new MainWindow();
		mainWindow.setVisible(true);
	}
	
	private static void initializeTables(Connection con) throws SQLException {
		try (Statement stmt = con.createStatement()) {
		stmt.addBatch("CREATE TABLE receipts(\n"
				+ "id INT NOT NULL AUTO_INCREMENT ,\n"
				+ "name VARCHAR(50),\r\n"
				+ "itin VARCHAR(10) NOT NULL,\n"
				+ "total INT,\n"
				+ "UNIQUE(itin),\n"
				+ "PRIMARY KEY(id)\n"
				+ ")AUTO_INCREMENT=0;");
		stmt.addBatch("CREATE TABLE items_data(\n"
				+ "id INT NOT NULL AUTO_INCREMENT,\n"
				+ "product_name VARCHAR(50),\n"
				+ "amount INT,\n"
				+ "unit_price INT,\n"
				+ "PRIMARY KEY(id)\n"
				+ ")AUTO_INCREMENT=0;");
		stmt.addBatch("CREATE TABLE items(\n"
				+ "id INT NOT NULL AUTO_INCREMENT,\n"
				+ "receipt_id INT,\n"
				+ "data_id INT,\n"
				+ "CONSTRAINT fk_receipt_id FOREIGN KEY(receipt_id) REFERENCES receipts(id),\n"
				+ "CONSTRAINT fk_data_id FOREIGN KEY(data_id) REFERENCES items_data(id),\n"
				+ "PRIMARY KEY(id)\n"
				+ ")AUTO_INCREMENT=0;");
		stmt.executeBatch();
		}
	}
	
	private static boolean isReady(Connection con) throws DBException {
		boolean allPresent = true;
		try {
			DatabaseMetaData dbm = con.getMetaData();
			try (ResultSet tables = dbm.getTables(null, null, TABLE_RECEIPTS.toUpperCase(), null)) {
				allPresent  = allPresent && tables.next();
			}
			try (ResultSet tables = dbm.getTables(null, null, TABLE_ITEMS_DATA.toUpperCase(), null)) {
				allPresent  = allPresent && tables.next();
			}
			try (ResultSet tables = dbm.getTables(null, null, TABLE_ITEMS.toUpperCase(), null)) {
				allPresent  = allPresent && tables.next();
			}
			return allPresent;
		}
		catch(SQLException e) {
			throw new DBException(e.getMessage());
		}
	}
	
	public boolean hasItem(Item item, Connection con) throws DBException {
		try (PreparedStatement stmt = con.prepareStatement("SELECT * FROM " + TABLE_ITEMS_DATA + " WHERE unit_price = ? AND product_name = ? AND amount = ? OR id = ?")) {
			stmt.setInt(1, item.getUnitPrice());
			stmt.setString(2, item.getProductName());
			stmt.setInt(3, item.getAmount());
			stmt.setInt(4, item.getDataId());
			stmt.execute();
			try (ResultSet results = stmt.getResultSet()) {
				return results.next();
			}
		}
		catch (SQLException e) {
			throw new DBException(e.getMessage());
		}
	}
	
	public void listItems(Connection con) throws DBException {
		try (Statement stmt = con.createStatement()) {
			stmt.executeQuery("SELECT * FROM " + TABLE_ITEMS);
			try (ResultSet result = stmt.getResultSet()) {
				while(result.next()) {
					int receiptId = result.getInt("receipt_id");
					int data = result.getInt("data_id");
					Optional<Receipt> itemReceipt = allReceipts.stream().filter(r -> r.getId() == receiptId).findFirst();
					try (Statement dataStmt = con.createStatement()) {
						dataStmt.executeQuery("SELECT * FROM " + TABLE_ITEMS_DATA + " WHERE id = " + data);
						try (ResultSet dataResult = dataStmt.getResultSet()) {
							while(dataResult.next()) {
								String productName = dataResult.getString("product_name");
								int amount = dataResult.getInt("amount");
								int unitPrice = dataResult.getInt("unit_price");
								Item item = new Item(data, amount, unitPrice, productName);
								itemReceipt.get().getItems().add(item);
							}
						}
					}
				}
			}
		}
		catch (SQLException e) {
			throw new DBException(e.getMessage());
		}
	}
	
	public void deleteItem(Item item, Connection con) throws DBException {
		try (PreparedStatement stmt = con.prepareStatement("DELETE FROM " + TABLE_ITEMS + " WHERE data_id IN (SELECT id FROM " + TABLE_ITEMS_DATA + " WHERE id = ?)")) {
			stmt.setInt(1, getItemId(item, con));
			stmt.executeUpdate();
			try (PreparedStatement dataStmt = con.prepareStatement("DELETE FROM " + TABLE_ITEMS_DATA + " WHERE amount = ? AND unit_price = ? AND product_name = ?")) {
				dataStmt.setInt(1,item.getAmount());
				dataStmt.setInt(2, item.getUnitPrice());
				dataStmt.setString(3, item.getProductName());
				dataStmt.executeUpdate();
			}
		}
		catch (SQLException e) {
			throw new DBException(e.getMessage());
		}
	}
	
	public void insertItem(Item item, Receipt receipt, Connection con) throws DBException {
		String queryItem = "INSERT INTO " + TABLE_ITEMS + "(receipt_id, data_id) VALUES(?, ?)";
		String queryData = item.getDataId() == -1 ? "INSERT INTO " + TABLE_ITEMS_DATA + "(product_name, amount, unit_price) VALUES(?, ?, ?)" :
			"INSERT INTO " + TABLE_ITEMS_DATA + "(product_name, amount, unit_price, id) VALUES(?, ?, ?, ?)";
		try (PreparedStatement stmt = con.prepareStatement(queryData)) {
			stmt.setString(1, item.getProductName());
			stmt.setInt(2, item.getAmount());
			stmt.setInt(3, item.getUnitPrice());
			if(item.getDataId() != -1)
				stmt.setInt(4, item.getDataId());
			stmt.execute();
			try (PreparedStatement itemStmt = con.prepareStatement(queryItem.toString())) {
				int dataId = getItemId(item, con);
				int receiptId = getReceiptId(receipt, con);
				itemStmt.setInt(1, receiptId);
				itemStmt.setInt(2, dataId);
				itemStmt.executeUpdate();
			}
		}
		catch (SQLException e) {
			throw new DBException(e.getMessage());
		}
	}
	
	public int getItemId(Item item, Connection con) throws DBException {
		try(PreparedStatement stmt = con.prepareStatement("SELECT id FROM " + TABLE_ITEMS_DATA + " WHERE amount = ? AND unit_price = ? AND product_name = ?")) {
			stmt.setInt(1, item.getAmount());
			stmt.setInt(2, item.getUnitPrice());
			stmt.setString(3, item.getProductName());
			stmt.execute();
			try(ResultSet result = stmt.getResultSet()) {
				while(result.next()) {
					return result.getInt("id");
				}
			}
		}
		catch (SQLException e) {
			throw new DBException(e.getMessage());
		}
		return -1;
	}
	
	public void updateItem(Item item, Connection con) throws DBException {
		try (PreparedStatement stmt = con.prepareStatement("UPDATE " + TABLE_ITEMS_DATA + " SET unit_price = ?, product_name = ?, amount = ? WHERE id = ?")) {
			stmt.setInt(1, item.getUnitPrice());
			stmt.setString(2, item.getProductName());
			stmt.setInt(3, item.getAmount());
			stmt.setInt(4, item.getDataId());
			stmt.executeUpdate();
		}
		catch (SQLException e) {
			throw new DBException(e.getMessage());
		}
	}
	
	public void listReceipts(Connection con) throws DBException {
		try (Statement stmt = con.createStatement()) {
			stmt.executeQuery("SELECT * FROM " + TABLE_RECEIPTS);
			try (ResultSet result = stmt.getResultSet()) {
				while(result.next()) {
					int id = result.getInt("id");
					String name = result.getString("name");
					String itin = result.getString("itin");
					int total = result.getInt("total");
					List<Item> receiptItems = new ArrayList<Item>();
					Receipt r = new Receipt(id, total, name, itin, receiptItems);
					allReceipts.add(r);
				}
			}
		}
		catch (SQLException e) {
			throw new DBException(e.getMessage());
		}
	}	
	
	public void deleteReceipt(Receipt receipt, Connection con) throws DBException {
		receipt.getItems().forEach(i -> {
				try {
					deleteItem(i, con);
				}
				catch (DBException e) {
					e.printStackTrace();
				}
		});
		try (PreparedStatement stmt = con.prepareStatement("DELETE FROM " + TABLE_RECEIPTS + " WHERE name = ? AND itin = ? AND total = ? OR id = ?")) {
			stmt.setString(1, receipt.getName());
			stmt.setString(2, receipt.getItin());
			stmt.setInt(3, receipt.getTotal());
			stmt.setInt(4, receipt.getId());
			stmt.executeUpdate();
			
		} catch (SQLException e) {
			throw new DBException(e.getMessage());
		}
	}
	
	public void insertReceipt(Receipt receipt, Connection con) throws DBException {
		StringBuilder query = new StringBuilder();
		query.append("INSERT INTO ").append(TABLE_RECEIPTS).append("(name, itin, total)").append(" VALUES (?, ?, ?)");
		try (PreparedStatement stmt = con.prepareStatement(query.toString())) {
			stmt.setString(1, receipt.getName());
			stmt.setString(2, receipt.getItin());
			stmt.setInt(3, receipt.getTotal());
			stmt.executeUpdate();
		}
		catch (SQLException e) {
			throw new DBException(e.getMessage());
		}
		for(Item item : receipt.getItems()) {
			insertItem(item, receipt, con);
		}
	}
	
	public void updateReceipt(Receipt receipt, Connection con) throws DBException {
		int id = receipt.getId();
		try (PreparedStatement stmt = con.prepareStatement("UPDATE " + TABLE_RECEIPTS + " SET name = ?, itin = ?, total = ? WHERE id = ?")) {
			stmt.setString(1, receipt.getName());
			stmt.setString(2, receipt.getItin());
			stmt.setInt(3, receipt.getTotal());
			stmt.setInt(4, id);
			stmt.executeUpdate();
		}
		catch (SQLException e) {
			throw new DBException(e.getMessage());
		}
	}
	
	public int getReceiptId(Receipt receipt, Connection con) throws DBException {
		try(PreparedStatement stmt = con.prepareStatement("SELECT id FROM " + TABLE_RECEIPTS + " WHERE name = ? AND itin = ?")) {
			stmt.setString(1, receipt.getName());
			stmt.setString(2, receipt.getItin());
			stmt.execute();
			try(ResultSet result = stmt.getResultSet()) {
				while(result.next()) {
					return result.getInt("id");
				}
			}
		}
		catch (SQLException e) {
			throw new DBException(e.getMessage());
		}
		return -1;
	}
}

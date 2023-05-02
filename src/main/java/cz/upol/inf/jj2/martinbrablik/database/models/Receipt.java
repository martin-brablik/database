package cz.upol.inf.jj2.martinbrablik.database.models;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import cz.upol.inf.jj2.martinbrablik.database.DatabaseClient;

public class Receipt {
	private int id = -1;
	private int total;
	private String name;
	private String itin;
	private List<Item> items;
	
	
	public Receipt(int total, String name, String itin, List<Item> items) {
		this.total = total;
		this.name = name;
		this.itin = itin;
		this.items = items;
	}
	
	public Receipt(int id, int total, String name, String itin, List<Item> items) {
		//Konstruktor pro naètení z databáze
		this.id = id;
		this.total = total;
		this.name = name;
		this.itin = itin;
		this.items = items;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.name)
		.append(System.lineSeparator()).append("Itin: ").append(this.itin)
		.append(System.lineSeparator()).append("Items:");
		for(Item i : this.items) {
			sb.append(System.lineSeparator()).append(i.toString());
		}
		sb.append(System.lineSeparator()).append(System.lineSeparator());
		return sb.toString();
	}
	
	public void insert() {
		try (Connection con = DriverManager.getConnection(DatabaseClient.connectionURL, DatabaseClient.USERNAME, DatabaseClient.PASSWORD)) {
			DatabaseClient.dbClient.insertReceipt(this, con);
			if(this.getId() == -1)
				this.setId(DatabaseClient.dbClient.getReceiptId(this, con));
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void update() {
		try (Connection con = DriverManager.getConnection(DatabaseClient.connectionURL, DatabaseClient.USERNAME, DatabaseClient.PASSWORD)) {
			DatabaseClient.dbClient.updateReceipt(this, con);
			con.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void delete() {
		try (Connection con = DriverManager.getConnection(DatabaseClient.connectionURL, DatabaseClient.USERNAME, DatabaseClient.PASSWORD)) {
			DatabaseClient.dbClient.deleteReceipt(this, con);
			con.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public String toHtml() {
		StringBuilder sb = new StringBuilder();
		sb.append("<html>").append(this.toString().replace(System.lineSeparator(), "<br />").replaceAll("\t", "&emsp;")).append("</html");
		return sb.toString();
	}
	
	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getItin() {
		return itin;
	}
	
	public void setItin(String itin) {
		this.itin = itin;
	}

	public List<Item> getItems() {
		return items;
	}
	
	public void setItems(List<Item> items) {
		this.items = items;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}

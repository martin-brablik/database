package cz.upol.inf.jj2.martinbrablik.database.models;

import java.sql.Connection;
import java.sql.DriverManager;

import cz.upol.inf.jj2.martinbrablik.database.DatabaseClient;

public class Item {
	private int dataId = -1;
	private int amount;
	private int unitPrice;
	private String productName;
	
	public Item(int amount, int unitPrice, String productName) {
		this.amount = amount;
		this.unitPrice = unitPrice;
		this.productName = productName;
		this.dataId++;	
	}
	
	public Item(int dataId, int amount, int unitPrice, String productName) {
		//Konstruktor pro naètení z databáze
		this.dataId = dataId;
		this.amount = amount;
		this.unitPrice = unitPrice;
		this.productName = productName;
	}
	
	public void delete() {
		try (Connection con = DriverManager.getConnection(DatabaseClient.connectionURL, DatabaseClient.USERNAME, DatabaseClient.PASSWORD)) {
			DatabaseClient.dbClient.deleteItem(this, con);
			con.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void update() {
		try (Connection con = DriverManager.getConnection(DatabaseClient.connectionURL, DatabaseClient.USERNAME, DatabaseClient.PASSWORD)) {
			DatabaseClient.dbClient.updateItem(this, con);
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void insert(Receipt receipt) {
		try (Connection con = DriverManager.getConnection(DatabaseClient.connectionURL, DatabaseClient.USERNAME, DatabaseClient.PASSWORD)) {
			DatabaseClient.dbClient.insertItem(this, receipt, con);
			if(this.getDataId() == -1)
				this.setDataId(DatabaseClient.dbClient.getItemId(this, con));
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public int getAmount() {
		return amount;
	}
	
	public void setAmount(int amount) {
		this.amount = amount;
	}
	
	public int getUnitPrice() {
		return unitPrice;
	}
	
	public void setUnitPrice(int unitPrice) {
		this.unitPrice = unitPrice;
	}
	
	public String getProductName() {
		return productName;
	}
	
	public void setProductName(String productName) {
		this.productName = productName;
	}
	
	public String toHtml() {
		StringBuilder sb = new StringBuilder();
		sb.append("<html>").append(this.toString().replace("\t", "&emsp;")).append("</html>");
		return sb.toString();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.productName).append('\t').append(this.unitPrice).append("\tx").append(this.amount);
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Item)) return false;
		Item other = (Item)obj;
		return this.getAmount() == other.getAmount() &&
				this.getUnitPrice() == other.getUnitPrice() &&
				this.getProductName().equals(other.getProductName());
	}

	public int getDataId() {
		return dataId;
	}

	public void setDataId(int dataId) {
		this.dataId = dataId;
	}
}

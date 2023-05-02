package cz.upol.inf.jj2.martinbrablik.database;

import java.sql.SQLException;

public class DBException extends SQLException {
	private static final long serialVersionUID = -450810239775278712L;
	public DBException() {
		super();
	}
	public DBException(String reason) {
		super(reason);
	}
}

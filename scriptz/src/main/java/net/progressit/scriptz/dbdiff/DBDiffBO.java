package net.progressit.scriptz.dbdiff;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;

import lombok.Data;

public class DBDiffBO {

	@Data
	public static class DbDiffLogEvent{
		private final String message;
	}
	
	private Connection con;
	private Multimap<String, String> interestedSchema = MultimapBuilder.linkedHashKeys().arrayListValues().build();
	private Multimap<String, Map<String, Object>> preData = MultimapBuilder.linkedHashKeys().arrayListValues().build();
	private Multimap<String, Map<String, Object>> diffData = MultimapBuilder.linkedHashKeys().arrayListValues().build();
	
	private EventBus bus;
	public DBDiffBO(EventBus bus) {
		this.bus = bus;
	}
	
	public void connect(String connection, String username, char[] password) {
		bus.post( new DbDiffLogEvent( "Opening database..." ) );
	      try {
	         Class.forName("org.postgresql.Driver");
	         con = DriverManager
	            .getConnection(connection, username, new String(password));
	      } catch (Exception e) {
	         e.printStackTrace();
	         System.err.println(e.getClass().getName()+": "+e.getMessage());
	         System.exit(0);
	      }
	      System.out.println("Opened database successfully");
	      bus.post( new DbDiffLogEvent( "Opened database successfully." ) );
	}

	public void grabSchema() {
		bus.post( new DbDiffLogEvent( "Grabbing schema..." ) );
		try {
			interestedSchema.clear();
			preData.clear();
			DatabaseMetaData dbmd = con.getMetaData();
			try (ResultSet tables = dbmd.getTables(null, "public", "%", new String[] { "TABLE" })) {
			    while (tables.next()) {
			    	System.out.println( tables.getString("TABLE_NAME") + ":" + tables.getString("TABLE_CAT") + ":" + tables.getString("TABLE_SCHEM") );
			        String tableName = tables.getString("TABLE_NAME");
			        try (ResultSet rs = dbmd.getColumns(null, "public", tableName, null)) {
			            while (rs.next()) {
			            	if(tableName.equals("attributes")) {
			            		System.out.println( rs.getString("TABLE_CAT") );
			            		System.out.println( rs.getString("TABLE_SCHEM") );
			            		System.out.println( rs.getString("COLUMN_NAME") );
			            		System.out.println( rs.getString("TYPE_NAME") );
			            	}
			            	String columnName = rs.getString("COLUMN_NAME"); //Also available: Column Size, Type Name, etc.
			            	interestedSchema.put(tableName, columnName);
			            }
			        }
			    }
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		
		Set<String> tables = interestedSchema.keySet();
		for(String table:tables) {
			System.out.print( table + ": ");
			System.out.println( interestedSchema.get(table) );
		}
		bus.post( new DbDiffLogEvent( "Completed." ) );
	}

	public void grabDiff() {
		Multimap<String, Map<String, Object>> nowData = MultimapBuilder.linkedHashKeys().arrayListValues().build();
		loadDataInto(nowData);
		getDiff(preData, nowData);
		preData = nowData;
		
	}

	public void printDiff() {
		bus.post( new DbDiffLogEvent( "Listing diff..." ) );
		Set<String> tables = diffData.keySet();
		String message = null;
		Gson g = new Gson();
		for(String table:tables) {
			Collection<Map<String, Object>> diffRows = diffData.get(table);
			if(diffRows.isEmpty()) continue;
			
			message = table + ": ";
			bus.post( new DbDiffLogEvent( message ) );
			for(Map<String, Object> diffRow:diffRows) {
				message = "\t" + g.toJson(diffRow);
				bus.post( new DbDiffLogEvent( message ) );
			}
		}
		bus.post( new DbDiffLogEvent( "Done logging diff." ) );
	}

	private void getDiff(Multimap<String, Map<String, Object>> preData,
			Multimap<String, Map<String, Object>> nowData) {
		bus.post( new DbDiffLogEvent( "Computing diff..." ) );
		diffData.clear();
		Set<String> tables = interestedSchema.keySet();
		int rows = 0;
		for(String table:tables) {
			if(table.equals("aaauserconfigrecord")) {
				System.out.println("beep.");
			}
			Collection<Map<String, Object>> tablePreRows = preData.get(table);
			Collection<Map<String, Object>> tablePostRows = nowData.get(table);
			Set<Map<String, Object>> setTablePreRows = new HashSet<>(tablePreRows);
			Set<Map<String, Object>> setTablePostRows = new HashSet<>(tablePostRows);
			setTablePreRows.removeAll(tablePostRows);
			setTablePostRows.removeAll(tablePreRows);
			for(Map<String, Object> preOnlyRow:setTablePreRows) {
				diffData.put(table, preOnlyRow);
				rows++;
			}
			for(Map<String, Object> postOnlyRow:setTablePostRows) {
				diffData.put(table, postOnlyRow);
				rows++;
			}
		}
		Set<String> diffTables = diffData.keySet();
		bus.post( new DbDiffLogEvent( "Diff ready. Tables: " + diffTables.size() + " Rows: " + rows) );
	}

	private void loadDataInto(Multimap<String, Map<String, Object>> nowData) {
		bus.post( new DbDiffLogEvent( "Grabbing new data..." ) );
		Set<String> tables = interestedSchema.keySet();
		StringBuilder sbSelect = new StringBuilder(2000);
		for(String table:tables) {
			Collection<String> cols = interestedSchema.get(table);
			sbSelect.setLength(0);
			for(String col:cols) {
				if(sbSelect.isEmpty()) sbSelect.append("Select ");
				else sbSelect.append(", ");
				sbSelect.append(col);
			}
			sbSelect.append(" from ").append(table);
			
			String select = sbSelect.toString();
			System.out.println(select);
			try(Statement st = con.createStatement();
					ResultSet rs = st.executeQuery(select)) {
	
				Map<String, Object> row = null;
				while(rs.next()) {
					row = new HashMap<>();
					for(String col:cols) {
						String colVal = rs.getString(col);
						row.put(col, colVal);
					}
					nowData.put(table, row);
				}
				
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
		bus.post( new DbDiffLogEvent( "New data ready." ) );
		
	}

}

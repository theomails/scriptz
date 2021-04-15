package net.progressit.scriptz.dbdiff;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
	private static final List<String> SKIP_TABLES = Arrays.asList(new String[] {"columndetails","oidtype","uvhvalues"});
	
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
			        String tableName = tables.getString("TABLE_NAME");
			        if( SKIP_TABLES.contains(tableName) ) {
			        	bus.post( new DbDiffLogEvent( String.format("Skipping too big table: %s", tableName) ) );
			        	continue; //No need to even read data for some of the HUGE static tables.
			        }
			        
			        try (ResultSet rs = dbmd.getColumns(null, "public", tableName, null)) {
			            while (rs.next()) {
			            	String columnName = rs.getString("COLUMN_NAME"); //Also available: Column Size, Type Name, etc.
			            	String typeName = rs.getString("TYPE_NAME"); //Also available: Column Size, Type Name, etc.
			            	if(typeName.startsWith("bytea")) {
			            		bus.post( new DbDiffLogEvent( String.format("Skipping un-diffable column: Column: %s, Type: %s", columnName, typeName) ) );
			            	}else {
			            		interestedSchema.put(tableName, columnName);
			            	}
			            }
			        }
			    }
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
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
		int rows = 0, size = 0;
		Set<Map<String, Object>> setTablePreRows = new HashSet<>();
		Set<Map<String, Object>> setTablePostRows = new HashSet<>();
		for(String table:tables) {
			long startnanos = System.nanoTime();
			Collection<Map<String, Object>> tablePreRows = preData.get(table);
			Collection<Map<String, Object>> tablePostRows = nowData.get(table);
			setTablePreRows.clear();
			setTablePostRows.clear();
			setTablePreRows.addAll(tablePreRows);
			setTablePostRows.addAll(tablePostRows);
			size = setTablePostRows.size();
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
			long endnanos = System.nanoTime();
			double diffnanos = (double)(endnanos-startnanos);
			if(diffnanos>1e8) {
				String msg = table + ": " + String.format("%.2f", diffnanos/1e9) + " for " + size + " rows.";
				bus.post( new DbDiffLogEvent(msg));
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
				if(sbSelect.length()==0) sbSelect.append("Select ");
				else sbSelect.append(", ");
				sbSelect.append(col);
			}
			sbSelect.append(" from ").append(table);
			
			String select = sbSelect.toString();
			try(Statement st = con.createStatement();
					ResultSet rs = st.executeQuery(select)) {
	
				Map<String, Object> row = null;
				while(rs.next()) {
					row = new HashMap<>();
					for(String col:cols) {
						Object colVal = rs.getObject(col);
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

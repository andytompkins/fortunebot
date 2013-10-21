package org.xmpp.bots;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
//import java.util.HashMap;
import java.util.TreeMap;
import java.util.Random;
import java.util.Arrays;
import java.util.List;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class SqlFortune implements Fortune {
	
	protected Random rand;
	protected Connection db = null;
	
	public SqlFortune() {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			System.err.println(e);
		}
		
		
		rand = new Random();
		
		try {
			db = DriverManager.getConnection("jdbc:sqlite:fortune.db");
		} catch (SQLException e) {
			System.err.println("Caught SQLException while connecting to db");
			System.err.println(e);	
		}
		
	}

	public String getCategoriesAsStr() {
		StringBuilder cats = new StringBuilder();
		try {
			Statement s = db.createStatement();
			s.setQueryTimeout(30);
			ResultSet rs = s.executeQuery("select * from categories");
			while (rs.next()) {
				cats.append(rs.getString("name") + " ");
			}
		} catch (SQLException e) {
			System.err.println("Caught SQLException while querying for categories");
			System.err.println(e);	
		}
		return(cats.toString());
	}
	
	private Long getCategoryId(String categoryName) {
		Long catId = -1L;
		try {
			Statement s = db.createStatement();
			s.setQueryTimeout(30);
			ResultSet rs = s.executeQuery("select rowid from categories where name=\"" + categoryName + "\"");
			if (rs.next()) {
				catId = rs.getLong("rowid");
			}
		} catch (SQLException e) {
			System.err.println("Caught SQLException while querying for category id");
			System.err.println(e);	
		}
		return catId;
	}
	
	private Long getRandomCategoryId() {
		ArrayList<Long> catids = new ArrayList<Long>();
		try {
			Statement s = db.createStatement();
			s.setQueryTimeout(30);
			ResultSet rs = s.executeQuery("select rowid,name from categories");
			while (rs.next()) {
				catids.add(rs.getLong("rowid"));
			}
		} catch (SQLException e) {
			System.err.println("Caught SQLException while querying for category ids");
			System.err.println(e);	
		}
		int catIndex = rand.nextInt(catids.size());
		return catids.get(catIndex);
	}
	
	private String getFortuneFromCategory(Long categoryId) {
		ArrayList<String> catFortunes = new ArrayList<String>();
		try {
			Statement s = db.createStatement();
			s.setQueryTimeout(30);
			ResultSet rs = s.executeQuery("select fortune from fortunes where categoryId=" + categoryId);
			while (rs.next()) {
				catFortunes.add(rs.getString("fortune"));
			}
		} catch (SQLException e) {
			System.err.println("Caught SQLException while querying for fortunes");
			System.err.println(e);	
		}
		int randomIndex = rand.nextInt(catFortunes.size());
		return catFortunes.get(randomIndex);
	}
	
	public String getFortune() {
		Long catId = getRandomCategoryId();
		return getFortuneFromCategory(catId);
	}
	
	public String getFortune(String cat) {
		String fortuneStr = "Unknown category [" + cat + "], valid fortune categories are: " + getCategoriesAsStr() + "\n";
		Long catId = getCategoryId(cat);
		if (catId != -1L) {
			fortuneStr = getFortuneFromCategory(catId);
		}
		return fortuneStr;
	}
	
	public void addFortuneToCategory(String cat, String fortune) {
		Long catId = getCategoryId(cat);
		try {
			Statement s = db.createStatement();
			s.setQueryTimeout(30);
			String safeF = fortune.replaceAll("'", "''");
			//System.out.println("==[ " + safeF + " ]==");
			s.executeUpdate("insert into fortunes values ('" + safeF + "', " + catId + ")");
		} catch (SQLException e) {
			System.err.println(e);
		}
	}
	
	public void addCategory(String cat) {
		Long catId = getCategoryId(cat);
		if (catId != -1L) {
			return;
		}
		try {
			Statement s = db.createStatement();
			s.setQueryTimeout(30);
			String safeC = cat.replaceAll("'", "''");
			s.executeUpdate("insert into categories values (\"" + safeC + "\")");
		} catch (SQLException e) {
			System.err.println(e);
		}
	}
	
	public void closeDb() {
		try {
			if (db != null) {
				db.close();
			}
		} catch (SQLException e) {
			System.err.println("Caught SQLException while closing db");
			System.err.println(e);
		}
	}
	
	public void createDb() {
		try {
			
			Statement s = db.createStatement();
			s.setQueryTimeout(30);
			
			s.executeUpdate("drop table if exists categories");
			s.executeUpdate("drop table if exists fortunes");
			
			s.executeUpdate("create table categories (name string)");
			s.executeUpdate("create table fortunes (fortune text, categoryId integer)");
			
		} catch (SQLException e) {
			System.err.println("Caught SQLException while creating db tables");
			System.err.println(e);	
		} 
	}
	
	public void loadFromFiles() {
		List<String> categories = Arrays.asList("pets", "futurama", "perl", 
				"smac", "starwars", "familyguy", "humorists", "computers", "linux", "goedel",
				"art", "ascii-art", "cookie", "debian", "definitions", "drugs", "education",
				"ethnic", "firefly", "food", "fortunes", "kids", "knghtbrd", "law", "linuxcookie",
				"literature", "love", "magic", "medicine", "men-women", "miscellaneous", "news",
				"paradoxum", "people", "platitudes", "politics", "riddles", "science", "songs-poems",
				"sports", "startrek", "wisdom", "work", "zippy", "offensive", "duckdynasty", "mismanagement", "grail",
				"airplane", "caddyshack", "animal-house", "princess-bride", "godfather", "office-space",
				"stripes", "spaceballs", "blazing-saddles", "this-is-the-end", "top100",
				"naked-gun", "police-academy", "big-lebowski", "young-frankenstein", "history-of-the-world",
				"pulp-fiction", "orange-county", "shawshank", "ghostbusters", "harold-and-kumar", "strange-brew"
				);
		TreeMap<String, ArrayList<String>> fortunes = new TreeMap<String, ArrayList<String>>();
		for (String cat : categories) {
			InputStream in = getClass().getResourceAsStream("/fortunes/" + cat);
			ArrayList<String> catList = new ArrayList<String>();
			try {
				DataInputStream ds = new DataInputStream(in);
				BufferedReader br = new BufferedReader(new InputStreamReader(ds));
			  
				String strLine;
				StringBuffer buf = new StringBuffer("");
			  
				while ((strLine = br.readLine()) != null)   {
					if (strLine.equalsIgnoreCase("%")) {
						String fullFortune = buf.toString();
						catList.add(fullFortune);
						buf.setLength(0);
					} else {
						buf.append(strLine + "\n");
					}  
				}
			  
				in.close();
				fortunes.put(cat, catList);
			  
			} catch (Exception e) {
				System.err.println("Fortune Error: " + e.getMessage());
			}
		}
		
		try {
			Statement s = db.createStatement();
			for (String cat : fortunes.keySet()) {
				System.out.println("inserting");
				s.executeUpdate("insert into categories values (\"" + cat + "\")");
				System.out.println("getting id");
				ResultSet rs = s.executeQuery("select last_insert_rowid()");
				if (rs.next()) {
					System.out.println("getting long");
					long catId = rs.getLong(1);
					System.out.println(cat + " got category id " + catId);
					// insert fortunes for cat
					ArrayList<String> catFortunes = fortunes.get(cat);
					for (String f : catFortunes) {
						System.out.println("inserting fortune");
						//String safeF = f;//f.replaceAll("\"", "\\\\\"");
						String safeF = f.replaceAll("'", "''");
						//System.out.println("==[ " + safeF + " ]==");
						s.executeUpdate("insert into fortunes values ('" + safeF + "', " + catId + ")");
					}
				}
				System.out.println("going to next cat");
			}
		} catch (SQLException e) {
			System.err.println("Caught SQLException while inserting stuff");
			System.err.println(e);
		}
		
	}
	
	
}

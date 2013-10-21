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

public class FileFortune implements Fortune {
	public static final List<String> categories = Arrays.asList("pets", "futurama", "perl", 
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

	//protected HashMap<String, ArrayList<String>> fortunes;
	protected TreeMap<String, ArrayList<String>> fortunes;
	protected Random rand;
	
	public FileFortune() {
		//fortunes = new HashMap<String, ArrayList<String>>();
		fortunes = new TreeMap<String, ArrayList<String>>();
		rand = new Random();
		
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
	}
	
	public void addFortuneToCategory(String cat, String fortune) {
		
	}

	public String getCategoriesAsStr() {
		StringBuilder cats = new StringBuilder();
		for (String s : fortunes.keySet())
		{
		    cats.append(s);
		    cats.append(" ");
		}
		return(cats.toString());
	}
	
	public String getFortune(String cat) {
		boolean catFound = false;
		for (String key : fortunes.keySet()) {
			if (key.equals(cat)) {
				catFound = true;
				break;
			}
		}
		String fortuneStr = "Unknown category [" + cat + "], valid fortune categories are: " + getCategoriesAsStr() + "\n";
		if (catFound) {
			ArrayList<String> catFortunes = fortunes.get(cat);
			int randomIndex = rand.nextInt(catFortunes.size());
			fortuneStr = catFortunes.get(randomIndex);
		}
		return fortuneStr;
	}
	
	public String getFortune() {
		int catIndex = rand.nextInt(fortunes.size());
		int index = 0;
		String chosenKey = "";
		for (String key : fortunes.keySet()) {
			if (index == catIndex) {
				chosenKey = key;
				break;
			}
			index++;
		}
		
		ArrayList<String> catFortunes = fortunes.get(chosenKey);
		int randomIndex = rand.nextInt(catFortunes.size());
		return catFortunes.get(randomIndex);
	}
	
	public void closeDb() {
		
	}

}

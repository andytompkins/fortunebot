package org.xmpp.bots;

public interface Fortune {
	
	public String addCategory(String cat);
	public String addFortuneToCategory(String cat, String fortune);
	
	public String getCategoriesAsStr();
	
	public String getFortune(String category);
	public String getFortune();
	
	public String listFortunes(String category);
	
	public String editFortune(String cat, int index, String fortune);
	public String deleteFortune(String cat, int index);
	
	public void closeDb();

}

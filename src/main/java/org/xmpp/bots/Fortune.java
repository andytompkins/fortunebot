package org.xmpp.bots;

public interface Fortune {
	
	public void addCategory(String cat);
	public void addFortuneToCategory(String cat, String fortune);
	
	public String getCategoriesAsStr();
	
	public String getFortune(String category);
	public String getFortune();
	
	public void closeDb();

}

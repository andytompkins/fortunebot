package org.xmpp.bots;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.client.fluent.Request;
import org.apache.http.client.ClientProtocolException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

	
public class IMDBMovieQuoteMaker {
	
	private Fortune fortune;
	
	public IMDBMovieQuoteMaker(Fortune fortune) {
		this.fortune = fortune;
	}
	
	public void makeMovieQuotes(String cat, String url) {
		String content = "";
		try {
			content = Request.Get(url).execute().returnContent().asString();
		} catch (ClientProtocolException e) {
			System.err.println("Protocol error: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("I/O error: " + e.getMessage());
			e.printStackTrace();
		}
		//System.out.println("Got content: " + content);
		
		ArrayList<String> quotes = new ArrayList<String>();
		Document doc = Jsoup.parse(content);
		Elements divs = doc.select("div.quote");
		for (Element div : divs) {
			Elements paras = div.select("p");
			String quote = "";
			for (Element p : paras) {
				String q = p.text() + "\n";
				quote += q;
			}
			//System.out.println("---------------------------------------");
			//System.out.println(quote);
			quotes.add(quote);
		}
		
		if (quotes.size() > 0) {
			System.out.println("Adding category: " + cat);
			fortune.addCategory(cat);
			for (String quote : quotes) {
				System.out.println("Adding quote \"" + quote + "\"");
				fortune.addFortuneToCategory(cat, quote);
			}
		}
		
	}
	
}
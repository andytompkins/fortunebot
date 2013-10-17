package org.xmpp.bots;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;

public class FortuneProcessor implements PacketListener {
	protected FortuneBot bot;
	protected Fortune fortune;
	protected MultiUserChat room;
	
	public FortuneProcessor(FortuneBot fortuneBot, Fortune fortune, MultiUserChat chatRoom) {
		bot = fortuneBot;
		this.fortune = fortune;
		room = chatRoom;
	}
		
	public void processPacket(Packet packet) {
		String fortuneStr = "No fortune.";
		String msg = ((Message)packet).getBody();
        	
		System.out.println("Received message: " + msg);
        
       	if (msg.equalsIgnoreCase("fortunebot quit")) {
        	//shouldRun = false;
			bot.stopRunning();
        	fortuneStr = "I'll BRB Wismar. Don't worry. Life will go on.";
        } else if (msg.matches("[Ff][Oo][Rr][Tt][Uu][Nn][Ee].*")) {
        	//System.out.println("Msg starts with fortune, trying to split");
        	String[] parts = msg.split("\\s+");
        	//System.out.println("parts.length = " + parts.length);
        	if (parts.length > 1) {
        		//System.out.println("category request: " + parts[1]);
        		fortuneStr = fortune.getFortune(parts[1]);
        	} else {
        		//System.out.println("normal fortune request");
        		fortuneStr = fortune.getFortune();
        	}
        }
        
        if (!fortuneStr.equalsIgnoreCase("No fortune.")) {
        	try {
        		// need delay before sending msg, or things appear out of order
        		try {
        			Thread.sleep(500);
        		}
        		catch (InterruptedException ex) {
        			System.err.println("Caught Interrupted Exception");
    				ex.printStackTrace();
        		}
        		room.sendMessage(fortuneStr);
			} catch (XMPPException ex) {
				System.err.println("Caught XMPP Exception");
				ex.printStackTrace();
			}
        }
	}
	
	
	
}

package com.alldata.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;

public class FortuneBot implements PacketListener {
	
	protected Connection connection;
	protected MultiUserChat room;
	protected Fortune fortune;
	protected boolean shouldRun = true;
	
	public FortuneBot() {
		Properties props = new Properties();
		try {
			InputStream in = getClass().getResourceAsStream("/connection.properties");
			props.load(in);
			in.close();
		} catch (FileNotFoundException ex) {
			System.out.println("File not found trying to read connection.properties.");
			//ex.printStackTrace();
			System.exit(1);
		} catch (IOException ex) {
			System.err.println("IOException while reading/closing file!");
			//ex.printStackTrace();
			System.exit(2);
		}
		
		System.out.println("Creating XMPP connection configuration");
		//System.out.println("props are: " + props.getProperty("server") + " and " + props.getProperty("port"));
		ConnectionConfiguration config = new ConnectionConfiguration(props.getProperty("server"), Integer.parseInt(props.getProperty("port")));
		config.setCompressionEnabled(true);
		config.setSASLAuthenticationEnabled(true);
		
		System.out.println("Creating XMPP connection");
		connection = new XMPPConnection(config);	
		try {
			connection.connect();
			connection.login(props.getProperty("user"), props.getProperty("pass"), props.getProperty("agent"));
			
			String confRoom = props.getProperty("room") + "@" + props.getProperty("conference");
			room = new MultiUserChat(connection, confRoom);
			DiscussionHistory history = new DiscussionHistory();
		    history.setMaxStanzas(0);
			room.join(props.getProperty("nick"), "", history, SmackConfiguration.getPacketReplyTimeout());
			
			room.addMessageListener(this);
		}
		catch (XMPPException ex) {
			System.err.println("Caught XMPP Exception");
			ex.printStackTrace();
		}

		fortune = new Fortune();
	}

	public void processPacket(Packet packet) {
		String fortuneStr = "No fortune.";
		
	 	String msg = ((Message)packet).getBody();
        System.out.println("Received message: " + msg);
        
        if (msg.equalsIgnoreCase("fortunebot quit")) {
        	shouldRun = false;
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
	
	public void quit() {
		System.out.println("Disconnecting XMPP connection");
		connection.disconnect();	
	}
	
	public boolean running() {
		return shouldRun;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		FortuneBot bot = new FortuneBot();
		while (bot.running()) {
			try {
				// pause briefly
				Thread.sleep(500);
			}
			catch (InterruptedException ex) {
				System.err.println("Caught Interrupted Exception");
				ex.printStackTrace();
			}
			
		}
		
		// another delay, so clean up can occur (eg Wismar msg).
		try {
			// pause briefly
			Thread.sleep(2000);
		}
		catch (InterruptedException ex) {
			System.err.println("Caught Interrupted Exception");
			ex.printStackTrace();
		}
	}
	
}

package org.xmpp.bots;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.ArrayList;


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

public class FortuneBot {
	
	protected Connection connection;
	//protected MultiUserChat room;
	protected ArrayList<FortuneProcessor> rooms;
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
		System.out.println("props are: " + props.getProperty("server") + " and " + props.getProperty("port"));
		ConnectionConfiguration config = new ConnectionConfiguration(props.getProperty("server"), Integer.parseInt(props.getProperty("port")));
		//config.setCompressionEnabled(true);
		config.setSASLAuthenticationEnabled(true);
		
		System.out.println("Creating XMPP connection");
		connection = new XMPPConnection(config);	
		try {
			connection.connect();
			SASLAuthentication.supportSASLMechanism("PLAIN", 0);
			connection.login(props.getProperty("user"), props.getProperty("pass"));
		}
		catch (XMPPException ex) {
			System.err.println("Caught XMPP Exception while connecting and logging in");
			ex.printStackTrace();
		}
		
		fortune = new SqlFortune();
		
		try {
			rooms = new ArrayList<FortuneProcessor>();
			String allConfRooms = props.getProperty("rooms");
			String[] confRooms = allConfRooms.split(",");
			for (String confRoom : confRooms) {
				System.out.println("Trying to join room: " + confRoom);
				String confRoomName = confRoom + "@" + props.getProperty("conference");
				MultiUserChat room = new MultiUserChat(connection, confRoomName);
				DiscussionHistory history = new DiscussionHistory();
				history.setMaxStanzas(0);
				FortuneProcessor fp = new FortuneProcessor(this, fortune, room);
				rooms.add(fp);
				room.addMessageListener(fp);
				room.join(props.getProperty("nick"), props.getProperty("pass"), history, SmackConfiguration.getPacketReplyTimeout());
			}
		}
		catch (XMPPException ex) {
			System.err.println("Caught XMPP Exception while joining room");
			ex.printStackTrace();
		}
	}

	
	
	public void quit() {
		System.out.println("Disconnecting XMPP connection");
		connection.disconnect();	
	}
	
	public boolean running() {
		return shouldRun;
	}
	
	public void stopRunning() {
		shouldRun = false;
		fortune.closeDb();
	}
	
	public static void main(String[] args) {
		
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("load-database")) {
				System.out.println("Loading database...");
				SqlFortune f = new SqlFortune();
				f.createDb();
				f.loadFromFiles();
				
				System.out.println(f.getFortune());
				System.out.println(f.getFortune("futurama"));
				
				
				f.closeDb();
			}
		} else {
			
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
	
}

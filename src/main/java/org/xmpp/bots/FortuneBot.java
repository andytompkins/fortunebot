package org.xmpp.bots;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Properties;
import java.util.ArrayList;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;

public class FortuneBot {
	
	protected Connection connection;
	//protected MultiUserChat room;
	protected ArrayList<FortuneProcessor> rooms;
	protected Fortune fortune;
	public IMDBMovieQuoteMaker quoteMaker;
	protected boolean shouldRun = true;
	static protected FortuneBot instance = null;
	private Properties props = null;
	
	public FortuneBot() {
		props = new Properties();
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

		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler(){
			@Override
			public void uncaughtException(Thread arg0, Throwable arg1) {
				System.err.println("Exception caught");
				arg1.printStackTrace();
			}
			
		});
		
		connect();
	}
	
	public void disconnect() {
		if (rooms != null) {
			for (FortuneProcessor room: rooms) {
				try {
					room.room.leave();
				} catch( IllegalStateException exc ) {
					System.err.println("Ignoring IllegalStateException. If we're not connected, we can't exactly leave a room...");
					exc.printStackTrace();
				}
			}
		}
	}
	
	public void connect() {		
		disconnect();
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
		quoteMaker = new IMDBMovieQuoteMaker(fortune);
		
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
		catch(IllegalStateException ex) {
			System.err.println("Caught IllegalStateException while joining room");
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
			
			instance = new FortuneBot();
			instance.connect();
			while (instance.running()) {
				try {
					// pause briefly
					Thread.sleep(500);
					if ( !instance.connection.isConnected()) {
						instance.connect();
					}
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

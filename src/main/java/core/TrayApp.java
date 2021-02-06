package core;

import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import lc.kra.system.keyboard.GlobalKeyboardHook;
import lc.kra.system.keyboard.event.GlobalKeyAdapter;
import lc.kra.system.keyboard.event.GlobalKeyEvent;

@SuppressWarnings("java:S106")
// using global hook and Robot().createScreenCapture create entirely new screenshot without clipboard
//using clipboard listener

public final class TrayApp {
	private static final String ICON_PATH = "resources/TrayIcon.png";
	private static long lastEvent = 0; //used for timer calculations
	private static ServerSocket uniqueServerSocket;
	private static final Clipboard SYSTEM_CLIPBOARD = Toolkit.getDefaultToolkit().getSystemClipboard();
	private static SimpleProperties config = new SimpleProperties();


	public static void main(String[] args) throws InterruptedException {
	// quit if trayApp isn't supported / App already running
		checkIfRunning(args);
		if (isImage(getClipboard()))
			setClipboard(new StringSelection("Check123"));
	// try to load icon
		Image icon = null;
		icon = getIcon(icon);

		TrayIcon trayIcon = null; // created later

	// get the SystemTray instance
		SystemTray tray = SystemTray.getSystemTray();

	// get global keyboard hook
		GlobalKeyboardHook keyboardHook = new GlobalKeyboardHook(true);

		// *--Quit Button--*

	// create menu item
		MenuItem quit = new MenuItem("Quit");

	// create listener
		ActionListener quitListener = exit -> {
			keyboardHook.shutdownHook();
			System.exit(0);
		};
	// add listener
		quit.addActionListener(quitListener);

		// *--Choose Output Dir Button--*

	// create menu item
		MenuItem dir = new MenuItem("Choose output dir");

	// create listener
		ActionListener dirListener = dir1 -> {
		// create new JFileChooser
			JFileChooser chooser = new JFileChooser();
		// opens on screenshot directory
			chooser.setCurrentDirectory(new java.io.File(config.getProperty(SimpleProperties.FIELD01)));
			chooser.setDialogTitle("Select Output Directory");
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		// disable the "All files" option.
			chooser.setAcceptAllFileFilterUsed(false);
			if (chooser.showOpenDialog(chooser) == JFileChooser.APPROVE_OPTION) { // approve button (open)
				// set screenshot directory at Path+\
				config.setProperty(SimpleProperties.FIELD01 , chooser.getSelectedFile().toString() + File.separator);
			} else // cancel button
				System.out.println("No Selection ");
		};
		
		// add listener
			dir.addActionListener(dirListener);
		
	// Left click / interact with trayIcon
		ActionListener clickListener = click -> {
			try {
				// opens screenshot directory
				Desktop.getDesktop().open(new File(config.getProperty(SimpleProperties.FIELD01)));
			} catch (IOException e) {
				System.err.println("IO Error when opening Screenshot Directory");
			}
		};

	// create a popup menu
		PopupMenu popup = new PopupMenu();

	// add menu items to popup menu
		popup.add(dir);
		popup.add(quit);
	// construct a TrayIcon
		trayIcon = new TrayIcon(icon, "ScreenshotZ", popup);
	// set the TrayIcon properties
		trayIcon.setImageAutoSize(true);
	// add trayIcon listener
		trayIcon.addActionListener(clickListener);
	// add the tray image
		try {
			tray.add(trayIcon);
		} catch (AWTException e) {
			System.err.println("Error loading icon");
		}
		
		// Global Keyboard Listener
				keyboardHook.addKeyListener(new GlobalKeyAdapter() {
					@Override
					public void keyPressed(GlobalKeyEvent event) {
						if (event.getVirtualKeyCode() == GlobalKeyEvent.VK_SNAPSHOT) { // if PrtScn
							try {
									// print to screenshot directory
								if(System.currentTimeMillis()-lastEvent>1000) {
									lastEvent = System.currentTimeMillis();
									System.out.println("Keyboard Listener Activated");									
									robotTo(config.getProperty(SimpleProperties.FIELD01));
								}
							} catch (Exception e) {
								System.err.println("Exception at print to dir");
								e.printStackTrace();
							}
						}
					}
				});				

		//Clipboard Style Listener
				SYSTEM_CLIPBOARD.addFlavorListener(listener -> {
					System.out.println("Clipboard Listener got event");
					try {
						//Sleep so that Keyboard Listener gets first event
						Thread.sleep(50);		
						if(System.currentTimeMillis()-lastEvent>1000) {
							System.out.println("Clipboard Listener Activated");
							clipboardTo(config.getProperty(SimpleProperties.FIELD01));
						}
					} catch(InterruptedException e) {
						System.err.println("Literally impossible - Thread sleep Error");
						Thread.currentThread().interrupt();
					}
					catch(Exception e) {
						System.err.println("Error during clipboardTo event");
					}
				});
	}

	/* -----------------------Helper Methods------------------------------ */

		//load icon from ICON_PATH
	private static Image getIcon(Image icon) {
		try {
			File iconFile = new File(ICON_PATH);
			if (iconFile.exists()) {
					// load into Image file
				icon = Toolkit.getDefaultToolkit().getImage(ICON_PATH);
					// wait for image to load
				while (icon.getWidth(null) == -1)
					Thread.sleep(50);
			} else {
				System.err.println("ERROR: TrayIcon not found, Exiting Program"); 
				System.exit(0);
			}
		} catch (Exception e) {
			System.err.println("Error loading image");
			e.getMessage();
		}
		return icon;
	}

		// print screenshot to 'directory'
	private static void robotTo(String directory) throws Exception {
	// create buffered image from new rectangle containing all screen
		BufferedImage img = new Robot().createScreenCapture(
				new Rectangle(
						Toolkit.getDefaultToolkit().getScreenSize()));
	// create file using getName (returns new image path)
		File outfile = new File(getName(directory));
	// write image to file
		ImageIO.write(img, "png", outfile);
		System.out.println("image made from robot to: " + outfile.getAbsolutePath());
	// flush buffered image
		img.flush();
	// Call garbage collector (temporary fix to memory leak from this method)
		Runtime.getRuntime().gc();
	}

		//print image from clipboard to 'directory'
	private static void clipboardTo (String directory) throws Exception {
	//grab clipboard
		Transferable content = getClipboard();
	//check thats its an image
		if(!isImage(content))
			return;
	//reset clipboard content - so that listener can notice new screenshot
		setClipboard(new StringSelection("check123"));
	//create buffered image from content
		BufferedImage img = (BufferedImage) content.getTransferData(DataFlavor.imageFlavor);
	//create file using getName (returns new image path)
		File outfile = new File(getName(directory));
	//write image to file
		ImageIO.write(img, "png", outfile);
		System.out.println("image copied from clipboard to: " + outfile.getAbsolutePath());
	//flush buffered image
		img.flush();
	//Call garbage collector (temporary fix to memory leak from this method)
		Runtime.getRuntime().gc();
	}

		// return new file path string using 'directory'
	private static String getName(String directory) {
	// create date format
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH.mm.ss");
	// get current date
		LocalDateTime now = LocalDateTime.now();
		System.out.println(directory + "  " + dtf.format(now));
	// try new name
		String name = directory + dtf.format(now);
		File tmpDir = new File(name + ".png");
	// create (num) suffix in case file already exist
		int num = 0;
		while (tmpDir.exists()) {
			num++;
			tmpDir = new File(name + " (" + num + ").png");
		}
		return tmpDir.getAbsolutePath(); // returns new path
	}

		//return Transferable content from Clipboard
	private static Transferable getClipboard() {		
		Transferable content = null;
		try {
			content = SYSTEM_CLIPBOARD.getContents(content);
		} catch(Exception e) {
			System.err.println("Error grabbing clipboard");
			e.printStackTrace();
		}
		return content;
		
	}
	
		//set Local Clipboard content to this.arg
	private static void setClipboard(Transferable content) {
		try {
			SYSTEM_CLIPBOARD.setContents(content, null);
		} catch (Exception e) {
			System.err.println("Error setting clipboard");
			e.printStackTrace();
		}
	}
	
		//check if transferable from clipboard is an image
	private static boolean isImage (Transferable content) {
			//check that content exist
		if (content == null) {
			System.err.println("nothing found in clipboard");
			return false;
		}	//check that content is an image
		if (!content.isDataFlavorSupported(DataFlavor.imageFlavor)) {
			System.err.println("no image found in clipboard");
			return false;
		}
		return true;
	}

	//capture screen if args[0]=="-capture" and quit or quit if already running
	private static void checkIfRunning(String[] args) {
			//check that tray is supported
		if (!SystemTray.isSupported()){
			System.err.println("System Tray isn't supported");
			System.exit(1);
		} //check if trayApp was started with args
		else if(args!=null && args.length > 0 && args[0].equals("-capture")) {
			try {
				robotTo(config.getProperty(SimpleProperties.FIELD01));
			} catch (Exception a) {
				System.err.println("Couldn't print before loading main method..");
				a.printStackTrace();
			} finally {
				System.exit(0);
			}
		}
		try { //Bind to localhost adapter with a zero connection queue [PORT 9999]
		  uniqueServerSocket = new ServerSocket(9999,0,InetAddress.getByAddress(new byte[] {127,0,0,1}));
		//throws BindException if already connected
		} catch (BindException e) {
		  System.err.println("Server Already running."); 
		  System.exit(2);
		} catch (IOException e) {
			System.err.println("Unexpected IO error.");
			e.printStackTrace();
			System.exit(3);
		} catch (Exception e) {
			System.err.println("Unexpected error.");
			e.printStackTrace();
			System.exit(3);
		}
	  }
}

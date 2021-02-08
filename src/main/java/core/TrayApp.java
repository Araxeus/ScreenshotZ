package core;

import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;

import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.datatransfer.Clipboard;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;

import javax.swing.JFileChooser;
import lc.kra.system.keyboard.GlobalKeyboardHook;
import lc.kra.system.keyboard.event.GlobalKeyAdapter;
import lc.kra.system.keyboard.event.GlobalKeyEvent;

@SuppressWarnings("java:S106")
// using global hook and Robot().createScreenCapture create entirely new
// screenshot without clipboard
// using clipboard listener

public final class TrayApp {

	private static boolean isCropping = false;
	private static long lastEvent = 0; // used for timer calculations

	@SuppressWarnings("unused")
	private static ServerSocket uniqueServerSocket; // used to allow only one instance

	private static final Clipboard SYSTEM_CLIPBOARD = Toolkit.getDefaultToolkit().getSystemClipboard();

	static SimpleProperties config = new SimpleProperties();

	private static GlobalKeyboardHook keyboardHook;

	public static void main(String[] args) throws InterruptedException {
		// run args / quit if trayApp isn't supported / App already running
		checkIfRunning(args);
		// reset clipboard if initial
		if (Utils.isImage(getClipboard()))
			setClipboard(new StringSelection(""));
		// try to load icon
		Image icon = Utils.getImage("TrayIcon.png");

		// get the SystemTray instance
		SystemTray tray = SystemTray.getSystemTray();

		// initialize global keyboard hook
		keyboardHook = new GlobalKeyboardHook(true);

		// *--Quit Button--*
		MenuItem quit = new MenuItem("Quit");
		// add listener
		quit.addActionListener(quitListener);

		// *--Choose Output Directory Button--*
		MenuItem dir = new MenuItem("Select Output Directory");
		// add listener
		dir.addActionListener(dirListener);

		// *--Choose Keybind Button--*
		MenuItem keybindMenu = new MenuItem("Choose Additional Keybind");
		// Create + Add listener
		keybindMenu.addActionListener(keybindListener -> GetKeybind.openWindow());

		// create a popup menu
		PopupMenu popup = new PopupMenu();
		// add menu items to popup menu
		popup.add(keybindMenu);
		popup.add(dir);
		popup.add(quit);
		// construct a TrayIcon
		TrayIcon trayIcon = new TrayIcon(icon, "ScreenshotZ", popup);
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
				// get input type
				byte mode;
				if (event.getVirtualKeyCode() == GlobalKeyEvent.VK_SNAPSHOT)
					mode = 1;
				else if (keyboardHook.areKeysHeldDown(config.keybind))
					mode = 2;
				else
					mode = 0;
				// [mode =1 -> prntscrn] , [mode =2 -> keybind] , [mode =3 -> alwaysCrop]
				if (mode == 1 || mode == 2) {
					try {
						if (System.currentTimeMillis() - lastEvent > 1000) {
							lastEvent = System.currentTimeMillis();
							System.out.println("Keyboard Listener Activated");
							Utils.robotTo(config.getProperty(SimpleProperties.FIELD01), mode);
						}
					} catch (Exception e) {
						System.err.println("Exception at print to dir");
						e.printStackTrace();
					}
				}
			}
		});

		// Clipboard Style Listener
		SYSTEM_CLIPBOARD.addFlavorListener(listener -> {
			try {
				// Sleep so that Keyboard Listener gets first event
				Thread.sleep(50);
				if (System.currentTimeMillis() - lastEvent > 1000) {
					System.out.println("Clipboard Listener Activated [Keyboard's wasn't]");
					Utils.clipboardTo(config.getProperty(SimpleProperties.FIELD01)); // TODO Switch to robotTo
					// reset clipboard content - so that listener can notice new screenshot
					setClipboard(new StringSelection(""));
				}
			} catch (InterruptedException e) {
				System.err.println("Literally impossible - Thread sleep Error");
				Thread.currentThread().interrupt();
			} catch (Exception e) {
				System.err.println("Error during clipboardTo event");
			}
		});
		// CropImage.openWindow(keyboardHook,
		// "C:\\Users\\Araxeus\\.ScreenshotZ\\Screenshots\\toCrop.png");
	}

	/* -----------------------Helper Methods------------------------------ */

	public static void setIsCropping(boolean getStatus) {
		isCropping = getStatus;
	}

	public static boolean isCropping() {
		return isCropping;
	}

	public static GlobalKeyboardHook getKeyboardHook() {
		return keyboardHook;
	}

	// return Transferable content from Clipboard
	public static Transferable getClipboard() {
		Transferable content = null;
		try {
			content = SYSTEM_CLIPBOARD.getContents(content);
		} catch (Exception e) {
			System.err.println("Error grabbing clipboard");
			e.printStackTrace();
		}
		return content;
	}

	// set Local Clipboard content to this.arg
	private static void setClipboard(Transferable content) {
		try {
			SYSTEM_CLIPBOARD.setContents(content, null);
		} catch (Exception e) {
			System.err.println("Error setting clipboard");
			e.printStackTrace();
		}
	}

	// Choose output directory button listener
	static ActionListener dirListener = directoryChooser -> {
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
			config.setProperty(SimpleProperties.FIELD01, chooser.getSelectedFile().toString() + File.separator);
		} else // cancel button
			System.out.println("No Selection ");
	};

	// Quit button listener
	static ActionListener quitListener = exit -> {
		keyboardHook.shutdownHook();
		System.exit(0);
	};

	// Left click / interact with trayIcon listener
	static ActionListener clickListener = click -> {
		try {
			// opens screenshot directory
			Desktop.getDesktop().open(new File(config.getProperty(SimpleProperties.FIELD01)));
		} catch (IOException e) {
			System.err.println("IO Error when opening Screenshot Directory");
		}
	};

	// capture screen if args[0]=="-capture" and quit or quit if already running
	private static void checkIfRunning(String[] args) {
		// check if trayApp was started with args
		// TODO explain mode logic
		if (args != null && args.length > 0 && args[0].equals("-capture")) {
			byte mode = 0;
			if (args.length > 1 && args[1].equals("-crop"))
				mode = 3;
			else
				isCropping = true;
			try {
				Utils.robotTo(config.getProperty(SimpleProperties.FIELD01), mode);
			} catch (Exception a) {
				System.err.println("Couldn't print before loading main method..");
				a.printStackTrace();
			} finally {
				System.exit(0);
			}
		}

		// check that tray is supported
		if (!SystemTray.isSupported()) {
			System.err.println("System Tray isn't supported");
			System.exit(1);
		}
		// Bind to localhost adapter with a zero connection queue [PORT 9999]
		try {
			uniqueServerSocket = new ServerSocket(9999, 0, InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 }));
			// throws BindException if already connected
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

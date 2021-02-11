package core;

import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.CheckboxMenuItem;

import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.datatransfer.Clipboard;

import java.awt.datatransfer.Transferable;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;


import lc.kra.system.keyboard.GlobalKeyboardHook;
import lc.kra.system.keyboard.event.GlobalKeyAdapter;
import lc.kra.system.keyboard.event.GlobalKeyEvent;

import org.lwjgl.PointerBuffer;
import org.lwjgl.util.nfd.NativeFileDialog;
import org.lwjgl.system.MemoryUtil;

@SuppressWarnings("java:S106")
// using global hook and Robot().createScreenCapture


public final class TrayApp {
	private static boolean isCropping = false;

	private static long lastEvent = 0; // used for timer calculations

	@SuppressWarnings("unused")
	private static ServerSocket uniqueServerSocket; // used to allow only one instance

	private static final Clipboard SYSTEM_CLIPBOARD = Toolkit.getDefaultToolkit().getSystemClipboard();

	private static GlobalKeyboardHook keyboardHook;


	public static void main(String[] args) throws InterruptedException {
		// run args / quit if trayApp isn't supported / App already running
		checkIfRunning(args);

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
		keybindMenu.addActionListener(keybindListener -> {
		isCropping=true;
		GetKeybind.openWindow();
	});

		// *--Crop Settings Check Box--*
		// create , add listener , and setState according to config
		CheckboxMenuItem checkBoxCrop03 = new CheckboxMenuItem("Crop on PrintScreen");
		checkBoxCrop03.setState(Config.FIELD03.getBoolean());
		checkBoxCrop03.addItemListener(crop03Listener);

		CheckboxMenuItem checkBoxCrop04 = new CheckboxMenuItem("Crop on Custom Keybind");
		checkBoxCrop04.setState(Config.FIELD04.getBoolean());
		checkBoxCrop04.addItemListener(crop04Listener);

		CheckboxMenuItem checkBoxCrop05 = new CheckboxMenuItem("Save Original onCrop");
		checkBoxCrop05.setState(Config.FIELD05.getBoolean());
		checkBoxCrop05.addItemListener(crop05Listener);

		CheckboxMenuItem checkBoxCrop06 = new CheckboxMenuItem("Exit UI onCrop");
		checkBoxCrop06.setState(Config.FIELD06.getBoolean());
		checkBoxCrop06.addItemListener(crop06Listener);

		
		// create subMenu
		PopupMenu subMenu = new PopupMenu("Crop Settings:");
		subMenu.add(checkBoxCrop03);
		subMenu.add(checkBoxCrop04);
		subMenu.add(checkBoxCrop05);
		subMenu.add(checkBoxCrop06);
		// create main popup menu
		PopupMenu popup = new PopupMenu();
		// add menu items to popup menu
		popup.add(subMenu);
		popup.addSeparator();
		popup.add(dir);
		popup.add(keybindMenu);	
		popup.addSeparator();
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
		} catch (Exception e) {
			System.err.println("Error loading System Looks and Feels");
		}

		// add Global Keyboard Listener
		keyboardHook.addKeyListener(keyboardAdapter);
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

	private static void setLastEvent(long newValue) {
		lastEvent = newValue;
	}

	// set Local Clipboard content to this.arg
	public static void setClipboard(Transferable content) {
		try {
			SYSTEM_CLIPBOARD.setContents(content, null);
		} catch (Exception e) {
			System.err.println("Error setting clipboard");
			e.printStackTrace();
		}
	}

	// Choose output directory button listener
	private static ActionListener dirListener = directoryChooser -> {
		//use LWJGL NativeFileDialog
		PointerBuffer path = MemoryUtil.memAllocPointer(1);
		switch (NativeFileDialog.NFD_PickFolder(Config.FIELD01.getString() , path)) {
            case NativeFileDialog.NFD_OKAY:
                System.out.println("Directory Chosen Successfully!");
				Config.FIELD01.setValue(path.getStringUTF8(0)+File.separator);
                System.out.println("New Path = "+Config.FIELD01.getString());
                NativeFileDialog.nNFD_Free(path.get(0));
                break;
            case NativeFileDialog.NFD_CANCEL:
                System.out.println("User pressed cancel.");
                break;
            default: // NFD_ERROR
                System.err.format("Error: %s%n", NativeFileDialog.NFD_GetError());
		}
	};

	// Quit button listener
	private static ActionListener quitListener = exit -> {
		System.out.println("Exiting Program");
		keyboardHook.shutdownHook();
		System.exit(0);
	};

	// Left click / interact with trayIcon listener
	private static ActionListener clickListener = click -> {
		try {
			// opens screenshot directory
			Desktop.getDesktop().open(new File(Config.FIELD01.getString()));
		} catch (IOException e) {
			System.err.println("IO Error when opening Screenshot Directory");
		}
	};

	/*
	** checkBox Listeners: [state 2 == false]  [state 1 = true]
	*/

	// crop on PrintScreen listener
	private static ItemListener crop03Listener = crop03 -> 
		Config.FIELD03.setValue(crop03.getStateChange() == 1);

	// crop on Custom Keybind listener
	private static ItemListener crop04Listener = crop04 -> 
		Config.FIELD04.setValue(crop04.getStateChange() == 1);
	
	//onCrop Save Original Listener
	private static ItemListener crop05Listener = crop05 -> 
		Config.FIELD05.setValue(crop05.getStateChange() == 1);

	//Exit UI onCrop Listener
	private static ItemListener crop06Listener = crop06 -> 
		Config.FIELD06.setValue(crop06.getStateChange() == 1);


	//Keyboard Listener
	private static GlobalKeyAdapter keyboardAdapter = new GlobalKeyAdapter() {
		@Override
		public void keyPressed(GlobalKeyEvent event) {
			// get input type
			int mode;
			if (event.getVirtualKeyCode() == GlobalKeyEvent.VK_SNAPSHOT)
				mode = 1;
			else if (keyboardHook.areKeysHeldDown(Config.getKeybinds()))
				mode = 2;
			else
				mode = 0;
			// [mode =1 -> printScreen] , [mode =2 -> keybind] , [mode =3 -> alwaysCrop]
			if (mode == 1 || mode == 2) {
				try {
					if (System.currentTimeMillis() - lastEvent > 1000) {
						setLastEvent(System.currentTimeMillis());
						System.out.println("Keyboard Listener Activated");
						Utils.robotTo(Config.FIELD01.getString(), mode);
					}
				} catch (Exception e) {
					System.err.println("Exception at print to dir");
					e.printStackTrace();
				}
			}
		}
	};

	// IF (args[0] == "-capture")->[capture screen and quit] ELSE [Quit if app already running]
	private static void checkIfRunning(String[] args) {
		// check if trayApp was started with args
		if (args != null && args.length > 0 && args[0].equals("-capture")) {
				//no cropping
				isCropping = true;
			try {
				Utils.robotTo(Config.FIELD01.getString(), 0);
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

package core;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Label;
import org.eclipse.wb.swt.SWTResourceManager;

import lc.kra.system.keyboard.GlobalKeyboardHook;
import lc.kra.system.keyboard.event.GlobalKeyAdapter;
import lc.kra.system.keyboard.event.GlobalKeyEvent;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

@SuppressWarnings("java:S106")
public class GetKeybind {

    private Shell shell;
    private Display display;
    private Label keyLabel;

    private GlobalKeyboardHook keyboardHook;

    // current pressed keys
    private ArrayList<Integer> keyChain;

    // is firstKey pressed?
    private boolean capturing;

    // PRIVATE constructor
    private GetKeybind(GlobalKeyboardHook keyboardHook) {
        // get keyboard hook
        this.keyboardHook = keyboardHook;
        // initialize keyChain
        keyChain = new ArrayList<>();
        // initialize display
        display = Display.getDefault();
        // onStart - not capturing
        capturing = false;
    }

    // PUBLIC window initializer
    public static void openWindow(GlobalKeyboardHook keyboardHook) {
        try {
            // create new GetKeybind window
            GetKeybind window = new GetKeybind(keyboardHook);
            // and then open that window
            window.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Open the window.
     */
    private void open() {
        // populate shell
        createContents();
        // open shell and focus on it
        shell.open();
        shell.layout();
       shell.forceActive();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

    /**
     * Create contents of the window.
     */
    private void createContents() {
        // Shell
        shell = new Shell(SWT.CLOSE | SWT.TITLE);
        // Get Icon
        Image icon = getImage("BaseIcon.ico");
        if (icon != null)
            shell.setImage(icon);
        else // this will execute only if jar was badly packaged
            shell.setImage(SWTResourceManager.getImage("Resources/BaseIcon.ico"));
        shell.setBackground(SWTResourceManager.getColor(81, 86, 88));
        shell.setTouchEnabled(true);
        shell.setSize(450, 211);
        shell.setText("Choose Keybind");
        // Create rectangle from display
        Rectangle screenSize = display.getPrimaryMonitor().getBounds();
        // Set shell location to middle of the screen
        shell.setLocation((screenSize.width - shell.getBounds().width) / 2,
                (screenSize.height - shell.getBounds().height) / 2);
        shell.setLayout(null);

        // KeyLabel
        keyLabel = new Label(shell, SWT.VERTICAL | SWT.CENTER);
        keyLabel.setForeground(SWTResourceManager.getColor(240, 255, 255));
        keyLabel.setBackground(SWTResourceManager.getColor(81, 86, 88));
        keyLabel.setFont(SWTResourceManager.getFont("Sitka Display", 26, SWT.BOLD | SWT.ITALIC));
        keyLabel.setBounds(10, 10, 424, 94);
        addOrigin();

        // Save Button
        Button saveButton = new Button(shell, SWT.NONE);
        saveButton.addSelectionListener(new SelectionAdapter() {
            // save and close on click
            @Override
            public void widgetSelected(SelectionEvent e) {
                System.out.println(keyChain.toString());
                // update main app config
                TrayApp.config.setProperty(SimpleProperties.FIELD02, keyChainToString());
                // close shell
                shell.close();
            }
        });
        saveButton.setForeground(SWTResourceManager.getColor(SWT.COLOR_TITLE_FOREGROUND));
        saveButton.setBackground(SWTResourceManager.getColor(112, 128, 144));
        saveButton.setFont(SWTResourceManager.getFont("Microsoft YaHei", 18, SWT.NORMAL));
        saveButton.setBounds(166, 110, 120, 50);
        saveButton.setText("Save");

        // Clear Button
        Button clearButton = new Button(shell, SWT.FLAT);
        clearButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                keyChain.clear();
                keyLabel.setText("");
            }
        });
        clearButton.setFont(SWTResourceManager.getFont("Microsoft YaHei", 11, SWT.NORMAL));
        clearButton.setBounds(390, 147, 44, 25);
        clearButton.setText("clear");
        clearButton.setBackground(SWTResourceManager.getColor(128, 128, 128));

        // create keybind listener
        GlobalKeyAdapter keybindListen = new GlobalKeyAdapter() {
            @Override
            public void keyPressed(GlobalKeyEvent event) {
                // sync threads to avoid 'Invalid Thread Access' error
                display.asyncExec(() -> {
                    // if not capturing -> this is the first key pressed
                    if (!capturing) {
                        // set mode to capturing
                        capturing = true;
                        // clear keychain
                        keyChain.clear();
                    } // anyway try to add current key
                    addKey(event.getVirtualKeyCode());
                });
            }

            @Override
            public void keyReleased(GlobalKeyEvent event) {
                display.asyncExec(() -> {
                    // if the released key is the first key that was pressed (added to keychain)
                    if (!keyChain.isEmpty() && event.getVirtualKeyCode() == keyChain.get(0))
                        // not capturing anymore
                        capturing = false;
                });
            }

        };
        // add Listener to imported keyboard_hook
        keyboardHook.addKeyListener(keybindListen);

        // On exit (dispose listener )
        shell.addDisposeListener(disposed -> {
            // remove keybind listener from imported keyboard_hook
            keyboardHook.removeKeyListener(keybindListen);
            System.out.println("Exited Keybind UI (and closed 2nd listener)");
        });
    }

    // Load Image from resources inputStream SWT STYLE
    public Image getImage(String name) {
        Image img = null;
        try (InputStream inputStream = TrayApp.class.getClassLoader().getResourceAsStream(name)) {
            img = new Image(display, inputStream);
        } catch (IOException e) {
            System.err.println("Error loading icon");
            e.getMessage();
        }
        return img;
    }

    private void addKey(int vKC) { // vKC = Virtual Key Code
        // check that key isn't already in keyChain && keyChain isn't full
        if (keyChain.size() < 3 && !keyChain.contains(vKC)) {
            // get Virtual Key Code to String
            String keyCode = keyToString(vKC);
            // ignore some keys
            if (!keyCode.equals("??")) {
                switch (keyChain.size()) {
                    case 0:
                        // replace label
                        keyLabel.setText(keyCode);
                        keyChain.add(vKC);
                        break;
                    case 1:
                    case 2:
                        // add to label
                        keyLabel.setText(keyLabel.getText() + " + " + keyCode);
                        keyChain.add(vKC);
                        break;
                }
            }
        }
    }

    // on launch - get current keybind
    private void addOrigin() {
        // 0 value means no keybind
        if (TrayApp.config.keybind.length == 1 && TrayApp.config.keybind[0] == 0)
            return;
        // add keybind from config
        for (int key : TrayApp.config.keybind)
            addKey(key);
    }

    // Code keyChain[] to String for config
    private String keyChainToString() {
        // empty keychain = value 0
        if (keyChain.isEmpty())
            return "0";
        // build string
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < keyChain.size(); i++) {
            output.append(keyChain.get(i));
            // add ',' delimiter between values
            if (i != keyChain.size() - 1)
                output.append(',');
        }
        return output.toString();
    }

    // Convert Virtual Key Code into String
    private static String keyToString(int vKC) { // vKC = Virtual Key Code
		switch(vKC) {
			case 3: return  "Break";
			case 8: return  "Backspace/delete";
			case 9: return  "Tab";
			case 12: return  "Clear";
			case 13: return  "Enter";
			case 16: return  "Shift";
			case 17: return  "Ctrl";
			case 18: return  "Alt";
			case 19: return  "Pause/break";
			case 20: return  "Caps lock";
			case 21: return  "Hangul";
			case 25: return  "Hanja";
			case 27: return  "Escape";
			case 28: return  "Conversion";
			case 29: return  "Non-conversion";
			case 32: return  "Spacebar";
			case 33: return  "Page up";
			case 34: return  "Page down";
			case 35: return  "End";
			case 36: return  "Home";
			case 37: return  "Left arrow";
			case 38: return  "Up arrow";
			case 39: return  "Right arrow";
			case 40: return  "Down arrow";
			case 41: return  "Select";
			case 42: return  "Print";
			case 43: return  "Execute";
			case 44: return  "Print Screen";
			case 45: return  "Insert";
			case 46: return  "Delete";
			case 47: return  "Help";
			case 48: return "0";
			case 49: return "1";
			case 50: return "2";
			case 51: return "3";
			case 52: return "4";
			case 53: return "5";
			case 54: return "6";
			case 55: return "7";
			case 56: return "8";
			case 57: return "9";
			case 58: return ":";
			case 60: return "<";	
			case 65: return  "A";
			case 66: return  "B";
			case 67: return  "C";
			case 68: return  "D";
			case 69: return  "E";
			case 70: return  "F";
			case 71: return  "G";
			case 72: return  "H";
			case 73: return  "I";
			case 74: return  "J";
			case 75: return  "K";
			case 76: return  "L";
			case 77: return  "M";
			case 78: return  "N";
			case 79: return  "O";
			case 80: return  "P";
			case 81: return  "Q";
			case 82: return  "R";
			case 83: return  "S";
			case 84: return  "T";
			case 85: return  "U";
			case 86: return  "V";
			case 87: return  "W";
			case 88: return  "X";
			case 89: return  "Y";
			case 90: return  "Z";
			case 91: return  "Windows Key";
			case 92: return  "Right window key";
			case 93: return  "Windows Menu";
			case 95: return  "Sleep";
			case 96: return  "Numpad 0";
			case 97: return  "Numpad 1";
			case 98: return  "Numpad 2";
			case 99: return  "Numpad 3";
			case 100: return  "Numpad 4";
			case 101: return  "Numpad 5";
			case 102: return  "Numpad 6";
			case 103: return  "Numpad 7";
			case 104: return  "Numpad 8";
			case 105: return  "Numpad 9";
			case 106: return  "Multiply";
			case 107: return  "Add";
			case 108: return  "Numpad period (firefox)";
			case 109: return  "Subtract";
			case 110: return  "Decimal point";
			case 111: return  "Divide";
			case 112: return  "F1";
			case 113: return  "F2";
			case 114: return  "F3";
			case 115: return  "F4";
			case 116: return  "F5";
			case 117: return  "F6";
			case 118: return  "F7";
			case 119: return  "F8";
			case 120: return  "F9";
			case 121: return  "F10";
			case 122: return  "F11";
			case 123: return  "F12";
			case 124: return  "F13";
			case 125: return  "F14";
			case 126: return  "F15";
			case 127: return  "F16";
			case 128: return  "F17";
			case 129: return  "F18";
			case 130: return  "F19";
			case 131: return  "F20";
			case 132: return  "F21";
			case 133: return  "F22";
			case 134: return  "F23";
			case 135: return  "F24";
			case 136: return  "F25";
			case 137: return  "F26";
			case 138: return  "F27";
			case 139: return  "F28";
			case 140: return  "F29";
			case 141: return  "F30";
			case 142: return  "F31";
			case 143: return  "F32";
			case 144: return  "Num lock";
			case 145: return  "Scroll lock";
			case 151: return  "Airplane mode";
			case 160: return "^";
			case 161: return "!";
			case 163: return "#";
			case 164: return "$";
			case 166: return  "Page backward";
			case 167: return  "Page forward";
			case 168: return  "Refresh";
			case 170: return "*";
			case 172: return  "Home key";
			case 174: return  "Decrease volume level";
			case 175: return  "Increase volume level";
			case 176: return  "Next";
			case 177: return  "Previous";
			case 178: return  "Stop";
			case 179: return  "Play/pause";
			case 180: return  "E-mail";
			case 186: return  "Semi-colon";
			case 187: return  "Equal sign";
			case 188: return  "Comma";
			case 189: return  "Dash";
			case 190: return  "Period";
			case 191: return  "Forward slash /";
			case 192: return  "Grave accent";
			case 193: return "?; / ";
			case 219: return  "Open bracket";
			case 220: return  "Back slash";
			case 221: return  "Close bracket";
			case 222: return  "Single quote";
			case 223: return "`";
			case 225: return  "Altgr";
			case 230: return  "GNOME Compose Key";
			case 233: return  "XF86Forward";
			case 234: return  "XF86Back";
			case 235: return  "Non-conversion";
			case 240: return  "Alphanumeric";
			case 242: return  "Hiragana/katakana";
			case 243: return  "Half-width/full-width";
			case 244: return  "Kanji";
			case 251: return  "Unlock trackpad";
			default: return "??";
		}
	}	
}

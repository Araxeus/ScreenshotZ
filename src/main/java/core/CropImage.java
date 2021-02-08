package core;


import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.awt.AWTException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import lc.kra.system.keyboard.GlobalKeyboardHook;
import lc.kra.system.keyboard.event.GlobalKeyAdapter;
import lc.kra.system.keyboard.event.GlobalKeyEvent;

@SuppressWarnings({"java:S1161","java:S110", "java:S1186" , "java:S106" , "java:S1948" , "java:S1659"})
public class CropImage extends JFrame implements MouseListener, MouseMotionListener {
	private static final long serialVersionUID = 6969L;
    private boolean isDragged = false;
	private int x1, y1, x2, y2;
	private GlobalKeyboardHook keyboardHook;
	private String imagePath;
	
	public static void main(String[] args) {
		//openWindow("C:\\Users\\Araxeus\\.ScreenshotZ\\Screenshots\\toCrop.png"); //dont run without shutting down keyboardHook at @windowClosed
	}
	
	private CropImage(String imagePath){
		this.keyboardHook=TrayApp.getKeyboardHook();
		this.imagePath=imagePath;
	}
	
	public static void openWindow(String imagePath) {
        TrayApp.setIsCropping(true);
		CropImage window = new CropImage(imagePath);
		window.open();
	}

	private void open() {
		// create Keyboard Listener
		GlobalKeyAdapter exitListener = new GlobalKeyAdapter () {
			@Override
			public void keyPressed(GlobalKeyEvent event) {
				if (event.getVirtualKeyCode() == GlobalKeyEvent.VK_RETURN || event.getVirtualKeyCode() == GlobalKeyEvent.VK_ESCAPE) { //Enter / Escape
					try {
						dispose();
					} catch (Exception e) {
						System.err.println("Exception at print to dir");
						e.printStackTrace();
					}
				}
			}
		}; //add listener to keyboard hook
		keyboardHook.addKeyListener(exitListener);
		
		ImagePanel im = new ImagePanel(imagePath);
		add(im);
		setSize(im.getWidth(), im.getHeight());
		setTitle("Crop Tool - [Press Enter / Escape To Quit]");
        setIconImage(Utils.getImage("TrayIcon.png"));
		addMouseListener(this);
		addMouseMotionListener(this);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
		addWindowListener(new WindowAdapter() {
		@Override
		public void windowClosed(WindowEvent e) {
			keyboardHook.removeKeyListener(exitListener);
            TrayApp.setIsCropping(false);
			//keyboardHook.shutdownHook(); //TODO Delete
			System.out.print("Closed Crop Frame (dont forget to delete hook shutdown on main release)");
			}
		});
	}

	private void draggedScreen() throws IOException, AWTException {
		int width = Math.abs(x2 - x1);
		int height = Math.abs(y2 - y1);
		int x = x1<x2 ? x1: x2 ,
			y = y1<y2 ? y1 : y2 ;
		Robot robot = new Robot();
		BufferedImage img = robot.createScreenCapture(new Rectangle(x, y, width, height));
		StringBuilder cropPath = new StringBuilder(imagePath)
		.insert(imagePath.indexOf(".png"), "(Cropped)");
		File savePath = new File(cropPath.toString());
		ImageIO.write(img, "JPG", savePath);
		System.out.println("Cropped image saved successfully.");
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		repaint();
		x1 = arg0.getX();
		y1 = arg0.getY();
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {		
		if (isDragged) {
			repaint();
			x2 = arg0.getX();
			y2 = arg0.getY();
			try {
				draggedScreen();
				isDragged=false;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		repaint();
		isDragged = true;
		x2 = arg0.getX();
		y2 = arg0.getY();
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {

	}


	public void paint(Graphics g) {
		super.paint(g);
		int width = Math.abs(x2 - x1);
		int height = Math.abs(y2 - y1);
		int x = x1<x2 ? x1: x2 ,
			y = y1<y2 ? y1 : y2 ;
        g.setColor(Color.RED);
		g.drawRect(x, y, width, height);
	}
}

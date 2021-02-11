package core;

import java.awt.Color;
import java.awt.Graphics;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;


import lc.kra.system.keyboard.GlobalKeyboardHook;
import lc.kra.system.keyboard.event.GlobalKeyAdapter;
import lc.kra.system.keyboard.event.GlobalKeyEvent;

@SuppressWarnings({"java:S1161","java:S110", "java:S1186" , "java:S106" , "java:S1948" , "java:S1659"})
public class CropImage extends JFrame implements MouseListener, MouseMotionListener {
	private static final long serialVersionUID = 6969L;

    private boolean isDragged;

	// Start point = (x1,y1) , End point = (x2,y2)
	private int x1, y1, x2, y2;
	//getting keyboard hook from TrayApp
	private GlobalKeyboardHook keyboardHook;
	//used to load image
    ImagePanel im;
    
	private String imagePath;
	
	//private initializer
	private CropImage(String imagePath){
		this.keyboardHook=TrayApp.getKeyboardHook();
		this.imagePath=imagePath;
        isDragged = false;
	}
	
	public static void openWindow(String imagePath) {
		//used to limit instance
        TrayApp.setIsCropping(true);
		//initialize window
		CropImage window = new CropImage(imagePath);
		window.open();
	}

	private void open() {
		// create Keyboard Listener
		GlobalKeyAdapter exitListener = new GlobalKeyAdapter () {
			@Override
			public void keyPressed(GlobalKeyEvent event) {
				//exit on Enter / Escape
				if (event.getVirtualKeyCode() == GlobalKeyEvent.VK_RETURN || event.getVirtualKeyCode() == GlobalKeyEvent.VK_ESCAPE)
						dispose();
			}
		}; //add listener to keyboard hook
		keyboardHook.addKeyListener(exitListener);
		//set paint color
		//setForeground(Color.RED);
		//create ImagePanel and use it
		im = new ImagePanel(imagePath);	
		add(im);
		setUndecorated(true);
		setAlwaysOnTop(true);
		setSize(im.getWidth(), im.getHeight());
		setTitle("Crop Tool - [Press Enter / Escape To Quit]");
        setIconImage(Utils.getImage("TrayIcon.png"));
		setBackground(new Color(0, 255, 0, 0));
		//add overridden listener
		addMouseListener(this);
		addMouseMotionListener(this);
		//dispose this window on exit (not quit app)
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setVisible(true);
		setFocusable(true);
		//request focus when frame is built
		SwingUtilities.invokeLater( () -> {
			requestFocusInWindow();
			toFront();
		});

		//Window Closed Listener
		addWindowListener(new WindowAdapter() {
		@Override
		public void windowClosed(WindowEvent e) {
			//remove key listener
			keyboardHook.removeKeyListener(exitListener);
			//flush image (cautionary)
            im.flush();
			//enable cropping again
            TrayApp.setIsCropping(false);
			System.out.println("Closed Crop Frame");
			}
		});
	}

	//overridden mouse listeners
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
	public void mousePressed(MouseEvent click) {
		//mouse pressed -> get Start point coordinates
		repaint();
		x1 = click.getX();
		y1 = click.getY();
	}

	@Override
	public void mouseReleased(MouseEvent release) {	
		//get End point coordinates and crop
		if (isDragged) {
			repaint();
			x2 = release.getX();
			y2 = release.getY();
			try {
				crop();
				isDragged=false;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		//get current end position for paint()
		repaint();
		isDragged = true;
		x2 = arg0.getX();
		y2 = arg0.getY();
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
	}


	public void paint(Graphics g) {
		//prepare canvas
		super.paint(g);

		//little algorithm to calculate rectangle left most corner
		int width = Math.abs(x2 - x1);
		int height = Math.abs(y2 - y1);
		int x = x1<x2 ? x1: x2 ,
			y = y1<y2 ? y1 : y2 ;
		g.setColor(new Color(211, 84, 0));
		g.drawRect(x, y, width, height);
		//COLORS: 
		//Gold: [255,204,51] (very nice)
		//Light orange [255,153,0] (very similar to gold)
		//Light Gray - White [204,204,204] (niceuh)
		//Pumpkin Orange [211, 84, 0] love orange :D
		//Red - Orange [231, 76, 60] (Alizarin)
		//Dim Emerald [39, 174, 96] (Nephritis)
 
	}

	private void crop() throws IOException {
		//same algorithm (not sure how not to repeat this)
		int width = Math.abs(x2 - x1);
		int height = Math.abs(y2 - y1);
		int x = x1<x2 ? x1 : x2 ,
			y = y1<y2 ? y1 : y2 ;

		//crop to rectangle
		BufferedImage img = im.getImage().getSubimage(x, y, width, height);

		//crop path .can. be new path
		StringBuilder cropPath = new StringBuilder(imagePath);

		//Save original onCrop Setting
		if(Config.FIELD05.getBoolean())
			cropPath.insert(imagePath.indexOf(".png"), "(Cropped)");
		
		//save image
		File savePath = new File(cropPath.toString());
		ImageIO.write(img, "png", savePath);
		TrayApp.setClipboard(new TransferableImage(img));
		System.out.println("Cropped image saved successfully.");

		//Quit onCrop Setting
		if(Config.FIELD06.getBoolean())
			dispose();
    }
}
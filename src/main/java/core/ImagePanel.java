package core;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JPanel;


	@SuppressWarnings ("java:S1948")
	class ImagePanel extends JPanel {
        private static final long serialVersionUID = 6969L;

        private Image img;
		private BufferedImage bfImg;

		  public ImagePanel (String path) {
		    this.img = new ImageIcon(path).getImage();
			try {
				bfImg = ImageIO.read(new File(path));
			} catch (IOException e) {
				e.printStackTrace();
			}
		    Dimension size = new Dimension(img.getWidth(null), img.getHeight(null));
		    setPreferredSize(size);
		    setMinimumSize(size);
		    setMaximumSize(size);
		    setSize(size);
		    setLayout(null);
            getHeight();
		  }

          @Override
		  public void paintComponent (Graphics g) {
		    g.drawImage(img, 0, 0, null);
		  }

          public void flush () {
              img.flush();
          }

		  public BufferedImage getImage () {
			  return bfImg;
		  }
	}
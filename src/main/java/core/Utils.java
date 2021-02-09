package core;

import java.awt.AWTException;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.awt.Rectangle;
import java.awt.Robot;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.Toolkit;

@SuppressWarnings("java:S106")

public class Utils {
    // hide constructor
    private Utils() {
    }

    /*
     * print screenshot to 'directory'
     */
    public static void robotTo(String directory, int mode) throws IOException, AWTException {
        // create buffered image from new rectangle containing all screen
        BufferedImage img = new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
        // create file using getName (returns new image path)
        File outfile = new File(getName(directory));
        // write image to file
        ImageIO.write(img, "png", outfile);
        System.out.println("image made from robot to: " + outfile.getAbsolutePath());
        // flush buffered image
        img.flush();
        // Call garbage collector (temporary fix to memory leak from this method)
        Runtime.getRuntime().gc();
        //run crop if not cropping and [mode1+config03](PrtScn mode+config) / [mode2+config04](Custom keybind mode+config)
        if (!TrayApp.isCropping()
                &&( (mode == 1 && Config.FIELD03.getBoolean())
                ||  (mode == 2 && Config.FIELD04.getBoolean()) ) )
            CropImage.openWindow(outfile.getAbsolutePath());
    }

    /*
     * print image from clipboard to 'directory'
     */
    public static void clipboardTo(String directory) throws Exception {
        // grab clipboard
        Transferable content = TrayApp.getClipboard();
        // check thats its an image
        if (!isImage(content))
            return;
		// reset clipboard content - so that listener can notice new screenshot
		TrayApp.setClipboard(new StringSelection(""));
        // create buffered image from content
        BufferedImage img = (BufferedImage) content.getTransferData(DataFlavor.imageFlavor);
        // create file using getName (returns new image path)
        File outfile = new File(getName(directory));
        // write image to file
        ImageIO.write(img, "png", outfile);
        System.out.println("image copied from clipboard to: " + outfile.getAbsolutePath());
        // flush buffered image
        img.flush();
        // Call garbage collector (temporary fix to memory leak from this method)
        Runtime.getRuntime().gc();
        //run crop if not cropping + config04
        if (!TrayApp.isCropping() && Config.FIELD03.getBoolean() )
            CropImage.openWindow(outfile.getAbsolutePath());
    }

    /*
     * return new file path string using 'directory'
     */
    public static String getName(String directory) {
        // create date format
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH.mm.ss");
        // get current date
        LocalDateTime now = LocalDateTime.now();

        System.out.println(directory + "  " + dtf.format(now));
        // try new name
        String name = directory + dtf.format(now);
        File tmpDir = new File(name + ".png");
        // create (num) suffix in case file already exist
        byte num = 0;
        while (tmpDir.exists()) {
            num++;
            tmpDir = new File(name + " (" + num + ").png");
        }
        return tmpDir.getAbsolutePath(); // returns new path
    }

    /*
     * check if transferable from clipboard is an image
     */
    public static boolean isImage(Transferable content) {
        // check that content exist
        if (content == null) {
            System.err.println("nothing found in clipboard");
            return false;
        } // check that content is an image
        if (!content.isDataFlavorSupported(DataFlavor.imageFlavor)) {
            System.err.println("no image found in clipboard");
            return false;
        }
        return true;
    }

    /*
     * load BufferedImage from resources inputStream
     */
    public static BufferedImage getImage(String name) {
        BufferedImage img = null;
        try (InputStream inputStream = TrayApp.class.getClassLoader().getResourceAsStream(name)) {
            img = ImageIO.read(inputStream);
            while (img.getWidth(null) == -1)
                Thread.sleep(50);
        } catch (Exception e) {
            System.err.println("Error Loading TrayIcon");
            e.getMessage();
            System.exit(1);
        }
        return img;
    }

}

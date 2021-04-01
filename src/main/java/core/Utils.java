package core;

import java.awt.AWTException;
import java.awt.image.BufferedImage;
import java.awt.Rectangle;
import java.awt.Robot;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;
import java.awt.Toolkit;

@SuppressWarnings ("java:S106")

public class Utils {
    // hide constructor
    private Utils () {
    }

    /*
     * print screenshot to 'directory'
     */
    public static void robotTo (String directory, int mode) throws IOException, AWTException {
        // create buffered image from new rectangle containing all screen
        BufferedImage img = new Robot().createScreenCapture(
            new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
        //check if newFolder setting is on
        if (Config.FIELD09.getBoolean()) {
            directory = addDateToDir(directory);
        }
        // create file using getName (returns new image path)
        if (Files.notExists(Paths.get(directory)))
            Files.createDirectories(Paths.get(directory));

        File outfile = new File(getName(directory));
        // write image to file
        ImageIO.write(img, "png", outfile);
        System.out.println("image made from robot to: " + outfile.getAbsolutePath());
        // flush buffered image
        img.flush();
        // Call garbage collector (temporary fix to memory leak from this method)
        Runtime.getRuntime().gc();
        //run crop if not cropping and [mode1+config03](PrtScn mode+config) / [mode2+config04](Custom keybind mode+config)
        if (!TrayApp.isCropping() && (  
                mode ==3
                ||  (mode == 1 && Config.FIELD03.getBoolean())
                ||  (mode == 2 && Config.FIELD04.getBoolean()) 
                                     ))
            CropImage.openWindow(outfile.getAbsolutePath());
    }

    public static String addDateToDir (String dir) {
        LocalDateTime now = LocalDateTime.now();
        //new day starts at 5am ;)
        if (now.getHour()<5)
            now=now.minusDays(1);
        return dir+now.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))+File.separator;
    }

    /*
     * return new file path string using 'directory'
     */
    public static String getName (String directory) {
        // create date format
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH.mm.ss");
        // get current date
        LocalDateTime now = LocalDateTime.now();

        System.out.println(directory + "  " + dtf.format(now));
        // try new name
        String name = directory + dtf.format(now);
        File tmpDir = new File(name + ".png");
        // create (num) suffix in case file already exist
        for (int i = 0; tmpDir.exists(); i++) 
            tmpDir = new File(name + "("+i+").png");      
        return tmpDir.getAbsolutePath(); // returns new path
    }

    /*
     * load BufferedImage from resources inputStream
     */
    public static BufferedImage getImage (String name) {
        BufferedImage img = null;
        try (InputStream inputStream = TrayApp.class.getClassLoader().getResourceAsStream(name)) {
            img = ImageIO.read(inputStream);
            while (img.getWidth(null) == -1)
                Thread.sleep(50);
        } catch (Exception e) {
            System.err.println("Error Loading TrayIcon \n"
                + e.getMessage());
            System.exit(1);
        }
        return img;
    }

    /*
    * Check if directory is empty
    */
    public static boolean isDirEmpty(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (Stream<Path> entries = Files.list(path)) {
                return !entries.findFirst().isPresent();
            }
        } else {
            System.err.println("Error - Path is not directory: "+System.lineSeparator()+path.toString());
        }
            
        return false;
    }

}

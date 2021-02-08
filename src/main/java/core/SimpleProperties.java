package core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

@SuppressWarnings({ "java:S106", "java:S1659" })

public class SimpleProperties {
    public static final String 
        DEFAULT_PATH = System.getProperty("user.home") + File.separator + ".ScreenshotZ" + File.separator + "config.xml", 
        FIELD01 = "Screenshot Dir",
        FIELD01_DEFAULT_VALUE = new File(DEFAULT_PATH).getParent() + File.separator + "Screenshots" + File.separator,
        FIELD02 = "Keybind", 
        FIELD02_DEFAULT_VALUE = "0" ,
        FIELD03 = "Crop on PrintScreen" ,
        FIELD03_DEFAULT_VALUE = "false" ,
        FIELD04 = "Crop on Alternate Keybind" ,
        FIELD04_DEFAULT_VALUE = "true";

    // config always has updated keybind array

    int[] keybind;

    private String propertiesFilePath;

    private Properties properties;

    // PUBLIC CONSTRUCTOR create config at default path
    public SimpleProperties() {
        this(DEFAULT_PATH);
    }

    // PUBLIC CONSTRUCTOR create config
    public SimpleProperties(String path) {
        propertiesFilePath = path;
        properties = new Properties();
        // checkPath create/check propertiesFilePath - return false if failed
        if (checkPath())
            try (FileInputStream in = new FileInputStream(propertiesFilePath)) {
                // load properties from xml
                properties.loadFromXML(in);
            } catch (InvalidPropertiesFormatException e) {
                System.err.println("Format error on config.xml -> writing default config");
            } catch (IOException e) {
                System.err.println("RARE Error Opening FileInputStream");
            } finally {
                // create missing properties and set to default - returns false if nothing changed
                if (updateProperties())
                    store();
            }
        else
            System.err.println("oh no.. config wasn't loaded");
    }

    // PUBLIC set property
    public void setProperty(String key, String value) {
        // check that new value is different from old value
        if (!properties.getProperty(key).equals(value)) {
            properties.setProperty(key, value);
            store();
            // update keybind array if FIELD02 was updated
            if (key.equals(FIELD02)) {
                keybind = getKeybind();
            }
        } else
            System.err.println("Trying to set property '" + key + "' to the same value (" + value + ")");
    }

    // PUBLIC get property
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public boolean getBooleanProperty(String key) {
        return properties.getProperty(key).equalsIgnoreCase("true");
    }

    // int[] keybind updater
    private int[] getKeybind() {
        // new String array from splitting property around ','
        String[] temp = properties.getProperty(FIELD02).split(",");
        // convert String array to int array
        int[] output = new int[temp.length];
        for (int i = 0; i < temp.length; i++)
            output[i] = Integer.parseInt(temp[i]);
        return output;
    }

    // store data to config file
    private void store() {
        try (FileOutputStream outStream = new FileOutputStream(propertiesFilePath)) {
            properties.storeToXML(outStream, "ScreenshotZ Program parameters");
            System.out.println("Stored properties :" + properties.toString());
        } catch (IOException e) {
            System.err.println("IOException");
            e.printStackTrace();
        }
    }

    // create missing properties and set to default - returns false if nothing changed
    private boolean updateProperties() {
        boolean changed = false;
        // SCREENSHOT DIRECTORY
        if (!properties.containsKey(FIELD01)) {
            properties.setProperty(FIELD01, FIELD01_DEFAULT_VALUE);
            changed = true;
            System.err.println("Properties didn't contain " + FIELD01);
            // initialize default screenshot directory if it doesn't exist
            try {
                if (Files.notExists(Paths.get(FIELD01_DEFAULT_VALUE))) {
                    Files.createDirectories(Paths.get(FIELD01_DEFAULT_VALUE));
                }
            } catch (IOException e) {
                System.err.println("couldn't create default screenshot dir");
            }
        }
        // KEYBIND SEQUENCE
        if (!properties.containsKey(FIELD02) || properties.getProperty(FIELD02).equals("")) {
            properties.setProperty(FIELD02, FIELD02_DEFAULT_VALUE);
            changed = true;
            System.err.println("Properties didn't contain " + FIELD02);
        }
        // initialize keybind[]
        if (keybind == null)
            keybind = getKeybind();
        // Crop on Screenshot
        if (!properties.containsKey(FIELD03)) {
            properties.setProperty(FIELD03, FIELD03_DEFAULT_VALUE);
            changed = true;
            System.err.println("Properties didn't contain " + FIELD03);
        }
        // Crop on Additional Keybind
        if (!properties.containsKey(FIELD04)) {
            properties.setProperty(FIELD04, FIELD04_DEFAULT_VALUE);
            changed = true;
            System.err.println("Properties didn't contain " + FIELD04);
        }
        return changed;
    }

    // check path to config.xml
    private boolean checkPath() {
        File config = new File(propertiesFilePath);
        try {
            if (Files.notExists(config.toPath())) {
                // create path to config if it doesn't exist (create directories)
                Files.createDirectories(config.toPath().getParent());
                // create config.txt
                if (!config.createNewFile()) {
                    System.err.println("Error creating file");
                    return false;
                }
            }
            return true;
        } catch (IOException e) {
            System.err.println("IO Exception");
            return false;
        }
    }
}
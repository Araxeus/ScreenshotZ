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

enum Config 
        { 
            FIELD01 ("Screenshot Dir", new File(SimpleProperties.DEFAULT_PATH).getParent() + File.separator + "Screenshots" + File.separator) , 
            FIELD02 ("Keybind", "0") ,
            FIELD03 ("Crop on PrintScreen", "false") ,
            FIELD04 ("Crop on Alternate Keybind", "true");
        
            final String DEFAULT_VALUE ,   
                         KEY;
            // enum constructor called separately for each 
            // constant 
            private Config (String key, String defaultValue) 
            { 
                this.KEY = key;
                this.DEFAULT_VALUE = defaultValue;
            } 
        
            public void setValue (String newValue) 
            { 
                SimpleProperties.getInstance().setProperty(this, newValue); 
            }

            public String getString () 
            { 
                return SimpleProperties.getInstance().getProperty(this); 
            }

            public boolean getBoolean () {
                return SimpleProperties.getInstance().getBooleanProperty(this);
            }

            public static int[] getKeybinds () {
                return SimpleProperties.getInstance().getKeybinds();
            }


        } 

public class SimpleProperties {

    public static final String DEFAULT_PATH = System.getProperty("user.home") + File.separator + ".ScreenshotZ" + File.separator + "config.xml";   

    private static SimpleProperties instance;
    // config always has updated keybind array

    private int[] keybind;

    private String propertiesFilePath;

    private Properties properties;

    // PUBLIC singleton instance getter
    public static SimpleProperties getInstance() {
        if(instance == null) {
            instance = new SimpleProperties(DEFAULT_PATH);
            System.out.println("created new properties instance");
        }
        return instance;
    }

    // PRIVATE CONSTRUCTOR create config
    private SimpleProperties(String path) {
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
            if (key.equals(Config.FIELD02.KEY)) {
                keybind = getKeybind();
            }
        } else
            System.err.println("Trying to set property '" + key + "' to the same value (" + value + ")");
    }

    public void setProperty(Config field, String value) {
        setProperty(field.KEY , value);
    }

    // PUBLIC get property
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getProperty(Config field) {
        return getProperty(field.KEY);
    }

    public boolean getBooleanProperty(String key) {
        return properties.getProperty(key).equalsIgnoreCase("true");
    }

    public boolean getBooleanProperty(Config field) {
        return getBooleanProperty(field.KEY);
    }

    public int[] getKeybinds(){
        return keybind.clone();
    }

    // int[] keybind updater
    private int[] getKeybind() {
        // new String array from splitting property around ','
        String[] temp = properties.getProperty(Config.FIELD02.KEY).split(",");
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

        for(Config field : Config.values()) {
            if (!properties.containsKey(field.KEY)) {
                properties.setProperty(field.KEY, field.DEFAULT_VALUE);
                changed = true;
                System.err.println("Properties didn't contain " + field.KEY);
            }
        }
        //check that screenshot dir exit and create if needed
            try {
                if (Files.notExists(Paths.get(properties.getProperty(Config.FIELD01.KEY)))) {
                    System.out.println("Creating screenshot directory");
                    Files.createDirectories(Paths.get(properties.getProperty(Config.FIELD01.KEY)));
                }
            } catch (IOException e) {
                System.err.println("couldn't create default screenshot dir");
            }
        
            // initialize keybind[]
        if (keybind == null)
            keybind = getKeybind();

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
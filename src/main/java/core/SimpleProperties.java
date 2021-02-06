package core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;


@SuppressWarnings({"java:S106","java:S1659"})

public class SimpleProperties
{   // default path
    public static final String 
    DEFAULT_PATH = System.getProperty("user.home") + File.separator + ".ScreenshotZ" + File.separator + "config.xml" ,
    FIELD01 = "Screenshot Dir" ,
    FIELD01_DEFAULT_VALUE = new File(DEFAULT_PATH).getParent() + File.separator + "Screenshots" + File.separator;

    private String propertiesFilePath;
    private Properties properties;

    public SimpleProperties() {
        this(DEFAULT_PATH);
    }

    public SimpleProperties(String path) {
        propertiesFilePath = path;
        properties = new Properties();
        if(checkPath())
            try (FileInputStream in = new FileInputStream(propertiesFilePath))
            {
                properties.loadFromXML(in);
                if(!isValidProperties())
                    throw new InvalidPropertiesFormatException("Missing Keys in Config");
            } catch (InvalidPropertiesFormatException e) {
                System.err.println("Format error on config.xml -> writing default config");
                setDefault();
            } catch (IOException e) {
                System.err.println("RARE Error Opening FileInputStream");
            }
         else
            System.err.println("oh no.. config wasn't loaded");      
    }

    private boolean isValidProperties() {
        return properties.containsKey(FIELD01);
    }

    public void setProperty(String key, String value){
        if(!properties.getProperty(key).equals(value)) {
        properties.setProperty(key, value);
        store();
        } else
             System.err.println("Trying to set property '"+key+"' to the same value ("+value+")");
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    private void store() {
        try(FileOutputStream outStream = new FileOutputStream(propertiesFilePath)) {
        properties.storeToXML(outStream , "ScreenshotZ Program parameters");
        System.err.println("Stored properties :"+properties.toString()); //?
        } catch (IOException e) {
            System.err.println("IOException");
			e.printStackTrace();
        }
    }

    protected void setDefault() {
        properties.setProperty(FIELD01, FIELD01_DEFAULT_VALUE);
        store();
    }

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
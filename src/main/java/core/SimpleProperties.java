package core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;


@SuppressWarnings("java:S106")

public class SimpleProperties
{   // default path
    private static final String DEFAULT_PATH = System.getProperty("user.home") + File.separator + ".ScreenshotZ" + File.separator + "config.xml";
    
    private String propertiesFilePath;
    private Properties properties;

    public SimpleProperties() throws IOException{
        this(DEFAULT_PATH);
    }

    public SimpleProperties(String path) throws IOException
    {
        propertiesFilePath = path;
        properties = new Properties();
        if(checkPath())
            try (FileInputStream in = new FileInputStream(propertiesFilePath))
            {
                properties.loadFromXML(in);
            } catch (InvalidPropertiesFormatException e) {
                System.err.println("Format error on config.xml -> writing default config");
                setDefault();
                store();
            }
         else
             throw new IOException("Error creating new config.xml");      
    }

    public void setProperty(String key, String value) throws IOException
    {
        properties.setProperty(key, value);

        store();
    }

    public String getProperty(String key)
    {
        return properties.getProperty(key);
    }

    private void store() throws IOException
    {
        try(FileOutputStream outStream = new FileOutputStream(propertiesFilePath)) {
        properties.storeToXML(outStream , "ScreenshotZ Program parameters");
        } catch (IOException e) {
            System.err.println("IOException");
			e.printStackTrace();
        }
    }

    private void setDefault() {
        properties.setProperty("Screenshot Dir", new File(propertiesFilePath).getParent() + "Screenshots" + File.separator);
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
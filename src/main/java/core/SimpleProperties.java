package core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;


@SuppressWarnings({"java:S106","java:S1659"})

public class SimpleProperties
{   // default path
    public static final String 
    DEFAULT_PATH = System.getProperty("user.home") + File.separator + ".ScreenshotZ" + File.separator + "config.xml" ,
    FIELD01 = "Screenshot Dir" ,
    FIELD01_DEFAULT_VALUE = new File(DEFAULT_PATH).getParent() + File.separator + "Screenshots" + File.separator ,
    FIELD02 = "Keybind" ,
    FIELD02_DEFAULT_VALUE = "0";

    int[] keybind;

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
                if(updateProperties())
                    store();
            } catch (InvalidPropertiesFormatException e) {
                System.err.println("Format error on config.xml -> writing default config");
                if(updateProperties())
                    store();
            } catch (IOException e) {
                System.err.println("RARE Error Opening FileInputStream");
            }
         else
            System.err.println("oh no.. config wasn't loaded");      
    }



    public void setProperty(String key, String value){
        if(!properties.getProperty(key).equals(value)) {
        properties.setProperty(key, value);
        store();
        //update keybind array if settings was updated
            if(key.equals(FIELD02)){
                keybind=getKeybind();
            }
        } else
             System.err.println("Trying to set property '"+key+"' to the same value ("+value+")");
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    private int[] getKeybind() {
        String[] temp = properties.getProperty(FIELD02).split(",");
        int[] output = new int[temp.length];
        for (int i=0; i<temp.length; i++)
            output[i]=Integer.parseInt(temp[i]);
        return output;
    }

    private void store() {
        try(FileOutputStream outStream = new FileOutputStream(propertiesFilePath)) {
        properties.storeToXML(outStream , "ScreenshotZ Program parameters");
        System.out.println("Stored properties :"+properties.toString()); //?
        } catch (IOException e) {
            System.err.println("IOException");
			e.printStackTrace();
        }
    }

    private boolean updateProperties() {
        boolean changed = false;   
        if(!properties.containsKey(FIELD01)) {
            properties.setProperty(FIELD01, FIELD01_DEFAULT_VALUE);
            changed = true;
        //initialize default screenshot directory 
            try{
                if(Files.notExists(Paths.get(FIELD01_DEFAULT_VALUE))){
                    Files.createDirectories(Paths.get(FIELD01_DEFAULT_VALUE));
                }
            } catch (IOException e) {
                System.err.println("couldn't create default screenshot dir");
            }
            System.err.println("Properties didn't contain "+FIELD01);
        }
        if(!properties.containsKey(FIELD02) || properties.getProperty(FIELD02).equals("")) {
            properties.setProperty(FIELD02, FIELD02_DEFAULT_VALUE);
            changed = true;
            System.err.println("Properties didn't contain "+FIELD02);
        }
        //initialize keybind
        if(keybind == null)
            keybind = getKeybind();
        return changed;
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
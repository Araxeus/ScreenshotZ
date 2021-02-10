package core;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit test for simple App.
 */
public class AppTest {
    /**
     * Rigorous Test.
     */
    @Test
    public void testApp(){
        getNames(false);
    }
    
    public void getNames(boolean showMore) {
        assertTrue(true);
        for (Config field : Config.values()) {
            System.out.println(field + " = " + field.KEY);
            if(showMore) {
                System.out.println("\t Current Settings = " + field.getString() );
                if(!field.getString().equals(field.DEFAULT_VALUE))
                    System.out.println("\t Default Setting is - "+field.DEFAULT_VALUE);  
            //assertEquals("",field.DEFAULT_VALUE, field.getString());
            }
        }
    }


}

package core;

import org.junit.Test;
import static org.junit.Assert.*;

import org.lwjgl.PointerBuffer;
import org.lwjgl.util.nfd.NativeFileDialog;
import org.lwjgl.system.MemoryUtil;
/**
 * Unit test for simple App.
 */
public class AppTest {
    /**
     * Rigorous Test.
     */
    @Test
    public void testApp(){
        assertTrue(true);
        //printConfigFields(false);
        
        openWindow();
    }

    public void openWindow(){
        
    }
    
    public void printConfigFields(boolean showMore) {
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

    
    public void lwjgl_nfd(){
        assertTrue(true);
        PointerBuffer out = MemoryUtil.memAllocPointer(1);
		int result = NativeFileDialog.NFD_PickFolder(Config.FIELD01.getString() , out);
		checkResult(result, out);

    }

    private  void checkResult(int result, PointerBuffer path) {
        switch (result) {
            case NativeFileDialog.NFD_OKAY:
                System.out.println("Success!");
                System.out.println(path.getStringUTF8(0));
                NativeFileDialog.nNFD_Free(path.get(0));
                break;
            case NativeFileDialog.NFD_CANCEL:
                System.out.println("User pressed cancel.");
                break;
            default: // NFD_ERROR
                System.err.format("Error: %s\n", NativeFileDialog.NFD_GetError());
        }
    }   

}

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
    public void testApp() {
        assertTrue(true);
        byte shot = 1;
        System.out.println(shot==1);
        for(Persons person : Persons.values()) {
            System.out.println(person.ID);
        }
    }
}

enum Persons 
{ 
    GAL, AVI, DOR; 
  
    final String DEFAULT_VALUE;
    // enum constructor called separately for each 
    // constant 
    private Persons(String val) 
    { 
        DEFAULT_VALUE = val;
        this.toString();
    } 
  
    public void colorInfo() 
    { 
        System.out.println("Universal Color"); 
    } 
} 

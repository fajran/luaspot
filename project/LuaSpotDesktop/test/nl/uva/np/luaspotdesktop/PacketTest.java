/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.uva.np.luaspotdesktop;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author iang
 */
public class PacketTest {

    public PacketTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getId method, of class Packet.
     */
    @Test
    public void testGetId() {
        System.out.println("getId");
        Packet instance = new Packet("id", "1", "src", "dst", "data");
        String expResult = "id";
        String result = instance.getId();
        assertEquals(expResult, result);
    }

    /**
     * Test of getMaxHop method, of class Packet.
     */
    @Test
    public void testGetMaxHop() {
        System.out.println("getMaxHop");
        Packet instance = new Packet("id", "100", "src", "dst", "data");
        int expResult = 100;
        int result = instance.getMaxHop();
        assertEquals(expResult, result);
    }

    /**
     * Test of getSrc method, of class Packet.
     */
    @Test
    public void testGetSrc() {
        System.out.println("getSrc");
        Packet instance = new Packet("id", "1", "src", "dst", "data");
        String expResult = "src";
        String result = instance.getSrc();
        assertEquals(expResult, result);
    }

    /**
     * Test of getDst method, of class Packet.
     */
    @Test
    public void testGetDst() {
        System.out.println("getDst");
        Packet instance = new Packet("id", "1", "src", "dst", "data");
        String expResult = "dst";
        String result = instance.getDst();
        assertEquals(expResult, result);
    }

    /**
     * Test of getData method, of class Packet.
     */
    @Test
    public void testGetData() {
        System.out.println("getData");
        Packet instance = new Packet("id", "1", "src", "dst", "data");
        String expResult = "data";
        String result = instance.getData();
        assertEquals(expResult, result);
    }

    /**
     * Test of getMessage method, of class Packet.
     */
    @Test
    public void testGetMessage() {
        System.out.println("getMessage");
        Packet instance = new Packet("id", "123", "src", "dst", "data");
        String expResult = "id 123 src dst data";
        String result = instance.getMessage();
        assertEquals(expResult, result);
    }

    /**
     * Test of parse method, of class Packet.
     */
    @Test
    public void testParse() {
        System.out.println("parse");
        Packet instance = Packet.parse("id 345 src dst data");
        Packet expResult = new Packet("id", "345", "src", "dst", "data");
        assertEquals(expResult.getData(), instance.getData());
        assertEquals(expResult.getDst(), instance.getDst());
        assertEquals(expResult.getSrc(), instance.getSrc());
        assertEquals(expResult.getId(), instance.getId());
        assertEquals(expResult.getMaxHop(), instance.getMaxHop());
        assertEquals(expResult.getMessage(), instance.getMessage());
    }

}
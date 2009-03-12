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
public class RPCMessageTest {

    public RPCMessageTest() {
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
     * Test of getMessage method, of class RPCMessage.
     */
    @Test
    public void testGetMessage() {
        System.out.println("getMessage");
        RPCMessage instance = new RPCMessage("app", "func", "satu", "dua");
        String expResult = "c app func satu dua";
        String result = instance.getMessage();
        assertEquals(expResult, result);
    }

    /**
     * Test of getApp method, of class RPCMessage.
     */
    @Test
    public void testGetApp() {
        System.out.println("getApp");
        RPCMessage instance = new RPCMessage("app", "func", "satu", "dua");
        String expResult = "app";
        String result = instance.getApp();
        assertEquals(expResult, result);
    }

    /**
     * Test of getFunc method, of class RPCMessage.
     */
    @Test
    public void testGetFunc() {
        System.out.println("getFunc");
        RPCMessage instance = new RPCMessage("app", "func", "satu", "dua");
        String expResult = "func";
        String result = instance.getFunc();
        assertEquals(expResult, result);
    }

    /**
     * Test of getParam method, of class RPCMessage.
     */
    @Test
    public void testGetParam() {
        System.out.println("getParam");
        RPCMessage instance = new RPCMessage("app", "func", "satu", "dua");
        String expResult = "satu dua";
        String result = instance.getParam();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetMessagePacket() {
        System.out.println("getMessagePacket");
        RPCMessage instance = new RPCMessage("router", "route", (new Packet("id", 123, "src", "dst", new RPCMessage("app", "func", "satu", "dua"))).getMessage());
        String expResult = "c router route id 123 src dst c app func satu dua";
        String result = instance.getMessage();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetParamPacket() {
        System.out.println("getParamPacket");
        RPCMessage instance = new RPCMessage("router", "route", (new Packet("id", 123, "src", "dst", new RPCMessage("app", "func", "satu", "dua"))).getMessage());
        String expResult = "id 123 src dst c app func satu dua";
        String result = instance.getParam();
        assertEquals(expResult, result);
    }

    /**
     * Test of parse method, of class RPCMessage.
     */
    @Test
    public void testParse() {
        System.out.println("parse");
        RPCMessage instance = RPCMessage.parse("c app func satu dua");
        RPCMessage expResult = new RPCMessage("app", "func", "satu", "dua");
        assertEquals(expResult.getApp(), instance.getApp());
        assertEquals(expResult.getFunc(), instance.getFunc());
        assertEquals(expResult.getMessage(), instance.getMessage());
        assertEquals(expResult.getParam(), instance.getParam());
    }

    @Test
    public void testParse2() {
        System.out.println("parse2");
        RPCMessage instance = RPCMessage.parse("c app func");
        RPCMessage expResult = new RPCMessage("app", "func");
        assertEquals(expResult.getApp(), instance.getApp());
        assertEquals(expResult.getFunc(), instance.getFunc());
        assertEquals(expResult.getMessage(), instance.getMessage());
        assertEquals(expResult.getParam(), instance.getParam());
    }

}
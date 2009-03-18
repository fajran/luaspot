/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.uva.np.luaspot;

import com.sun.spot.util.Utils;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;
import javax.microedition.io.DatagramConnection;
import se.krka.kahlua.stdlib.BaseLib;
import se.krka.kahlua.vm.JavaFunction;
import se.krka.kahlua.vm.LuaCallFrame;
import se.krka.kahlua.vm.LuaState;
import se.krka.kahlua.vm.LuaTable;

/**
 *
 * @author iang
 */
public class LuaSpotLib implements JavaFunction {

    private static final String DEFAULT_MAX_HOP = "10";

    public static final String[] NAMES = {
        "send",
        "send_raw",
        "add_id",
        "check_id",
        "get_new_id",
        "get_node_addr",
        "get_pkt_src",
        "mem_set",
        "mem_get",
        "dispatch",
        "lock",
        "unlock",
        "next_token"
    };

    public static final int SEND = 0;                   // implemented
    public static final int SEND_RAW = 1;                   // implemented
    public static final int ADD_ID = 2;                   // implemented
    public static final int CHECK_ID = 3;                   // implemented
    public static final int GET_NEW_ID = 4;                   // implemented
    public static final int GET_NODE_ADDR = 5;
    public static final int GET_PKT_SRC = 6;                   // implemented
    public static final int MEM_SET = 7;                   // implemented, tested
    public static final int MEM_GET = 8;                   // implemented, tested
    public static final int DISPATCH = 9;                   // implemented
    public static final int LOCK = 10;
    public static final int UNLOCK = 11;
    public static final int NEXT_TOKEN = 12;

    public static final int NUM_FUNCTIONS = NAMES.length;

    private int index;
    private static LuaSpotLib[] functions;

    private static Vector ids = new Vector();
    private static Hashtable mem = new Hashtable();
    private static Hashtable locks = new Hashtable();
    private static Object locksLock = new Object();
    private static int msgCounter = 0;

    private static String nodeAddress = "";

    public static void register(LuaState state) {
        initFunctions();
        LuaTable sunspot = new LuaTable();
        state.getEnvironment().rawset("luaspot", sunspot);

        for (int i=0; i<NUM_FUNCTIONS; i++) {
            sunspot.rawset(NAMES[i], functions[i]);
        }
    }

    private static synchronized void initFunctions() {
        if (functions == null) {
            functions = new LuaSpotLib[NUM_FUNCTIONS];
            for (int i=0; i<NUM_FUNCTIONS; i++) {
                functions[i] = new LuaSpotLib(i);
            }
        }
    }

    public static void setNodeAddress(String address) {
        nodeAddress = address;
    }

    public static String getNodeAddress() {
        return nodeAddress;
    }

    public synchronized static String getNewMessageId() {
        return getNodeAddress() + (msgCounter++);
    }

    public static Object sendLock = new Object();

    public static void send(final String address, final String data) {

        new Thread() {

            public void run() {

                synchronized (sendLock) {
                    
                    // We create a DatagramConnection
                    DatagramConnection dgConnection = null;
                    Datagram dg = null;

                    try {
                        // The Connection is a broadcast so we specify it in the creation string
                        dgConnection = (DatagramConnection) Connector.open("radiogram://" + address + ":37");
                        // Then, we ask for a datagram with the maximum size allowed
                        dg = dgConnection.newDatagram(dgConnection.getMaximumLength());
                    } catch (IOException ex) {
                        System.out.println("Could not open radiogram connection");
                        ex.printStackTrace();
                        return;
                    }

                    try {
                        // We send the message (UTF encoded)
                        System.out.println("[sender] Sending data to " + address + ", len=" + data.length());
//                        System.out.println("[sender] data=" + data);
                        dg.reset();

                        char[] cdata = data.toCharArray();
                        byte[] bdata = new byte[cdata.length];
                        for (int i=0; i<cdata.length; i++) {
                            bdata[i] = (byte)cdata[i];
                        }

                        int sum=0;
                        for (int i=0; i<bdata.length; i++) {
                            sum = (sum + bdata[i]) % 16777216;
                        }
                        System.out.println("[sender] sum=" + sum);


                        dg.write(bdata);
                        dgConnection.send(dg);
                        dgConnection.close();
                        System.out.println("sent.");

                    }
                    catch (IOException ex) {
                        ex.printStackTrace();
                    }

                }
            }
        }.start();
    }


    public LuaSpotLib(int index) {
        this.index = index;
    }

    public int call(LuaCallFrame callFrame, int nArguments) {
        switch (index) {
            case SEND:
                return send(callFrame, nArguments);
            case SEND_RAW:
                return sendRaw(callFrame, nArguments);
            case ADD_ID:
                return addId(callFrame, nArguments);
            case CHECK_ID:
                return checkId(callFrame, nArguments);
            case GET_NEW_ID:
                return getNewId(callFrame, nArguments);
            case GET_NODE_ADDR:
                return getNodeAddr(callFrame, nArguments);
            case MEM_SET:
                return memSet(callFrame, nArguments);
            case MEM_GET:
                return memGet(callFrame, nArguments);
            case DISPATCH:
                return dispatch(callFrame, nArguments);
            case NEXT_TOKEN:
                return nextToken(callFrame, nArguments);
            case LOCK:
                return lock(callFrame, nArguments);
            case UNLOCK:
                return unlock(callFrame, nArguments);
        }
        return 0;
    }

    private int send(LuaCallFrame callFrame, int nArguments) {
        BaseLib.luaAssert(nArguments >= 2, "Not enough arguments");
        String dst = (String)callFrame.get(0);
        String msg = (String)callFrame.get(1);

        String id = getNewMessageId();
        String maxhop = DEFAULT_MAX_HOP;
        String src = getNodeAddress();

        if (nArguments >= 3) {
            maxhop = "" + (int)(((Double)callFrame.get(2)).doubleValue());
        }

        StringBuffer sb = new StringBuffer();
        sb.append("c router route ");
        sb.append(id);
        sb.append(" ");
        sb.append(maxhop);
        sb.append(" ");
        sb.append(src);
        sb.append(" ");
        sb.append(dst);
        sb.append(" ");
        sb.append(msg);

        send(dst, sb.toString());

        return 0;
    }

    private int sendRaw(LuaCallFrame callFrame, int nArguments) {
        BaseLib.luaAssert(nArguments >= 2, "Not enough arguments");
        String dst = (String)callFrame.get(0);
        String msg = (String)callFrame.get(1);

        send(dst, msg);

        return 0;
    }

    private int addId(LuaCallFrame callFrame, int nArguments) {
        BaseLib.luaAssert(nArguments >= 1, "Not enough arguments");
        String id = (String)callFrame.get(0);
        addId(id);
        return 0;
    }

    public static void addId(String id) {
        synchronized (ids) {
            ids.addElement(id);
        }
    }

    private int checkId(LuaCallFrame callFrame, int nArguments) {
        BaseLib.luaAssert(nArguments >= 1, "Not enough arguments");
        String id = (String)callFrame.get(0);
        synchronized (ids) {
            if (ids.indexOf(id) == -1) {
                callFrame.push(Boolean.FALSE);
            }
            else {
                callFrame.push(Boolean.TRUE);
            }
        }
        return 1;
    }

    private int getNewId(LuaCallFrame callFrame, int nArguments) {
        BaseLib.luaAssert(nArguments >= 1, "Not enough arguments");
        callFrame.push(getNewMessageId());
        return 1;
    }

    private int getNodeAddr(LuaCallFrame callFrame, int nArguments) {
        callFrame.push(getNodeAddress());
        return 1;
    }

    private int memSet(LuaCallFrame callFrame, int nArguments) {
        BaseLib.luaAssert(nArguments >= 3, "Not enough arguments");

        String app = (String)callFrame.get(0);
        String var = (String)callFrame.get(1);
        Object data = callFrame.get(2);

        memSet(app, var, data);
        
        return 0;
    }

    public static void memSet(String app, String var, Object data) {
        String key = app +"_" + var;

        System.out.println("[luaspot] memset: key=" + key + ", data.hash=" + data.hashCode());

        synchronized (mem) {
            mem.put(key, data);
        }
    }

    private int memGet(LuaCallFrame callFrame, int nArguments) {
        BaseLib.luaAssert(nArguments >= 2, "Not enough arguments");

        String app = (String)callFrame.get(0);
        String var = (String)callFrame.get(1);

        callFrame.push(memGet(app, var));

        return 1;
    }

    public static Object memGet(String app, String var) {
        String key = app +"_" + var;
        Object data = null;

        synchronized (mem) {
            data = mem.get(key);
        }

        System.out.println("[luaspot] memget: key=" + key + ", data.hash=" + data.hashCode());

        return data;
    }

    private int dispatch(LuaCallFrame callFrame, int nArguments) {
        BaseLib.luaAssert(nArguments >= 2, "Not enough arguments");

        String src = (String)callFrame.get(0);
        String data = (String)callFrame.get(1);

        ServiceProvider service = ServiceProvider.getInstance();
        service.dispatch(src, data);

        return 0;
    }

    private int nextToken(LuaCallFrame callFrame, int nArguments) {
        BaseLib.luaAssert(nArguments >= 1, "Not enough arguments");

        String str = (String)callFrame.get(0);
        int pos = str.indexOf(' ');
        if (pos == -1) {
            callFrame.push(str);
            callFrame.push(null);
        }
        else {
            callFrame.push(str.substring(0, pos));
            callFrame.push(str.substring(pos+1));
        }
        return 2;
    }

    private int lock(LuaCallFrame callFrame, int nArguments) {
        BaseLib.luaAssert(nArguments >= 1, "Not enough arguments");
        String key = (String)callFrame.get(0);

        Semaphore lock = null;
        synchronized (locksLock) {
            lock = (Semaphore)locks.get(key);
            if (lock == null) {
                lock = new Semaphore();
                locks.put(key, lock);
            }
        }

        lock.get();

        return 0;
    }

    private int unlock(LuaCallFrame callFrame, int nArguments) {
        BaseLib.luaAssert(nArguments >= 1, "Not enough arguments");
        String key = (String)callFrame.get(0);

        Semaphore lock = null;
        
        synchronized (locksLock) {
            lock = (Semaphore)locks.get(key);
        }

        lock.release();

        return 0;
    }

}

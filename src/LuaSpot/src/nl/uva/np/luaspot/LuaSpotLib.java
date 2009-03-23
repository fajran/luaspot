/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.uva.np.luaspot;

import java.util.Hashtable;
import java.util.Vector;
import se.krka.kahlua.stdlib.BaseLib;
import se.krka.kahlua.vm.JavaFunction;
import se.krka.kahlua.vm.LuaCallFrame;
import se.krka.kahlua.vm.LuaState;
import se.krka.kahlua.vm.LuaTable;

/**
 * Lua SPOT API for Lua script. This API contains functions that are
 * provided by Lua SPOT. All functions will be placed under <code>luaspot</code>
 * table, so they have to be called by using <code>luaspot.</code> prefix.
 * For example:
 *
 * <pre><code>-- This is a Lua script to acquire a synchronization lock
 *luaspot.lock()</code></pre>
 *
 * <p>Available functions:
 *
 * <ul>
 * <li><code>send(dst, msg)</code>: Send a message <code>msg</code> to
 *     <code>dst</code> by putting the message inside a router function call.
 * <li><code>send_raw(dst, msg)</code>: Send a raw message <code>msg</code>
 *     to <code>dst</code>. By using this function, the message will be sent
 *     as is without using the routing function call.
 * <li><code>add_id(id)</code>: Insert the given <code>id</code> to the
 *     packet identifier list.
 * <li><code>check_id(id)</code>: Check whether the given <code>id</code> is
 *     already in the packet identifier list.
 * <li><code>get_new_id()</code>: Get new unique id to be used in routing
 *     packet.
 * <li><code>get_node_addr()</code>: Get network address of this sensor network.
 * <li><s><code>get_pkt_src()</code></s>: Use <code>luaspot.sender</code>
 *     instead.
 * <li><code>mem_set(app, var, val)</code>: Save a value <code>val</code>
 *     and use <code>app</code> and <code>var</code> as the identifier.
 * <li><code>mem_get(app, var)</code>: Get saved value identified by
 *     <code>app</code> and <code>var</code>.
 * <li><code>dispatch(addr, msg)</code>: Call the <code>dispatch()</code>
 *     function of ServiceManager and forward the <code>addr</code> and
 *     <code>msg</code> parameters to it.
 * <li><code>lock(app)</code>: Acquire a lock identified by <code>app</code>
 * <li><code>release(app)</code>: Release a lock identified by <code>app</code>
 * <li><code>next_token(str, del)</code>: Return the first token inside the
 *     string <code>str</code> and delete the token if <code>del</code> is
 *     set to true
 * </ul>
 *
 * <p>The table <code>luaspot</code> also has one variable that contains
 * the address of sender node. This address can be retrieved by using
 * <code>luaspot.sender</code> variable.
 * 
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

    /**
     * Register all function to the Lua virtual machine given in the parameter.
     *
     * @param state The Lua virtual machine state
     */
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

    /**
     * Set the node address.
     * 
     * @param address the node address.
     */
    public static void setNodeAddress(String address) {
        nodeAddress = address;
    }

    /**
     * Get the node address.
     *
     * @return the node address.
     */
    public static String getNodeAddress() {
        return nodeAddress;
    }

    /**
     * Get new globally unique message id.
     *
     * @return message id
     */
    public synchronized static String getNewMessageId() {
        return getNodeAddress() + (msgCounter++);
    }

    /**
     * Create a Lua function handler in Java for function with index specified
     * in the parameter.
     *
     * @param index The function index that is handled by this object
     */
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

        Util.send(dst, sb.toString());

        return 0;
    }

    private int sendRaw(LuaCallFrame callFrame, int nArguments) {
        BaseLib.luaAssert(nArguments >= 2, "Not enough arguments");
        String dst = (String)callFrame.get(0);
        String msg = (String)callFrame.get(1);

        Util.send(dst, msg);

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

        System.out.println("[luaspot] memset: key=" + key + ", data=" + data);

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

        System.out.println("[luaspot] memget: key=" + key + ", data.hash=" + data);

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

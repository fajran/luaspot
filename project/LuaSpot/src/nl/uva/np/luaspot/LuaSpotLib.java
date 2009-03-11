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
 *
 * @author iang
 */
public class LuaSpotLib implements JavaFunction {

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

    private static Vector ids;
    private static Hashtable mem;
    private static int msgCounter;

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
        if (ids == null) {
            ids = new Vector();
            mem = new Hashtable();
            msgCounter = 0;
        }
    }

    private static String getNodeAddress() {
        // TODO
        return "";
    }

    private synchronized static String getNewMessageId() {
        return getNodeAddress() + (msgCounter++);
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
        }
        return 0;
    }

    private int send(LuaCallFrame callFrame, int nArguments) {
        BaseLib.luaAssert(nArguments >= 2, "Not enough arguments");
        String dst = (String)callFrame.get(0);
        String msg = (String)callFrame.get(1);

        String id = getNewMessageId();
        String maxhop = "";
        String src = "";

        StringBuffer sb = new StringBuffer();
        sb.append("s router route ");
        sb.append(id);
        sb.append(" ");
        sb.append(maxhop);
        sb.append(" ");
        sb.append(src);
        sb.append(" ");
        sb.append(dst);
        sb.append(" ");
        sb.append(msg);

        // TODO
        // send(dst, sb.toString());

        return 0;
    }

    private int sendRaw(LuaCallFrame callFrame, int nArguments) {
        BaseLib.luaAssert(nArguments >= 2, "Not enough arguments");
        String dst = (String)callFrame.get(0);
        String msg = (String)callFrame.get(1);

        // TODO
        // send(dst, msg);

        return 0;
    }

    private int addId(LuaCallFrame callFrame, int nArguments) {
        BaseLib.luaAssert(nArguments >= 1, "Not enough arguments");
        String id = (String)callFrame.get(0);
        synchronized (ids) {
            ids.addElement(id);
        }
        return 0;
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
        String data = (String)callFrame.get(2);

        String key = app +"_" + var;

        System.out.println("[luaspot] memset: key=" + key + ", data=" + data);

        synchronized (mem) {
            mem.put(key, data);
        }

        return 0;
    }

    private int memGet(LuaCallFrame callFrame, int nArguments) {
        BaseLib.luaAssert(nArguments >= 2, "Not enough arguments");

        String app = (String)callFrame.get(0);
        String var = (String)callFrame.get(1);

        String key = app +"_" + var;
        String data = null;

        synchronized (mem) {
            data = (String)mem.get(key);
        }

        System.out.println("[luaspot] memget: key=" + key + ", data=" + data);

        callFrame.push(data);

        return 1;
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

}

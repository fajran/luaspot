
package nl.uva.np.luaspot;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import se.krka.kahlua.vm.LuaClosure;
import se.krka.kahlua.vm.LuaPrototype;
import se.krka.kahlua.vm.LuaState;

/**
 * This class hold the application codes and takes care about application
 * installation and removal to/from the memory. The ManagerServices class
 * uses this class to store the application binaries.
 */
public class ApplicationRegistry {

    public static Hashtable apps = new Hashtable();
    public static Hashtable appReady = new Hashtable();

    /**
     * Prepare a new slot to store application binary.
     *
     * @param name the application name
     */
    public static void prepareApplication(String name) {
        synchronized (appReady) {
            appReady.put(name, new Boolean(false));
            apps.put(name, new StringBuffer());
        }
    }

    /**
     * Append a new partial binary segment to the existing codes in the
     * slot.
     *
     * @param name the application name
     * @param data the data that is need to be inserted
     */
    public static void updateApplication(String name, String data) {
        Boolean ready = null;
        synchronized (appReady) {
            ready = (Boolean)appReady.get(name);
        }
        if ((ready != null) && !ready.booleanValue()) {
            synchronized (apps) {
                StringBuffer sb = (StringBuffer)apps.get(name);
                sb.append(data);
            }
        }
    }

    /**
     * Set the application to final so it will be ready to be used.
     * 
     * @param name the application name
     */
    public static void finalizeApplication(String name) {
        synchronized (appReady) {
            Boolean ready = (Boolean)appReady.get(name);
            if (ready != null) {
                appReady.put(name, new Boolean(true));
            }
        }
    }

    /**
     * Remove an existing application.
     *
     * @param name the application name
     */
    public static void removeApplication(String name) {
        synchronized (apps) {
            apps.remove(name);
            appReady.remove(name);
        }
    }

    /**
     * Register all available applications to the Lua virtual machine.
     * 
     * @param state the Lua virtual machine
     */
    public static void register(LuaState state) {
        synchronized (apps) {
            Enumeration e = apps.keys();
            while (e.hasMoreElements()) {
                String name = (String)e.nextElement();
                synchronized (appReady) {
                    Boolean ready = (Boolean)appReady.get(name);
                    if ((ready == null) || !ready.booleanValue()) {
                        continue;
                    }
                }
                
                try {
                    StringBuffer data = (StringBuffer)apps.get(name);
                    install(state, name, data.toString());
                }
                catch (IOException ee) {
                    System.out.println(ee.toString());
                }
            }
                   
        }
    }

    /**
     * Application code injection to the Lua virtual machine.
     *
     * @param state the Lua virtual machine
     * @param app the application name
     * @param data the applicatino binary vode
     * @throws java.io.IOException
     */
    private static void install(LuaState state, String app, String data) throws IOException {
        try {
            byte[] bdata = data.getBytes();
            int sum = 0;
            for (int i=0; i<bdata.length; i++) {
                sum = (sum + bdata[i]) % 16777216;
            }
            DataInputStream is = new DataInputStream(new ByteArrayInputStream(data.getBytes()));
            LuaClosure closure = LuaPrototype.loadByteCode(is, state.getEnvironment());
            state.call(closure, null, null, null);
        }
        catch (RuntimeException e) {
        }
    }
}

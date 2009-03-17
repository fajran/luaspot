/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

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
 *
 * @author iang
 */
public class ApplicationRegistry {

    public static Hashtable apps = new Hashtable();
    public static Hashtable appReady = new Hashtable();

    public static void prepareApplication(String name) {
        synchronized (appReady) {
            appReady.put(name, new Boolean(false));
            apps.put(name, new StringBuffer());
            System.out.println("[appreg] prepare: app=" + name);
        }
    }

    public static void updateApplication(String name, String data) {
        Boolean ready = null;
        synchronized (appReady) {
            ready = (Boolean)appReady.get(name);
            System.out.println("[appreg] getready: app=" + name + ", ready=" + ready);
        }
        if ((ready != null) && !ready.booleanValue()) {
            synchronized (apps) {
                StringBuffer sb = (StringBuffer)apps.get(name);
                sb.append(data);

                StringBuffer sbd = new StringBuffer(data);
                int i;
                int sum = 0;
                for (i=0; i<sbd.length(); i++) {
                     sum = (sum + (byte)sbd.charAt(i)) % 16777216;
                }

                System.out.println("[appreg] update: app=" + name + ", len=" + data.length() + ", sum=" + sum);
            }
        }
    }

    public static void finalizeApplication(String name) {
        synchronized (appReady) {
            Boolean ready = (Boolean)appReady.get(name);
            if (ready != null) {
                appReady.put(name, new Boolean(true));
                System.out.println("[appreg] finalize: app=" + name);
            }
        }
    }

    public static void removeApplication(String name) {
        synchronized (apps) {
            System.out.println("[appreg] remove: app=" + name);
            apps.remove(name);
            appReady.remove(name);
        }
    }

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

    private static void install(LuaState state, String app, String data) throws IOException {
        System.out.println("[appreg] register: app=" + app + ", len=" + data.length());
        DataInputStream is = new DataInputStream(new ByteArrayInputStream(data.getBytes()));
        LuaClosure closure = LuaPrototype.loadByteCode(is, state.getEnvironment());
        state.call(closure, null, null, null);
    }
}

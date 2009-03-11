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

    public static void addApplication(String name, String data) {
        synchronized (apps) {
            System.out.println("[appreg] add: app=" + name);
            apps.put(name, data);
        }
    }

    public static void removeApplication(String name) {
        synchronized (apps) {
            System.out.println("[appreg] remove: app=" + name);
            apps.remove(name);
        }
    }

    public static void register(LuaState state) {
        synchronized (apps) {
            Enumeration e = apps.keys();
            while (e.hasMoreElements()) {
                String name = (String)e.nextElement();
                try {
                    install(state, name, (String)apps.get(name));
                }
                catch (IOException ee) {
                    System.out.println(ee.toString());
                }
            }
                   
        }
    }

    private static void install(LuaState state, String app, String data) throws IOException {
        System.out.println("[appreg] register: app=" + app);
        DataInputStream is = new DataInputStream(new ByteArrayInputStream(data.getBytes()));
        LuaClosure closure = LuaPrototype.loadByteCode(is, state.getEnvironment());
        state.call(closure, null, null, null);
    }
}

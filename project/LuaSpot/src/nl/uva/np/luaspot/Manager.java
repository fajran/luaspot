/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.uva.np.luaspot;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;
import se.krka.kahlua.stdlib.MathLib;
import se.krka.kahlua.stdlib.OsLib;
import se.krka.kahlua.test.UserdataArray;
import se.krka.kahlua.vm.LuaClosure;
import se.krka.kahlua.vm.LuaPrototype;
import se.krka.kahlua.vm.LuaState;

/**
 *
 * @author iang
 */
public class Manager extends Thread {

    private Hashtable states;
    private Hashtable apps;
    private Vector runningApps;
    private boolean running;

    protected static Manager instance = null;

    protected Manager() {
        apps = new Hashtable();
        states = new Hashtable();
        runningApps = new Vector();
        running = false;
    }

    public static Manager getInstance() {
        if (instance == null) {
            instance = new Manager();
        }
        return instance;
    }

    public void add(String id, byte[] data) {
        System.out.println("New application is added: " + id);
        try {
            LuaState state = new LuaState(System.out);
            UserdataArray.register(state);
            OsLib.register(state);
            MathLib.register(state);
            LuaStdLib.register(state);
            SunSpotLib.register(state);
            ManagerLib.register(state);

            loadData(state, data);

            apps.put(id, state);
            states.put(state, id);
        }
        catch (IOException e) {
            // FIXME
        }
    }

    private void loadData(LuaState state, byte[] data) throws IOException {
        DataInputStream is = new DataInputStream(new ByteArrayInputStream(data));
        LuaClosure closure = LuaPrototype.loadByteCode(is, state.getEnvironment());
        state.call(closure, null, null, null);
    }

    public void run(String id) {
        System.out.println("Application is run: " + id);
        runningApps.addElement(id);
    }

    public void stop(String id) {
        System.out.println("Stop application: " + id);
        runningApps.removeElement(id);
    }

    public void stop(LuaState state) {
        stop((String)states.get(state));
    }

    public void schedule() {
        
    }

    public void run() {
        System.out.println("Application manager is running..");

        Hashtable appList = new Hashtable();
        
        running = true;
        while (running) {
            int total = runningApps.size();
            if (total > 0) {
                for (int i=0; i<total; i++) {
                    String id = (String)runningApps.elementAt(i);
                    LuaState state = (LuaState)apps.get(id);
                    if (state != null) {
                        LuaClosure func = (LuaClosure)state.getEnvironment().rawget("step");
                        state.call(func, null, null, null);
                    }
                }

                // XXX
                try {
                    Thread.currentThread().sleep(1000);
                }
                catch (InterruptedException e) {
                }

            }
            else {
                try {
                    Thread.currentThread().sleep(100);
                }
                catch (InterruptedException e) {
                }
            }
        }
    }

}

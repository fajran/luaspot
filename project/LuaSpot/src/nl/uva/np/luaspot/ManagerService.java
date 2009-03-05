/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.uva.np.luaspot;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import se.krka.kahlua.vm.LuaClosure;
import se.krka.kahlua.vm.LuaPrototype;
import se.krka.kahlua.vm.LuaState;

/**
 *
 * @author iang
 */
public class ManagerService {

    private LuaState state;

    public ManagerService(LuaState state) {
        this.state = state;
    }

    public void call(String func, byte[] param) {
        if ("install".equals(func)) {
            install(param);
        }
        else if ("remove".equals(func)) {
            remove(param);
        }
    }

    private void install(byte[] param) {
        // param: appname length data..
        String app = null;
        byte[] data = null;

        try {
            install(app, data);
        }
        catch (IOException e) {
        }
    }

    public void install(String app, byte[] data) throws IOException {
        DataInputStream is = new DataInputStream(new ByteArrayInputStream(data));
        LuaClosure closure = LuaPrototype.loadByteCode(is, state.getEnvironment());
        state.call(closure, null, null, null);
    }

    private void remove(byte[] param) {
        String app = new String(param);
        state.getEnvironment().rawset(app, null);
    }
}

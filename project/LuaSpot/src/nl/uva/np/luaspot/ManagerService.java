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

    public void call(String func, String param) {
        if ("install".equals(func)) {
            StringBuffer sb = new StringBuffer(param);
            String app = Util.getNextToken(sb, true);
            install(app, sb.toString());
        }
        else if ("remove".equals(func)) {
            remove(param);
        }
    }

    public void install(String app, String data) {
        ApplicationRegistry.addApplication(app, data);
    }

    private void remove(String app) {
        ApplicationRegistry.removeApplication(app);
    }
}

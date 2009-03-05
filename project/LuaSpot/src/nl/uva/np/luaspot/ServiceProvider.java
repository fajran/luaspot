/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.uva.np.luaspot;

import se.krka.kahlua.vm.LuaClosure;
import se.krka.kahlua.vm.LuaState;
import se.krka.kahlua.vm.LuaTable;

/**
 *
 * @author iang
 */
public class ServiceProvider {

    private ManagerService manager;
    private LuaState state;

    public ServiceProvider() {
        initLuaVM();
        manager = new ManagerService(state);
    }

    private void initLuaVM() {
        state = new LuaState(System.out);
        SunSpotLib.register(state);
    }

    // Handle data with the following format:
    //
    //    call [app] [func] [data..]
    //

    public void call(String app, String func, byte[] data) {
        if (manager.equals(app)) {
            manager.call(func, data);
        }
        else {
            call(app, func, Util.makeArguments(data));
        }
    }

    public void call(String app, String func, Object[] param) {
        try {
            LuaTable application = (LuaTable)state.getEnvironment().rawget(app);
            LuaClosure function = (LuaClosure)application.rawget(func);
            state.call(function, param);
        }
        catch (NullPointerException e) {
        }
    }

    public ManagerService getManagerService() {
        return manager;
    }

}

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
    
    private static ServiceProvider instance = null;

    public static synchronized ServiceProvider getInstance() {
        if (instance == null) {
            instance = new ServiceProvider();
        }
        return instance;
    }

    protected ServiceProvider() {
        manager = new ManagerService(state);
    }

    public ManagerService getManagerService() {
        return manager;
    }

    public void dispatch(String src, String msg) {
        System.out.println("[service] dispatch: src=" + src + ", msg=" + msg);
        
        StringBuffer sb = new StringBuffer(msg);
        String type = Util.getNextToken(sb, true);

        System.out.println("type=" + type);

        if ("c".equals(type)) {
            System.out.println("call message");
            
            String app = Util.getNextToken(sb, true);
            String func = Util.getNextToken(sb, true);

            System.out.println("app=" + app + ", func=" + func);
            System.out.println("param=" + sb.toString());

            if ("manager".equals(app)) {
                manager.call(func, sb.toString());
            }
            else {

                LuaState state = new LuaState(System.out);
                initLuaVM(state);

                LuaTable table = (LuaTable)state.getEnvironment().rawget("luaspot");
                table.rawset("pkt_src", src);
                table.rawset("sender", src);

                LuaTable application = (LuaTable)state.getEnvironment().rawget(app);
                LuaClosure function = (LuaClosure)application.rawget(func);
                state.call(function, new Object[] { sb.toString() });

            }
        }
        else {
            System.out.println("unknown message");
        }
        
    }

    private void initLuaVM(LuaState state) {
        SunSpotLib.register(state);
        LuaSpotLib.register(state);
        ApplicationRegistry.register(state);
    }

}

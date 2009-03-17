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
    
    private static ServiceProvider instance = null;

    public static synchronized ServiceProvider getInstance() {
        if (instance == null) {
            instance = new ServiceProvider();
        }
        return instance;
    }

    protected ServiceProvider() {
        manager = new ManagerService();
    }

    public ManagerService getManagerService() {
        return manager;
    }

    public void dispatch(String src, String msg) {
        System.out.println("[service] dispatch: src=" + src + ", msg len=" + msg.length());
//        System.out.println("           message: " + msg);
        
        StringBuffer sb = new StringBuffer(msg);


        int sum=0;
        for (int i=0; i<sb.length(); i++) {
            sum = (sum + (int)sb.charAt(i)) % 16777216;
        }
        System.out.println("[dispatch] len=" + sb.length() + ", sum=" + sum);


        String type = Util.getNextToken(sb, true);

        System.out.println("type=" + type);

        if ("c".equals(type)) {
            System.out.println("call message");
            
            String app = Util.getNextToken(sb, true);
            String func = Util.getNextToken(sb, true);

            System.out.println("app=" + app + ", func=" + func + ", param len=" + sb.length());

            if ("manager".equals(app)) {
                manager.call(src, func, sb.toString());
            }
            else if ("debug".equals(app)) {
                return;
            }
            else {

                LuaState state = new LuaState(System.out);
                initLuaVM(state);

                LuaTable table = (LuaTable)state.getEnvironment().rawget("luaspot");
                table.rawset("pkt_src", src);
                table.rawset("sender", src);

                LuaTable application = (LuaTable)state.getEnvironment().rawget(app);
                if (application == null) {
                    System.out.println("Unknown application: " + app);
                    return;
                }
                System.out.println("[dispatch] application=" + application);

                LuaClosure function = (LuaClosure)application.rawget(func);
                if (function == null) {
                    System.out.println("Unknown function: " + function);
                    return;
                }
                System.out.println("[dispatch] function=" + function);



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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.uva.np.luaspot;

import se.krka.kahlua.vm.JavaFunction;
import se.krka.kahlua.vm.LuaCallFrame;
import se.krka.kahlua.vm.LuaState;

/**
 *
 * @author iang
 */
public class ManagerLib {

    public static void register(LuaState state) {
        final LuaState s = state;
        state.getEnvironment().rawset("manager_stop", new JavaFunction() {
            public int call(LuaCallFrame callFrame, int nArguments) {
                System.out.println("An application is stopped");
                Manager manager = Manager.getInstance();
                manager.stop(s);
                return 0;
            }
        });
    }
    
}

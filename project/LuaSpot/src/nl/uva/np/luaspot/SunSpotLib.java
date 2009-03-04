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
public class SunSpotLib implements JavaFunction {

    public int call(LuaCallFrame callFrame, int nArguments) {
        return 0;
    }

    public static void register(LuaState state) {

    }

}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.uva.np.luaspot;

import com.sun.spot.sensorboard.EDemoBoard;
import com.sun.spot.sensorboard.peripheral.ITriColorLED;
import se.krka.kahlua.stdlib.BaseLib;
import se.krka.kahlua.vm.JavaFunction;
import se.krka.kahlua.vm.LuaCallFrame;
import se.krka.kahlua.vm.LuaState;
import se.krka.kahlua.vm.LuaTable;

/**
 *
 * @author iang
 */
public class SunSpotLib implements JavaFunction {

    private static ITriColorLED[] leds;

    private static final int LED_ON = 0;
    private static final int LED_OFF = 1;
    private static final int LED_RGB = 2;

    private static final int NUM_FUNCTIONS = 3;
    private static String[] names;
    
    static {
        names = new String[NUM_FUNCTIONS];
        names[LED_ON] = "led_on";
        names[LED_OFF] = "led_off";
        names[LED_RGB] = "led_rgb";

        leds = EDemoBoard.getInstance().getLEDs();
    }

    private int index;
    private static SunSpotLib[] functions;

    public SunSpotLib(int index) {
        this.index = index;
    }

    public static void register(LuaState state) {
        initFunctions();
        LuaTable sunspot = new LuaTable();
        state.getEnvironment().rawset("sunspot", sunspot);
        
        for (int i=0; i<NUM_FUNCTIONS; i++) {
            sunspot.rawset(names[i], functions[i]);
        }
    }

    private static synchronized void initFunctions() {
        if (functions == null) {
            functions = new SunSpotLib[NUM_FUNCTIONS];
            for (int i=0; i<NUM_FUNCTIONS; i++) {
                functions[i] = new SunSpotLib(i);
            }
        }
    }

    public String toString() {
        return "sunspot." + names[index];
    }

    public int call(LuaCallFrame callFrame, int nArguments) {
        switch (index) {
            case LED_ON: return led(LED_ON, callFrame, nArguments);
            case LED_OFF: return led(LED_OFF, callFrame, nArguments);
            case LED_RGB: return led(LED_RGB, callFrame, nArguments);
            default: return 0;
        }
    }

    private static int led(int function, LuaCallFrame callFrame, int nArguments) {
        BaseLib.luaAssert(nArguments >= 1, "Not enough arguments");
        
        int pos = (int)LuaState.fromDouble(callFrame.get(0));
        System.out.println("led: type=" + function + ", pos=" + pos);

        switch (function) {
            case LED_ON: {
                leds[pos].setOn();
                break;
            }
            case LED_OFF: {
                leds[pos].setOff();
                break;
            }
            case LED_RGB: {
                BaseLib.luaAssert(nArguments >= 4, "Not enough arguments");
                int r = (int)LuaState.fromDouble(callFrame.get(1));
                int g = (int)LuaState.fromDouble(callFrame.get(2));
                int b = (int)LuaState.fromDouble(callFrame.get(3));

                leds[pos].setRGB(r, g, b);
            }
        }
        
        return 0;
    }

    
}

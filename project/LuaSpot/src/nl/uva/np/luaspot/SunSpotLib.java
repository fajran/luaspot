
package nl.uva.np.luaspot;

import com.sun.spot.sensorboard.EDemoBoard;
import com.sun.spot.sensorboard.peripheral.IAccelerometer3D;
import com.sun.spot.sensorboard.peripheral.ITemperatureInput;
import com.sun.spot.sensorboard.peripheral.ITriColorLED;
import com.sun.spot.util.Utils;
import java.io.IOException;
import se.krka.kahlua.stdlib.BaseLib;
import se.krka.kahlua.vm.JavaFunction;
import se.krka.kahlua.vm.LuaCallFrame;
import se.krka.kahlua.vm.LuaState;
import se.krka.kahlua.vm.LuaTable;

/**
 * Sun SPOT API for Lua script. This API contains functions that are
 * provided by Sun SPOT. All functions will be placed under <code>sunspot</code>
 * table, so they have to be called by using <code>sunspot.</code> prefix.
 * For example:
 *
 * <pre><code>-- This is a Lua script to set the first led color to yellow
 * sunspot.led_rgb(0, 255, 255, 0)</code></pre>
 *
 * <p>Provided functions:
 *
 * <ul>
 * <li><code>led_on(i)</code>: Turn on a LED with index <code>i</code>
 * <li><code>led_off(i)</code>: Turn off a LED with index <code>i</code>
 * <li><code>led_rgb(i, r, g, b)</code>: Set color LED with index <code>i</code>
 *     to the color with RGB value of <code>r</code>, <code>g</code>, and
 *     <code>b</code>
 * <li><code>temp_raw()</code>: Get raw temperature value
 * <li><code>temp_celcius()</code>: Get temperature value in Celcius
 * <li><code>temp_fahrenheit()</code>: Get temperature value in Fahrenheit
 * <li><code>accel_x()</code>: Get accelerometer value in X axis
 * <li><code>accel_y()</code>: Get accelerometer value in Y axis
 * <li><code>accel_z()</code>: Get accelerometer value in Z axis
 * <li><code>accel_relx()</code>: Get relative accelerometer value in X axis
 * <li><code>accel_rely()</code>: Get relative accelerometer value in Y axis
 * <li><code>accel_relz()</code>: Get relative accelerometer value in Z axis
 * <li><code>accel_rest()</code>: Set the base of relative accelerometer value
 * <li><code>sleep(n)</code>: Sleep (suspend the thread) for <code>n</code ms
 * </ul>
 */
public class SunSpotLib implements JavaFunction {

    private static ITriColorLED[] leds = EDemoBoard.getInstance().getLEDs();
    private static ITemperatureInput temperature = EDemoBoard.getInstance().getADCTemperature();
    private static IAccelerometer3D accel = EDemoBoard.getInstance().getAccelerometer();

    private static final int NUM_FUNCTIONS = 14;
    
    private static final int LED_ON = 0;
    private static final int LED_OFF = 1;
    private static final int LED_RGB = 2;

    private static final int TEMP_RAW = 3;
    private static final int TEMP_CELCIUS = 4;
    private static final int TEMP_FAHRENHEIT = 5;

    private static final int ACCEL_X = 6;
    private static final int ACCEL_Y = 7;
    private static final int ACCEL_Z = 8;
    private static final int ACCEL_REL_X = 9;
    private static final int ACCEL_REL_Y = 10;
    private static final int ACCEL_REL_Z = 11;
    private static final int ACCEL_SET_REST = 12;

    private static final int SLEEP = 13;

    private static String[] names;

    /*
     * List of functions that can be accessed from Lua script. All function
     * is under "sunspot" table, hence prefix the function by "sunspot."
     * in order to use it.
     */
    static {
        names = new String[NUM_FUNCTIONS];
        
        names[LED_ON] = "led_on";
        names[LED_OFF] = "led_off";
        names[LED_RGB] = "led_rgb";

        names[TEMP_RAW] = "temp_raw";
        names[TEMP_CELCIUS] = "temp_celcius";
        names[TEMP_FAHRENHEIT] = "temp_fahrenheit";

        names[ACCEL_X] = "accel_x";
        names[ACCEL_Y] = "accel_y";
        names[ACCEL_Z] = "accel_z";
        names[ACCEL_REL_X] = "accel_relx";
        names[ACCEL_REL_Y] = "accel_rely";
        names[ACCEL_REL_Z] = "accel_relz";
        names[ACCEL_SET_REST] = "accel_rest";

        names[SLEEP] = "sleep";
    }

    private int index;
    private static SunSpotLib[] functions;

    /**
     * Create a Lua function handler in Java for function with index specified
     * in the parameter.
     *
     * @param index The function index that is handled by this object
     */
    public SunSpotLib(int index) {
        this.index = index;
    }

    /**
     * Register all function to the Lua virtual machine given in the parameter.
     *
     * @param state The Lua virtual machine state
     */
    public static void register(LuaState state) {
        initFunctions();
        LuaTable sunspot = new LuaTable();
        
        for (int i=0; i<NUM_FUNCTIONS; i++) {
            sunspot.rawset(names[i], functions[i]);
        }

        state.getEnvironment().rawset("sunspot", sunspot);

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
            case LED_ON: 
            case LED_OFF: 
            case LED_RGB:
                return led(index, callFrame, nArguments);

            case TEMP_RAW:
            case TEMP_CELCIUS:
            case TEMP_FAHRENHEIT:
                return temperature(index, callFrame, nArguments);
                
            case ACCEL_X:
            case ACCEL_Y:
            case ACCEL_Z:
            case ACCEL_REL_X:
            case ACCEL_REL_Y:
            case ACCEL_REL_Z:
            case ACCEL_SET_REST:
                return accel(index, callFrame, nArguments);

            case SLEEP:
                return sleep(callFrame, nArguments);

            default: return 0;
        }
    }

    private static int led(int function, LuaCallFrame callFrame, int nArguments) {
        BaseLib.luaAssert(nArguments >= 1, "Not enough arguments");
        
        int pos = (int)LuaState.fromDouble(callFrame.get(0));
        if (pos < 0) { pos = 0; }
        if (pos > 7) { pos = 7; }

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

    private static int temperature(int function, LuaCallFrame callFrame, int nArguments) {

        try {
            switch (function) {
                case TEMP_RAW: {
                    callFrame.push(new Double(temperature.getValue()));
                    return 1;
                }
                case TEMP_CELCIUS: {
                    callFrame.push(new Double(temperature.getCelsius()));
                    return 1;
                }
                case TEMP_FAHRENHEIT: {
                    callFrame.push(new Double(temperature.getFahrenheit()));
                    return 1;
                }
            }
        }
        catch (IOException e) {
        }
        
        return 0;
    }

     private static int accel(int function, LuaCallFrame callFrame, int nArguments) {

        try {
            switch (function) {
                case ACCEL_X: {
                    callFrame.push(new Double(Math.toDegrees(accel.getTiltX())));
                    return 1;
                }
                case ACCEL_Y: {
                    callFrame.push(new Double(Math.toDegrees(accel.getTiltY())));
                    return 1;
                }
                case ACCEL_Z: {
                    callFrame.push(new Double(Math.toDegrees(accel.getTiltZ())));
                    return 1;
                }
                case ACCEL_REL_X: {
                    callFrame.push(new Double(Math.toDegrees(accel.getRelativeAccelX())));
                    return 1;
                }
                case ACCEL_REL_Y: {
                    callFrame.push(new Double(Math.toDegrees(accel.getRelativeAccelY())));
                    return 1;
                }
                case ACCEL_REL_Z: {
                    callFrame.push(new Double(Math.toDegrees(accel.getRelativeAccelZ())));
                    return 1;
                }
                case ACCEL_SET_REST: {
                    accel.setRestOffsets();
                    return 0;
                }
            }
        }
        catch (IOException e) {
        }

        return 0;
    }

    private static int sleep(LuaCallFrame callFrame, int nArguments) {
        BaseLib.luaAssert(nArguments >= 1, "Not enough arguments");
        int delay = (int)LuaState.fromDouble(callFrame.get(0));
        Utils.sleep(delay);
        return 0;
    }
}

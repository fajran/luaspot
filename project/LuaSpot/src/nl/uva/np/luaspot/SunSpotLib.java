/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.uva.np.luaspot;

import com.sun.spot.sensorboard.EDemoBoard;
import com.sun.spot.sensorboard.peripheral.IAccelerometer3D;
import com.sun.spot.sensorboard.peripheral.ITemperatureInput;
import com.sun.spot.sensorboard.peripheral.ITriColorLED;
import java.io.IOException;
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

    private static ITriColorLED[] leds = EDemoBoard.getInstance().getLEDs();
    private static ITemperatureInput temperature = EDemoBoard.getInstance().getADCTemperature();
    private static IAccelerometer3D accel = EDemoBoard.getInstance().getAccelerometer();

    private static final int NUM_FUNCTIONS = 13;
    
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

    private static String[] names;
    
    static {
        names = new String[NUM_FUNCTIONS];
        names[LED_ON] = "led_on";
        names[LED_OFF] = "led_off";
        names[LED_RGB] = "led_rgb";

        names[TEMP_RAW] = "temp_raw";
        names[TEMP_CELCIUS] = "temp_ceclcius";
        names[TEMP_FAHRENHEIT] = "temp_fahrenheit";

        names[ACCEL_X] = "accel_x";
        names[ACCEL_Y] = "accel_y";
        names[ACCEL_Z] = "accel_z";
        names[ACCEL_REL_X] = "accel_relx";
        names[ACCEL_REL_Y] = "accel_rely";
        names[ACCEL_REL_Z] = "accel_relz";
        names[ACCEL_SET_REST] = "accel_rest";

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
                    callFrame.push(new Double(accel.getTiltX()));
                    return 1;
                }
                case ACCEL_Y: {
                    callFrame.push(new Double(accel.getTiltY()));
                    return 1;
                }
                case ACCEL_Z: {
                    callFrame.push(new Double(accel.getTiltZ()));
                    return 1;
                }
                case ACCEL_REL_X: {
                    callFrame.push(new Double(accel.getRelativeAccelX()));
                    return 1;
                }
                case ACCEL_REL_Y: {
                    callFrame.push(new Double(accel.getRelativeAccelY()));
                    return 1;
                }
                case ACCEL_REL_Z: {
                    callFrame.push(new Double(accel.getRelativeAccelZ()));
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
    
}

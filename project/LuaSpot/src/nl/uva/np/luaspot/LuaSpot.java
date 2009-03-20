
package nl.uva.np.luaspot;

import com.sun.spot.peripheral.radio.RadioFactory;
import com.sun.spot.sensorboard.EDemoBoard;
import com.sun.spot.sensorboard.peripheral.ITriColorLED;
import com.sun.spot.util.BootloaderListener;
import com.sun.spot.util.IEEEAddress;
import com.sun.spot.util.Utils;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

/**
 * The startApp method of this class is called by the VM to start the
 * application.
 * 
 * The manifest specifies this class as MIDlet-1, which means it will
 * be selected for execution.
 */
public class LuaSpot extends MIDlet {

    private ITriColorLED [] leds = EDemoBoard.getInstance().getLEDs();

    protected void startApp() throws MIDletStateChangeException {
        new BootloaderListener().start();
        long ourAddr = RadioFactory.getRadioPolicyManager().getIEEEAddress();
        System.out.println("Our radio address = " + IEEEAddress.toDottedHex(ourAddr));

        {
            for (int i=0; i<8; i++) {
                leds[i].setRGB(255, 0, 0);
                leds[i].setOn();
            }
        }

        // Set the node address
        LuaSpotLib.setNodeAddress(IEEEAddress.toDottedHex(ourAddr));

        // Prepare the service provider
        ServiceProvider sp = ServiceProvider.getInstance();
        ManagerService manager = sp.getManagerService();

        // Install the pre-installed applications (beside the manager)
        manager.install("router", new String(Applications.ROUTER));
        manager.install("demo", new String(Applications.DEMO));
        manager.install("hotpath", new String(Applications.HOTPATH));

        {
            for (int i=0; i<8; i++) {
                leds[i].setRGB(0, 255, 0);
            }
        }

        // Set the node to start receiving data
        Util.startReceiverThread();

        {
            Utils.sleep(1000);
            for (int i=0; i<8; i++) {
                leds[i].setOff();
            }
        }

    }

    protected void pauseApp() {
        // This is not currently called by the Squawk VM
    }

    /**
     * Called if the MIDlet is terminated by the system.
     * I.e. if startApp throws any exception other than MIDletStateChangeException,
     * if the isolate running the MIDlet is killed with Isolate.exit(), or
     * if VM.stopVM() is called.
     * 
     * It is not called if MIDlet.notifyDestroyed() was called.
     *
     * @param unconditional If true when this method is called, the MIDlet must
     *    cleanup and release all resources. If false the MIDlet may throw
     *    MIDletStateChangeException  to indicate it does not want to be destroyed
     *    at this time.
     */
    protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
        for (int i = 0; i < 8; i++) {
            leds[i].setOff();
        }
    }

}

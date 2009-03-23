
package nl.uva.np.luaspot;

import com.sun.spot.sensorboard.EDemoBoard;
import com.sun.spot.sensorboard.peripheral.ITriColorLED;
import com.sun.spot.util.Utils;

/**
 * The application manager service that handle application installation
 * and removal.
 *
 * <p>This application has two function: <code>install</code> and
 * <code>remove</code>.
 *
 * <p>The <code>install</code> function needs 4 parameters:
 *
 * <ol>
 * <li>Application name.
 * <li>The segment number, starts from zero.
 * <li>The last segment number.
 * <li>Application data.
 * </ol>
 *
 * <p>If the segment number is zero, the application slot will be initialized.
 * All incoming data will be appended to the slot and when the last segment
 * comes, the applicatoin will be finalized and ready to be used.
 *
 * <p>Applications code are stored inside a singleton class ApplicationRegistry.
 */
public class ManagerService {

    private ITriColorLED [] leds = EDemoBoard.getInstance().getLEDs();

    public ManagerService() {
    }

    /**
     * A standard interface according to the RPC packet format that will be
     * called by the dispatch() function of ServiceProvider.
     * 
     * @param addr The sender address
     * @param func The function name
     * @param param The parameter
     */
    public void call(String addr, String func, String param) {

        if ("install".equals(func)) {
            StringBuffer sb = new StringBuffer(param);
            String app = Util.getNextToken(sb, true);
            int segment = Integer.parseInt(Util.getNextToken(sb, true));
            int lastSegment = Integer.parseInt(Util.getNextToken(sb, true));

            if (segment == 0) {
                prepare(app);
            }

            if (segment % 8 == 0) {
                for (int i=0; i<8; i++) {
                    leds[i].setRGB(0, 0, 0);
                    leds[i].setOff();
                }
            }

            update(app, sb.toString());

            if (segment == lastSegment) {
                finalize(app);

                for (int i=0; i<8; i++) {
                    leds[i].setRGB(0, 0, 255);
                    leds[i].setOn();
                }
                Utils.sleep(2000);
                for (int i=0; i<8; i++) {
                    leds[i].setRGB(255, 255, 0);
                    leds[i].setOff();
                }
            }
            else {
                leds[segment % 8].setRGB(255, 255, 0);
                leds[segment % 8].setOn();
            }
        }
        else if ("remove".equals(func)) {
            remove(param);
        }
    }

    private void prepare(String app) {
        ApplicationRegistry.prepareApplication(app);
    }

    private void update(String app, String data) {
        ApplicationRegistry.updateApplication(app, data);
    }

    private void finalize(String app) {
        ApplicationRegistry.finalizeApplication(app);
    }

    public void remove(String app) {
        ApplicationRegistry.removeApplication(app);
    }

    public void install(String app, String data) {
        prepare(app);
        update(app, data);
        finalize(app);
    }
}

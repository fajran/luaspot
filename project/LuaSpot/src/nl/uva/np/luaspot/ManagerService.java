/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.uva.np.luaspot;

import com.sun.spot.sensorboard.EDemoBoard;
import com.sun.spot.sensorboard.peripheral.ITriColorLED;
import com.sun.spot.util.Utils;

/**
 *
 * @author iang
 */
public class ManagerService {

    private ITriColorLED [] leds = EDemoBoard.getInstance().getLEDs();

    public ManagerService() {
    }

    public void call(String addr, String func, String param) {
        System.out.println("[manager] function=" + func);
        if ("install".equals(func)) {
            StringBuffer sb = new StringBuffer(param);
            String app = Util.getNextToken(sb, true);
            int segment = Integer.parseInt(Util.getNextToken(sb, true));
            int lastSegment = Integer.parseInt(Util.getNextToken(sb, true));

            if (segment == 0) {
                prepare(app);
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

    public void prepare(String app) {
        ApplicationRegistry.prepareApplication(app);
    }

    public void update(String app, String data) {
        ApplicationRegistry.updateApplication(app, data);
    }

    public void finalize(String app) {
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

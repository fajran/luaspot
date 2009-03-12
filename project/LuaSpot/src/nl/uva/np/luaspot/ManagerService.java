/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.uva.np.luaspot;

import se.krka.kahlua.vm.LuaState;

/**
 *
 * @author iang
 */
public class ManagerService {

    private LuaState state;

    public ManagerService(LuaState state) {
        this.state = state;
    }

    public void call(String func, String param) {
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

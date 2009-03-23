
package nl.uva.np.luaspot;

import se.krka.kahlua.vm.LuaClosure;
import se.krka.kahlua.vm.LuaState;
import se.krka.kahlua.vm.LuaTable;

/**
 * The service provider that stands in the front of packet processing. This
 * singleton class contains the dispatch() function that determines how
 * the incoming packet should be processed.
 *
 * <p>The dispatch() function will select the correct application and
 * function and then invoke the function. Typically, the function is implemented
 * in Lua script and therefore a new Lua virtual machine should be created
 * and run.
 *
 * <p>This class is a singleton class. Therefore, use the static getInstance()
 * method to get its object.
 *
 * <pre><code>ServiceProvider sp = ServiceProvider.getInstance();
 * </code></pre>
 * 
 * <p>After that, you can call the dispatch() function.
 *
 * <pre><code>sp.dispatch(sourceAddr, packet);
 * </code></pre>
 */
public class ServiceProvider {

    // ManagerService handles application installation and removal
    private ManagerService manager;
    
    private static ServiceProvider instance = null;

    /**
     * Create the instance of ServiceProvider.
     *
     * @return an object of ServiceProvider.
     */
    public static synchronized ServiceProvider getInstance() {
        if (instance == null) {
            instance = new ServiceProvider();
        }
        return instance;
    }

    protected ServiceProvider() {
        manager = new ManagerService();
    }

    /**
     * Get application manager service.
     *
     * @return an application manager object.
     */
    public ManagerService getManagerService() {
        return manager;
    }

    /**
     * Dispatch a message and take an action according the message content.
     * 
     * @param src The address of the message sender
     * @param msg The message that will be processed
     */
    public void dispatch(String src, String msg) {
        
        StringBuffer sb = new StringBuffer(msg);

        String type = Util.getNextToken(sb, true);

        /*
         * Only handle message with "c" as its first token.
         */
        if ("c".equals(type)) {
            
            String app = Util.getNextToken(sb, true);
            String func = Util.getNextToken(sb, true);

            /*
             * If the application is "manager", then use the ManagerService
             * object.
             */
            if ("manager".equals(app)) {
                manager.call(src, func, sb.toString());
            }

            /*
             * A do nothing application
             */
            else if ("debug".equals(app)) {
                return;
            }

            /*
             * Other applications are assumed to be implemented as a Lua
             * application
             */
            else {

                /*
                 * Initialize a new Lua virtual machine
                 */

                LuaState state = new LuaState(System.out);
                initLuaVM(state);

                LuaTable table = (LuaTable)state.getEnvironment().rawget("luaspot");
                table.rawset("pkt_src", src);
                table.rawset("sender", src);

                /*
                 * Get the application
                 */

                LuaTable application = (LuaTable)state.getEnvironment().rawget(app);

                if (application == null) {
                    return;
                }

                /*
                 * Get the function
                 */

                LuaClosure function = (LuaClosure)application.rawget(func);

                if (function == null) {
                    return;
                }

                /*
                 * Application and function exists, so call the function.
                 */
                state.call(function, new Object[] { sb.toString() });

            }
        }
    }

    private void initLuaVM(LuaState state) {
        SunSpotLib.register(state);
        LuaSpotLib.register(state);
        ApplicationRegistry.register(state);
    }

}

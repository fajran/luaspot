/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.uva.np.luaspotdesktop;

/**
 *
 * @author iang
 */
public class RPCMessage implements Message {

    private String app;
    private String func;
    private String param;

    public RPCMessage(String app, String func) {
        this.app = app;
        this.func = func;
        this.param = null;
    }

    public RPCMessage(String app, String func, String... param) {
        this.app = app;
        this.func = func;
        this.param = join(param);
    }

    private String join(String[] arr) {
        StringBuffer sb = new StringBuffer();
        int i, len = arr.length;
        if (len > 0) {
            sb.append(arr[0]);
            for (i=1; i<len; i++) {
                sb.append(' ');
                sb.append(arr[i]);
            }
        }
        return sb.toString();
    }

    public String getMessage() {
        StringBuffer sb = new StringBuffer();
        sb.append("c ");
        sb.append(getApp());
        sb.append(" ");
        sb.append(getFunc());
        if (param != null) {
            sb.append(" ");
            sb.append(param);
        }

        return sb.toString();
    }

    /**
     * @return the app
     */
    public String getApp() {
        return app;
    }

    /**
     * @return the func
     */
    public String getFunc() {
        return func;
    }

    /**
     * @return the param
     */
    public String getParam() {
        return param;
    }

    public static RPCMessage parse(String data) {
        StringBuffer sb = new StringBuffer(data);

        int pos = 0;
        int next = 0;

        next = sb.indexOf(" ", pos);
        String type = sb.substring(pos, next);
        pos = next + 1;

        next = sb.indexOf(" ", pos);
        String app = sb.substring(pos, next);
        pos = next + 1;

        next = sb.indexOf(" ", pos);
        String func = sb.substring(pos);

        if (next == -1) {
            return new RPCMessage(app, func);
        }
        else {
            func = sb.substring(pos, next);
            pos = next + 1;

            String param = sb.substring(pos);
            return new RPCMessage(app, func, param);
        }
    }

}

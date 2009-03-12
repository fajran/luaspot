/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.uva.np.luaspotdesktop;

/**
 *
 * @author iang
 */
public class Packet implements Message {

    private String id;
    private String maxHop;
    private String src;
    private String dst;
    private String data;

    public Packet(String id, String maxHop, String src, String dst, String data) {
        this.id = id;
        this.maxHop = maxHop;
        this.src = src;
        this.dst = dst;
        this.data = data;
    }

    public Packet(String id, int maxHop, String src, String dst, String data) {
        this(id, "" + maxHop, src, dst, data);
    }

    public Packet(String id, String maxHop, String src, String dst, Message msg) {
        this(id, maxHop, src, dst, msg.getMessage());
    }

    public Packet(String id, int maxHop, String src, String dst, Message msg) {
        this(id, maxHop, src, dst, msg.getMessage());
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @return the maxHop
     */
    public int getMaxHop() {
        return Integer.parseInt(maxHop);
    }

    /**
     * @return the src
     */
    public String getSrc() {
        return src;
    }

    /**
     * @return the dst
     */
    public String getDst() {
        return dst;
    }

    /**
     * @return the msg
     */
    public String getData() {
        return data;
    }

    public String getMessage() {
        StringBuffer sb = new StringBuffer();
        sb.append(id);
        sb.append(" ");
        sb.append(maxHop);
        sb.append(" ");
        sb.append(src);
        sb.append(" ");
        sb.append(dst);
        sb.append(" ");
        sb.append(data);

        return sb.toString();
    }

    public static Packet parse(String data) {
        StringBuffer sb = new StringBuffer(data);

        int pos = 0;
        int next = 0;

        next = sb.indexOf(" ", pos);
        String id = sb.substring(pos, next);
        pos = next + 1;

        next = sb.indexOf(" ", pos);
        String maxHop = sb.substring(pos, next);
        pos = next + 1;

        next = sb.indexOf(" ", pos);
        String src = sb.substring(pos, next);
        pos = next + 1;

        next = sb.indexOf(" ", pos);
        String dst = sb.substring(pos, next);
        pos = next + 1;

        next = sb.indexOf(" ", pos);
        String msg = sb.substring(pos);

        return new Packet(id, maxHop, src, dst, msg);
    }
}

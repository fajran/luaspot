/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.uva.np.luaspot;

/**
 *
 * @author iang
 */
public class Util {

    public static Object[] makeArguments(byte[] param) {
        // FIXME: tokenize
        if (param == null) {
            return null;
        }
        else {
            return new Object[] { new String(param) };
        }
    }

    public static String getNextToken(StringBuffer sb, boolean delete) {
        int i = 0;
        int len = sb.length();
        for (i=0; i<len; i++) {
            if (sb.charAt(i) == ' ') {
                len = i;
                break;
            }
        }

        char[] chars = new char[len];
        sb.getChars(0, len, chars, 0);

//        System.out.println("getToken: prev=[" + sb.toString() + "]");
        if (delete) {
//            System.out.println("getToken: delete 0-" + len);
            sb.delete(0, len);
            if (sb.length() > 0) {
                sb.delete(0, 1);
            }
        }
//        System.out.println("getToken: prev=[" + sb.toString() + "]");


        return new String(chars);
    }


}

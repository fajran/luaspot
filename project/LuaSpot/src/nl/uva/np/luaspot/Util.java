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

}

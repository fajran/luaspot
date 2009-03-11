/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.uva.np.luaspotdesktop;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author iang
 */
public class Main {

    public static void main(String[] args) {
        // Get the native look and feel class name
        String nativeLF = UIManager.getSystemLookAndFeelClassName();

        // Install the look and feel
        try {
            UIManager.setLookAndFeel(nativeLF);
        } catch (InstantiationException e) {
        } catch (ClassNotFoundException e) {
        } catch (UnsupportedLookAndFeelException e) {
        } catch (IllegalAccessException e) {
        }


        MainFrame mf = new MainFrame();
        mf.setVisible(true);
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.uva.np.luaspot;

/**
 *
 * @author iang
 */
public class Semaphore {

    private boolean free = true;
    private Object lock = new Object();

    public void get() {
        while (true) {
            if (!free) {
                try {
                    lock.wait();
                }
                catch (InterruptedException e) {
                }
            }
            
            synchronized (this) {
                if (free) {
                    free = false;
                    break;
                }
            }
        }
    }
    
    public void release() {
        synchronized (this) {
            free = true;
            lock.notifyAll();
        }
    }
    
}

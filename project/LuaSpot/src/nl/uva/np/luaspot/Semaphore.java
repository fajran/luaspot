
package nl.uva.np.luaspot;

/**
 * A simple semaphore object that can be used to guard a critical section
 * from execution of multiple threads at the same time. Calling the get()
 * method will activate the semaphore so all other threads that try to call
 * the get() function will wait() until the thread that acquire the get() 
 * release the lock by calling release().
 * 
 * <p>Keep in mind that a get() invocation should be paired with a release()
 * call.
 */
public class Semaphore {

    private boolean free = true;

    /**
     * Acquire the lock. If the lock is already acquired by a thread, other
     * threads will get blocked until that thread release the lock. In this
     * case, the first thread can proceed to the critical section and run
     * the code safely.
     */
    public void get() {
        while (true) {
            if (!free) {
                try {
                    synchronized (this) {
                        wait();
                    }
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

    /**
     * Release the acquired lock. This should be called after executing a
     * critical section.
     */
    public void release() {
        synchronized (this) {
            free = true;
            try {
                notifyAll();
            }
            catch (IllegalMonitorStateException e) {
                System.out.println("[semaphore] release EXCEPTION: " + e.toString());
            }
        }
    }
    
}


package nl.uva.np.luaspot;

import com.sun.spot.io.j2me.radiogram.RadiogramConnection;
import java.io.IOException;
import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;
import javax.microedition.io.DatagramConnection;

/**
 * A class that hold utility functions.
 */
public class Util {

    private static Object sendLock = new Object();

    /**
     * In-place string tokenizer. If the delete parameter is set true,
     * the next token will be taken out from the input StringBuffer sb.
     * Otherwise, the token will be just read without changing anything.
     *
     * @param sb String in StringBuffer that will be tokenized
     * @param delete delete the next token from the input String?
     * @return the next token
     */
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

        if (delete) {
            sb.delete(0, len);
            if (sb.length() > 0) {
                sb.delete(0, 1);
            }
        }

        return new String(chars);
    }

    /**
     * Send a message to the specified address. This function call
     * is asynchronous since a new thread will be created and the actual
     * send will be done inside it.
     *
     * @param address the destination address
     * @param data the message that will be transmitted
     */
    public static void send(final String address, final String data) {

        new Thread() {

            public void run() {

                synchronized (sendLock) {

                    DatagramConnection dgConnection = null;
                    Datagram dg = null;

                    try {
                        dgConnection = (DatagramConnection) Connector.open("radiogram://" + address + ":37");
                        dg = dgConnection.newDatagram(dgConnection.getMaximumLength());
                    } catch (IOException ex) {
                        System.out.println("Could not open radiogram connection");
                        ex.printStackTrace();
                        return;
                    }

                    try {
                        dg.reset();

                        char[] cdata = data.toCharArray();
                        byte[] bdata = new byte[cdata.length];
                        for (int i=0; i<cdata.length; i++) {
                            bdata[i] = (byte)cdata[i];
                        }

                        int sum=0;
                        for (int i=0; i<bdata.length; i++) {
                            sum = (sum + bdata[i]) % 16777216;
                        }

                        dg.write(bdata);
                        dgConnection.send(dg);
                        dgConnection.close();

                    }
                    catch (IOException ex) {
                        ex.printStackTrace();
                    }

                }
            }
        }.start();
    }

    public static void startReceiverThread() {
        new Thread() {
            public void run() {
                String tmp = null;
                RadiogramConnection dgConnection = null;
                Datagram dg = null;

                try {
                    dgConnection = (RadiogramConnection) Connector.open("radiogram://:37");
                    // Then, we ask for a datagram with the maximum size allowed
                    System.out.println("Maximum length: " + dgConnection.getMaximumLength());
                    dg = dgConnection.newDatagram(dgConnection.getMaximumLength());
                } catch (IOException e) {
                    System.out.println("Could not open radiogram receiver connection");
                    e.printStackTrace();
                    return;
                }

                while (true) {
                    try {
                        dg.reset();
                        dgConnection.receive(dg);

                        final String con_addr = dg.getAddress();
                        byte[] bdata = new byte[dg.getLength()];
                        dg.readFully(bdata);

                        final String data = new String(bdata);

                        /*
                         * Asynchronously call the dispatch() function so
                         * this thread can start listening to the new
                         * connection again
                         */
                        new Thread() {
                            public void run() {
                                ServiceProvider sp = ServiceProvider.getInstance();
                                sp.dispatch(con_addr, data);
                            }
                        }.start();

                    } catch (IOException e) {
                        System.out.println("Nothing received");
                    }
                }
            }
        }.start();
    }


}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * SunSpotHostApplication.java
 *
 * Created on Mar 16, 2009, 2:18:07 PM
 */

package net.java.dev.netbeansspot;

import com.sun.spot.io.j2me.radiogram.RadiogramConnection;
import com.sun.spot.peripheral.radio.RadioFactory;
import com.sun.spot.util.IEEEAddress;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;
import javax.microedition.io.DatagramConnection;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author iang
 */
public class SunSpotHostApplication extends javax.swing.JFrame {

    public static final int FRAGMENT_SIZE = 256;

    /** Creates new form SunSpotHostApplication */
    public SunSpotHostApplication() {
        startReceiverThread();
        initComponents();
    }

    public void startReceiverThread() {
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
                        byte[] bdata = new byte[dg.getLength()];
                        dg.readFully(bdata);
                        final String data = new String(bdata);
                        final String con_addr = dg.getAddress();

                        logMonitor("New connection from: " + con_addr);
                        logMonitor("Received: data.len=" + data.length() + " from " + con_addr);
                        logMonitor("Data: " + data);
                        
                        processMessage(con_addr, data);
                    } catch (IOException e) {
                        System.out.println("Nothing received");
                    }
                }
            }
        }.start();
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

    private void processMessage(String addr, String data) {
        StringBuffer sb = new StringBuffer(data);
        if ("c".equals(getNextToken(sb, true))) {
            String app = getNextToken(sb, true);
            String func = getNextToken(sb, true);

            if ("who".equals(app)) {
                if ("pong".equals(func)) {
                    log(txtMonitorPing, "Ping response from " + addr);
                }
            }
            else if ("router".equals(app)) {
                if ("route".equals(func)) {
                    String id = getNextToken(sb, true);
                    String hop = getNextToken(sb, true);
                    String src = getNextToken(sb, true);
                    String dst = getNextToken(sb, true);
                    String msg = sb.toString();
                    if (ADDRESS.equals(dst)) {
                        processMessage(src, msg);
                    }
                }
            }
        }
    }

    private static int msgId = 0;
    
    public static String getMessageId() {
        return ADDRESS + (++msgId);
    }

    public static String getMaxHop() {
        return "10";
    }

    public void send(String address, String data) {
        StringBuffer sb = new StringBuffer("c router route ");
        sb.append(getMessageId());
        sb.append(" ");
        sb.append(getMaxHop());
        sb.append(" ");
        sb.append(ADDRESS);
        sb.append(" ");
        sb.append(address);
        sb.append(" ");
        sb.append(data);
        sendRaw("broadcast", sb.toString());
    }

    public void sendRaw(final String address, final String data) {

        new Thread() {

            public void run() {

                // We create a DatagramConnection
                DatagramConnection dgConnection = null;
                Datagram dg = null;

                try {
                    // The Connection is a broadcast so we specify it in the creation string
                    System.out.println("Opening new connection..");
                    dgConnection = (DatagramConnection) Connector.open("radiogram://" + address + ":37");
                    // Then, we ask for a datagram with the maximum size allowed
                    dg = dgConnection.newDatagram(dgConnection.getMaximumLength());
                } catch (IOException ex) {
                    System.out.println("Could not open radiogram connection");
                    ex.printStackTrace();
                    return;
                }

                try {
                    // We send the message (UTF encoded)
                    logMonitor("[sender] Sending data to " + address + ", len=" + data.length());
//                        System.out.println("[sender] data=" + data);
                    dg.reset();

                    char[] cdata = data.toCharArray();
                    byte[] bdata = new byte[cdata.length];
                    for (int i=0; i<cdata.length; i++) {
                        bdata[i] = (byte)cdata[i];
                    }

                    dg.write(bdata);
                    dgConnection.send(dg);
                    dgConnection.close();
                    System.out.println("sent.");

                }
                catch (IOException ex) {
                    ex.printStackTrace();
                }

            }
        }.start();
    }

    private void logMonitor(String msg) {
        log(txtMonitor, msg);
    }

    private void log(JTextArea ta, String msg) {
        StringBuffer sb = new StringBuffer(ta.getText());
        sb.append("[");
        sb.append((new Date()).toString());
        sb.append("] ");
        sb.append(msg);
        sb.append("\n");
        ta.setText(sb.toString());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtMsg = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        txtAddr = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        txtPingAddr = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtMonitorPing = new javax.swing.JTextArea();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        txtInstallFile = new javax.swing.JTextField();
        jButton5 = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        txtInstallAddr = new javax.swing.JTextField();
        jButton6 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        lblInstallSize = new javax.swing.JLabel();
        lblInstallFragments = new javax.swing.JLabel();
        jButton7 = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        lblInstallSent = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        txtInstallName = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtMonitor = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Message");

        txtMsg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtMsgActionPerformed(evt);
            }
        });

        jButton1.setText("Send");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel2.setText("Address");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtMsg, javax.swing.GroupLayout.DEFAULT_SIZE, 463, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(18, 18, 18)
                        .addComponent(txtAddr, javax.swing.GroupLayout.DEFAULT_SIZE, 462, Short.MAX_VALUE))
                    .addComponent(jButton1, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtMsg, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtAddr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addContainerGap(128, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Raw message", jPanel1);

        jLabel3.setText("Address");

        jButton2.setText("Direct Send");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        txtMonitorPing.setColumns(20);
        txtMonitorPing.setRows(5);
        jScrollPane2.setViewportView(txtMonitorPing);

        jButton3.setText("Clear");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setText("Send");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 531, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtPingAddr, javax.swing.GroupLayout.DEFAULT_SIZE, 327, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton4))
                    .addComponent(jButton3, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jButton2)
                    .addComponent(txtPingAddr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 138, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton3)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Ping", jPanel2);

        jLabel4.setText("Lua binary");

        jButton5.setText("...");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jLabel5.setText("Address");

        jButton6.setText("Load");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jLabel6.setText("Size");

        jLabel7.setText("Fragments");

        lblInstallSize.setText("jLabel8");

        lblInstallFragments.setText("jLabel9");

        jButton7.setText("Send next fragment");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jLabel10.setText("Sent");

        lblInstallSent.setText("jLabel11");

        jLabel8.setText("Name");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 84, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblInstallSent, javax.swing.GroupLayout.DEFAULT_SIZE, 293, Short.MAX_VALUE)
                            .addComponent(lblInstallFragments, javax.swing.GroupLayout.DEFAULT_SIZE, 293, Short.MAX_VALUE)
                            .addComponent(lblInstallSize, javax.swing.GroupLayout.DEFAULT_SIZE, 293, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton7))
                    .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 531, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                                .addComponent(txtInstallFile, javax.swing.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton6))
                            .addComponent(txtInstallAddr, javax.swing.GroupLayout.DEFAULT_SIZE, 454, Short.MAX_VALUE)
                            .addComponent(txtInstallName, javax.swing.GroupLayout.DEFAULT_SIZE, 454, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtInstallFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton6)
                    .addComponent(jButton5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(txtInstallAddr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(txtInstallName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(lblInstallSize)
                    .addComponent(jButton7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(lblInstallFragments))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(lblInstallSent))
                .addContainerGap(34, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Install", jPanel3);

        txtMonitor.setColumns(20);
        txtMonitor.setRows(5);
        jScrollPane1.setViewportView(txtMonitor);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 559, Short.MAX_VALUE)
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 559, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 254, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 187, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtMsgActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtMsgActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_txtMsgActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        String msg = txtMsg.getText();
        String addr = txtAddr.getText();
        if ((msg.length() == 0) || (addr.length() == 0)) {
            JOptionPane.showMessageDialog(this, "Please enter message and address.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        else {
            sendRaw(addr, msg);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        txtMonitorPing.setText("");
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        String addr = txtPingAddr.getText();
        if (addr.length() == 0) {
            JOptionPane.showMessageDialog(this, "Please enter the destination address", "Error", JOptionPane.ERROR_MESSAGE);
        }
        else {
            String msg = "c who ping";
            sendRaw(addr, msg);
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        String addr = txtPingAddr.getText();
        if (addr.length() == 0) {
            JOptionPane.showMessageDialog(this, "Please enter the destination address", "Error", JOptionPane.ERROR_MESSAGE);
        }
        else {
            String msg = "c who ping";
            send(addr, msg);
        }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        JFileChooser jfc = new JFileChooser();
        jfc.showOpenDialog(this);
        File f = jfc.getSelectedFile();
        txtInstallFile.setText(f.getAbsolutePath());
    }//GEN-LAST:event_jButton5ActionPerformed

    private File installFile;
    private byte[] installData;
    private int installSize;
    private int installFragments;
    private int installSent;
    private int installOffset;
    private int installRemaining;
    private String installName;

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        
        installFile = new File(txtInstallFile.getText());
        installSize = (int)installFile.length();
        installFragments = (int)Math.ceil((double)installSize / FRAGMENT_SIZE);
        installSent = 0;
        installName = txtInstallName.getText();
        installOffset = 0;
        installRemaining = installSize;

        installData = new byte[installSize];
        try {
            FileInputStream fis = new FileInputStream(installFile);
            fis.read(installData);
        }
        catch (IOException e) {
            System.out.println(e.toString());
        }

        lblInstallSize.setText(installSize + " Byte");
        lblInstallFragments.setText("" + installFragments);
        lblInstallSent.setText(""+installSent);
        
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        String addr = txtInstallAddr.getText();
        String app = txtInstallName.getText();
        
        if (installSent < installFragments) {
            int seg = installSent;
            int lseg = installFragments - 1;

            int size;
            if (installRemaining < FRAGMENT_SIZE) {
                size = installRemaining;
            }
            else {
                size = FRAGMENT_SIZE;
            }
            byte[] data = new byte[size];
            System.arraycopy(installData, installOffset, data, 0, size);

            int sum = 0;
            for (int i=0; i<data.length; i++) {
                sum = (sum + data[i]) % 16777216;
            }
            logMonitor("checksum: " + sum);

            StringBuffer sb = new StringBuffer("c manager install "+app+" "+seg+" "+lseg+" ");
            for (int i=0; i<data.length; i++) {
                sb.append((char)data[i]);
            }
//            String msg = "c manager install "+app+" "+seg+" "+lseg+" "+(new String(data));
            sendRaw(addr, sb.toString());

            installSent++;
            lblInstallSent.setText(""+installSent);

            installRemaining -= size;
            installOffset += size;
        }
    }//GEN-LAST:event_jButton7ActionPerformed

    public static String ADDRESS;
    
    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        String nativeLF = UIManager.getSystemLookAndFeelClassName();

        // Install the look and feel
        try {
            UIManager.setLookAndFeel(nativeLF);
        } catch (InstantiationException e) {
        } catch (ClassNotFoundException e) {
        } catch (UnsupportedLookAndFeelException e) {
        } catch (IllegalAccessException e) {
        }


        long ourAddr = RadioFactory.getRadioPolicyManager().getIEEEAddress();
        ADDRESS = IEEEAddress.toDottedHex(ourAddr);
        System.out.println("Our radio address.... = " + IEEEAddress.toDottedHex(ourAddr));
        new SunSpotHostApplication().setVisible(true);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel lblInstallFragments;
    private javax.swing.JLabel lblInstallSent;
    private javax.swing.JLabel lblInstallSize;
    private javax.swing.JTextField txtAddr;
    private javax.swing.JTextField txtInstallAddr;
    private javax.swing.JTextField txtInstallFile;
    private javax.swing.JTextField txtInstallName;
    private javax.swing.JTextArea txtMonitor;
    private javax.swing.JTextArea txtMonitorPing;
    private javax.swing.JTextField txtMsg;
    private javax.swing.JTextField txtPingAddr;
    // End of variables declaration//GEN-END:variables

}

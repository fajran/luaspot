/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * LuaDeskSpot.java
 *
 * Created on Mar 19, 2009, 11:07:29 AM
 */

package nl.uva.np.luadeskspot;

import com.sun.spot.io.j2me.radiogram.RadiogramConnection;
import com.sun.spot.peripheral.radio.RadioFactory;
import com.sun.spot.util.IEEEAddress;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;
import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;
import javax.microedition.io.DatagramConnection;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.controls.DragControl;
import prefuse.controls.PanControl;
import prefuse.controls.ZoomControl;
import prefuse.controls.ZoomToFitControl;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.GraphicsLib;
import prefuse.util.display.DisplayLib;
import prefuse.visual.VisualItem;

/**
 *
 * @author iang
 */
public class LuaDeskSpot extends javax.swing.JFrame {

    public static final int FRAGMENT_SIZE = 256;
    private File installFile;
    private byte[] installData;
    private int installSize;
    private int installFragments;
    private int installSent;
    private int installOffset;
    private int installRemaining;

    public static String ADDRESS;
    
    /** Creates new form Main */
    public LuaDeskSpot() {
        startReceiverThread();
        initComponents();
        initPrefuse();
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

                        int sum=0;
                        for (int i=0; i<bdata.length; i++) {
                            sum = (sum + bdata[i]) % 16777216;
                        }

                        logMonitor("New connection from: " + con_addr);
                        logMonitor("Received: data.len=" + data.length() + " from " + con_addr + " sum=" + sum);

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

        if (delete) {
            sb.delete(0, len);
            if (sb.length() > 0) {
                sb.delete(0, 1);
            }
        }

        return new String(chars);
    }

    private void processMessage(String addr, String data) {
        log(txtSniffer, "[" + addr + "] " + data);

        StringBuffer sb = new StringBuffer(data);
        if ("c".equals(getNextToken(sb, true))) {
            String app = getNextToken(sb, true);
            String func = getNextToken(sb, true);

            if ("demo".equals(app)) {
                if ("pong".equals(func)) {
                    log(txtMonitorPing, "Ping response from " + addr);
                }
                else if ("connection".equals(func)) {
                    String src = addr;
                    String dst = getNextToken(sb, true);
                    logMonitor("Connection: " + src + " - " + dst);
                    addEdge(src, dst);
                }
                else if ("add".equals(func)) {

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
            else if ("debug".equals(app)) {
                if ("print".equals(func)) {
                    log(txtDebug, "[from=" + addr + "] " + sb.toString());
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
                    logMonitor("[sender] Sending data to " + address + ", len=" + data.length());
                    dg.reset();

                    char[] cdata = data.toCharArray();
                    byte[] bdata = new byte[cdata.length];
                    for (int i=0; i<cdata.length; i++) {
                        bdata[i] = (byte)cdata[i];
                    }

                    int sum=0;
                    for (int i=0; i<bdata.length; i++) {
                        sum = (sum + (bdata[i] & 0xFF)) % 16777216;
                    }
                    logMonitor("[sender] sum=" + sum);

                    hexdump("Outgoing data", bdata);

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

    public static void hexdump(String label, byte[] data) {
        System.out.println("== hexdump ============== " + label);
        for (int i=0; i<data.length; i++) {
            if (i % 32 == 0) { System.out.println(); }
            System.out.print(Integer.toHexString(data[i] & 0xFF) + " ");
        }
        System.out.println();
        System.out.println("=========================");
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

    private Visualization vis;
    private Display display;
    private Graph graph;
    private Table nodes;
    private Hashtable labelNodes;

    private void initPrefuse() {
        labelNodes = new Hashtable();

        nodes = new Table();
        nodes.addColumn("val", String.class);

        graph = new Graph(nodes, false);

        vis = new Visualization();
        vis.add("graph", graph);

        LabelRenderer label = new LabelRenderer("val");
        label.setRoundedCorner(8, 8);
        vis.setRendererFactory(new DefaultRendererFactory(label));

        ColorAction edges = new ColorAction("graph.edges", VisualItem.STROKECOLOR, ColorLib.gray(200));
        ColorAction text = new ColorAction("graph.nodes", VisualItem.TEXTCOLOR, ColorLib.gray(0));
        ColorAction fill = new ColorAction("graph.nodes", VisualItem.FILLCOLOR, ColorLib.rgb(200, 200, 255));

        ActionList color = new ActionList();
        color.add(edges);
        color.add(text);
        color.add(fill);
        vis.putAction("color", color);

        ActionList layout = new ActionList(-1);
        layout.add(new ForceDirectedLayout("graph", true));
        layout.add(new RepaintAction());
        vis.putAction("layout", layout);

        ActionList repaint = new ActionList();
        repaint.add(color);
        vis.putAction("repaint", color);

        display = new Display(vis);

        display.addControlListener(new DragControl());
        display.addControlListener(new PanControl());
        display.addControlListener(new ZoomControl());
        display.addControlListener(new ZoomToFitControl());
        display.setSize(panelTopology.getSize());

        panelTopology.add(display);

        vis.run("layout");
    }

    private void centerGraph() {
        int margin = 50;
        int duration = 2000;

        Rectangle2D bounds = vis.getBounds("graph");
        GraphicsLib.expand(bounds, margin + (int)(1/display.getScale()));
        DisplayLib.fitViewToBounds(display, bounds, duration);
    }

    private void repaintGraph() {
        vis.run("repaint");
        System.out.println("repaint");
        centerGraph();
    }

    private Node addNode(String label) {
        Node n = (Node)labelNodes.get(label);
        if (n == null) {
            n = graph.addNode();
            n.set("val", label);
            labelNodes.put(label, n);
        }
        return n;
    }

    private Edge addEdge(Node n1, Node n2) {
        Edge e = graph.addEdge(n1, n2);
        return e;
    }

    private void addEdge(String s1, String s2) {
        Node n1 = addNode(s1);
        Node n2 = addNode(s2);
        addEdge(n1, n2);
        repaintGraph();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtMonitor = new javax.swing.JTextArea();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtMsg = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        txtAddr = new javax.swing.JTextField();
        jButton10 = new javax.swing.JButton();
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
        jButton11 = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        panelTopology = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        txtDebug = new javax.swing.JTextArea();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane = new javax.swing.JScrollPane();
        txtSniffer = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("LuaDeskSpot");

        jSplitPane1.setBorder(null);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        txtMonitor.setColumns(20);
        txtMonitor.setRows(5);
        jScrollPane1.setViewportView(txtMonitor);

        jSplitPane1.setRightComponent(jScrollPane1);

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

        txtAddr.setText("broadcast");

        jButton10.setText("Direct Send");
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                        .add(jButton10)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButton1))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel2)
                            .add(jLabel1))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(txtMsg, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 485, Short.MAX_VALUE)
                            .add(txtAddr, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 485, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(txtMsg, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(txtAddr, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButton1)
                    .add(jButton10))
                .addContainerGap(158, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Raw message", jPanel1);

        jLabel3.setText("Address");

        jButton2.setText("Direct Send");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        txtPingAddr.setText("broadcast");

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

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 549, Short.MAX_VALUE)
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(jLabel3)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(txtPingAddr, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 298, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButton2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButton4))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jButton3))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(jButton2)
                    .add(txtPingAddr, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jButton4))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jButton3)
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

        txtInstallAddr.setText("broadcast");

        jButton6.setText("Load");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jLabel6.setText("Size");

        jLabel7.setText("Fragments");

        lblInstallSize.setText("..");

        lblInstallFragments.setText("..");

        jButton7.setText("Send next fragment");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jLabel10.setText("Sent");

        lblInstallSent.setText("..");

        jLabel8.setText("Name");

        jButton11.setText("Send next fragment (direct)");
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel3Layout.createSequentialGroup()
                        .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(jLabel10, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(jLabel7, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(jLabel6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 84, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lblInstallSent, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 249, Short.MAX_VALUE)
                            .add(lblInstallFragments, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 249, Short.MAX_VALUE)
                            .add(lblInstallSize, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 249, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(jButton7)
                            .add(jButton11)))
                    .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 549, Short.MAX_VALUE)
                    .add(jPanel3Layout.createSequentialGroup()
                        .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel8, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel5))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3Layout.createSequentialGroup()
                                .add(txtInstallFile, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 309, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jButton5)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jButton6))
                            .add(txtInstallAddr, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 474, Short.MAX_VALUE)
                            .add(txtInstallName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 474, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(txtInstallFile, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jButton6)
                    .add(jButton5))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(txtInstallAddr, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel8)
                    .add(txtInstallName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel6)
                    .add(lblInstallSize)
                    .add(jButton7))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel7)
                    .add(lblInstallFragments)
                    .add(jButton11))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel10)
                    .add(lblInstallSent))
                .addContainerGap(41, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Install", jPanel3);

        jButton8.setText("Clear");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        jButton9.setText("Discover");
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });

        panelTopology.setBackground(new java.awt.Color(255, 255, 255));
        panelTopology.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                panelTopologyComponentResized(evt);
            }
        });

        org.jdesktop.layout.GroupLayout panelTopologyLayout = new org.jdesktop.layout.GroupLayout(panelTopology);
        panelTopology.setLayout(panelTopologyLayout);
        panelTopologyLayout.setHorizontalGroup(
            panelTopologyLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 549, Short.MAX_VALUE)
        );
        panelTopologyLayout.setVerticalGroup(
            panelTopologyLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 191, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(panelTopology, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jPanel4Layout.createSequentialGroup()
                        .add(jButton8)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButton9)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButton8)
                    .add(jButton9))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(panelTopology, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Topology", jPanel4);

        txtDebug.setColumns(20);
        txtDebug.setRows(5);
        jScrollPane3.setViewportView(txtDebug);

        org.jdesktop.layout.GroupLayout jPanel5Layout = new org.jdesktop.layout.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 549, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Debug", jPanel5);

        txtSniffer.setColumns(20);
        txtSniffer.setRows(5);
        jScrollPane.setViewportView(txtSniffer);

        org.jdesktop.layout.GroupLayout jPanel6Layout = new org.jdesktop.layout.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 549, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Sniffer", jPanel6);

        jSplitPane1.setLeftComponent(jTabbedPane1);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 650, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 610, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 464, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE)
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
        } else {
            send(addr, msg);
        }
}//GEN-LAST:event_jButton1ActionPerformed

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        String msg = txtMsg.getText();
        String addr = txtAddr.getText();
        if ((msg.length() == 0) || (addr.length() == 0)) {
            JOptionPane.showMessageDialog(this, "Please enter message and address.", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            sendRaw(addr, msg);
        }
}//GEN-LAST:event_jButton10ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        String addr = txtPingAddr.getText();
        if (addr.length() == 0) {
            JOptionPane.showMessageDialog(this, "Please enter the destination address", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            String msg = "c demo ping";
            sendRaw(addr, msg);
        }
}//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        txtMonitorPing.setText("");
}//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        String addr = txtPingAddr.getText();
        if (addr.length() == 0) {
            JOptionPane.showMessageDialog(this, "Please enter the destination address", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            String msg = "c demo ping";
            send(addr, msg);
        }
}//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        JFileChooser jfc = new JFileChooser();
        jfc.showOpenDialog(this);
        File f = jfc.getSelectedFile();
        txtInstallFile.setText(f.getAbsolutePath());
}//GEN-LAST:event_jButton5ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed

        installFile = new File(txtInstallFile.getText());
        installSize = (int)installFile.length();
        installFragments = (int)Math.ceil((double)installSize / FRAGMENT_SIZE);
        installSent = 0;
        installOffset = 0;
        installRemaining = installSize;

        installData = new byte[installSize];
        try {
            FileInputStream fis = new FileInputStream(installFile);
            fis.read(installData);

            int sum = 0;
            for (int i=0; i<installData.length; i++) {
                sum = (sum + installData[i]) % 16777216;
            }
            System.out.println("[install data] len=" + installData.length + ", sum=" +sum);

        } catch (IOException e) {
            System.out.println(e.toString());
        }

        lblInstallSize.setText(installSize + " Byte");
        lblInstallFragments.setText("" + installFragments);
        lblInstallSent.setText(""+installSent);
}//GEN-LAST:event_jButton6ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        installSendNext(false);
    }

    private void installSendNext(boolean direct) {
        String addr = txtInstallAddr.getText();
        String app = txtInstallName.getText();

        if (installSent < installFragments) {
            int seg = installSent;
            int lseg = installFragments - 1;

            int size;
            if (installRemaining < FRAGMENT_SIZE) {
                size = installRemaining;
            } else {
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
                sb.append((char)(data[i] & 0xFF));
            }

            if (direct) {
                sendRaw(addr, sb.toString());
            } else {
                send(addr, sb.toString());
            }

            installSent++;
            lblInstallSent.setText(""+installSent);

            installRemaining -= size;
            installOffset += size;
        }
}//GEN-LAST:event_jButton7ActionPerformed

    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
        installSendNext(true);
}//GEN-LAST:event_jButton11ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        graph.clear();
        labelNodes.clear();
        repaintGraph();
}//GEN-LAST:event_jButton8ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        send("broadcast", "c demo discover");
}//GEN-LAST:event_jButton9ActionPerformed

    private void panelTopologyComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_panelTopologyComponentResized
        if (display != null) {
            display.setSize(panelTopology.getSize());
            repaintGraph();
        }
}//GEN-LAST:event_panelTopologyComponentResized

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
        new LuaDeskSpot().setVisible(true);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
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
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel lblInstallFragments;
    private javax.swing.JLabel lblInstallSent;
    private javax.swing.JLabel lblInstallSize;
    private javax.swing.JPanel panelTopology;
    private javax.swing.JTextField txtAddr;
    private javax.swing.JTextArea txtDebug;
    private javax.swing.JTextField txtInstallAddr;
    private javax.swing.JTextField txtInstallFile;
    private javax.swing.JTextField txtInstallName;
    private javax.swing.JTextArea txtMonitor;
    private javax.swing.JTextArea txtMonitorPing;
    private javax.swing.JTextField txtMsg;
    private javax.swing.JTextField txtPingAddr;
    private javax.swing.JTextArea txtSniffer;
    // End of variables declaration//GEN-END:variables

}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.johngillespie.cryptid;

/**
 *
 * @author jesgi
 */
// Main Application Class
import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.WWUtil;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.List;
import java.util.Timer;

import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;


public class CryptidTracker extends JFrame {
    private WorldWindowGLCanvas wwd;
    private RenderableLayer sightingLayer;
    private List<Sighting> sightings;
    private JTable sightingTable;
    private DefaultTableModel tableModel;
    private Timer animationTimer;
    private long animationStartTime;
    private boolean isPlaying = false;
    private JButton playButton;
    
    
    // Begin kode update
    private static final String SIGHTING_KEY = "CRYPTID_SIGHTING";
    // End kode update
    
    public CryptidTracker() {
        super("CryptidTracker - WorldWind Sighting Viewer");
        sightings = new ArrayList<>();
        initComponents();
        setupWorldWind();
    }
    
    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLayout(new BorderLayout());
        
        // Create WorldWind canvas
        // get jar files from https: //jogamp.org/deployment/jogamp-current/jar/
        wwd = new WorldWindowGLCanvas();
        wwd.setPreferredSize(new Dimension(1000, 900));
        
        // Create control panel
        JPanel controlPanel = createControlPanel();
        
        // Add components
        add(wwd, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.EAST);
        
        setLocationRelativeTo(null);
    }
    
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(400, 900));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Title
        JLabel title = new JLabel("Sighting Manager", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 5, 5));
        
        JButton importBtn = new JButton("Import CSV");
        importBtn.addActionListener(e -> importSightings());
        
        playButton = new JButton("Play");
        playButton.setEnabled(false);
        playButton.addActionListener(e -> toggleAnimation());
        
        JButton resetBtn = new JButton("Reset");
        resetBtn.addActionListener(e -> resetAnimation());
        
        buttonPanel.add(importBtn);
        buttonPanel.add(playButton);
        buttonPanel.add(resetBtn);
        
        // Table
        String[] columns = {"Date", "Type", "Lat", "Lon"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        sightingTable = new JTable(tableModel);
        sightingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sightingTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editSelectedSighting();
                }
            }
        });
        
        JScrollPane tableScroll = new JScrollPane(sightingTable);
        
        // Instructions
        JTextArea instructions = new JTextArea(
            "Instructions:\n" +
            "1. Import a CSV file (date,type,lat,lon)\n" +
            "2. Double-click a sighting to edit icon and waypoints\n" +
            "3. Click Play to animate\n" +
            "4. Use mouse to navigate globe:\n" +
            "   - Left drag: rotate\n" +
            "   - Right drag: pan\n" +
            "   - Scroll: zoom"
        );
        instructions.setEditable(false);
        instructions.setWrapStyleWord(true);
        instructions.setLineWrap(true);
        instructions.setBackground(panel.getBackground());
        instructions.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        // Assemble panel
        panel.add(title, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(buttonPanel, BorderLayout.NORTH);
        centerPanel.add(tableScroll, BorderLayout.CENTER);
        centerPanel.add(instructions, BorderLayout.SOUTH);
        
        panel.add(centerPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void setupWorldWind() {
        Model m = (Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
        wwd.setModel(m);
        
        // Begin kode update
        //WorldWind does not generate mouse events for renderables automatically, so I am adding a WorldWind select event here
        // then I'm gonna store a reference ( pm.setValue(AVKey.USER_OBJECT, s); ) over in updateGlobe and updateAnimation
        // then I'll add a handlePlacemarkDoubleClick method so that double-clicking the icon opens the info in the editor
        
        
        // this is the Lambda listener - not gonna use this
     //   wwd.addSelectListener(event -> {
     //       if (event.getEventAction().equals(SelectEvent.LEFT_DOUBLE_CLICK)) {
     //           Object top = event.getTopObject();
     //           if (top instanceof PointPlacemark) {
     //               handlePlacemarkDoubleClick((PointPlacemark) top);
     //           }
     //       }
     //   });
        
     // this is the anonymous class listener
    wwd.addSelectListener(new SelectListener() {
        @Override
        public void selected(SelectEvent event) {
            if (SelectEvent.LEFT_DOUBLE_CLICK.equals(event.getEventAction())) {
                Object top = event.getTopObject();
                if (top instanceof PointPlacemark) {
                    handlePlacemarkDoubleClick((PointPlacemark) top);
                }
            }
        }
    });

        // End kode update
        
        // Add basic layers
        LayerList layers = m.getLayers();
        layers.add(new CompassLayer());
        layers.add(new ScalebarLayer());
        
        // Create sighting layer
        sightingLayer = new RenderableLayer();
        sightingLayer.setName("Sightings");
        layers.add(sightingLayer);
    }
    
    private void importSightings() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
        
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                loadCSV(fc.getSelectedFile());
                JOptionPane.showMessageDialog(this, 
                    "Imported " + sightings.size() + " sightings", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                playButton.setEnabled(!sightings.isEmpty());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "Error importing file: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void loadCSV(File file) throws IOException {
        sightings.clear();
        tableModel.setRowCount(0);
        sightingLayer.removeAllRenderables();
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    if (line.toLowerCase().contains("date")) continue;
                }
                
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    Date date = sdf.parse(parts[0].trim());
                    String type = parts[1].trim();
                    double lat = Double.parseDouble(parts[2].trim());
                    double lon = Double.parseDouble(parts[3].trim());
                    
                    Sighting s = new Sighting(date, type, lat, lon);
                    sightings.add(s);
                    
                    tableModel.addRow(new Object[]{
                        sdf.format(date), type, 
                        String.format("%.4f", lat), 
                        String.format("%.4f", lon)
                    });
                }
            }
        } catch (Exception e) {
            throw new IOException("Failed to parse CSV: " + e.getMessage());
        }
        
        updateGlobe();
    }
    
    private void editSelectedSighting() {
        int row = sightingTable.getSelectedRow();
        if (row >= 0) {
            Sighting s = sightings.get(row);
            SightingEditorDialog dlg = new SightingEditorDialog(this, s);
            dlg.setVisible(true);
            updateGlobe();
        }
    }
    
    private void updateGlobe() {
        sightingLayer.removeAllRenderables();
        
        for (Sighting s : sightings) {
            Position pos = Position.fromDegrees(s.lat, s.lon, 0);
            
            PointPlacemark pm = new PointPlacemark(pos);
            pm.setLabelText(s.type);
            pm.setLineEnabled(false);
            pm.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
            
            
            
            
            PointPlacemarkAttributes attrs = new PointPlacemarkAttributes();
            attrs.setImageColor(s.getIconColor());
            
            // Begin kode update
            pm.setValue(SIGHTING_KEY, s);

          //  pm.setValue(AVKey.USER_OBJECT, s);
            // End kode update
            
            
            //attrs.setScale(14.0);
            attrs.setScale(0.5);
            //attrs.setImageAddress("gov/nasa/worldwindx/examples/images/pushpins/" + s.getIconFile());
            attrs.setImageAddress("resources/images/pushpins/" + s.getIconFile());
            pm.setAttributes(attrs);
            
            sightingLayer.addRenderable(pm);
        }
        
        wwd.redraw();
    }
    
    
    // Begin kode update
    private void handlePlacemarkDoubleClick(PointPlacemark pm) {
    Object obj = pm.getValue(SIGHTING_KEY);
    if (obj instanceof Sighting) {
        Sighting s = (Sighting) obj;
        SightingEditorDialog dlg = new SightingEditorDialog(this, s);
        dlg.setVisible(true);
        updateGlobe();
    }
}

    //End kode update

    private void toggleAnimation() {
        if (isPlaying) {
            stopAnimation();
        } else {
            startAnimation();
        }
    }
    
    private void startAnimation() {
        isPlaying = true;
        playButton.setText("Pause");
        animationStartTime = System.currentTimeMillis();
        
        animationTimer = new Timer();
        animationTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                updateAnimation();
            }
        }, 0, 50); // 20 FPS
    }
    
    private void stopAnimation() {
        isPlaying = false;
        playButton.setText("Play");
        if (animationTimer != null) {
            animationTimer.cancel();
        }
    }
    
    private void resetAnimation() {
        stopAnimation();
        updateGlobe();
    }
    
    private void updateAnimation() {
        long elapsed = System.currentTimeMillis() - animationStartTime;
        double hours = elapsed / 1000.0; // 1 sec = 1 hour for demo
        
        sightingLayer.removeAllRenderables();
        
        for (Sighting s : sightings) {
            Position pos = s.getPositionAtTime(hours);
            
            PointPlacemark pm = new PointPlacemark(pos);
            pm.setLabelText(s.type);
            pm.setLineEnabled(false);
            pm.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
            
            PointPlacemarkAttributes attrs = new PointPlacemarkAttributes();
            attrs.setImageColor(s.getIconColor());
            
            // Begin kode update
            pm.setValue(SIGHTING_KEY, s);

       //     pm.setValue(AVKey.USER_OBJECT, s);
            // End kode update
            
            
            //attrs.setScale(14.0);
            attrs.setScale(0.5);
            //attrs.setImageAddress("gov/nasa/worldwindx/examples/images/pushpins/" + s.getIconFile());
            attrs.setImageAddress("resources/images/pushpins/" + s.getIconFile());
            pm.setAttributes(attrs);
            
            sightingLayer.addRenderable(pm);
        }
        
        SwingUtilities.invokeLater(() -> wwd.redraw());
    }
    
    public static void main(String[] args) {
        
        //System.out.println(System.getProperty("os.arch"));
        System.out.println("java.library.path = " + System.getProperty("java.library.path"));

        
        SwingUtilities.invokeLater(() -> {
            new CryptidTracker().setVisible(true);
        }); 
    }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.johngillespie.cryptid;


import javax.swing.*;

import java.awt.*;


/**
 *
 * @author jesgi
 */
// Editor Dialog
class SightingEditorDialog extends JDialog {
    private Sighting sighting;
    private JComboBox<String> iconCombo;
    private DefaultListModel<String> waypointListModel;
    private JList<String> waypointList;
    
    public SightingEditorDialog(Frame owner, Sighting s) {
        super(owner, "Edit Sighting", true);
        this.sighting = s;
        initUI();
        pack();
        setLocationRelativeTo(owner);
    }
    
    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Info panel
        JPanel infoPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        infoPanel.add(new JLabel("Type:"));
        infoPanel.add(new JLabel(sighting.type));
        infoPanel.add(new JLabel("Location:"));
        infoPanel.add(new JLabel(String.format("%.4f, %.4f", sighting.lat, sighting.lon)));
        
        infoPanel.add(new JLabel("Icon:"));
        String[] icons = {"Red Pin", "Blue Pin", "Green Pin", "Yellow Pin", "Purple Pin"};
        iconCombo = new JComboBox<>(icons);
        iconCombo.addActionListener(e -> updateIcon());
        infoPanel.add(iconCombo);
        
        // Waypoints panel
        JPanel waypointPanel = new JPanel(new BorderLayout(5, 5));
        waypointPanel.setBorder(BorderFactory.createTitledBorder("Movement Waypoints"));
        
        waypointListModel = new DefaultListModel<>();
        for (Waypoint wp : sighting.waypoints) {
            waypointListModel.addElement(String.format("%.4f, %.4f @ %.1fh", 
                wp.lat, wp.lon, wp.timeHours));
        }
        
        waypointList = new JList<>(waypointListModel);
        JScrollPane wpScroll = new JScrollPane(waypointList);
        wpScroll.setPreferredSize(new Dimension(350, 200));
        
        JPanel wpButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("Add");
        addBtn.addActionListener(e -> addWaypoint());
        JButton removeBtn = new JButton("Remove");
        removeBtn.addActionListener(e -> removeWaypoint());
        wpButtonPanel.add(addBtn);
        wpButtonPanel.add(removeBtn);
        
        waypointPanel.add(wpScroll, BorderLayout.CENTER);
        waypointPanel.add(wpButtonPanel, BorderLayout.SOUTH);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okBtn = new JButton("OK");
        okBtn.addActionListener(e -> dispose());
        buttonPanel.add(okBtn);
        
        contentPanel.add(infoPanel, BorderLayout.NORTH);
        contentPanel.add(waypointPanel, BorderLayout.CENTER);
        
        add(contentPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void updateIcon() {
        int idx = iconCombo.getSelectedIndex();
        String[] files = {"plain-red.png", "plain-blue.png", "plain-green.png", 
                         "plain-yellow.png", "plain-purple.png"};
        Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, 
                         new Color(128, 0, 128)};
        
        sighting.iconFile = files[idx];
        sighting.iconColor = colors[idx];
    }
    
    private void addWaypoint() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        JTextField latField = new JTextField(String.format("%.4f", sighting.lat));
        JTextField lonField = new JTextField(String.format("%.4f", sighting.lon));
        JTextField timeField = new JTextField("1.0");
        
        panel.add(new JLabel("Latitude:"));
        panel.add(latField);
        panel.add(new JLabel("Longitude:"));
        panel.add(lonField);
        panel.add(new JLabel("Time (hours):"));
        panel.add(timeField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, 
            "Add Waypoint", JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                double lat = Double.parseDouble(latField.getText());
                double lon = Double.parseDouble(lonField.getText());
                double time = Double.parseDouble(timeField.getText());
                
                Waypoint wp = new Waypoint(lat, lon, time);
                sighting.waypoints.add(wp);
                waypointListModel.addElement(String.format("%.4f, %.4f @ %.1fh", 
                    lat, lon, time));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid number format", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void removeWaypoint() {
        int idx = waypointList.getSelectedIndex();
        if (idx >= 0) {
            sighting.waypoints.remove(idx);
            waypointListModel.remove(idx);
        }
    }
}
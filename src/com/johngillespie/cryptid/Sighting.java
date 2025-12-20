/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.johngillespie.cryptid;

import gov.nasa.worldwind.geom.*;

import java.awt.*;

import java.util.*;
import java.util.List;

/**
 *
 * @author jesgi
 */
// Sighting data class
class Sighting {
    Date date;
    String type;
    double lat, lon;
    String iconFile = "plain-red.png";
    Color iconColor = Color.RED;
    List<Waypoint> waypoints;
    
    public Sighting(Date date, String type, double lat, double lon) {
        this.date = date;
        this.type = type;
        this.lat = lat;
        this.lon = lon;
        this.waypoints = new ArrayList<>();
    }
    
    public Color getIconColor() {
        return iconColor;
    }
    
    public String getIconFile() {
        return iconFile;
    }
    
    public Position getPositionAtTime(double hours) {
        if (waypoints.isEmpty()) {
            return Position.fromDegrees(lat, lon, 0);
        }
        
        // Find current segment
        for (int i = 0; i < waypoints.size(); i++) {
            Waypoint wp = waypoints.get(i);
            if (hours < wp.timeHours) {
                // Interpolate between previous and current
                double prevLat = (i == 0) ? lat : waypoints.get(i-1).lat;
                double prevLon = (i == 0) ? lon : waypoints.get(i-1).lon;
                double prevTime = (i == 0) ? 0 : waypoints.get(i-1).timeHours;
                
                double t = (hours - prevTime) / (wp.timeHours - prevTime);
                double currLat = prevLat + t * (wp.lat - prevLat);
                double currLon = prevLon + t * (wp.lon - prevLon);
                
                return Position.fromDegrees(currLat, currLon, 0);
            }
        }
        
        // Past all waypoints, use last position
        Waypoint last = waypoints.get(waypoints.size() - 1);
        return Position.fromDegrees(last.lat, last.lon, 0);
    }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.johngillespie.cryptid;

/**
 *
 * @author jesgi
 */
class Waypoint {
    double lat, lon;
    double timeHours;
    
    public Waypoint(double lat, double lon, double timeHours) {
        this.lat = lat;
        this.lon = lon;
        this.timeHours = timeHours;
    }
}
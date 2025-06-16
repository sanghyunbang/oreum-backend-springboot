package com.oreum.external.kma.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GridCoordinateWithLatLon {
    private double lat;
    private double lon;
    private int nx;
    private int ny;

    public GridCoordinateWithLatLon(double lat, double lon, int nx, int ny){
        this.lat = lat;
        this.lon = lon;
        this.nx = nx;
        this.ny = ny;
    }
    
}

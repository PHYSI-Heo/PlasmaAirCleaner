package com.physis.nrf.plasma.data;

public class CleanerData {
    private String name, address;

    public CleanerData(String name, String address){
        this.name = name;
        this.address = address;
    }

    public String getName(){
        return name;
    }

    public String getAddress() {
        return address;
    }
}

package org.cic.datacollection.model;

public class Site {
    private String desc;
    private String ipAddress;
    private int port;

    public Site() {

    }

    public Site(String desc, String ipAddress, int port) {
        this.desc = desc;
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}

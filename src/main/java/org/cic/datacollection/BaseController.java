package org.cic.datacollection;

import org.cic.datacollection.config.DruidConfig;
import org.springframework.web.context.WebApplicationContext;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class BaseController{
    public Object handleServer = null;
    private String port = "8020";

    public Object getHandleServer () {
        if (((WebApplicationContext) DruidConfig.getCtx()).getServletContext().getAttribute("net.handle.server.HandleServerExtend") != null) {
            handleServer = ((WebApplicationContext) DruidConfig.getCtx()).getServletContext().getAttribute("net.handle.server.HandleServerExtend");
        }
        return handleServer;
    }

    public Object getPort(){
        if (((WebApplicationContext) DruidConfig.getCtx()).getServletContext().getAttribute("port") != null) {
            port = ((WebApplicationContext) DruidConfig.getCtx()).getServletContext().getAttribute("port").toString();
        }
        return port;
    }

    public String getIp(){
        InetAddress localHost = null;
        try {
            localHost = Inet4Address.getLocalHost();
        } catch (UnknownHostException e) {

        }
        if (localHost != null) {
            return localHost.getHostAddress();
        } else {
            return "";
        }

    }
}

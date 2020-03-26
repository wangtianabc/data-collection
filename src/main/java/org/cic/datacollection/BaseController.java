package org.cic.datacollection;

import net.handle.hdllib.Interface;
import net.handle.server.HandleServer;
import org.cic.datacollection.config.DruidConfig;
import org.springframework.web.context.WebApplicationContext;

public class BaseController{
    private static Object handleServer = null;
    private static Object handleResolver = null;

    public static Object getHandleServer () {
        if (BaseController.handleServer == null && ((WebApplicationContext) DruidConfig.getCtx()).getServletContext().getAttribute("net.handle.server.HandleServer") != null) {
            BaseController.handleServer = ((WebApplicationContext) DruidConfig.getCtx()).getServletContext().getAttribute("net.handle.server.HandleServer");
        }
        return BaseController.handleServer;
    }

    public static Object getHandleResolver () {
        if (BaseController.handleResolver == null && ((WebApplicationContext) DruidConfig.getCtx()).getServletContext().getAttribute("net.handle.hdllib.HandleResolver") != null) {
            BaseController.handleResolver = ((WebApplicationContext) DruidConfig.getCtx()).getServletContext().getAttribute("net.handle.hdllib.HandleResolver");
        }
        return BaseController.handleResolver;
    }

    public int getPort(){
        int port = 8000;
        try {
            Interface[] interfaces = ((HandleServer)BaseController.getHandleServer()).getSiteInfo().servers[0].interfaces;
            for (int i = 0; i < interfaces.length; i ++) {
                if (interfaces[i].protocol == Interface.SP_HDL_HTTP || interfaces[i].protocol == Interface.SP_HDL_HTTPS) {
                    port = interfaces[i].port;
                }
            }
        } catch (Exception e) {

        }
        return port;
    }

    public String getIp(){
        return ((HandleServer)BaseController.getHandleServer()).getSiteInfo().servers[0].getAddressString();
    }
}

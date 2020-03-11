package org.cic.datacollection.controller;

import io.swagger.annotations.ApiOperation;
import net.handle.apps.simple.SiteInfoConverter;
import net.handle.hdllib.*;
import org.apache.http.client.methods.HttpGet;
import org.cic.datacollection.BaseController;
import org.cic.datacollection.model.HandleCollection;
import org.cic.datacollection.model.HandleModel;
import org.cic.datacollection.model.Handles;
import org.cic.datacollection.model.Site;
import org.cic.datacollection.repository.HandleCollectionRepository;
import org.cic.datacollection.repository.HandleRepository;
import org.cic.datacollection.util.UtilFunc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class DataCollectionController extends BaseController {

    @Autowired
    private HandleCollectionRepository handleCollectionRepository;

    @Autowired
    private HandleRepository handleRepository;

    @ApiOperation(value="测试", notes="测试", hidden = true)
    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public String index() {

        if (this.getHandleServer() == null) {
            return "server is null" + "url:" + this.getIp() + ":" + this.getPort();
        } else {
            return "hello:" + this.getHandleServer().getClass().getName() + "url:" + this.getIp() + ":" + this.getPort();
        }
    }

    /**
     * 获取下级站点信息列表
     * @return
     */
    @ApiOperation(value="获取下级站点", notes="主要用于二级站点采集企业节点注册信息")
    @RequestMapping(value = "/subsitelist", method = RequestMethod.GET)
    public Map<String, Object> getSubSiteList() {
        List<Handles> handles = handleRepository.findByIdx(200L);
        Map<String, Object> map = new HashMap<>();
        List<Site> siteList = new ArrayList<>();
        if (handles != null && handles.size() > 0) {
            for (Handles handle : handles) {
                byte[] data = UtilFunc.getBytes(handle.getData());
                SiteInfo siteinfo = new SiteInfo();
                Site site = new Site();
                try {
                    if (Util.looksLikeBinary(data)) {
                        Encoder.decodeSiteInfoRecord(data, 0, siteinfo);
                    } else {
                        SiteInfo converted = SiteInfoConverter.convertToSiteInfo(new String(data, "UTF-8"));
                        if (converted != null) siteinfo = converted;
                    }
                } catch (Exception e) {
                    continue;
                }

                int port = 0;
                for (int i = 0; i < siteinfo.servers[0].interfaces.length; i ++) {
                    Interface inter = (Interface) siteinfo.servers[0].interfaces[i];
                    if (inter.protocol == Interface.SP_HDL_HTTP || inter.protocol == Interface.SP_HDL_HTTPS) {
                        port = inter.port;
                    }
                }
                site.setPort(port);
                site.setIpAddress(siteinfo.servers[0].getAddressString());
                site.setDesc(Util.decodeString(siteinfo.getAttribute(Util.encodeString("desc"))));
                siteList.add(site);
            }
        } else {
            map.put("result", "no sub site");
        }
        map.put("result", siteList);
        return map;
    }

    @ApiOperation(value="获取handle操作记录", notes="用于查询当前站点下的handle操作记录")
    @RequestMapping(value = "/collections", method = RequestMethod.GET)
    public Map<String, Object> getCollection() {
        List<HandleCollection> handleCollections = handleCollectionRepository.findAll();
        Map<String, Object> map = new HashMap<>();
        if (handleCollections != null && handleCollections.size() > 0) {
            map.put("result", handleCollections);
        } else {
            map.put("result", "nothing");
        }
        return map;
    }

    @ApiOperation(value="获取handle信息", notes="循环调用handle查询接口，获取handle具体内容")
    @RequestMapping(value = "/handles", method = RequestMethod.GET)
    public Map<String, Object> getHandlesData() {
        List<HandleCollection> handleCollections = handleCollectionRepository.findPartHandleCollections();
        Map<String, Object> map = new HashMap<>();
        if (handleCollections != null && handleCollections.size() > 0) {
            HttpGet[] requests = new HttpGet[handleCollections.size()];
            List<HandleModel> handleRecordList = new ArrayList<>();
            for (int i = 0; i < handleCollections.size(); i ++) {
                requests[i] = new HttpGet("http://" + this.getIp() + ":" + this.getPort() + "/api/handles/" + handleCollections.get(i).getHandle() + "?enhance=1");
                requests[i].setHeader("opt", handleCollections.get(i).getOperate());
            }
            try {
                handleRecordList = UtilFunc.getValueFromHttp(requests, handleRecordList);
                map.put("result", handleRecordList);
            } catch (Exception e) {
                map.put("result", "http error");
            }
        } else {
            map.put("result", "nothing");
        }
        return map;
    }

    @ApiOperation(value="获取handle信息", notes="根据元数据handle，获取handle具体内容")
    @RequestMapping(value = "/refhandle/{prefix}/**", method = RequestMethod.GET)
    public Map<String, Object> getCollectionByRefHandle(@PathVariable String prefix, HttpServletRequest request) {
        String suffix = UtilFunc.extractPathFromPattern(request);
        String refHandle = prefix + "/" + suffix;
        List<HandleCollection> handleCollections = handleCollectionRepository.findHandleCollectionByRefHandle(refHandle);

        Map<String, Object> map = new HashMap<>();
        if (handleCollections != null && handleCollections.size() > 0) {
            HttpGet[] requests = new HttpGet[handleCollections.size()];
            List<HandleModel> handleRecordList = new ArrayList<>();
            for (int i = 0; i < handleCollections.size(); i ++) {
                requests[i] = new HttpGet("http://" + this.getIp() + ":" + this.getPort() + "/api/handles/" + handleCollections.get(i).getHandle() + "?enhance=1");
                requests[i].setHeader("opt", handleCollections.get(i).getOperate());
            }
            try {
                handleRecordList = UtilFunc.getValueFromHttp(requests, handleRecordList);
                map.put("result", handleRecordList);
            } catch (Exception e) {
                map.put("result", "http error");
            }
        } else {
            map.put("result", "nothing");
        }
        return map;
    }

}

package org.cic.datacollection.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.handle.apps.simple.SiteInfoConverter;
import net.handle.hdllib.Encoder;
import net.handle.hdllib.Interface;
import net.handle.hdllib.SiteInfo;
import net.handle.hdllib.Util;
import org.apache.commons.lang3.StringUtils;
import org.cic.datacollection.model.CollectionData;
import org.cic.datacollection.model.HandleModel;
import org.cic.datacollection.model.Handles;
import org.cic.datacollection.model.Site;
import org.cic.datacollection.repository.HandleRepository;
import org.cic.datacollection.service.CollectionService;
import org.cic.datacollection.util.RestJsonApi;
import org.cic.datacollection.util.UtilFunc;
import org.cic.datacollection.vo.ResultHelper;
import org.cic.datacollection.vo.ResultInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class CollectionServiceImpl implements CollectionService {

    private Logger logger = LoggerFactory.getLogger(CollectionServiceImpl.class);
    private final ExecutorService executorService = Executors.newFixedThreadPool(50);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HandleRepository handleRepository;

    @Override
    public ResultInfo collectData() {
        List<Site> siteList = getSubSiteList();
        List<HandleModel> collectionDataList = Collections.synchronizedList(new ArrayList<>());

        final CountDownLatch latch = new CountDownLatch(siteList.size());
        siteList.forEach((site) -> {
            CollectExcute collectExcute = new CollectExcute(site,latch, collectionDataList,false,null);
            executorService.execute(collectExcute);
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            logger.error("采集异常:"+e.getMessage());
            return  ResultHelper.getSuccess(collectionDataList);
        }
        return ResultHelper.getSuccess(collectionDataList);
    }

    @Override
    public ResultInfo collectDataByMeta(String metaHandleCode) {
        List<Site> siteList = getSubSiteList();
        List<HandleModel> collectionDataList = Collections.synchronizedList(new ArrayList<>());
        final CountDownLatch latch = new CountDownLatch(siteList.size());
        siteList.forEach((site) -> {
            CollectExcute collectExcute = new CollectExcute(site,latch, collectionDataList,true,metaHandleCode);
            executorService.execute(collectExcute);
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            logger.error("采集异常:"+e.getMessage());
            return  ResultHelper.getSuccess(collectionDataList);
        }
        return ResultHelper.getSuccess(collectionDataList);
    }

    /**
     * 获取下级站点信息列表
     *
     * @return
     */
    private List<Site> getSubSiteList() {
        List<Handles> handles = handleRepository.findByIdx(200L);
        List<Site> siteList = new ArrayList<>();
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
            for (int i = 0; i < siteinfo.servers[0].interfaces.length; i++) {
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

        return siteList;
    }

    class CollectExcute implements Runnable {

        private final CountDownLatch latch;
        private final Site site;
        private final List<HandleModel> collectionDataList;
        private final boolean byMeta;
        private final String metahandle;

        public CollectExcute(Site site, CountDownLatch latch, List<HandleModel> collectionDataList,boolean byMeta,String metahandle) {
            this.site = site;
            this.metahandle = metahandle;
            this.byMeta = byMeta;
            this.latch = latch;
            this.collectionDataList = collectionDataList;
        }

        @Override
        public void run() {
            synchronized (this) {
                try {
                    String host = "http://" + this.site.getIpAddress() + ":" + this.site.getPort();
                    String path = (byMeta && StringUtils.isNotEmpty(this.metahandle))?"/data-api/refhandle/"+this.metahandle:"/data-api/handles";
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    ResultInfo resultInfo = RestJsonApi.doGet(host, path, headers, null, ResultInfo.class);
                    if (resultInfo.isOK()) {
                        Object result = resultInfo.getObj();
                        while(result instanceof ResultInfo ){
                            result = ((ResultInfo) result).getObj();
                        }
                        if(null != result){
                            this.collectionDataList.addAll((List<HandleModel>)result);
                        }
                    }
                } catch (Exception e) {
                    logger.error("采集异常:"+e.getMessage());
                    this.collectionDataList.add(null);
                } finally {
                    this.latch.countDown();
                }
            }
        }
    }

}

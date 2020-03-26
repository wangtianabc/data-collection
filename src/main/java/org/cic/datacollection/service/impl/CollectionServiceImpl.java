package org.cic.datacollection.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.handle.apps.admintool.controller.AuthenticationUtil;
import net.handle.apps.simple.SiteInfoConverter;
import net.handle.hdllib.*;
import org.apache.commons.lang3.StringUtils;
import org.cic.datacollection.BaseController;
import org.cic.datacollection.model.HandleCollection;
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
    public ResultInfo collectData(String handlePrefix, String handleSuffix, String password) {
        String authStr = "/" + handlePrefix + "/" + handleSuffix + "/" + password;
        List<Site> siteList = getSubSiteList();
        List<HandleModel> collectionDataList = Collections.synchronizedList(new ArrayList<>());

        final CountDownLatch latch = new CountDownLatch(siteList.size());
        siteList.forEach((site) -> {
            CollectExcute collectExcute = new CollectExcute(site,latch, collectionDataList,false,null, authStr);
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
    public ResultInfo collectDataByMeta(String handlePrefix, String handleSuffix, String password, String metaHandleCode) {
        String authStr = "/" + handlePrefix + "/" + handleSuffix + "/" + password;
        List<Site> siteList = getSubSiteList();
        List<HandleModel> collectionDataList = Collections.synchronizedList(new ArrayList<>());
        final CountDownLatch latch = new CountDownLatch(siteList.size());
        siteList.forEach((site) -> {
            CollectExcute collectExcute = new CollectExcute(site,latch, collectionDataList,true,metaHandleCode, authStr);
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
    public ResultInfo checkAuth(String handlePrefix, String handleSuffix, String password) {
        if (handlePrefix == null || handlePrefix.equals("") || handleSuffix == null || handleSuffix.equals("") || password == null || password.equals("")) {
            return ResultHelper.requestFaild("handle or password is null");
        }
        try {
            String[] id = handlePrefix.split(":");
            AuthenticationInfo auth = new SecretKeyAuthenticationInfo(Util.encodeString(id[1] + "/" + handleSuffix), Integer.parseInt(id[0]), Util.encodeString(password), false);
            CheckAuthentication checkAuth = new CheckAuthentication(auth);
            Thread thread = new Thread(checkAuth);
            thread.start();
            thread.join();
            if (checkAuth.wasSuccessful()) {
                //logger.info("auth success");
                ClientSessionTracker sessionTracker = new ClientSessionTracker();
                sessionTracker.setSessionSetupInfo(new SessionSetupInfo());
                ((HandleResolver) BaseController.getHandleResolver()).setSessionTracker(sessionTracker);
                return ResultHelper.getSuccess(auth);
            } else {
                //logger.info("auth failed");
            }
        } catch (Exception e) {
            //logger.info(e.getMessage());
        }
        return ResultHelper.requestFaild("auth failed");
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

    public ResultInfo getHandleRecordList(List<HandleCollection> handleCollections, AuthenticationInfo auth) {
        // 去除重复
        Map<String, HandleCollection> hashMap = new HashMap<>();
        for (HandleCollection collection : handleCollections) {
            if (!hashMap.containsKey(collection.getHandle())) {
                hashMap.put(collection.getHandle(), collection);
            } else {
                if (collection.getId() > hashMap.get(collection.getHandle()).getId()) {
                    hashMap.remove(collection.getHandle());
                    hashMap.put(collection.getHandle(), collection);
                }
            }
        }
        //logger.info(((SecretKeyAuthenticationInfo)auth).toString());
        List<HandleModel> handleModels = new ArrayList<>();
        for (Map.Entry<String, HandleCollection> entry : hashMap.entrySet()) {
            ResolutionRequest request = new ResolutionRequest(Util.encodeString(entry.getValue().getHandle()), null, null, auth);
            request.withCitationInfo = 1;
            request.ignoreRestrictedValues = auth == null;
            //request.collectedId = handle.getId();
            try {
                AbstractResponse response = ((HandleResolver) BaseController.getHandleResolver()).processRequest(request);
                if (response instanceof ResolutionResponse) {
                    HandleModel handleModel = new HandleModel();
                    handleModel.setHandle(Util.decodeString(((ResolutionResponse)response).handle));
                    handleModel.setOpt(entry.getValue().getOperate());
                    //logger.info(Util.decodeString(((ResolutionResponse)response).handle));
                    for (HandleValue value : ((ResolutionResponse)response).getHandleValues()) {
                        if (value.getTypeAsString().contains("DD_")) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("data", value.getDataAsString());
                            map.put("index", value.getIndex());
                            map.put("type", value.getTypeAsString());
                            handleModel.getValues().add(map);
                        }
                    }
                    handleModels.add(handleModel);
                }
            } catch (Exception e) {
                continue;
            }
        }
        return ResultHelper.getSuccess(handleModels);
    }

    class CollectExcute implements Runnable {

        private final CountDownLatch latch;
        private final Site site;
        private final List<HandleModel> collectionDataList;
        private final boolean byMeta;
        private final String metahandle;
        private final String authStr;

        public CollectExcute(Site site, CountDownLatch latch, List<HandleModel> collectionDataList, boolean byMeta, String metahandle, String authStr) {
            this.site = site;
            this.metahandle = metahandle;
            this.byMeta = byMeta;
            this.latch = latch;
            this.collectionDataList = collectionDataList;
            this.authStr = authStr;
        }

        @Override
        public void run() {
            synchronized (this) {
                try {
                    String host = "http://" + this.site.getIpAddress() + ":" + this.site.getPort();
                    String path = (byMeta && StringUtils.isNotEmpty(this.metahandle))?"/data-api/refhandle" + authStr + "/" + this.metahandle : "/data-api/handles" + authStr;
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

    private class CheckAuthentication implements Runnable {
        @SuppressWarnings("hiding")
        private AuthenticationInfo authInfo = null;
        private boolean success = false;
        private String errorMessage = null;
        private Integer index;

        CheckAuthentication(AuthenticationInfo authInfo) {
            this.authInfo = authInfo;
        }

        public boolean wasSuccessful() {
            return success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public Integer getIndex() {
            return index;
        }

        @Override
        public void run() {
            this.success = false;
            try {
                if (BaseController.getHandleResolver() == null) {
                    logger.info("resolver is null");
                }
                AuthenticationUtil authUtil = new AuthenticationUtil((HandleResolver) BaseController.getHandleResolver());
                this.success = authUtil.checkAuthentication(authInfo);
                this.index = authUtil.getIndex();
            } catch (Exception e) {
                this.errorMessage = String.valueOf(e);
                e.printStackTrace(System.err);
            }
        }
    }

}

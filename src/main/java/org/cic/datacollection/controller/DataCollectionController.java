package org.cic.datacollection.controller;

import io.swagger.annotations.ApiOperation;
import net.handle.hdllib.*;
import org.apache.http.client.methods.HttpGet;
import org.cic.datacollection.BaseController;
import org.cic.datacollection.event.DeleteEvent;
import org.cic.datacollection.event.DeletePublisher;
import org.cic.datacollection.model.*;
import org.cic.datacollection.repository.HandleCollectionRepository;
import org.cic.datacollection.service.CollectionService;
import org.cic.datacollection.util.UtilFunc;
import org.cic.datacollection.vo.ResultHelper;
import org.cic.datacollection.vo.ResultInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@RestController
public class DataCollectionController extends BaseController {

    private Logger logger = LoggerFactory.getLogger(DataCollectionController.class);
    @Autowired
    private HandleCollectionRepository handleCollectionRepository;


    @Autowired
    private CollectionService collectionService;

    @Autowired
    private DeletePublisher publisher;

    @GetMapping("/getCollectionData/{handle_prefix}/{handle_suffix}/{password}")
    public Callable<ResultInfo> getCollectionData(@PathVariable("handle_prefix") String handle_prefix, @PathVariable("handle_suffix") String handle_suffix, @PathVariable("password") String password){
        return ()-> collectionService.collectData(handle_prefix, handle_suffix, password);
    }

    @GetMapping("/getCollectionDataByMetaHandle/{handle_prefix}/{handle_suffix}/{password}/{prefix}/{suffix}")
    public Callable<ResultInfo> getCollectionDataByMeta(@PathVariable("handle_prefix") String handle_prefix, @PathVariable("handle_suffix") String handle_suffix, @PathVariable("password") String password, @PathVariable("prefix") String prefix,@PathVariable("suffix") String suffix){
        return ()-> collectionService.collectDataByMeta(handle_prefix, handle_suffix, password, prefix+"/"+suffix);
    }


    @ApiOperation(value="测试", notes="测试", hidden = true)
    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public String index() {

        if (this.getHandleServer() == null) {
            return "server is null" + "url:" + this.getIp() + ":" + this.getPort();
        } else {
            return "hello:" + this.getIp() + ":" + this.getPort();
        }
    }

    @ApiOperation(value="获取handle信息", notes="循环调用handle查询接口，获取handle具体内容")
    @GetMapping(value = "/handles/{handle_prefix}/{handle_suffix}/{password}")
    public Callable<ResultInfo> getHandlesData(@PathVariable("handle_prefix") String handle_prefix, @PathVariable("handle_suffix") String handle_suffix, @PathVariable("password") String password) {
        ResultInfo authResult = collectionService.checkAuth(handle_prefix, handle_suffix, password);
        if (authResult.isOK()) {
            return ()->{
                List<HandleCollection> handleCollections = handleCollectionRepository.findPartHandleCollections();
                ResultInfo resultInfo = collectionService.getHandleRecordList(handleCollections, (AuthenticationInfo)authResult.getObj());
                publisher.publish(new DeleteEvent("delete", handleCollections));
                return resultInfo;
            };

        } else {
            return () -> authResult;
        }
    }

    @ApiOperation(value="获取handle信息", notes="根据元数据handle，获取handle具体内容")
    @GetMapping(value = "/refhandle/{handle_prefix}/{handle_suffix}/{password}/{prefix}/{suffix}")
    public Callable<ResultInfo> getCollectionByRefHandle(@PathVariable("handle_prefix") String handle_prefix, @PathVariable("handle_suffix") String handle_suffix, @PathVariable("password") String password, @PathVariable("prefix") String prefix, @PathVariable("suffix") String suffix) {
        ResultInfo authResult = collectionService.checkAuth(handle_prefix, handle_suffix, password);
        if (authResult.isOK()) {
            return () -> {
                //String suffix = UtilFunc.extractPathFromPattern(request);
                String refHandle = prefix + "/" + suffix;
                List<HandleCollection> handleCollections = handleCollectionRepository.findHandleCollectionByRefHandle(refHandle);
                ResultInfo resultInfo = collectionService.getHandleRecordList(handleCollections, (AuthenticationInfo)authResult.getObj());
                publisher.publish(new DeleteEvent("delete", handleCollections));
                return resultInfo;
            };
        } else {
            return () -> authResult;
        }
    }



    private ResultInfo getHandleRecordList(List<HandleCollection> handleCollections) {
        if (handleCollections != null && handleCollections.size() > 0) {
            HttpGet[] requests = new HttpGet[handleCollections.size()];
            List<HandleModel> handleRecordList = new ArrayList<>();
            for (int i = 0; i < handleCollections.size(); i ++) {
                requests[i] = new HttpGet("http://" + this.getIp() + ":" + this.getPort() + "/api/handles/" + handleCollections.get(i).getHandle() + "?enhance=1");
                requests[i].setHeader("opt", handleCollections.get(i).getOperate());
            }
            try {
                handleRecordList = UtilFunc.getValueFromHttp(requests, handleRecordList);
                return ResultHelper.getSuccess(handleRecordList);
            } catch (Exception e) {
                return ResultHelper.requestFaild("http error");
            }
        } else {
            return ResultHelper.requestFaild("nothing");
        }
    }
}

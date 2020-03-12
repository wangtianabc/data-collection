package org.cic.datacollection.controller;

import io.swagger.annotations.ApiOperation;
import org.apache.http.client.methods.HttpGet;
import org.cic.datacollection.BaseController;
import org.cic.datacollection.model.*;
import org.cic.datacollection.repository.HandleCollectionRepository;
import org.cic.datacollection.service.CollectionService;
import org.cic.datacollection.util.UtilFunc;
import org.cic.datacollection.vo.ResultHelper;
import org.cic.datacollection.vo.ResultInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@RestController
public class DataCollectionController extends BaseController {

    @Autowired
    private HandleCollectionRepository handleCollectionRepository;


    @Autowired
    private CollectionService collectionService;

    @GetMapping("/getCollectionData")
    public Callable<ResultInfo> getCollectionData(){
        return ()->{
            return collectionService.collectData();
        };
    }


    /*
    @ApiOperation(value="测试", notes="测试", hidden = true)
    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public String index() {

        if (this.getHandleServer() == null) {
            return "server is null" + "url:" + this.getIp() + ":" + this.getPort();
        } else {
            return "hello:" + this.getHandleServer().getClass().getName() + "url:" + this.getIp() + ":" + this.getPort();
        }
    }*/

    @ApiOperation(value="获取handle信息", notes="循环调用handle查询接口，获取handle具体内容")
    @GetMapping(value = "/handles")
    public Callable<ResultInfo> getHandlesData() {
        return ()->{
            List<HandleCollection> handleCollections = handleCollectionRepository.findPartHandleCollections();
            return getHandleRecordList(handleCollections);
        };
    }

    @ApiOperation(value="获取handle信息", notes="根据元数据handle，获取handle具体内容")
    @GetMapping(value = "/refhandle/{prefix}/**")
    public Callable<ResultInfo> getCollectionByRefHandle(@PathVariable String prefix, HttpServletRequest request) {
        return ()->{
            String suffix = UtilFunc.extractPathFromPattern(request);
            String refHandle = prefix + "/" + suffix;
            List<HandleCollection> handleCollections = handleCollectionRepository.findHandleCollectionByRefHandle(refHandle);
            return getHandleRecordList(handleCollections);
        };
    }

    private ResultInfo  getHandleRecordList(List<HandleCollection> handleCollections){
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

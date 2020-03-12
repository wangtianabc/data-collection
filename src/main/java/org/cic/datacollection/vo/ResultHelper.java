package org.cic.datacollection.vo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rest交互Vo工具类
 *
 * @author LeeHongxiao
 */
public class ResultHelper {
    private static final Logger logger = LoggerFactory.getLogger(ResultHelper.class);


    public static ResultInfo getSuccess(Object obj){
        ResultInfo ri = new ResultInfo();
        ri.setCode(HttpCodeDef.Success.getCode()+"");
        ri.setMsg(HttpCodeDef.Success.getMsg());
        ri.setOK(true);
        ri.setObj(obj);
        return ri;
    }

    public static ResultInfo requestFaild(HttpCodeDef httpCodeDef){
        ResultInfo ri = new ResultInfo();
        ri.setCode(httpCodeDef.getCode()+"");
        ri.setMsg(httpCodeDef.getMsg());
        ri.setOK(false);
        ri.setObj(null);
        logger.warn(httpCodeDef.getMsg());
        return ri;
    }

    public static ResultInfo requestFaild(String message, String code) {
        ResultInfo ri = new ResultInfo();
        ri.setCode(code);
        ri.setMsg(message);
        ri.setOK(false);
        ri.setObj(null);
        logger.warn(message);
        return ri;
    }
    public static ResultInfo requestFaild(String message) {
        ResultInfo ri = new ResultInfo();
        ri.setCode(HttpCodeDef.InterError.getCode()+"");
        ri.setMsg(message);
        ri.setOK(false);
        ri.setObj(null);
        logger.warn(message);
        return ri;
    }
}

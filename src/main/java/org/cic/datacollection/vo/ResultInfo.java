package org.cic.datacollection.vo;


/**
 * RestAPI 交互VO
 * @author LeeHongxiao
 */
public class ResultInfo {

    private String code;
    private String msg;
    private Object obj;
    private boolean isOK;

    public boolean isOK() {
        return isOK;
    }

    public void setOK(boolean OK) {
        isOK = OK;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }
}

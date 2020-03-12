package org.cic.datacollection.vo;

/**
 * Http状态码
 *
 * @author LeeHongxiao
 */
public enum HttpCodeDef {

    Success(200,"成功"),
    NotFount(404,"没找到对应的资源"),
    InvalidToken(401,"无效的凭证"),
    NoAuth(403,"权限不足"),
    InterError(500,"服务器内部错误"),
    ParamError(-100,"传参错误"),
    UNKnowError(-1,"未知错误"),

    ;

    private int i;
    private String msg;
    HttpCodeDef(int i, String msg) {
        this.i = i;
        this.msg = msg;
    }

    public int getCode() {
        return i;
    }

    public String getMsg() {
        return msg;
    }
}

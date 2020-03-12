package org.cic.datacollection.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.cic.datacollection.vo.ResultHelper;
import org.cic.datacollection.vo.ResultInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * RestJsonApi工具
 *
 * @author LeeHongxiao
 */
public class RestJsonApi {

    private static final Logger logger = LoggerFactory.getLogger(RestJsonApi.class);

    public static <T> ResultInfo doGet(String host, String path,
                                       Map<String, String> headers,
                                       Map<String, String> querys,
                                       Class<T> transToObjClass) {
        int responseCode = -1;
        byte[] responseBytes = null;
        HttpEntity httpEntity = null;
        try {
            CloseableHttpResponse closeableHttpResponse = (CloseableHttpResponse) HttpUtils.doGet(host, path, headers, querys);
            responseCode = closeableHttpResponse.getStatusLine().getStatusCode();
            httpEntity = closeableHttpResponse.getEntity();

            byte[] responseBody = EntityUtils.toByteArray(httpEntity);
            if (responseBody != null) {
                responseBytes = responseBody;
            } else {
                InputStream respStream = null;
                try {
                    respStream = httpEntity.getContent();
                    int respBodySize = respStream.available();
                    if (respBodySize <= 0)
                        throw new IOException("Invalid respBodySize: " + respBodySize);
                    responseBytes = new byte[respBodySize];
                    if (respStream.read(responseBytes) != respBodySize)
                        throw new IOException("Read respBody Error");
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    throw new RuntimeException(e.getMessage());
                } finally {
                    if (respStream != null) {
                        respStream.close();
                    }
                }
            }
            String result = new String(responseBytes);
            if (responseCode == 200) {
                if (null == transToObjClass) {
                    return ResultHelper.getSuccess(result);
                } else {
                    ObjectMapper objectMapper = new ObjectMapper();
                    T obj = objectMapper.readValue(result, transToObjClass);
                    return ResultHelper.getSuccess(obj);
                }
            } else {
                return ResultHelper.requestFaild(result, responseCode + "");
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            try {
                EntityUtils.consume(httpEntity);
            } catch (IOException e2) {
                logger.error(e2.getMessage());
            }
            throw new RuntimeException(e.getMessage(), e);
        }
    }


}


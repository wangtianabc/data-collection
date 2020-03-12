package org.cic.datacollection.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.util.EntityUtils;
import org.cic.datacollection.model.HandleModel;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.sql.Blob;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class UtilFunc {
    public static byte[] getBytes(Blob blob) {
        BufferedInputStream bufferedInputStream = null;
        try {
            //利用Blob自带的一个函数去将blob转换成InputStream
            bufferedInputStream = new BufferedInputStream(blob.getBinaryStream());
            //申请一个字节流，长度和blob相同
            byte[] bytes = new byte[(int) blob.length()];
            int len = bytes.length;
            int offset = 0;
            int read = 0;
            while (offset < len//确保不会读过头
                    && (read = bufferedInputStream.read(bytes, offset, len - offset)) >= 0) {
                //BufferedInputStream内部有一个缓冲区，默认大小为8M，每次调用read方法的时候，它首先尝试从缓冲区里读取数据，
                //若读取失败（缓冲区无可读数据），则选择从物理数据源（譬如文件）读取新数据（这里会尝试尽可能读取多的字节）放入到缓冲区中，
                //最后再将缓冲区中的内容部分或全部返回给用户
                //也就是说read函数一次性可能读不完，所以可能会分多次读，于是就有了上面的逻辑
                offset += read;
            }
            return bytes;
        } catch (Exception e) {
            return null;
        } finally {
            try {
                bufferedInputStream.close();
            } catch (IOException e) {
                return null;
            }
        }
    }

    public static List<HandleModel> getValueFromHttp(HttpGet[] httpGets, List<HandleModel> records) throws Exception, IOException{
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(30000)
                .setConnectTimeout(30000).build();
        CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();
        try {
            httpclient.start();
            final CountDownLatch latch = new CountDownLatch(httpGets.length);
            for (final HttpGet request: httpGets) {
                httpclient.execute(request, new FutureCallback<HttpResponse>() {
                    @Override
                    public void completed(final HttpResponse response) {
                        latch.countDown();
                        try {
                            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                                String strResult = EntityUtils.toString(response.getEntity(),"UTF-8");
                                JsonObject rtnJson = new JsonParser().parse(strResult).getAsJsonObject();
                                // 获取成功
                                if (rtnJson.get("responseCode").getAsString().equals("1")) {
                                    HandleModel record = new HandleModel();
                                    record.setHandle(rtnJson.get("handle").getAsString());
                                    record.setOpt(request.getFirstHeader("opt").getValue());
                                    if (rtnJson.get("values") != null && rtnJson.get("values") instanceof JsonArray) {
                                        JsonArray valueArr = (JsonArray)rtnJson.get("values");
                                        for (int i = 0; i < valueArr.size(); i ++) {
                                            JsonObject valueObj = valueArr.get(i).getAsJsonObject();
                                            Map<String, Object> map = new HashMap<>();
                                            map.put("index", Long.parseLong(valueObj.get("index").getAsString()));
                                            map.put("type", valueObj.get("type").getAsString());
                                            if (valueObj.get("data") instanceof JsonObject) {
                                                if (valueObj.get("data").getAsJsonObject().get("format").getAsString().equals("string")) {
                                                    try {
                                                        map.put("data", valueObj.get("data").getAsJsonObject().get("value").getAsString());
                                                    } catch (Exception e) {
                                                        System.out.println("json parse error:" + e.getMessage());
                                                    }
                                                }
                                            }
                                            record.getValues().add(map);
                                        }
                                    }
                                    records.add(record);
                                }
                            }
                        } catch (IOException e) {

                        }
                    }

                    @Override
                    public void failed(final Exception ex) {
                        latch.countDown();
                        System.out.println(request.getRequestLine() + "->" + ex);
                        //logError(ServerLog.ERRLOG_LEVEL_INFO, request.getRequestLine() + "->" + ex);
                    }

                    @Override
                    public void cancelled() {
                        latch.countDown();
                        System.out.println(request.getRequestLine() + " cancelled");
                        //logError(ServerLog.ERRLOG_LEVEL_INFO, request.getRequestLine() + " cancelled");
                    }
                });
            }
            latch.await();
        } catch (Exception e) {

        } finally {
            httpclient.close();
        }
        return records;
    }

    public static String extractPathFromPattern(final HttpServletRequest request) {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String bestMatchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        return new AntPathMatcher().extractPathWithinPattern(bestMatchPattern, path);
    }
}

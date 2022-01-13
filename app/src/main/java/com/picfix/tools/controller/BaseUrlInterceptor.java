package com.picfix.tools.controller;

import com.picfix.tools.http.ApiConfig;
import com.picfix.tools.utils.JLog;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author Herr_Z
 * @description:
 * @date : 2021/6/28 10:26
 */
public class BaseUrlInterceptor implements Interceptor {

    @NotNull
    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        //获取request
        Request request = chain.request();
        //从request中获取原有的HttpUrl实例oldHttpUrl
        HttpUrl oldHttpUrl = request.url();
        //获取request的创建者builder
        Request.Builder builder = request.newBuilder();
        //从request中获取headers，通过给定的键url_name
        List<String> headerValues = request.headers("urlname");
        if (headerValues.size() > 0) {
            //如果有这个header，先将配置的header删除，因此header仅用作app和okhttp之间使用
            builder.removeHeader("urlname");
            //匹配获得新的BaseUrl
            String headerValue = headerValues.get(0);
            HttpUrl newBaseUrl;
            if ("v1".equals(headerValue)) {
                newBaseUrl = HttpUrl.parse(ApiConfig.BASE_URL_1);
            } else if ("v2".equals(headerValue)) {
                newBaseUrl = HttpUrl.parse(ApiConfig.BASE_URL_2);
            } else {
                newBaseUrl = HttpUrl.parse(ApiConfig.BASE_URL_1);
            }

            if(newBaseUrl==null) return chain.proceed(request);

            //重建新的HttpUrl，修改需要修改的url部分
            HttpUrl newFullUrl = oldHttpUrl
                    .newBuilder()
                    .scheme("https")//更换网络协议
                    .host(newBaseUrl.host())//更换主机名
                    .port(newBaseUrl.port())//更换端口
                    .removePathSegment(0)//移除第一个参数
                    .build();
            //重建这个request，通过builder.url(newFullUrl).build()；
            // 然后返回一个response至此结束修改
            JLog.i("Url", "intercept: " + newFullUrl.toString());
            return chain.proceed(builder.url(newFullUrl).build());
        }
        return chain.proceed(request);
    }
}

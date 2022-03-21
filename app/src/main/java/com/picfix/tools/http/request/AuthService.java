package com.picfix.tools.http.request;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * @author Herr_Z
 * @description 获取百度智能云的accesstoken
 * @date : 2021/6/28 10:08
 */
public class AuthService {
    public static String accessToken = "";
    //线上id
    public static String clientId = "ox0Uz65dzs60GHqIloRYcxyL";
    //线下id
//    public static String clientId = "BZznWwsm4PxCOoGY5V8SDuUa";
    //线上clientSecret
    public static String clientSecret = "5OUUiU62kaR6jujt1d5me9kVTV7DuC9v";
    //线上clientSecret
//    public static String clientSecret = "mpnDji1vfDqsj3ffDGp5jnNXZhQ3rETe";

    /**
     * 获取权限token,线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取
     *
     * @return 返回示例：
     * {
     * "access_token": "24.460da4889caad24cccdb1fea17221975.2592000.1491995545.282335-1234567",
     * "expires_in": 2592000
     * }
     */
    public static String getAuth() {
        if (accessToken.equals("")) {
            accessToken = getAuth(clientId, clientSecret);
        } else {
            return getAuth(clientId, clientSecret);
        }

        return accessToken;
    }

    /**
     * 获取API访问token
     * 该token有一定的有效期，需要自行管理，当失效时需重新获取.
     *
     * @param ak - 百度云官网获取的 API Key
     * @param sk - 百度云官网获取的 Securet Key
     * @return assess_token 示例：
     * "24.460da4889caad24cccdb1fea17221975.2592000.1491995545.282335-1234567"
     */
    public static String getAuth(String ak, String sk) {
        // 获取token地址
        String authHost = "https://aip.baidubce.com/oauth/2.0/token?";
        String getAccessTokenUrl = authHost
                // 1. grant_type为固定参数
                + "grant_type=client_credentials"
                // 2. 官网获取的 API Key
                + "&client_id=" + ak
                // 3. 官网获取的 Secret Key
                + "&client_secret=" + sk;
        try {
            URL realUrl = new URL(getAccessTokenUrl);
            // 打开和URL之间的连接
            HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            // 获取所有响应头字段
            Map<String, List<String>> map = connection.getHeaderFields();
            // 遍历所有的响应头字段
            for (String key : map.keySet()) {
                System.err.println(key + "--->" + map.get(key));
            }

            // 定义 BufferedReader输入流来读取URL的响应
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }

//            connection.disconnect();

            // 返回结果示例
            System.err.println("result:" + result);
            JSONObject jsonObject = new JSONObject(result.toString());
            return jsonObject.getString("access_token");
        } catch (Exception e) {
            System.err.println("获取token失败！");
            e.printStackTrace(System.err);
        }

        return null;
    }
}

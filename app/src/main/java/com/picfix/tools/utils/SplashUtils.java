package com.picfix.tools.utils;

import com.bytedance.msdk.adapter.baidu.BaiduNetworkRequestInfo;
import com.bytedance.msdk.adapter.gdt.GdtNetworkRequestInfo;
import com.bytedance.msdk.adapter.sigmob.SigmobNetworkRequestInfo;
import com.bytedance.msdk.api.v2.GMNetworkRequestInfo;

public class SplashUtils {
    public static GMNetworkRequestInfo getGMNetworkRequestInfo() {
        GMNetworkRequestInfo networkRequestInfo;
//        //穿山甲兜底，参数分别是appId和adn代码位。注意第二个参数是代码位，而不是广告位。
//        networkRequestInfo = new PangleNetworkRequestInfo("5224072", "887658153");
//        //gdt兜底
        networkRequestInfo = new GdtNetworkRequestInfo("1200437456", "2013304904956563");
//        //ks兜底
//        networkRequestInfo = new KsNetworkRequestInfo("90009", "4000000042");
//        //百度兜底
//        networkRequestInfo = new BaiduNetworkRequestInfo("ca6c7421", "7994653");
//        //Sigmob兜底
//        networkRequestInfo = new SigmobNetworkRequestInfo("19695", "0dd334b2607cd321", "efa13c1ee2d");
//        // mintegral兜底
//        networkRequestInfo = new MintegralNetworkRequestInfo("118690", "7c22942b749fe6a6e361b675e96b3ee9", "209547");
        //游可赢兜底
//        networkRequestInfo = new KlevinNetworkRequestInfo("30008", "30029");
        return networkRequestInfo;
    }
}

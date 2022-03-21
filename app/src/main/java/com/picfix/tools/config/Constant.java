package com.picfix.tools.config;


import android.os.Handler;

import com.tencent.mm.opensdk.openapi.IWXAPI;

public class Constant {

    public static final String PRODUCT_ID = "7";
    public static boolean isDebug = false;
    public static Handler mHandler = null;
    public static Handler mSecondHandler = null;
    public static String ROM = "";
    public static final String ROM_MIUI = "MIUI";
    public static final String ROM_EMUI = "EMUI";
    public static final String ROM_FLYME = "FLYME";
    public static final String ROM_OPPO = "OPPO";
    public static final String ROM_VIVO = "VIVO";
    public static final String ROM_OTHER = "OTHER";
    public static String CLIENT_TOKEN = "";
    public static String QUEST_TOKEN = "";
    public static String USER_NAME = "";
    public static String USER_ID = "";
    public static Boolean ScanStop = false;
    public static IWXAPI api = null;


    //应用宝 "bef03da7cb82efd7" 11
    //百度应用商店 "42ad6f989640c170" 13
    //百度推广A001 "e5662617319e78c1" 5
    //百度应用商店A01 "74c94129e1d82b94" 6
    //百度推广A002 "ac2999198df31940" 7
    //百度应用商店A002 "fb031e0e61441dee" 8
    //百度推广A006 "5c87b8ed1ca69e96"  06
    //百度应用商店A006 "94dddccbe2ac9263" 06s
    //百度推广A007 "d38c3b1164c26b76"  07
    //百度应用商店A007 "b3289bb361849381" 07s
    //萌内朵003  60594f8cd5f0b473
    //UC cafd10e2d4c8376f
    //头条 1a36ecfcda18e730
    //360 b7f988d9946e9209
    //搜狗 a6f02350cff48f50
    public static Boolean OCPC = false;
    public static Boolean AD_OPENNING = true;
    public static String CHANNEL_ID = "a6f02350cff48f50";
    public static String CHANNEL_HUAWEI = "539cf1fbda8b8191";
    public static String CHANNEL_HUAWEI_NEW = "5ccd4758a1115ff5";
    public static String CHANNEL_OPPO = "0f62d749fcd4d65f";
    public static String CHANNEL_XIAOMI = "6514e45c8c42f469";
    public static String CHANNEL_VIVO = "46abc5e760a15230";
    public static String CHANNEL_FLYME = "9e65372a35cdc6fa";
    public static String WEBSITE = "";
    public static String APP_VERSION = "0";

    //Baidu com.picfix.tools
    public static long USER_ACTION_SET_ID = 12901;
    public static String APP_SECRET_KEY = "69cb857e3ec03c263a2be06619982b51";

    //Baidu com.picfix.tool
//    public static long USER_ACTION_SET_ID = 14912;
//    public static String APP_SECRET_KEY = "77ad5d9c17125813e15403955ababc0e";

    //service_code
    public static String REC = "rec";
    public static String COM = "com";
    public static String REPL = "repl";
    public static String BILL = "billrec";
    public static String DELETE = "delete";
    public static String PHOTO_FIX = "photofix";
    public static String PHOTO_FIX_TIMES = "times";
    public static String PHOTO_HAND_FIX = "atf";

    //service_expire
    public static String EXPIRE_TYPE_FOREVER = "2";
    public static String EXPIRE_TYPE_YEAR = "1";
    public static String EXPIRE_TYPE_MONTH = "3";

    public static String EXPORT_PATH = "/export/";
    public static String WX_HIGN_VERSION_PATH = "/Android/data/com.tencent.mm/";
    public static String MM_RESOURCE_PATH = "/Android/data/com.immomo.momo/";
    public static String SOUL_RESOURCE_PATH = "/Android/data/cn.soulapp.android/";
    public static String WX_PICTURE_PATH = "/Pictures/WeiXin/";
    public static String PICTURE_PATH = "/Pictures/";
    public static String DOWNLOAD_PATH = "/Download/";
    public static String DCIM_PATH = "/DCIM/";
    public static String WX_DB_PATH = "/App/com.tencent.mm/MicroMsg/";
    public static String WX_ZIP_PATH = "APP/com.tencent.mm.zip";
    public static String WX_RESOURCE_PATH = "/tencent/";
    public static String WX_DOWNLOAD_PATH = "/Download/Weixin/";
    public static String QQ_RESOURCE_PATH = "/tencent/MobileQQ/";
    public static String QQ_HIGN_VERSION_PATH = "/Android/data/com.tencent.mobileqq/";
    public static String FLYME_BACKUP_PATH = "/backup/";
    public static String WX_PACK_NAME = "com.tencent.mm";

    public static String BACKUP_PATH = "/aA123456在此/";
    public static String XM_BACKUP_PATH = "/MIUI/backup/AllBackup/";
    public static String OPPO_BACKUP_PATH = "/backup/App/";
    public static String HW_BACKUP_NAME_TAR = "com.tencent.mm.tar";
    public static String HW_BACKUP_APP_DATA_TAR = "com.tencent.mm_appDataTar";
    public static String XM_BACKUP_NAME_BAK = "微信(com.tencent.mm).bak";
    public static String OPPO_BACKUP_NAME_TAR = "com.tencent.mm.tar";
    public static String VIVO_BACKUP_NAME_TAR = "5a656b0891e6321126f9b7da9137994c72220ce7";
    public static String HW_BACKUP_NAME_XML = "info.xml";
    public static String JX_BACKUP_PATH = "/backup/";

    public static String DB_NAME = "EnMicroMsg.db";

    public static int PERMISSION_CAMERA_REQUEST_CODE = 0x00000011;
    public static int CAMERA_REQUEST_CODE = 0x00000012;

    //Realm
    public static String ROOM_DB_NAME = "EnMicroMsg";

    //IM
    public static String SDK_APP_ID = "132041";
    public static String SDK_APP_KEY = "1482210305025478#photo-fix";
    public static String SDK_SERVICE_ID = "photofix_001";
    public static String SDK_DEFAULT_PASSWORD = "123456";
    public static String SDK_TENANT_ID = "90871";

    //Notification
    public static String Notification_title = "消息提醒";
    public static String Notification_content = "您有新的客服消息";

    //Bugly
    public static String BUGLY_APPID = "0dc1cc3833";

    //oss
    public static String END_POINT = "http://oss-cn-shenzhen.aliyuncs.com";
    public static String END_POINT_WITHOUT_HTTP = "oss-cn-shenzhen.aliyuncs.com";
    public static String BUCKET_NAME = "qlrecovery";

    //tencent Pay
    public static String TENCENT_APP_ID = "wx402f05656134648b";
    public static String TENCENT_MINI_PROGRAM_APP_ID = "gh_72629534a52d";
    public static String TENCENT_PARTNER_ID = "1605572449";
}

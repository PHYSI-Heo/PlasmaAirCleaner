package com.physi.pac.setter.http;

public class HttpPacket {

//    private static final String BASEURL = "http://192.168.1.12:3000";
    private static final String BASEURL = "http://13.124.176.173:3000";

    public static final String THUMBNAIL_BASE = BASEURL + "/thumb/";
    /*
         API
     */
    public static final String EXIST_ID_URL = BASEURL + "/identity/exist";
    public static final String GET_INFO_URL = BASEURL + "/db/get/info";
    public static final String UPDATE_INFO_URL = BASEURL + "/db/update/info";

    public static final String GET_IMGs_URL = BASEURL + "/db/get/imgs";
    public static final String UPDATE_IMGs_URL = BASEURL + "/db/update/imgs";

    public static final String UPLOAD_IMGs_URL = BASEURL + "/image/upload";

    /*
        # Params
     */
    public static final String PARAMS_DEVICE_ID = "did";
    public static final String PARAMS_ID_EXIST = "exist";
    public static final String PARAMS_DEVICE_INFO = "info";
    public static final String PARAMS_CITY = "city";
    public static final String PARAMS_PROVINCE = "province";
    public static final String PARAMS_LOCATION_LAT = "lat";
    public static final String PARAMS_LOCATION_LON = "lon";
    public static final String PARAMS_DISPLAY_TIME = "displayTime";
    public static final String PARAMS_CCTV_ENABLE = "cctvEnable";

    public static final String PARAMS_IMG_INFOs =  "images";
    public static final String PARAMS_IMG_ORDER = "_order";
    public static final String PARAMS_IMG_FILE_NAME = "fileName";
    public static final String PARAMS_IMG_FILE_PATH = "filePath";

    public static final String PARAMS_RES_CODE = "resCode";
    public static final String PARAMS_ROW_DATA = "rows";

}

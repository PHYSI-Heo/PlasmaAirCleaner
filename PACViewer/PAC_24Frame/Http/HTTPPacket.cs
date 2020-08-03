using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace PAC_24Frame.Http
{
    public class HTTPPacket
    {
        /*
         *      REST API
         */
        //private const string BaseUrl = "http://192.168.1.12:3000";
        private const string BaseUrl = "http://13.124.176.173:3000";

        //  Web Server Url
        public const string Url_Create_ID = BaseUrl + "/identity/create";
        public const string Url_Register_ID = BaseUrl + "/db/register";
        public const string Url_Get_Info = BaseUrl + "/db/get/info";
        public const string Url_Get_IMGs = BaseUrl + "/db/get/imgs";
        public const string Url_IMG_Downlaod = BaseUrl + "/image/download";
        public const string Url_Get_Default_IMGs = BaseUrl + "/image/default/imgs";
        //  Params 
        public const string Param_Res_Code = "resCode";
        public const string Param_Res_Rows = "rows";
        public const string Param_Device_ID = "did";
        public const string Param_City = "city";
        public const string Param_Province = "province";
        public const string Param_Location_Lat = "lat";
        public const string Param_Location_Lon = "lon";
        public const string Param_Display_Time = "displayTime";
        public const string Param_CCTV_Enable = "cctvEnable";
        public const string Param_IMG_Name = "fileName";
        public const string Param_File_List = "files";

        public const string Res_Success = "1001";

        // Air Quality JSON Key
        public const string Key_Air_Status = "status";
        public const string Key_Air_Data = "data";
        public const string Key_Air_Info = "iaqi";
        public const string Key_Air_PM10 = "pm10";
        public const string Key_Air_PM25 = "pm25";
        public const string Key_Air_Value = "v";
        // OpenWeather JSON Key
        public const string Key_WT_Code = "cod";
        public const string Key_WT_List = "list";
        public const string Key_WT_Main = "main";
        public const string Key_WT_Temp = "temp";
        public const string Key_WT_Humidity = "humidity";
        public const string Key_WT_Weather = "weather";
        public const string Key_WT_Description = "description";
        public const string Key_WT_ID = "id";
    }
}

using Newtonsoft.Json.Linq;
using PAC_24Frame.Http;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace PAC_24Frame.Environment
{
    public class EnvironmentCore
    {
        private const string Air_Access_Key = "9aa065238fb467062a8aa77333293dd08167832f";
        private const string Weather_Access_Key = "746a25fbeaa3ad8f494a911c5a8f3f3f";

        private HTTPClient httpClient;

        private string cityName, provinceName, latitude, longitude;
        private EnvironmentData environmentData;

        public EnvironmentCore()
        {
            httpClient = new HTTPClient();
            environmentData = new EnvironmentData();
        }

        public void SetEnvironmentOptions(string cityName, string provinceName, string latitude, string longitude)
        {
            this.cityName = cityName;
            this.provinceName = provinceName;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public string GetLocation()
        {
            string str = cityName + " " + provinceName;
            if (str.Equals(" "))
                str = "지역정보를 설정하세요.";
            return str;
        }

        public async Task<EnvironmentData> GetEnvironment()
        {
            if (cityName == null || provinceName == null || cityName == "" || provinceName == "")
                return null;

            await GetDustData();
            await GetWeatherData();

            return environmentData;
        }

        private async Task GetDustData()
        {
            try
            {
                string url = new StringBuilder().AppendFormat("https://api.waqi.info/feed/geo:{0};{1}/?token={2}",
                    latitude, longitude, Air_Access_Key).ToString();

                JObject obj = await httpClient.Requester(url, "GET", null);
                //Console.WriteLine("# Public Quality.\n{0}", obj.ToString());

                if (obj.GetValue(HTTPPacket.Key_Air_Status).ToString().Equals("ok"))
                {
                    JObject dataObj = JObject.Parse(obj[HTTPPacket.Key_Air_Data][HTTPPacket.Key_Air_Info].ToString());
                    string pm10 = dataObj[HTTPPacket.Key_Air_PM10][HTTPPacket.Key_Air_Value].ToString();
                    string pm25 = dataObj[HTTPPacket.Key_Air_PM25][HTTPPacket.Key_Air_Value].ToString();
                    Console.WriteLine("[Public API] - Air Quality : pm10 ({0}), pm25 ({1})", pm10, pm25);
                    environmentData.SetDust(pm10, pm25);
                }
            }
            catch (Exception e)
            {
                Console.WriteLine(e.StackTrace);
            }
        }

        private async Task GetWeatherData()
        {
            try
            {
                string url = new StringBuilder().AppendFormat("http://api.openweathermap.org/data/2.5/find?lat={0}&lon={1}&appid={2}",
                     latitude, longitude, Weather_Access_Key).ToString();
                JObject obj = await httpClient.Requester(url, "GET", null);
                //Console.WriteLine("# Public Weather.\n{0}", obj.ToString());

                if (obj.GetValue(HTTPPacket.Key_WT_Code).ToString().Equals("200"))
                {
                    JObject dataObj = JObject.Parse(obj[HTTPPacket.Key_WT_List][0].ToString());
                    float tempValue = float.Parse(dataObj[HTTPPacket.Key_WT_Main][HTTPPacket.Key_WT_Temp].ToString());
                    string temp = string.Format("{0:0.0}", tempValue - 273);
                    string humidity = dataObj[HTTPPacket.Key_WT_Main][HTTPPacket.Key_WT_Humidity].ToString();
                    JObject weatherObj = JObject.Parse(dataObj[HTTPPacket.Key_WT_Weather][0].ToString());
                    string weather = weatherObj[HTTPPacket.Key_WT_Main].ToString();
                    string weather_id = weatherObj[HTTPPacket.Key_WT_ID].ToString();
                    string description = weatherObj[HTTPPacket.Key_WT_Description].ToString();
                    Console.WriteLine("[Public API] - Weather : {0}({1}) / {2}'c, {3}%", weather, description, temp, humidity);
                    environmentData.SetWeather(temp, humidity, weather, weather_id, description);
                }
            }
            catch (Exception e)
            {
                Console.WriteLine(e.StackTrace);
            }
        }
    }
}

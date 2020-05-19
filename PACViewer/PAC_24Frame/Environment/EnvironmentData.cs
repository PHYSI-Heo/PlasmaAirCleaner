using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace PAC_24Frame.Environment
{
    public class EnvironmentData
    {
        private string dust_pm10 = null, dust_pm25 = null;
        private string temperature = null, humidity = null;
        private string weather = null, weatherID = null, description = null;

        public void SetDust(string dust_pm10, string dust_pm25)
        {
            this.dust_pm10 = dust_pm10;
            this.dust_pm25 = dust_pm25;
        }

        public void SetDusetPM10(string dust_pm10)
        {
            this.dust_pm10 = dust_pm10;
        }

        public string GetDustPM10()
        {
            return dust_pm10;
        }

        public void SetDusetPM25(string dust_pm25)
        {
            this.dust_pm25 = dust_pm25;
        }

        public string GetDustPM25()
        {
            return dust_pm25;
        }

        public void SetTemperature(string temperature)
        {
            this.temperature = temperature;
        }

        public string GetTemperature()
        {
            return temperature;
        }

        public void SetHumidity(string humidity)
        {
            this.humidity = humidity;
        }

        public string GetHumidity()
        {
            return humidity;
        }

        public void SetWeather(string weather, string id, string description)
        {
            this.weather = weather;
            this.weatherID = id;
            this.description = description;
        }

        public void SetWeather(string temperature, string humidity, string weather, string id, string description)
        {
            this.temperature = temperature;
            this.humidity = humidity;
            this.weather = weather;
            this.weatherID = id;
            this.description = description;
        }

        public string GetWeather()
        {
            return weather;
        }

        public int GetWeatherID()
        {
            return int.Parse(weatherID);
        }

        public string GetWeatherDescription()
        {
            return description;
        }
    }
}

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace PAC_7Frame.Display
{
    public class SettingData
    {
        private string city, province, latitude, longitude, displayTime;

        public SettingData(string city, string province, string latitude,
            string longitude, string displayTime)
        {
            this.city = city;
            this.province = province;
            this.latitude = latitude;
            this.longitude = longitude;
            this.displayTime = displayTime;
        }

        public string GetCity()
        {
            return city;
        }

        public string GetProvince()
        {
            return province;
        }

        public string GetLatitude()
        {
            return latitude;
        }

        public string GetLongitude()
        {
            return longitude;
        }

        public string GetDisplayTime()
        {
            return displayTime;
        }
        
    }
}

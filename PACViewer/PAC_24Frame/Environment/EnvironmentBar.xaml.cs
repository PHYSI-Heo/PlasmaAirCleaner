using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;
using System.Windows.Threading;

namespace PAC_24Frame.Environment
{
    /// <summary>
    /// EnvironmentBar.xaml에 대한 상호 작용 논리
    /// </summary>
    public partial class EnvironmentBar : UserControl
    {
        public EnvironmentBar()
        {
            InitializeComponent();
            StartClockTimer();
        }
        
        private void StartClockTimer()
        {
            DispatcherTimer timer = new DispatcherTimer();
            timer.Interval = new TimeSpan(0, 0, 1);
            timer.Tick += Timer_Tick; ;
            timer.Start();
        }

        private void Timer_Tick(object sender, EventArgs e)
        {
            Tb_DateTime.Text = DateTime.Now.ToString();
        }

        public void ShowSensorData(string data)
        {
            if (data.StartsWith("$") && data.EndsWith("#"))
            {
                string[] infos = data.Split(',');
                string dust = infos[0].Substring(1);
                string voc = infos[1].Substring(0, infos[1].Length - 1);

                Tb_S_Dust_PM25.Text = dust + " ㎍/㎥";
                Tb_Voc.Text = voc + " ppm";
            }
        }

        public void ShowPublicData(EnvironmentData data)
        {
            if (data == null)
                return;

            Tb_Humidity.Text = data.GetHumidity() + " %";
            //Tb_P_Dust_PM10.Text = data.GetDustPM10() + " ㎍/㎥"; ;
            Tb_P_Dust_PM25.Text = data.GetDustPM25() + " ㎍/㎥"; ;
            Tb_Temperature.Text = data.GetTemperature() + " ℃"; ;
            SetWeatherData(data.GetWeatherID());
        }

        private void SetWeatherData(int weatherID)
        {
            //https://openweathermap.org/weather-conditions
            string iconPath = null;
            string weatherTxt = null;
            if (weatherID == 800)
            {
                iconPath = "Icon/ic_clear_sky.png";
                weatherTxt = "맑음";
            }
            else if (weatherID == 801)
            {
                iconPath = "Icon/ic_few_clouds.png";
                weatherTxt = "흐림";
            }
            else if (weatherID < 232)
            {
                iconPath = "Icon/ic_thunderstorm.png";
                weatherTxt = "번개";
            }
            else if (weatherID >= 300 && weatherID <= 321)
            {
                iconPath = "Icon/ic_rain.png";
                weatherTxt = "비";
            }
            else if (weatherID >= 500 && weatherID <= 504)
            {
                iconPath = "Icon/ic_light_rain.png";
                weatherTxt = "비";
            }
            else if (weatherID >= 511 && weatherID <= 531)
            {
                iconPath = "Icon/ic_rain.png";
                weatherTxt = "비";
            }
            else if (weatherID >= 600 && weatherID <= 622)
            {
                iconPath = "Icon/ic_snow.png";
                weatherTxt = "눈";
            }
            else if (weatherID >= 701 && weatherID <= 781)
            {
                iconPath = "Icon/ic_atmosphere.png";
                weatherTxt = "안개";
            }
            else
            {
                iconPath = "Icon/ic_clouds.png";
                weatherTxt = "흐림";
            }
            Ic_Weather.Source = new BitmapImage(new Uri(iconPath, UriKind.Relative));
            Tb_Weather.Text = weatherTxt;
        }
    }
}

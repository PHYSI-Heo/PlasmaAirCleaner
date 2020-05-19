using PAC_24Frame.CCTV;
using PAC_24Frame.Display;
using PAC_24Frame.Environment;
using PAC_24Frame.Mqtt;
using PAC_24Frame.Uart;
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

namespace PAC_24Frame
{
    /// <summary>
    /// MainWindow.xaml에 대한 상호 작용 논리
    /// </summary>
    public partial class MainWindow : Window
    {
        private ResourceCore resourceCore;
        private EnvironmentCore environmentCore;
        private MQTTClient mqttClient;
        private CameraCore cameraCore;
        private Serial serial;
        
        private DispatcherTimer weatherResetTimer;
        private DispatcherTimer cctvTimer;

        public MainWindow()
        {
            InitializeComponent();
            InitializeObject();
        }

        private void InitializeObject()
        {
            resourceCore = new ResourceCore();
            environmentCore = new EnvironmentCore();
            mqttClient = new MQTTClient();
            serial = new Serial(9600);

            weatherResetTimer = new DispatcherTimer();
            weatherResetTimer.Tick += WeatherResetTimer_Tick;
            weatherResetTimer.Interval = new TimeSpan(0, 30, 0);

            cctvTimer = new DispatcherTimer();
            cctvTimer.Tick += CctvTimer_Tick;
            cctvTimer.Interval = new TimeSpan(0, 0, 10);
        }
      
        private void Window_ContentRendered(object sender, EventArgs e)
        {
            if (!CheckProductCode())
                return;

            SetConfiguration(true, true);
            StartMQTT();
            ConnectSerial();
            AccessCCTV();

            weatherResetTimer.Start();
        }
        private void CctvTimer_Tick(object sender, EventArgs e)
        {
            cameraCore.SetMode(CameraCore.MODE_ALARM_LISTEN);
            cctvTimer.Stop();
        }

        private async void WeatherResetTimer_Tick(object sender, EventArgs e)
        {
            EnvironmentData environmentData = await environmentCore.GetEnvironment();
            Dispatcher.Invoke(new Action(delegate {
                Eb_Air_State.ShowPublicData(environmentData);
            }), DispatcherPriority.Normal);
        }

        private void MqttClient_SubscribeListener(string message)
        {
            Dispatcher.Invoke(new Action(delegate {
                SetConfiguration(message.Equals(SystemEnv.UPDATE_SETTING_INFO), 
                    message.Equals(SystemEnv.UPDATE_IMAGE_RESOURCE));
            }), DispatcherPriority.Normal);
        }
        
        private void Serial_ReceiveListener(string data)
        {
            Dispatcher.Invoke(new Action(delegate {
                Eb_Air_State.ShowSensorData(data);
            }), DispatcherPriority.Normal);
        }

        private void CameraCore_ModeListener(int mode)
        {
            switch (mode)
            {
                case CameraCore.MODE_PREVIEW:
                    Dispatcher.Invoke(new Action(delegate {
                        Wfh_CCTV.Visibility = Visibility.Visible;
                        cctvTimer.Start();       // Start Preview Timer
                    }), DispatcherPriority.Normal);
                    break;
                case CameraCore.MODE_ALARM_LISTEN:
                    Dispatcher.Invoke(new Action(delegate {
                        Wfh_CCTV.Visibility = Visibility.Collapsed;
                    }), DispatcherPriority.Normal);
                    break;
                case CameraCore.MODE_GUEST_DETECT:
                    cameraCore.SetMode(CameraCore.MODE_PREVIEW);
                    break;
            }
        }

        private async void AccessCCTV()
        {
            cameraCore = new CameraCore(Pb_CCTV);
            bool access = await cameraCore.UserAccess();
            if (access)
            {
                cameraCore.SetMode(CameraCore.MODE_ALARM_LISTEN);
                cameraCore.ModeListener += CameraCore_ModeListener;
            }
        }
        
        private async void ConnectSerial()
        {
            bool connected = await serial.Open();
            if (connected)
            {
                serial.ReceiveListener += Serial_ReceiveListener;
            }
        }
        
        private async void StartMQTT()
        {
            bool connected = await mqttClient.Connect();
            if (connected)
            {
                mqttClient.StartSubscribe();
                mqttClient.SubscribeListener += MqttClient_SubscribeListener;
            }
        }

        private async void SetConfiguration(bool isSetting, bool isImageResource)
        {
            Dp_Image.Stop();
            Pg_Loading.Visibility = Visibility.Visible;
            
            if (isImageResource)
            {
                Pg_Loading.SetMessage("이미지 정보를 가져옵니다.");
                List<ImageResource> sources = await resourceCore.GetImageSources();
                Dp_Image.SetImageResources(sources);
            }

            if (isSetting)
            {
                Pg_Loading.SetMessage("설정 정보를 가져옵니다.");
                SettingData settingData = await resourceCore.GetSettingData();
                if(settingData != null)
                {
                    environmentCore.SetEnvironmentOptions(settingData.GetCity(), settingData.GetProvince(), settingData.GetLatitude(), settingData.GetLongitude());
                    EnvironmentData environmentData = await environmentCore.GetEnvironment();

                    Dp_Image.SetOutputTime(int.Parse(settingData.GetDisplayTime()));
                    Eb_Air_State.ShowPublicData(environmentData);
                }               
            }

            Pg_Loading.Visibility = Visibility.Collapsed;
            Dp_Image.Start(isImageResource);
        }

        private bool CheckProductCode()
        {
            if(SystemEnv.GetProductKey() == "0")
            {
                RegisterWindow registerView = new RegisterWindow();
                registerView.Owner = this;
                Nullable<bool> result = registerView.ShowDialog();
                return result.Value;
            }
            return true;          
        }

    }
}

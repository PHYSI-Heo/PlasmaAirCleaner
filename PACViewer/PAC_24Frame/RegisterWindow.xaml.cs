using Newtonsoft.Json.Linq;
using PAC_24Frame.Http;
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
using System.Windows.Shapes;

namespace PAC_24Frame
{
    /// <summary>
    /// RegisterWindow.xaml에 대한 상호 작용 논리
    /// </summary>
    public partial class RegisterWindow : Window
    {
        private HTTPClient httpClient;

        public RegisterWindow()
        {
            InitializeComponent();
            InitializeObject();
        }

        private void InitializeObject()
        {
            httpClient = new HTTPClient();
        }

        private void Btn_Register_Click(object sender, RoutedEventArgs e)
        {
            string code = Tb_ProductCode.Text;

            if(code == null || code.Length != 6)
            {
                ShowErrorMessage("제품코드(6 자리)를 입력하세요.");
                return;
            }
            RequestRegisterCode(code);
        }

        private async void Btn_Create_Click(object sender, RoutedEventArgs e)
        {
            await RequestCreateCode();
        }

        private async Task RequestCreateCode()
        {
            try
            {
                JObject obj = await httpClient.Requester(HTTPPacket.Url_Create_ID, "POST", null);

                string resCode = obj.GetValue(HTTPPacket.Param_Res_Code).ToString();
                Console.WriteLine("[Register] - Create Product Result.\n{0}", obj.ToString());
                if (resCode.Equals(HTTPPacket.Res_Success))
                {
                    Tb_ProductCode.Text = obj.GetValue(HTTPPacket.Param_Device_ID).ToString();                  
                }
                else
                {
                    ShowErrorMessage("제품 코드 생성에 실패하였습니다.");
                }
            }
            catch(Exception e)
            {
                Console.WriteLine(e.Message);
                ShowErrorMessage("서버와 연결이 불안정합니다. 잠시 후 다시 시도해주세요.");
            }
           
        }


        private async void RequestRegisterCode(string code)
        {
            try
            {
                JObject paramObj = new JObject();
                paramObj.Add(HTTPPacket.Param_Device_ID, code);

                JObject obj = await httpClient.Requester(HTTPPacket.Url_Register_ID, "POST", paramObj);

                string resCode = obj.GetValue(HTTPPacket.Param_Res_Code).ToString();
                Console.WriteLine("[Register] - Register Product Result.\n{0}", obj.ToString());
                if (resCode.Equals(HTTPPacket.Res_Success))
                {
                    SystemEnv.SetProductKey(code);
                    this.DialogResult = true;
                    this.Close();
                }
                else
                {
                    ShowErrorMessage("제품 등록에 실패하였습니다.");
                }
            }
            catch (Exception e)
            {
                Console.WriteLine(e.Message);
                ShowErrorMessage("서버와 연결이 불안정합니다. 잠시 후 다시 시도해주세요.");
            }

        }

        private void ShowErrorMessage(string message)
        {
            Tb_Explain.Text = message;
            Tb_Explain.Foreground = Brushes.Red;
        }
    }
}

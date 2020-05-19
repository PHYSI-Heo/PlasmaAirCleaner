using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Forms;
using System.Windows.Threading;

namespace PAC_7Frame.CCTV
{
    class CameraCore
    {
        private Int32 m_lUserID = -1;
        private Int32 m_lAlarmHandle = -1;
        private Int32 m_lRealHandle = -1;
        private bool m_bInitSDK = false;
        
        public const int MODE_ALARM_LISTEN = 1;
        public const int MODE_PREVIEW = 2;
        public const int MODE_GUEST_DETECT = 3;

        private CHCNetSDK.REALDATACALLBACK RealData = null;
        private CHCNetSDK.MSGCallBack_V31 m_falarmData_V31 = null;
        
        private const string CCTV_IP = "192.168.1.27";
        private const int CCTV_PORT = 8000;
        private const string ACCESS_ID = "admin";
        private const string ACCESS_PWD = "adminIOSPZA";

        private const int GUEST_DETECT_COUNT = 3;

        private DispatcherTimer detectResetTimer;
        private PictureBox Pb_CCTV;

        private int mode = -1;
        private int detectCount = 0;
        
        public delegate void Mode_Listener(int mode);
        public event Mode_Listener ModeListener;


        public CameraCore(PictureBox Pb_CCTV)
        {
            this.Pb_CCTV = Pb_CCTV;
            Initialize();
        }


        private void Initialize()
        {
            detectResetTimer = new DispatcherTimer();
            detectResetTimer.Interval = new TimeSpan(0, 0, 15);
            detectResetTimer.Tick += DetectResetTimer_Tick;

            m_bInitSDK = CHCNetSDK.NET_DVR_Init();
            Console.WriteLine("[CCTV] - NET_DVR_Init Result : {0}", m_bInitSDK);
            if (m_bInitSDK)
            {
                if (m_falarmData_V31 == null)
                {
                    m_falarmData_V31 = new CHCNetSDK.MSGCallBack_V31(MsgCallback_V31);
                }
                CHCNetSDK.NET_DVR_SetDVRMessageCallBack_V31(m_falarmData_V31, IntPtr.Zero);
            }
        }


        private void DetectResetTimer_Tick(object sender, EventArgs e)
        {
            detectCount = 0;
            Console.WriteLine("[CCTV] - Detect Reset Count ({0}).", detectCount);
            detectResetTimer.Stop();
        }


        public void Dispose()
        {
            if (m_lRealHandle >= 0)
            {
                CHCNetSDK.NET_DVR_StopRealPlay(m_lRealHandle);
            }
            if (m_lUserID >= 0)
            {
                CHCNetSDK.NET_DVR_Logout(m_lUserID);
            }
            if (m_bInitSDK == true)
            {
                CHCNetSDK.NET_DVR_Cleanup();
            }
        }
        public void SetMode(int mode)
        {
            if (this.mode == mode)
                return;

            this.mode = mode;

            if (mode == MODE_ALARM_LISTEN)
            {
                StopPreview();
                StartAlarmListener();              
            }
            else
            {
                StopAlarmListener();
                StartPreview();             
            }
        }


        public async Task<bool> UserAccess()
        {

            await Task.Run(() =>
            {
                if (m_lUserID < 0)
                {
                    CHCNetSDK.NET_DVR_DEVICEINFO_V30 DeviceInfo = new CHCNetSDK.NET_DVR_DEVICEINFO_V30();
                    m_lUserID = CHCNetSDK.NET_DVR_Login_V30(CCTV_IP, CCTV_PORT, ACCESS_ID, ACCESS_PWD, ref DeviceInfo);
                    if (m_lUserID < 0)
                    {
                        Console.WriteLine("[CCTV] - Login Error : {0}", CHCNetSDK.NET_DVR_GetLastError());
                    }
                    else
                    {
                        Console.WriteLine("[CCTV] - Login Successful. ( User ID : {0} )", m_lUserID);
                    }
                }
                else
                {
                    // Logout the device
                    /*if (m_lRealHandle >= 0)
                    {
                        SystemEnv.ShowLogMessage("> (Cam) Please Stop Preview.");
                    }

                    if (!CHCNetSDK.NET_DVR_Logout(m_lUserID))
                    {
                        iLastErr = CHCNetSDK.NET_DVR_GetLastError();
                        SystemEnv.ShowLogMessage("> (Cam) Logout Error : ({0})", CHCNetSDK.NET_DVR_GetLastError());
                    }
                    m_lUserID = -1;*/
                }
            });

            return m_lUserID >= 0;
        }


        private void StartPreview()
        {
            if (m_lUserID < 0)
            {
                Console.WriteLine("[CCTV] - Access Error.");
                return;
            }

            if (m_lRealHandle < 0)
            {
                CHCNetSDK.NET_DVR_PREVIEWINFO lpPreviewInfo = new CHCNetSDK.NET_DVR_PREVIEWINFO();
                if (Pb_CCTV.InvokeRequired)
                {
                    Pb_CCTV.Invoke(new MethodInvoker(delegate {
                        lpPreviewInfo.hPlayWnd = Pb_CCTV.Handle;
                    }));
                }
                else
                {
                    lpPreviewInfo.hPlayWnd = Pb_CCTV.Handle;
                }

                lpPreviewInfo.lChannel = 1;
                lpPreviewInfo.dwStreamType = 0;
                lpPreviewInfo.dwLinkMode = 1;
                lpPreviewInfo.bBlocked = true;
                lpPreviewInfo.dwDisplayBufNum = 1;
                lpPreviewInfo.byProtoType = 0;
                lpPreviewInfo.byPreviewMode = 0;

                if (RealData == null)
                {
                    RealData = new CHCNetSDK.REALDATACALLBACK(RealDataCallBack);
                }

                m_lRealHandle = CHCNetSDK.NET_DVR_RealPlay_V40(m_lUserID, ref lpPreviewInfo, null, IntPtr.Zero);
                if (m_lRealHandle < 0)
                {
                    Console.WriteLine("[CCTV] - NET_DVR_RealPlay_V40 Failed : {0}", CHCNetSDK.NET_DVR_GetLastError());
                }
                else
                {
                    Console.WriteLine("[CCTV] - Start Preview.");
                    // Event Trigger
                    if (ModeListener != null)
                        ModeListener(MODE_PREVIEW);
                }
            }
            //SystemEnv.ShowLogMessage("[CCTV] - RealHandle : {0}", m_lRealHandle);
        }

        private void StopPreview()
        {
            if (m_lRealHandle >= 0)
            {
                if (!CHCNetSDK.NET_DVR_StopRealPlay(m_lRealHandle))
                {
                    Console.WriteLine("[CCTV] - NET_DVR_StopRealPlay Failed : {0}", CHCNetSDK.NET_DVR_GetLastError());
                }
                else
                {
                    m_lRealHandle = -1;
                    Console.WriteLine("[CCTV] - Stop Preview.");
                }
            }
            //SystemEnv.ShowLogMessage("[CCTV] - RealHandle : {0}", m_lRealHandle);
        }

        private void RealDataCallBack(Int32 lRealHandle, UInt32 dwDataType, IntPtr pBuffer, UInt32 dwBufSize, IntPtr pUser)
        {
            if (dwBufSize > 0)
            {
                byte[] sData = new byte[dwBufSize];
                Marshal.Copy(pBuffer, sData, 0, (Int32)dwBufSize);
                FileStream fs = new FileStream("RealData_Callback_Log.ps", FileMode.Create);
                int iLen = (int)dwBufSize;
                fs.Write(sData, 0, iLen);
                fs.Close();
            }
        }

        private void StartAlarmListener()
        {
            if (m_lUserID < 0)
                return;

            CHCNetSDK.NET_DVR_SETUPALARM_PARAM struAlarmParam = new CHCNetSDK.NET_DVR_SETUPALARM_PARAM();
            struAlarmParam.dwSize = (uint)Marshal.SizeOf(struAlarmParam);
            struAlarmParam.byLevel = 1;
            struAlarmParam.byAlarmInfoType = 1;
            struAlarmParam.byFaceAlarmDetection = 1;

            m_lAlarmHandle = CHCNetSDK.NET_DVR_SetupAlarmChan_V41(m_lUserID, ref struAlarmParam);

            if (m_lAlarmHandle < 0)
            {
                Console.WriteLine("[CCTV] - NET_DVR_SetupAlarmChan_V41 Failed : {0)", CHCNetSDK.NET_DVR_GetLastError());
            }
            else
            {
                Console.WriteLine("[CCTV] - Start Alarm Listener.");
                if (ModeListener != null)
                    ModeListener(MODE_ALARM_LISTEN);
            }
            //SystemEnv.ShowLogMessage("[CCTV] - AlarmHandle : {0}", m_lAlarmHandle);
        }

        private void StopAlarmListener()
        {
            if (m_lUserID < 0)
                return;

            if (m_lAlarmHandle >= 0)
            {
                if (!CHCNetSDK.NET_DVR_CloseAlarmChan_V30(m_lAlarmHandle))
                {
                    Console.WriteLine("[CCTV] -  NET_DVR_CloseAlarmChan_V30 Failed : {0}", CHCNetSDK.NET_DVR_GetLastError());
                }
                else
                {
                    m_lAlarmHandle = -1;
                    Console.WriteLine("[CCTV] - Stop Alarm Listener.");
                }
            }
            //SystemEnv.ShowLogMessage("[CCTV] - AlarmHandle : {0}", m_lAlarmHandle);
        }


        private bool MsgCallback_V31(int lCommand, ref CHCNetSDK.NET_DVR_ALARMER pAlarmer, IntPtr pAlarmInfo, uint dwBufLen, IntPtr pUser)
        {
            AlarmMessageHandle(lCommand, ref pAlarmer, pAlarmInfo, dwBufLen, pUser);
            return true;
        }

        private void AlarmMessageHandle(int lCommand, ref CHCNetSDK.NET_DVR_ALARMER pAlarmer, IntPtr pAlarmInfo, uint dwBufLen, IntPtr pUser)
        {
            if (lCommand == CHCNetSDK.COMM_ALARM_V30)
            {
                CHCNetSDK.NET_DVR_ALARMINFO_V30 struAlarmInfo = new CHCNetSDK.NET_DVR_ALARMINFO_V30();
                struAlarmInfo = (CHCNetSDK.NET_DVR_ALARMINFO_V30)Marshal.PtrToStructure(pAlarmInfo, typeof(CHCNetSDK.NET_DVR_ALARMINFO_V30));
                if (struAlarmInfo.dwAlarmType == 3)
                {
                    // Motion Detect
                    // pAlarmer.sDeviceIP

                    string detectTime = DateTime.Now.ToString();
                    if(detectResetTimer.IsEnabled)
                        detectResetTimer.Stop();
                    detectResetTimer.Start();
                    detectCount++;
                    Console.WriteLine("[CCTV] - Detect Count : {0}", detectCount);

                    if (detectCount >= GUEST_DETECT_COUNT)
                    {
                        if (ModeListener != null)
                            ModeListener(MODE_GUEST_DETECT);
                    }
                }
            }
        }
    }
}

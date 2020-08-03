using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using uPLibrary.Networking.M2Mqtt;
using uPLibrary.Networking.M2Mqtt.Exceptions;
using uPLibrary.Networking.M2Mqtt.Messages;

namespace PAC_24Frame.Mqtt
{
    public class MQTTClient
    {
        public delegate void Subscribe_Listener(string message);
        public event Subscribe_Listener SubscribeListener;

        private MqttClient mqttClient = null;

        private const string BROKER_IP = "13.124.176.173";

        public void StartSubscribe()
        {
            if (mqttClient != null && mqttClient.IsConnected)
            {
                mqttClient.Subscribe(new string[] { SystemEnv.GetProductKey() }, new byte[] { MqttMsgBase.QOS_LEVEL_AT_MOST_ONCE });
            }
        }

        public async Task<bool> Connect()
        {
            await Task.Run(() =>
            {
                try
                {
                    mqttClient = new MqttClient(BROKER_IP);
                    mqttClient.ConnectionClosed += MqttClient_ConnectionClosed;
                    mqttClient.MqttMsgPublishReceived += MqttClient_MqttMsgPublishReceived;
                    mqttClient.Connect(SystemEnv.GetProductKey() + SystemEnv.PRODUCT_TYPE);
                }
                catch (MqttConnectionException e)
                {
                    Console.WriteLine(e.StackTrace);
                }                
            });
            Console.WriteLine("[MQTT] - Connect Result : {0}", mqttClient.IsConnected);
            return mqttClient != null && mqttClient.IsConnected;
        }

        public bool IsConnected()
        {
            return mqttClient != null && mqttClient.IsConnected;
        }

        private void MqttClient_ConnectionClosed(object sender, EventArgs e)
        {
            Console.WriteLine("[MQTT] - Disconnected.");
        }

        private void MqttClient_MqttMsgPublishReceived(object sender, MqttMsgPublishEventArgs e)
        {
            string message = Encoding.Default.GetString(e.Message).ToString();
            Console.WriteLine("[MQTT] - Publish Receiveed : {0}", message);
            if (SubscribeListener != null)
                SubscribeListener(message);
        }
    }
}

using Newtonsoft.Json.Linq;
using PAC_24Frame.Http;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace PAC_24Frame.Display
{
    public class ResourceCore
    {
        private const int dummyDelay = 1000;
        private const string Download_Type_User = "user";
        private const string Download_Type_Default = "default";

        private string[] IMG_TYPEs = new string[] { ".jpg", ".png", };
        
        private HTTPClient httpClient;
        
        public ResourceCore()
        {
            httpClient = new HTTPClient();

            SystemEnv.InitResourceFolder();
        }

        public async Task<SettingData> GetSettingData()
        {
            await Task.Delay(dummyDelay);
            SettingData data = null;
            JObject infoObj = await GetUserSettingData();
            if (infoObj != null)
            {
                data = new SettingData(
                            infoObj.GetValue(HTTPPacket.Param_City).ToString(),
                            infoObj.GetValue(HTTPPacket.Param_Province).ToString(),
                            infoObj.GetValue(HTTPPacket.Param_Location_Lat).ToString(),
                            infoObj.GetValue(HTTPPacket.Param_Location_Lon).ToString(),
                            infoObj.GetValue(HTTPPacket.Param_Display_Time).ToString());
            }
            return data;
        }

        private async Task<JObject> GetUserSettingData()
        {
            JObject infoObj = null;
            try
            {
                JObject paramObj = new JObject();
                paramObj.Add(HTTPPacket.Param_Device_ID, SystemEnv.GetProductKey());
                JObject obj = await httpClient.Requester(HTTPPacket.Url_Get_Info, "POST", paramObj);
                string resCode = obj.GetValue(HTTPPacket.Param_Res_Code).ToString();
                Console.WriteLine("[Dislay] User Setting Data.\n{0}", obj.ToString());
                if (resCode.Equals(HTTPPacket.Res_Success))
                {
                    JArray arrayObj = JArray.Parse(obj.GetValue(HTTPPacket.Param_Res_Rows).ToString());
                    infoObj = JObject.FromObject(arrayObj[0]);
                }
            }
            catch (Exception e)
            {
                Console.WriteLine(e.StackTrace);
            }
            return infoObj;
        }

        public async Task<List<ImageResource>> GetImageSources()
        {
            await Task.Delay(dummyDelay);

            string resourceFolderPath = SystemEnv.DownloadFolderPath;
            List<string> fileNames = await GetUserImageList();
            if(fileNames.Count == 0)
            {
                fileNames = await GetDefaultImageList();
                resourceFolderPath = SystemEnv.DefaultFolderPath;
            }

            await DownloadIMGFiles(resourceFolderPath, fileNames);

            List<ImageResource> sources = new List<ImageResource>();
            foreach(string file in fileNames)
            {
                string fileType = file.Substring(file.LastIndexOf("."));
                sources.Add(new ImageResource(
                            IMG_TYPEs.Contains(fileType) ? ImageResource.FILE_TYPE_PICTURE : ImageResource.FILE_TYPE_VIDEO,
                            new Uri(resourceFolderPath + @"/" + file, UriKind.Absolute)));
            }

            return sources;
        }

        private async Task DownloadIMGFiles(string folderPath, List<string> fileNames)
        {
            DirectoryInfo directory = new DirectoryInfo(folderPath);
            List<string> existingFiles = new List<string>();
            foreach (FileInfo info in directory.GetFiles())
            {
                existingFiles.Add(info.Name);
            }

            List<string> downloadFileNames = new List<string>();
            foreach (string fileName in fileNames)
            {
                if (!existingFiles.Contains(fileName))
                    downloadFileNames.Add(fileName);
            }

            foreach (string fileName in downloadFileNames)
            {
                StringBuilder urlStr = new StringBuilder();
                if (folderPath.Equals(SystemEnv.DefaultFolderPath))
                {
                    urlStr.AppendFormat(@"{0}/{1}/{2}/{3}",
                        HTTPPacket.Url_IMG_Downlaod,
                        Download_Type_Default, 
                        fileName, 
                        "0");
                }
                else
                {
                    urlStr.AppendFormat(@"{0}/{1}/{2}/{3}",
                        HTTPPacket.Url_IMG_Downlaod,
                        Download_Type_User,
                        fileName,
                        SystemEnv.GetProductKey());
                }
                string filePath = folderPath + @"/" + fileName;
                Console.WriteLine("> Downlaod Url : {0}", urlStr);
                await httpClient.DownloadFile(new Uri(urlStr.ToString()), filePath);
            }
        }

        private async Task<List<string>> GetDefaultImageList()
        {
            List<string> fileNames = new List<string>();
            try
            {
                JObject obj = await httpClient.Requester(HTTPPacket.Url_Get_Default_IMGs, "POST", null);
                string resCode = obj.GetValue(HTTPPacket.Param_Res_Code).ToString();
                Console.WriteLine("[Display] Default IMG Resource.\n{0}", obj.ToString());
                if (resCode.Equals(HTTPPacket.Res_Success))
                {
                    JArray arrayObj = JArray.Parse(obj.GetValue(HTTPPacket.Param_File_List).ToString());
                    foreach (JObject imgObj in arrayObj.Children<JObject>())
                    {
                        string fileName = imgObj.GetValue(HTTPPacket.Param_IMG_Name).ToString();
                        fileNames.Add(fileName);
                    }
                }
            }
            catch (Exception e)
            {
                Console.WriteLine(e.StackTrace);
            }
            return fileNames;
        }

        private async Task<List<string>> GetUserImageList()
        {
            List<string> fileNames = new List<string>();
            try
            {
                JObject paramObj = new JObject();
                paramObj.Add(HTTPPacket.Param_Device_ID, SystemEnv.GetProductKey());
                JObject obj = await httpClient.Requester(HTTPPacket.Url_Get_IMGs, "POST", paramObj);
                string resCode = obj.GetValue(HTTPPacket.Param_Res_Code).ToString();
                Console.WriteLine("[Display] User IMG Resource.\n{0}", obj.ToString());
                if (resCode.Equals(HTTPPacket.Res_Success))
                {
                    JArray arrayObj = JArray.Parse(obj.GetValue(HTTPPacket.Param_Res_Rows).ToString());
                    foreach (JObject imgObj in arrayObj.Children<JObject>())
                    {
                        string fileName = imgObj.GetValue(HTTPPacket.Param_IMG_Name).ToString();
                        fileNames.Add(fileName);
                    }
                }
            }
            catch (Exception e)
            {
                Console.WriteLine(e.StackTrace);
            }
            return fileNames;
        }
    }
}

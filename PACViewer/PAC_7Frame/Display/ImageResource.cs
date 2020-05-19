using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace PAC_7Frame.Display
{
    public class ImageResource
    {
        public const int FILE_TYPE_PICTURE = 1;
        public const int FILE_TYPE_VIDEO = 2;

        private Uri uri;
        private int type;

        public ImageResource(int type, Uri uri)
        {
            this.type = type;
            this.uri = uri;
        }

        public Uri GetUri()
        {
            return uri;
        }

        public int GetFileType()
        {
            return type;
        }
    }
}

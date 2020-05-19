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
using System.Windows.Media.Animation;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;
using System.Windows.Threading;

namespace PAC_7Frame.Display
{
    /// <summary>
    /// Displayer.xaml에 대한 상호 작용 논리
    /// </summary>
    public partial class Displayer : UserControl
    {
        /*
            Fade In/Out
            - 이미지/영상 종료 함수 호출
            - Fade Effect Timer 시작 
            - Fade Effect는 2초, Timer Tick 1초
            - 초기 Tick에서 이미지/영상 리소스 변경
        */

        private DispatcherTimer disPlayTimer;
        private DispatcherTimer fadeEffectTimer;

        private List<ImageResource> imageSources;
        private int outputTime = 5, fadeEffectTime = 6;
        private int fadeEffectCount;
        private int sourcePosition = 0;
        private bool isPlaying = false;


        public Displayer()
        {
            InitializeComponent();
            InitializeObject();
        }

        private void InitializeObject()
        {
            disPlayTimer = new DispatcherTimer();
            disPlayTimer.Tick += DisPlayTimer_Tick;
            disPlayTimer.Interval = new TimeSpan(0, 0, outputTime);

            fadeEffectTimer = new DispatcherTimer();
            fadeEffectTimer.Tick += FadeEffectTimer_Tick; ;
            fadeEffectTimer.Interval = new TimeSpan(0, 0, 1);
        }

        private void FadeEffectTimer_Tick(object sender, EventArgs e)
        {
            Dispatcher.Invoke(new Action(delegate
            {
                FadeEffectHandler();
            }), DispatcherPriority.Normal);
        }

        private void DisPlayTimer_Tick(object sender, EventArgs e)
        {
            disPlayTimer.Stop();
            fadeEffectCount = fadeEffectTime;
            fadeEffectTimer.Start();
        }

        private void mPlayer_MediaOpened(object sender, RoutedEventArgs e)
        {
            isPlaying = true;
            Console.WriteLine("[Display] - Media Source Opened.");
        }

        private void mPlayer_MediaEnded(object sender, RoutedEventArgs e)
        {
            isPlaying = false;
            Console.WriteLine("[Display] - Media Source Ended.");
            fadeEffectCount = fadeEffectTime;
            fadeEffectTimer.Start();
        }



        private void FadeEffectHandler()
        {
            if (fadeEffectCount == fadeEffectTime)
            {
                StartFadeAnimation();
                if (Cv_Cover.Visibility == Visibility.Collapsed)
                    Cv_Cover.Visibility = Visibility.Visible;
            }
            else if (fadeEffectCount == fadeEffectTime / 2)
            {
                ShowImageSource();
            }
            else if (fadeEffectCount == 1)
            {
                Console.WriteLine("[Display] - Disable Fade Effiect.");
                fadeEffectTimer.Stop();
                //Cv_Cover.Visibility = Visibility.Collapsed;
            }
            fadeEffectCount--;
        }

        private void StartFadeAnimation()
        {
            Console.WriteLine("[Display] - Enable Fade Effiect.");
            DoubleAnimation fadeAnimation = new DoubleAnimation();
            fadeAnimation.From = 0;
            fadeAnimation.To = 1;
            fadeAnimation.AutoReverse = true;
            fadeAnimation.Duration = new Duration(TimeSpan.FromSeconds(fadeEffectTime / 2));
            Cv_Cover.BeginAnimation(Canvas.OpacityProperty, fadeAnimation);
        }

        public void SetImageResources(List<ImageResource> sources)
        {
            imageSources = sources;
        }

        public void SetOutputTime(int sec)
        {
            outputTime = sec;
        }

        public void SetFadeEffectTime(int sec)
        {
            fadeEffectTime = sec;
        }

        public void Start(bool isResetPosition)
        {
            Stop();
            //ShowImageSource();

            if (isResetPosition)
                sourcePosition = 0;

            if (imageSources.Count != 0)
            {
                fadeEffectCount = fadeEffectTime;
                fadeEffectTimer.Start();
            }
        }

        public void Stop()
        {
            try
            {
                if (disPlayTimer.IsEnabled)
                    disPlayTimer.Stop();

                if (isPlaying)
                {
                    mPlayer.Pause();
                }

                if (fadeEffectTimer.IsEnabled)
                    fadeEffectTimer.Stop();
            }
            catch (Exception e)
            {
                //Console.WriteLine(e.StackTrace);
            }
        }

        private void ShowImageSource()
        {
            if (this.Background == null)
                this.Background = Brushes.Black;

            if (sourcePosition == imageSources.Count)
                sourcePosition = 0;

            Console.WriteLine("[Display] - Show Image : ({0})",
                imageSources[sourcePosition].GetUri().ToString());

            if (imageSources[sourcePosition].GetFileType() == ImageResource.FILE_TYPE_PICTURE)
            {
                if (mPlayer.Visibility == Visibility.Visible)
                    mPlayer.Visibility = Visibility.Collapsed;

                if (iViewer.Visibility == Visibility.Collapsed)
                    iViewer.Visibility = Visibility.Visible;

                iViewer.Source = new BitmapImage(imageSources[sourcePosition].GetUri());
                disPlayTimer.Start();
            }
            else
            {
                if (mPlayer.Visibility == Visibility.Collapsed)
                    mPlayer.Visibility = Visibility.Visible;
                if (iViewer.Visibility == Visibility.Visible)
                    iViewer.Visibility = Visibility.Collapsed;

                if (disPlayTimer.IsEnabled)
                    disPlayTimer.Stop();

                //mPlayer.Stop();
                mPlayer.Source = imageSources[sourcePosition].GetUri();
                mPlayer.Play();
            }
            sourcePosition++;
        }
    }
}

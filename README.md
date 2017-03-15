# (自定义view实现）音量波形图

标签： 自定义view 音量波形 音波 

---

本文目的：主要是记录自己在实现自定义view的时候，一些思路和解决方案。

## 目标

![音量波形图][1]
 
 绘制两个音量波形，并且能够向右运动，上面的波形移动速度慢，下面的波形移动速度快，并且振幅能够根据音量的高低进行改变。

## 分解目标
先考虑静止状态，上图有两个波形图，现只考虑一个波形图，每个波形图类似于两个正弦函数的闭合。所以我们第一步要绘制一个**正弦图形**。

![正弦函数图像][2]

## 绘制正弦函数
关于自定义view的图形绘制，一般都需要onMeasure，onLayout，onDraw三个步骤。由于是自定义view，而不是viewGroup，所以并不需要实现onLayout方法。
在绘制之前，要在onMeasure方法里，计算出画布的高度、宽度、中心点等需要计算的变量，这里就不详细说明了。
为了便于绘制图形正弦函数，要把画布的坐标原点移动到绘制view的中间位置。
也就是下图中标明的点，这样坐标原点(0,0)，就位于view的中间，便于函数计算。

正弦函数方法参考：

        private double sine(float x, int period, float drawWidth) {
            return Math.sin(2 * Math.PI * period * x / drawWidth);
        }

其中period为在画布里有多少个周期，假设period为3，就是在画布里有三个周期。drawWidth为画布宽度。

在ondraw 方法里进行绘制。
这里调用drawsine方法

    private void drawSine(Canvas canvas, Path path, Paint paint, int period, float drawWidth, float amplitude) {
        float halfDrawWidth = drawWidth / 2f;
        path.reset();
        path.moveTo(-halfDrawWidth, 0);//将绘制的起点移动到最左边
        float y;
        for (float x = -halfDrawWidth; x <= halfDrawWidth; x++) {
            y = (float) sine(x, period, drawWidth) * amplitude; 
            path.lineTo(x, y);
        }   
        canvas.drawPath(path, paint);
        canvas.save();
        canvas.restore(); 
    }
amplitude 为振幅的高度，也就是半个画布的高度。绘制出的图形如下（在手机里，y轴正方形是向下的，x轴正方形是向右的）

![正弦函数图][3]

## 绘制两个关于y轴对称正弦函数
绘制反方向正弦函数，并且填充里面的内容。只是相当于将y值乘以-1，这里不详细列出具体代码

![两个正弦函数图][4]

进行内容填充 mPaint.setStyle(Style.FILL); 画笔的样式设置为填充，填充后的效果如下

![音波图][5]

这样勉强能算作一个波形图了。
## 缩放波形图
观察刚开始的效果图，发现每个波形的振幅并不相同，所以要考虑对波形图进行缩放。
采用缩放函数，就是按比例将振幅逐渐增大或者减小。

        double scaling;
        for (float x = -halfDrawWidth; x <= halfDrawWidth; x++) {
            scaling = 1 - Math.pow(x / halfDrawWidth, 2);// 对y进行缩放
            y = (float) (sine(x, period, drawWidth) * amplitude * (1) * Math
                    .pow(scaling, 3));
            path.lineTo(x, y);
        }
为了更好的效果，我们缩放了三次，Math.pow(scaling, 3) 
现在感觉和图一的效果差不多了。基本满足需求，就是每个波形之间的间隙还是很小。（后续会进行优化）

![音波缩放图][6]

## 让波形图动起来
在view里定义一个移动线程MoveThread，每隔一段时间就执行一次刷新postInvalidate()，每次刷新图像的时候，都会改变该图形的相位。
所谓相位，查看下图，一个函数是sin(x),另外一个函数是sin(x+0.5)，两个函数之间就相差了0.5个相位。
![正弦函数相位 + 0.5 图][7]
  相位变化了0.5，看起来就会向左移动0.5的距离。（图形右上角有标注函数）
在线程中不断更新相位的取值，这样不断的刷新图形，就会看起来形成一种移动的效果。（大家可以想象以前放电影时用的胶片，实现原理类似）这样我们的图形就能运动起来了。
修改后的sine函数

    private double sine(float x, int period, float drawWidth, double phase) {
        return Math.sin(2 * Math.PI * period * (x + phase) / drawWidth);
    }
定义一个MoveThread

    private class MoveThread extends Thread {
        private static final int MOVE_STOP = 1;

        private static final int MOVE_START = 0;

        private int state;

        @Override
        public void run() {
            mPhase = 0;
            state = MOVE_START;
            while (true) {
                if (state == MOVE_STOP) {
                    break;
                }
                try {
                    sleep(30);
                } catch (InterruptedException e) {
                }
                mPhase -= MOVE_DISTACE;
                postInvalidate();
            }
        }
        public void stopRunning() {
            state = MOVE_STOP;
        }
    }
    
这样当线程开启的时候，我们就能根据不断的改变sine函数的相位，就会形成不断右移动的效果。

## 绘制两个波形，并且设置不同的移动速度
两个波形的区别只是颜色不同，最大振幅不同，以及移动速度不同。
所谓移动速度不同，就是相位每次改变的值不同。可以在计算sine函数的时候，对固定相位值乘以不同的比例，就会得到不同的移动速度。从下图中的移动我们可以看到效果，已经很接近目标了。

![音波移动图形][8]

（这里可以在图中看到不同的实现效果，为了便于有些同学学习和实践，将整个view进行了解剖，能更快的学习view的绘制过程）

## 根据音量改变波形图的振幅
通过音量设置波形图振幅，这样能够让波形图随着声音大小的变化而变化。
我们改变sin函数的振幅，图形就会升高或者下降。也就是在相同的x位置处，y的取值会发生变化。

![振幅不同，两个正弦的高度也不同][9]

但是，随着音频的变化，振幅的变动幅度变大，这样会造成一种图形的闪动。

## 解决图形闪动
当音量变化时，我们的振幅会发生变化，也就是这个图形，会随着振幅的变化按比例变大或者变小。如下图标记的两个点，如果我们刷新间隔为1s，就是1s之后，点1会突然变成点2的位置。这样就会造成闪动。

![点在不同的振幅，所在的高度不同][10]

我们的要求是图形要平滑的变动，意思就是不能这么快的进行变化，要怎么解决呢？
首先我们规定上升的最大速度为为1px每秒，现在的y值为1px，也就是当前1的位置。
现在只考虑点1的位置，假设我们每1s刷新一次，上升的最大速度为1px每秒，这样我们就可以计算出下一次变化y的最高位置为 1px + 1px/秒 * 1秒 = 2。

 - 如果当前音量发生变化，也就是振幅发生改变，得到的y值为3px，这个时候y值，3px >
   我们计算的2px，这个时候就要用我们的2px。也就保证了最大速度不能超过我们规定的速度。
 - 如果当前音量发生变化，也就是振幅发生改变，得到的y值为1.5px，这个时候y值，1.5px <
   我们计算的2px，这个时候就要用我们的1.5px。根据实际位置进行设定。

下降同理，这样我们就能保证上升或者下降的最大速度。

        // 计算当前时间下的振幅
        private float currentVolumeAmplitude(long curTime) {
            if (lastAmplitude == nextTargetAmplitude) {
                return nextTargetAmplitude;
            }

            if (curTime == amplitudeSetTime) {
                return lastAmplitude;
            }

            if (nextTargetAmplitude > lastAmplitude) {
                float target = lastAmplitude + mVerticalSpeed
                        * (curTime - amplitudeSetTime) / 1000;
                if (target >= nextTargetAmplitude) {
                    target = nextTargetAmplitude;
                    lastAmplitude = nextTargetAmplitude;
                    amplitudeSetTime = curTime;
                    nextTargetAmplitude = mMinAmplitude;
                }
                return target;
            }

            if (nextTargetAmplitude < lastAmplitude) {
                float target = lastAmplitude - mVerticalRestoreSpeed
                        * (curTime - amplitudeSetTime) / 1000;
                if (target <= nextTargetAmplitude) {
                    target = nextTargetAmplitude;
                    lastAmplitude = nextTargetAmplitude;
                    amplitudeSetTime = curTime;
                    nextTargetAmplitude = mMinAmplitude;
                }
                return target;
            }

            return mMinAmplitude;
        }

## 图形优化
因为中间的间隙过小，我们要把中间的间歇变大，类似于下图。这样效果可能会更好一点。

![优化后的波形图][11]

实施方案，将正弦函数上移，下面的正弦函数下移动，这样中间留有固定宽度的，通过缩放函数之后，效果如下：

![优化后的音量波形图][12]

## 实验过程中存在的问题以及解决方案：
### 中间线条的问题
横线的原因，是因为缩放造成了这 两个波形之间的点 x对应的值，y不等于0，会闭合不到中间的点。造成这个的现象是因为我们只是针对半个正弦曲线就进行填充了

![有瑕疵的波形图][13]

所以我们要将正反两个曲线画出来之后，把路径闭合之后再进行填充。这样就不会出现上面中间有横线的瑕疵

![修复后的波形图][14]

### 闪动问题
参考上文解决方案

## 可以直接使用的view

源代码地址：[https://github.com/duchao/VolumeView][15]

##可以直接使用的view
VolumeView.java
API:  start() 开始
       stop() 结束
       setVolume(float volume) 设置音量

  [1]: http://upload-images.jianshu.io/upload_images/3050535-330c294cd7882d02.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240
  [2]: http://upload-images.jianshu.io/upload_images/3050535-eadb0d5bbea603bd.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240
  [3]: http://upload-images.jianshu.io/upload_images/3050535-cf4afe45500e5c25.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240
  [4]: http://upload-images.jianshu.io/upload_images/3050535-2d83125173ad0cbd.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240
  [5]: http://upload-images.jianshu.io/upload_images/3050535-d1a39b1e5ec3a2a6.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240
  [6]: http://upload-images.jianshu.io/upload_images/3050535-462a1fd28452cc6d.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240
  [7]: http://upload-images.jianshu.io/upload_images/3050535-b4cff2b8776c3600.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240
  [8]: http://upload-images.jianshu.io/upload_images/3050535-5a9e12ec667ce851.gif?imageMogr2/auto-orient/strip
  [9]: http://upload-images.jianshu.io/upload_images/3050535-7c1c959115883a06.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240
  [10]: http://upload-images.jianshu.io/upload_images/3050535-283ad28a988d46ce.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240
  [11]: http://upload-images.jianshu.io/upload_images/3050535-4804659eb60b780a.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240
  [12]: http://upload-images.jianshu.io/upload_images/3050535-1a47d6f71bb450f1.gif?imageMogr2/auto-orient/strip
  [13]: http://upload-images.jianshu.io/upload_images/3050535-c2fa84fa7e1157b0.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240
  [14]: http://upload-images.jianshu.io/upload_images/3050535-42955fd827a69809.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240
  [15]: https://github.com/duchao/VolumeView

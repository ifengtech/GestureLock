【高仿】手势解锁
============

本项目直接基于
[https://github.com/7heaven/GestureLock](https://github.com/7heaven/GestureLock "@7heaven/GestureLock") 项目原型，并效仿支付宝手势解锁交互方式进行二次开发。

原项目主要将手势触点和图案绘制逻辑做了很好的拆分，GestureLock继承自RelativeLayout实现点阵布局，GestureLockView自定义View则专注于触点逻辑。逻辑上比较清晰，代码质量较高，这也是基于此项目迭代的原因。

此次迭代将加入几个新功能：

1. APP主题做了全新的定制
2. 添加了`GestureDetector`自定义View，用于实时显示触点，或者记录手势划过触点的位置。在此基础上，修改了原`GestureLock.OnGestureEventListener`回调接口。
3. 将`GestureLock`定制为两种模式：解锁模式和设置模式，也就是对应于支付宝手势的解锁以及设置功能。
4. 添加手势编码的逻辑，即绘制的手势可以用一个字符串来表达，当然也可以将一个特定的字符串转化为手势图案，以利于后续的手势保存功能。

接口的调用比较简单，`MainActivity`中代码片段如下：

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		gestureView = (GestureLock) findViewById(R.id.gesture_lock);
		gestureView.setCorrectGesture(new int[]{0, 3, 6, 7, 8, 5, 2, 1, 4});
		gestureView.setMode(GestureLock.MODE_EDIT);
		gestureDetector = (GestureDetector) findViewById(R.id.gesture_detector);
		gestureDetector.setGestureLock(gestureView);
		gestureDetector.setShowMode(GestureDetector.MODE_SHOW_ONCE);
	}

截图如下：

![](/art/screenshot0.png)
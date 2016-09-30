package com.yoyo.yobutton;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class FloatViewService extends AccessibilityService {
    private static final String TAG = "FloatViewService";
    // 定义浮动窗口布局
    private LinearLayout mFloatLayout;
    private WindowManager.LayoutParams wmParams;
    // 创建浮动窗口设置布局参数的对象
    private WindowManager mWindowManager;
    private ImageButton mFloatView;

    private boolean waitDouble = true;
    private static final int DOUBLE_CLICK_TIME = 250; // 两次单击的时间间隔
    boolean isLongClick;
    public static AccessibilityService initialize;
    @Override
    public void onCreate() {
        super.onCreate();
        initialize=this;
        Log.i(TAG, "onCreate");
        createFloatView();
    }

    @SuppressWarnings("static-access")
    @SuppressLint("InflateParams")
    private void createFloatView() {
        wmParams = new WindowManager.LayoutParams();
        // 通过 getApplication 获取的是 WindowManagerImpl.CompatModeWrapper
        mWindowManager = (WindowManager) getApplication().getSystemService(getApplication().WINDOW_SERVICE);
        // 设置 window type
        wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        // 设置图片格式，效果为背景透明
        wmParams.format = PixelFormat.RGBA_8888;
        // 设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        // 调整悬浮窗显示的停靠位置为左侧置顶
        wmParams.gravity = Gravity.START | Gravity.TOP;
        // 以屏幕左上角为原点，设置 x、y 初始值，相对于 gravity
        wmParams.x = 0;
        wmParams.y = 152;

        // 设置悬浮窗口长宽数据
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        LayoutInflater inflater = LayoutInflater.from(getApplication());
        // 获取浮动窗口视图所在布局
        mFloatLayout = (LinearLayout) inflater.inflate(R.layout.alert_window_menu, null);
        // 添加 mFloatLayout
        mWindowManager.addView(mFloatLayout, wmParams);
        // 浮动窗口按钮
        mFloatView = (ImageButton) mFloatLayout.findViewById(R.id.alert_window_imagebtn);

        mFloatLayout.measure(View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
                .makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        // 设置监听浮动窗口的触摸移动
        mFloatView.setOnTouchListener(new View.OnTouchListener() {

            boolean isClick;

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mFloatView.setBackgroundResource(R.drawable.btn_down);
                        isClick = false;
                        isLongClick=true;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        isClick = true;
                        isLongClick=false;
                        // getRawX 是触摸位置相对于屏幕的坐标，getX 是相对于按钮的坐标
                        wmParams.x = (int) event.getRawX()
                                - mFloatView.getMeasuredWidth() / 2;
                        // 减 25 为状态栏的高度
                        wmParams.y = (int) event.getRawY()
                                - mFloatView.getMeasuredHeight() / 2 - 75;
                        // 刷新
                        mWindowManager.updateViewLayout(mFloatLayout, wmParams);
                        return true;
                    case MotionEvent.ACTION_UP:
                        isLongClick=false;
                        mFloatView.setBackgroundResource(R.drawable.btn);
                        return isClick;// 此处返回 false 则属于移动事件，返回 true 则释放事件，可以出发点击否。

                    default:
                        break;
                }
                return false;
            }
        });

        mFloatView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (waitDouble) {
                    waitDouble = false;
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            try {
                                sleep(DOUBLE_CLICK_TIME);
                                if (!waitDouble) {
                                    waitDouble = true;
                                    singleClick();
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    thread.start();
                } else {
                    waitDouble = true;
                    doubleClick();
                }
            }
        });
        mFloatView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(isLongClick){
                    Utils.recentApps(FloatViewService.this);
                }

                return true;
            }
        });
    }

    // 单击响应事件
    private void singleClick() {
        Log.i(TAG, "singleClick");
        Utils.virtualBack(this);
    }


    // 双击响应事件
    private void doubleClick() {
        Log.i(TAG, "doubleClick");
        Utils.virtualHome(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFloatLayout != null) {
            // 移除悬浮窗口
            mWindowManager.removeView(mFloatLayout);
        }
        initialize=null;
    }



    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }

    @Override
    public void onInterrupt() {

    }

}

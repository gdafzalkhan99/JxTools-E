package com.picfix.tools.view.views;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.customview.widget.ViewDragHelper;

import com.picfix.tools.utils.JLog;

/**
 * @author Herr_Z
 * @description:
 * @date : 2021/12/21 14:47
 */
public class MoveViewByViewDragHelper extends LinearLayout {

    private int mParentWidth;
    private int mWidth;

    private int mLeft, mTop;

    private ViewDragHelper mViewDragHelper;
    private FrameLayout dynamicLayout;
    private int dWidth = 0;
    private int currentWidth = 0;

    public MoveViewByViewDragHelper(Context context) {
        super(context);
        init(context);
    }

    public MoveViewByViewDragHelper(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MoveViewByViewDragHelper(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MoveViewByViewDragHelper(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        MyDragHelper myDragHelper = new MyDragHelper();
        mViewDragHelper = ViewDragHelper.create(this, myDragHelper);
//        setBackgroundColor(Color.rgb(0, 125, 125));
        post(() -> {
            if (mWidth == 0) {
                mWidth = getWidth();
            }
            if (mParentWidth == 0) {
                mParentWidth = ((View) getParent()).getWidth();
            }
            JLog.i(mWidth + "--" + mParentWidth);
        });
    }

    public void setLayout(FrameLayout layout, int width) {
        dynamicLayout = layout;
        dWidth = width;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mViewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mViewDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    public void computeScroll() {
        if (mViewDragHelper.continueSettling(true)) {
            invalidate();
        }
    }

    class MyDragHelper extends ViewDragHelper.Callback {

        @Override
        public boolean tryCaptureView(@NonNull View child, int pointerId) {
            //true :child view can move
            mLeft = child.getLeft();
            mTop = child.getTop();

//            dWidth = mWidth;

            JLog.i("left =" + mLeft);
            return true;
        }

        @Override
        public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
            if (child == getChildAt(0) || child == getChildAt(2)) {
                JLog.i("left2 =" + left);
                ViewGroup.LayoutParams layoutParams = dynamicLayout.getLayoutParams();
                if (left >= mLeft) {
                    currentWidth = dWidth + (left - mLeft);
                    layoutParams.width = dWidth + (left - mLeft);
                } else {
                    currentWidth = dWidth - (mLeft - left);
                    layoutParams.width = dWidth - (mLeft - left);
                }
                dynamicLayout.setLayoutParams(layoutParams);
                return left;
            }
            return super.clampViewPositionHorizontal(child, left, dx);
        }

        @Override
        public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
            if (child == getChildAt(1) || child == getChildAt(2)) {
                JLog.i("top2 =" + top);
                return top;
            }
            return super.clampViewPositionVertical(child, top, dy);
        }

        @Override
        public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
            JLog.i("finger up");
            dWidth = currentWidth;
            if (releasedChild == getChildAt(2)) {
                JLog.i("finger up");
                //smoothSlideViewTo、settleCapturedViewAt、flingCapturedView这三个方法类似，在内部是用的ScrollerCompat来实现滑动的。
                //需要重写computeScroll，使scroll生效
                mViewDragHelper.settleCapturedViewAt(mLeft, mTop);
                invalidate();
            } else {
                super.onViewReleased(releasedChild, xvel, yvel);
            }

        }

    }
}

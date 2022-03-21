package com.picfix.tools.view.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import com.picfix.tools.utils.AppUtil
import com.picfix.tools.utils.JLog

/**
 * @author Herr_Z
 * @description:
 * @date : 2021/7/16 15:43
 */
class TestView(context: Context, attributeSet: AttributeSet) : AppCompatImageView(context, attributeSet) {
    private var pointX = 0f
    private var pointY = 0f
    private var currentX = 0f
    private var currentY = 0f
    private var x = 0
    private lateinit var dynamicLayout: FrameLayout
    private var mWidth = AppUtil.getScreenWidth(context)
    private var mHeight = AppUtil.getScreenHeight(context)
    private var scroll = false
    private var scrollWidth = 0


    fun setLayout(view: FrameLayout, width: Int) {
        dynamicLayout = view
        mWidth = width
        currentX = mWidth / 2.0f
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        when (event!!.action) {
            MotionEvent.ACTION_DOWN -> {
                pointX = event.x
                pointY = event.y

                scroll = pointX <= currentX + 20 && pointX >= currentX - 20

            }

            MotionEvent.ACTION_MOVE -> {
                if (scroll) {
                    scrollWidth = (currentX - event.x).toInt()
                    scrollX = scrollWidth

                    JLog.i("scrollWidth = $scrollWidth")

                    val dynamicLayoutParam = dynamicLayout.layoutParams
                    dynamicLayoutParam.width = mWidth / 2 - scrollWidth
                    dynamicLayout.layoutParams = dynamicLayoutParam
                }
            }

            MotionEvent.ACTION_UP -> {
                if (scroll) {
                    currentX = (mWidth / 2 - scrollWidth).toFloat()
                    JLog.i("currentX = $currentX")
                }

                performClick()
                return true
            }
        }

        return super.onTouchEvent(event)
    }
}
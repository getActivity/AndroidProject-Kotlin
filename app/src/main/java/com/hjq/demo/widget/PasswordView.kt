package com.hjq.demo.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import com.hjq.demo.R

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2018/12/2
 *    desc   : 密码遮挡自定义控件
 */
class PasswordView @JvmOverloads constructor
    (context: Context?, attrs: AttributeSet? = null,
     defStyleAttr: Int = 0, defStyleRes: Int = 0) :
    View(context, attrs, defStyleAttr, defStyleRes) {

    companion object {

        /** 中心黑点的半径大小 */
        private const val POINT_RADIUS: Int = 15

        /** 中心黑点的颜色 */
        private val POINT_COLOR: Int = Color.parseColor("#666666")

        /** 密码框边界线的颜色值 */
        private val STROKE_COLOR: Int = Color.parseColor("#ECECEC")

        /** 密码总个数 */
        const val PASSWORD_COUNT: Int = 6
    }

    private val paint: Paint = Paint()
    private val path: Path = Path()
    private val pointPaint: Paint = Paint()

    /** 单个密码框的宽度 */
    private val itemWidth: Int = resources.getDimension(R.dimen.dp_44).toInt()

    /** 单个密码框的高度 */
    private val itemHeight: Int = resources.getDimension(R.dimen.dp_41).toInt()

    /** 已经输入的密码个数，也就是需要显示的小黑点个数 */
    private var currentIndex: Int = 0

    init {
        // 设置抗锯齿
        paint.isAntiAlias = true
        // 设置颜色
        paint.color = STROKE_COLOR
        // 设置描边
        paint.style = Paint.Style.STROKE

        path.moveTo(0f, 0f)
        path.lineTo((itemWidth * PASSWORD_COUNT).toFloat(), 0f)
        path.lineTo((itemWidth * PASSWORD_COUNT).toFloat(), itemHeight.toFloat())
        path.lineTo(0f, itemHeight.toFloat())
        path.close()

        pointPaint.isAntiAlias = true
        pointPaint.style = Paint.Style.FILL
        pointPaint.color = POINT_COLOR
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var finalWidthMeasureSpec: Int = widthMeasureSpec
        var finalHeightMeasureSpec: Int = heightMeasureSpec
        when (MeasureSpec.getMode(finalWidthMeasureSpec)) {
            MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED ->
                finalWidthMeasureSpec = MeasureSpec.makeMeasureSpec(itemWidth * PASSWORD_COUNT, MeasureSpec.EXACTLY)
            MeasureSpec.EXACTLY -> {}
        }
        when (MeasureSpec.getMode(finalHeightMeasureSpec)) {
            MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED ->
                finalHeightMeasureSpec = MeasureSpec.makeMeasureSpec(itemHeight, MeasureSpec.EXACTLY)
            MeasureSpec.EXACTLY -> {}
        }
        setMeasuredDimension(finalWidthMeasureSpec, finalHeightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        paint.strokeWidth = 5f
        canvas.drawPath(path, paint)

        // 画单个的分割线
        paint.strokeWidth = 3f
        for (index in 1 until PASSWORD_COUNT) {
            canvas.drawLine((itemWidth * index).toFloat(), 0f, (itemWidth * index).toFloat(), itemHeight.toFloat(), paint)
        }

        // 绘制中间的小黑点
        if (currentIndex == 0) {
            return
        }
        for (i in 1..currentIndex) {
            canvas.drawCircle(
                i * itemWidth - itemWidth.toFloat() / 2,
                itemHeight.toFloat() / 2,
                POINT_RADIUS.toFloat(),
                pointPaint
            )
        }
    }

    /**
     * 改变密码提示小黑点的个数
     */
    fun setPassWordLength(index: Int) {
        currentIndex = index
        invalidate()
    }
}
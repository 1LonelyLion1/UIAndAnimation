package com.example.uiandanimation

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import kotlin.math.*

/*

1.Нужно еще засинхронить анимации
2.додумать как правильно получать цвет бэкграунда
(нужно попробовать через onAnimationEnd() устанавливать бэк)
или менять чтото еще бэк(совместиь с другой view)
3.Разделить анимацию изменения цвета(можно еще переделать акселераацию по зонам по красивее) на зоны по цветам(пока нет идей :( )
4.Возможно стоит прикрутить атрибуты для более гибкой настройки

 */

class SpeedView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0

) : View(context, attributeSet, defStyleAttr, defStyleRes) {

    private var paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var value: Int = 1
    private val maxValue = 120



    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        var width = MeasureSpec.getSize(widthMeasureSpec).toFloat()

        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        var height = MeasureSpec.getSize(heightMeasureSpec).toFloat()

        val ratio = width / height
        val normalRatio = 2f / 1f
        if (ratio > normalRatio) {
            if (widthMode != MeasureSpec.EXACTLY) {
                width = ((normalRatio * height))
            }
        }
        if (ratio < normalRatio) {
            if (heightMode != MeasureSpec.EXACTLY) {
                height = ((width / normalRatio))
            }
        }
        setMeasuredDimension(width.toInt(), height.toInt())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val scale = 0.9f
        val longScale = 0.9f
        val textPadding = 0.85f
        val markRange = 10


        var width = width
        var height = height
        // вычисление корректного соотношения
        val ratio = width / height
        val normalRatio = 2f/1f
        if ( ratio > normalRatio) {
            width = (normalRatio * height).toInt()
        }
        if ( ratio < normalRatio){
            height = (width / normalRatio).toInt()
        }


        canvas.save()


        //перенос системы координат для удобства расчетов
        canvas.scale(.5f * width, -1f * height)
        canvas.translate(1f, -1f)

//отрисовка круга
        paint.color = Color.BLACK
        paint.style = Paint.Style.FILL

        canvas.drawCircle(0F, 0F, 1F, paint)



//отрисовка шкалы
        paint.color = Color.GREEN
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.005f


        val  step: Double = Math.PI / maxValue
        for (i in 0..maxValue) {
            val x1 = cos(Math.PI - step * i).toFloat()
            val y1 = sin(Math.PI - step * i).toFloat()
            var x2: Float
            var y2: Float
            if (i % 20 == 0) {
                x2 = x1 * scale * longScale
                y2 = y1 * scale * longScale
            } else {
                x2 = x1 * scale
                y2 = y1 * scale
            }
            canvas.drawLine(x1, y1, x2, y2, paint)
        }

        canvas.restore()
        canvas.save()

//отрисовка чисел на шкале
        canvas.translate((width / 2).toFloat(), 0F)

        paint.textSize = height / 10.toFloat()
        paint.color = Color.GREEN
        paint.style = Paint.Style.FILL

        val factor: Float = height * scale * longScale * textPadding


            var i = 0
            while (i <= maxValue) {
                val x = cos(Math.PI - step * i).toFloat() * factor
                val y = sin(Math.PI - step * i).toFloat() * factor
                val text = i.toString()
                val textLen: Int = paint.measureText(text).roundToInt()
                canvas.drawText(
                    i.toString(), (x - textLen / 2),
                    (height - y), paint
                )
                i += markRange
            }

        canvas.restore()
        canvas.save()

//отрисовка поворота и начальной позиции
        canvas.translate((width / 2).toFloat(), height.toFloat())
        canvas.scale(.5f * width, -1f * height)
        canvas.rotate(90 - 180.toFloat() * (value / maxValue.toFloat()))
//отрисовка стрелки
        paint.color = -0x7767
        paint.strokeWidth = 0.02f
        canvas.drawLine(0.01f, 0F, 0F, 1f, paint)
        canvas.drawLine(-0.01f, 0F, 0F, 1f, paint)

// отрисовка круга в центре
        paint.style = Paint.Style.FILL
        paint.color = -0x770067
        canvas.drawCircle(0f, 0f, .05f, paint)

        canvas.restore()

    }

    //так и не понял почему кидает варнинг, ппришлось заюзать это
     @SuppressLint("ClickableViewAccessibility")
     override fun onTouchEvent(event: MotionEvent): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val newValue: Int = getTouchValue(event.x, event.y)
                setValueAnimated(newValue)
                true
            }
            MotionEvent.ACTION_MOVE -> true
            MotionEvent.ACTION_UP -> true
            else -> super.onTouchEvent(event)
        }
    }

    private fun getTouchValue(x: Float, y: Float): Int {
        return if (x != 0f && y != 0f) {
            val startX = width / 2.toFloat()
            val startY = height.toFloat()
            val dirX = startX - x
            val dirY = startY - y
            val angle =
                acos(dirX / sqrt(dirX * dirX + dirY * dirY.toDouble())).toFloat()
            (maxValue * (angle / Math.PI.toFloat())).roundToInt()
        } else {
            value
        }
    }

    fun setValue(value: Int) {
        this.value = value.coerceAtMost(maxValue)
        invalidate()
    }


    private fun setValueAnimated(localValue: Int) {
         ObjectAnimator.ofInt(this, "value", this.value, localValue).apply {
             duration = 5000
             interpolator = AccelerateDecelerateInterpolator()
             start()
         }
        if (localValue < this.value ) {
            ObjectAnimator.ofObject(
                this,
                "backgroundColor",
                ArgbEvaluator(),
                Color.RED,
                Color.WHITE
            )
                .setDuration(5000)
                .start()
        }
        if (localValue > this.value ) {
            ObjectAnimator.ofObject(
                this,
                "backgroundColor",
                ArgbEvaluator(),
                Color.WHITE,
                Color.RED
            )
                .setDuration(5000)
                .start()
        }

    }


}

package com.aayush.shoppingapp.common.helper

import android.animation.ValueAnimator
import android.content.Context
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

class UIHelper {
    companion object {

        fun toast(context: Context, message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

        fun snackBar(view: View, message: String, actionText: String, onAction:() -> Unit) {
            Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                .setAction(actionText) {
                    onAction.invoke()
                }
                .setDuration(3000)
                .show()
        }

        fun expand(v: View, duration: Int, targetHeight: Int) {
            val prevHeight = v.height
            v.visibility = View.VISIBLE
            val valueAnimator = ValueAnimator.ofInt(prevHeight, targetHeight)
            valueAnimator.addUpdateListener { animation ->
                v.layoutParams.height = animation.animatedValue as Int
                v.requestLayout()
            }
            valueAnimator.interpolator = DecelerateInterpolator()
            valueAnimator.duration = duration.toLong()
            valueAnimator.start()
        }

        fun collapse(v: View, duration: Int, targetHeight: Int) {
            val prevHeight = v.height
            val valueAnimator = ValueAnimator.ofInt(prevHeight, targetHeight)
            valueAnimator.interpolator = DecelerateInterpolator()
            valueAnimator.addUpdateListener { animation ->
                v.layoutParams.height = animation.animatedValue as Int
                v.requestLayout()
            }
            valueAnimator.interpolator = DecelerateInterpolator()
            valueAnimator.duration = duration.toLong()
            valueAnimator.start()
        }
    }
}
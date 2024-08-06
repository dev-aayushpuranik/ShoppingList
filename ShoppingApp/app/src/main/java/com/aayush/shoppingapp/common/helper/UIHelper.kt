package com.aayush.shoppingapp.common.helper

import android.animation.ValueAnimator
import android.content.Context
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.aayush.shoppingapp.R
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

        public fun showAlertDialog(context: Context, title:String, body: String, okButtonCLicked:()->Unit) {
            AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(body)
                .setCancelable(false)
                .setNegativeButton(context.getString(R.string.ok)) { listener, _ ->
                    okButtonCLicked()
                    listener.dismiss()
                }
                .show()
        }

        public fun showDeleteAlertDialog(context: Context, yesButtonClick:()->Unit, onNoButtonClick:()->Unit) {
            AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.delete_category_Item_title))
                .setMessage(context.getString(R.string.delete_category_Item_content))
                .setCancelable(false)
                .setPositiveButton(context.getString(R.string.yes)) { _, _ ->
                    yesButtonClick()
                }
                .setNegativeButton(context.getString(R.string.no)) { _, _ ->
                    onNoButtonClick()
                }
                .show()
        }
    }
}
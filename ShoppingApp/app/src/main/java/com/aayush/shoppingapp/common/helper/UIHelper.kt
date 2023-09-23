package com.aayush.shoppingapp.common.helper

import android.content.Context
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar

class UIHelper {
    companion object {

        fun toast(context: Context, message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

        fun snackbar(view: View, message: String, actionText: String, onAction:() -> Unit) {
            Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                .setAction(actionText) {
                    onAction.invoke()
                }.show()
        }
    }
}
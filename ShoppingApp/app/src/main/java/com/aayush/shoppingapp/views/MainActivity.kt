package com.aayush.shoppingapp.views

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentTransaction
import com.aayush.shoppingapp.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction().
        replace(R.id.container, CategoriesFragment()).commit()

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {

            try {
                if (supportFragmentManager.fragments.size > 0 && (supportFragmentManager.fragments[0] is SubtaskListFragment)) {
                    supportFragmentManager.popBackStack()
                } else {
                    AlertDialog.Builder(this@MainActivity)
                        .setMessage(getString(R.string.are_you_sure_you_want_to_exit))
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.yes)) { _, _ ->
                            finishAndRemoveTask()
                        }
                        .setNegativeButton(getString(R.string.no), null)
                        .show()
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

}
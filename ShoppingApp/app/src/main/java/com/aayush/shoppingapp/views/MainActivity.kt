package com.aayush.shoppingapp.views

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.aayush.shoppingapp.R
import com.aayush.shoppingapp.common.extensions.SetViewVisible
import com.aayush.shoppingapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.ShoppingAppTheme)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.toolbarColor)
        setToolbar(getString(R.string.app_name), null)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, CategoriesFragment()).commit()

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    fun setToolbar(title: String, onBackPress: (() -> Unit?)?) {
        binding.toolbar.toolbarTitle.text = title
        binding.toolbar.toolbarBackArrow.SetViewVisible(onBackPress != null)
        binding.toolbar.toolbarBackArrow.setOnClickListener { onBackPress?.invoke() }
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
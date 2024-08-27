package com.aayush.shoppingapp.views

import android.content.res.Configuration
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.aayush.shoppingapp.R
import com.aayush.shoppingapp.common.extensions.SetViewVisible
import com.aayush.shoppingapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        super.onCreate(savedInstanceState)
        setTheme(R.style.ShoppingAppTheme)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.recyclerViewBG)
        setToolbar(getString(R.string.app_name), null)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, CategoriesFragment()).commit()

        registerBackPressEvent()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        recreate()
    }

    fun setToolbar(title: String, onBackPress: (() -> Unit?)?) {
        binding.toolbar.toolbarTitle.text = title
        binding.toolbar.toolbarBackArrow.SetViewVisible(onBackPress != null)
        binding.toolbar.toolbarBackArrow.setOnClickListener { onBackPress?.invoke() }
    }

    fun registerBackPressEvent() {
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            try {
                if (supportFragmentManager.fragments.size > 0 && (supportFragmentManager.fragments[0] is SubtaskListFragment)) {
                    supportFragmentManager.popBackStack()
                } else {
                    showAlertDialog()
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    private fun showAlertDialog() {
        AlertDialog.Builder(this@MainActivity)
            .setMessage(getString(R.string.are_you_sure_you_want_to_exit))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                finish()
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

}
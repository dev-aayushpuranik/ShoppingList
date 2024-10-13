package com.aayush.shoppingapp.views

import android.app.Activity
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.NightMode
import androidx.core.content.ContextCompat
import com.aayush.shoppingapp.R
import com.aayush.shoppingapp.common.helper.ThemeManager
import com.aayush.shoppingapp.common.helper.UIHelper
import com.aayush.shoppingapp.databinding.ActivitySettingsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var themeManager: ThemeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        themeManager = ThemeManager(this)
        AppCompatDelegate.setDefaultNightMode(getUserPreferecTheme())
        super.onCreate(savedInstanceState)
        setTheme(R.style.ShoppingAppTheme)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.recyclerViewBG)

        binding.darkModeSwitch.isChecked = themeManager.isDarkThemeEnabled()

        binding.darkModeSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            ThemeManager(this).saveTheme(isChecked)
            AppCompatDelegate.setDefaultNightMode(getUserPreferecTheme())
            recreate()
        }

        binding.toolbar.toolbarBackArrow.setOnClickListener {
            navigateBack()

        }
    }

    private fun navigateBack() {
        CoroutineScope(Dispatchers.Main).launch {
            setResult(RESULT_OK)
            delay(500)
            finish()
        }
    }

    override fun onBackPressed() {
        CoroutineScope(Dispatchers.Main).launch {
            navigateBack()
            delay(500)
            super.onBackPressed()
        }
    }

    private fun getUserPreferecTheme(): Int {
        return if (themeManager.isDarkThemeEnabled()) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
    }
}
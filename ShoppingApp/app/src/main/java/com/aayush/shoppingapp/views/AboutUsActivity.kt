package com.aayush.shoppingapp.views

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.aayush.shoppingapp.R
import com.aayush.shoppingapp.common.helper.ThemeManager
import com.aayush.shoppingapp.databinding.ActivityAboutUsBinding
import com.aayush.shoppingapp.databinding.ActivitySettingsBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class AboutUsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAboutUsBinding

    private lateinit var themeManager: ThemeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        themeManager = ThemeManager(this)
        AppCompatDelegate.setDefaultNightMode(getUserPreferecTheme())
        super.onCreate(savedInstanceState)
        setTheme(R.style.ShoppingAppTheme)
        binding = ActivityAboutUsBinding.inflate(layoutInflater)

        val config = resources.configuration
        val lang = "fa" // your language code
        val locale = Locale(lang)
        Locale.setDefault(locale)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            createConfigurationContext(config)
        resources.updateConfiguration(config, resources.displayMetrics)


        setContentView(binding.root)

        window.clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.recyclerViewBG)


    }

    private fun getUserPreferecTheme(): Int {
        return if (themeManager.isDarkThemeEnabled()) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
    }
}
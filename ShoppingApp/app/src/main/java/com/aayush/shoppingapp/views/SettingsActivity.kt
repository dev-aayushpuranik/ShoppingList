package com.aayush.shoppingapp.views

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.aayush.shoppingapp.R
import com.aayush.shoppingapp.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState)
        setTheme(R.style.ShoppingAppTheme)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.recyclerViewBG)
    }
}
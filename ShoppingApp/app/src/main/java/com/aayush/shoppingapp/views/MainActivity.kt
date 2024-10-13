package com.aayush.shoppingapp.views

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.aayush.shoppingapp.R
import com.aayush.shoppingapp.common.extensions.SetViewVisible
import com.aayush.shoppingapp.common.helper.ThemeManager
import com.aayush.shoppingapp.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var themeManager: ThemeManager
    lateinit var settingsLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        themeManager = ThemeManager(this)
        AppCompatDelegate.setDefaultNightMode(getUserPreferecTheme())
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
            .replace(R.id.container, CategoriesFragment())
            .commit()

        settingsLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Refresh the MainActivity when returning from SettingsActivity
                recreate() // This will reload the activity and apply new UI changes like dark mode
            }
        }
        
        registerBackPressEvent()
    }

    fun navigateToSettingsPage() {
        if(::settingsLauncher.isInitialized) {
            val intent = Intent(this, SettingsActivity::class.java)
            settingsLauncher.launch(intent)
        }
    }

    private fun setToolbar(title: String, onBackPress: (() -> Unit?)?) {
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
                if (supportFragmentManager.fragments.size > 0
                    && ((supportFragmentManager.fragments[0] is SubtaskListFragment)
                            || (supportFragmentManager.fragments[0] is EditFragment))) {
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

    private fun getUserPreferecTheme(): Int {
        return if (themeManager.isDarkThemeEnabled()) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
    }


}
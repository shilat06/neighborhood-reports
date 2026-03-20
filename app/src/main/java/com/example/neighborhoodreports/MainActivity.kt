package com.example.neighborhoodreports

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.example.neighborhoodreports.data.repository.UserRepository
import com.example.neighborhoodreports.data.SessionManager
import com.example.neighborhoodreports.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    private val destinationsWithNavBar =
            setOf(R.id.homeFragment, R.id.accountFragment, R.id.adminFragment)

    // ActivityResultLauncher לבקשת הרשאת התראות (אנדרואיד 13+)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            fetchFcmToken()
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val heLocale = Locale.forLanguageTag("he")
        Locale.setDefault(heLocale)
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(heLocale)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController

        // Firebase Auth emails (password reset, verification) sent in Hebrew
        FirebaseAuth.getInstance().setLanguageCode("he")

        askNotificationPermission()

        // Admin tab visibility is refreshed on each destination change (see listener below)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id in destinationsWithNavBar) {
                // Re-read the role each time the nav bar appears so login updates take effect
                val isAdmin = SessionManager(this).getRole() == "admin"
                binding.bottomNav.menu.findItem(R.id.nav_admin)?.isVisible = isAdmin
                binding.bottomNav.visibility = View.VISIBLE
                when (destination.id) {
                    R.id.homeFragment ->
                            binding.bottomNav.menu.findItem(R.id.nav_reports)?.isChecked = true
                    R.id.accountFragment ->
                            binding.bottomNav.menu.findItem(R.id.nav_account)?.isChecked = true
                    R.id.adminFragment ->
                            binding.bottomNav.menu.findItem(R.id.nav_admin)?.isChecked = true
                }
            } else {
                binding.bottomNav.visibility = View.GONE
            }
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_reports -> {
                    if (navController.currentDestination?.id != R.id.homeFragment) {
                        navController.navigate(
                                R.id.homeFragment,
                                null,
                                androidx.navigation.NavOptions.Builder()
                                        .setPopUpTo(R.id.homeFragment, true)
                                        .build()
                        )
                    }
                    true
                }
                R.id.nav_admin -> {
                    if (navController.currentDestination?.id != R.id.adminFragment) {
                        navController.navigate(R.id.adminFragment)
                    }
                    true
                }
                R.id.nav_account -> {
                    if (navController.currentDestination?.id != R.id.accountFragment) {
                        navController.navigate(R.id.accountFragment)
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun askNotificationPermission() {
        // התראות דורשות הרשאה מפורשת רק מאנדרואיד 13 (Tiramisu) ומעלה
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    fetchFcmToken()
                }
                shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS) -> {
                    // כאן אפשר להציג הסבר למשתמש למה צריך את ההרשאה
                    requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    // בקשת ההרשאה ישירות
                    requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // עבור אנדרואיד 12 ומטה, ההרשאה ניתנת אוטומטית בהתקנה
            fetchFcmToken()
        }
    }

    private fun fetchFcmToken() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@addOnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result
                if (token != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            UserRepository().updateFcmToken(currentUser.uid, token)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }
}

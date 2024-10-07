package com.itolstoy.hearthstonecardviewer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.itolstoy.hearthstonecardviewer.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        navController = navHostFragment.navController
        val navView: BottomNavigationView = binding.navView

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_cards, R.id.navigation_favourites, R.id.navigation_card_fragment
            ))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when(destination.id) {
                R.id.navigation_card_fragment -> {
                    binding.navView.visibility = View.GONE
                }
                else -> {
                    binding.navView.visibility = View.VISIBLE
                }
            }
        }
        navView.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.navigation_cards -> {
                    navController.navigate(R.id.navigation_cards, null, navOptions {
                        popUpTo(R.id.navigation_cards) { saveState = true }
                        restoreState = true
                    })

                    true
                }
                R.id.navigation_favourites -> {
                    navController.navigate(R.id.navigation_favourites, null, navOptions {
                        popUpTo(R.id.navigation_favourites) { saveState = true }
                        restoreState = true
                    })

                    true
                }
                else -> false
            }
        }
        /*navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_cards -> {
                    navView.selectedItemId = R.id.navigation_cards
                    true
                }
                R.id.navigation_favourites -> {
                    navView.selectedItemId = R.id.navigation_favourites
                    true
                }
                else -> false
            }
        }*/
        navView.setupWithNavController(navController)
    }
}
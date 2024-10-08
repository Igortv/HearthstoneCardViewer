package com.itolstoy.hearthstonecardviewer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.itolstoy.hearthstonecardviewer.databinding.ActivityMainBinding
import com.itolstoy.hearthstonecardviewer.presentation.cards.CardListFragment
import com.itolstoy.hearthstonecardviewer.presentation.favourites.CardFavouritesFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private var activeFragment: Fragment? = null
    private val fragmentManager = supportFragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val cardsFragment = CardListFragment()
        val favouritesFragment = CardFavouritesFragment()

        fragmentManager.beginTransaction().apply {
            add(R.id.nav_host_fragment_activity_main, cardsFragment, "cards")
            add(R.id.nav_host_fragment_activity_main, favouritesFragment, "favourites")
            hide(favouritesFragment)
            commit()
        }

        activeFragment = cardsFragment

        binding.navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_cards -> {
                    fragmentManager.beginTransaction().hide(activeFragment!!).show(cardsFragment).commit()
                    activeFragment = cardsFragment
                    true
                }
                R.id.navigation_favourites -> {
                    fragmentManager.beginTransaction().hide(activeFragment!!).show(favouritesFragment).commit()
                    activeFragment = favouritesFragment
                    true
                }
                else -> false
            }
        }
    }
}
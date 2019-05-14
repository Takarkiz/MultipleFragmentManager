package com.test.sawada.fragnavoriginals

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val urlList: List<String> = listOf(
        "https://marvel.disney.co.jp/",
        "http://www.poneycomb.tokyo/fs/poneycombtokyo/c/marvel",
        "https://theriver.jp/tag/marvel/"
    )
    private lateinit var mfManager: MultipleFragmentManager


    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->

        mfManager.switchTab(getTabIndex(item.itemId))
        return@OnNavigationItemSelectedListener true
    }

    private val mOnNavigationItemReselectedListener = BottomNavigationView.OnNavigationItemReselectedListener { item ->
        mfManager.reSwitchTab(getTabIndex(item.itemId))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mfManager = MultipleFragmentManager(supportFragmentManager, R.id.container_nav, urlList)
        mfManager.rootFragments = createRootFragment(urlList)
        mfManager.initialize(defaultSelectedIndex = 0, numberOfTabs = 3, savedInstanceState = null)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        navigation.setOnNavigationItemReselectedListener(mOnNavigationItemReselectedListener)
    }

    fun loadNewFragment(urls: List<String>) {
        val fragment = createRootFragment(urls)[0]
        mfManager.insertFragment(fragment)
    }


    private fun getTabIndex(@IdRes tabId: Int): Int {
        return when (tabId) {
            R.id.navigation_home -> 0
            R.id.navigation_dashboard -> 1
            R.id.navigation_notifications -> 2
            else -> -1
        }
    }

    private fun createRootFragment(urls: List<String>): List<WebFragment> {
        val fragments: MutableList<WebFragment> = mutableListOf()

        for (url in urls) {
            val webFragment = WebFragment.newInstance(url)
            fragments.add(webFragment)
        }

        return fragments.toList()
    }
}

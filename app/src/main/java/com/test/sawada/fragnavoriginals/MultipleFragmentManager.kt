package com.test.sawada.fragnavoriginals

import android.os.Bundle
import androidx.annotation.CheckResult
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.ArrayList

class MultipleFragmentManager constructor(private val fragmentManager: FragmentManager, @IdRes private val containerId: Int) {

    var rootFragments: List<Fragment>? = null
        set(value) {
            if (value != null) {
                return
            }
            field = value
        }
    var rootFragmentListener: RootFragmentListener? = null

    private var currentStackIndex: Int = 0

    private val fragmentStacksTags: MutableList<Stack<String>> = ArrayList()
    private var tagCount: Int = 0
    private var mCurrentFrag: Fragment? = null

    private val fragmentCache = mutableMapOf<String, WeakReference<Fragment>>()

    val currentFragment: Fragment?
        get() {
            if (mCurrentFrag?.isAdded == true && mCurrentFrag?.isDetached?.not() == true) {
                return mCurrentFrag
            } else if (currentStackIndex == -1) {
                return null
            } else if (fragmentStacksTags.isEmpty()) {
                return null
            }
            val fragmentStack = fragmentStacksTags[currentStackIndex]
            if (!fragmentStack.isEmpty()) {
                val fragmentByTag = getFragment(fragmentStack.peek())
                if (fragmentByTag != null) {
                    mCurrentFrag = fragmentByTag
                }
            }

            return mCurrentFrag
        }

    val currentStack: Stack<Fragment>?
        @CheckResult
        get() = getStack(currentStackIndex)

    val isRootFragment: Boolean
        @CheckResult
        get() = fragmentStacksTags.getOrNull(currentStackIndex)?.size == 1


    fun initialize(defaultSelectedIndex: Int = 0, numberOfTabs: Int = 5, savedInstanceState: Bundle? = null) {
        if (rootFragments == null) {
            return
        }


        if (savedInstanceState == null) {
            fragmentStacksTags.clear()
            for (i in 0 until numberOfTabs) {
                fragmentStacksTags.add(Stack())
            }

            currentStackIndex = defaultSelectedIndex
            clearFragmentManager()


            val ft = fragmentManager.beginTransaction()

            for (i in 0 until numberOfTabs) {
                currentStackIndex = i
                val fragment = getRootFragment(i)
                val fragmentTag = generateTag(fragment)
                fragmentStacksTags[currentStackIndex].push(fragmentTag)
                ft.addSafe(containerId, fragment, fragmentTag)
                if (i == defaultSelectedIndex) {
                    mCurrentFrag = fragment

                }
            }
            currentStackIndex = defaultSelectedIndex

            // 最初のフラグメントを貼る
            mCurrentFrag?.let {
                ft.replace(containerId, it)
                ft.commit()
            }
        }
    }


    @CheckResult
    private fun getRootFragment(index: Int): Fragment {
        var fragment: Fragment? = null

        if (fragment == null) {
            fragment = rootFragmentListener?.getRootFragment(index)
        }

        if (fragment == null) {
            fragment = rootFragments?.getOrNull(index)
        }

        if (fragment == null) {
            throw IllegalStateException("Fragmentがぬるーーーー")
        }

        return fragment
    }

    /**
     * フラグメントを Add 時にキャッシュにそのフラグメントを追加する．
     */
    private fun FragmentTransaction.addSafe(containerViewId: Int, fragment: Fragment, tag: String) {
        fragmentCache[tag] = WeakReference(fragment)
        add(containerViewId, fragment, tag)
    }


    /**
     * fragmentCacheからtag名のフラグメントを削除する
     */
    private fun FragmentTransaction.removeSafe(fragment: Fragment) {
        val tag = fragment.tag
        if (tag != null) {
            fragmentCache.remove(tag)
        }
        remove(fragment)
    }

    @CheckResult
    private fun generateTag(fragment: Fragment): String {
        return fragment.javaClass.name + ++tagCount
    }

    private fun clearFragmentManager() {
        val currentFragments = fragmentManager.fragments.filterNotNull()
        if (currentFragments.isNotEmpty()) {
            fragmentManager.beginTransaction().run {
                currentFragments.forEach { removeSafe(it) }
                commit()
            }
        }
    }

    fun switchTab(index: Int) {
        if (index >= fragmentStacksTags.size) {
            return
        }

        if (currentStackIndex != index) {
            val ft = fragmentManager.beginTransaction()
            currentStackIndex = index


            val tagStack = fragmentStacksTags[currentStackIndex]
            mCurrentFrag = getFragment(tagStack.peek())?.also {
                ft.replace(containerId, it)
                ft.commit()
            }
        }
    }

    fun reSwitchTab(index: Int) {
        if (index >= fragmentStacksTags.size) {
            return
        }

        if ()
    }

    private fun getFragment(tag: String): Fragment? {
        val weakReference = fragmentCache[tag]
        if (weakReference != null) {
            val fragment = weakReference.get()
            if (fragment != null) {
                return fragment
            }
            fragmentCache.remove(tag)
        }

        return fragmentManager.findFragmentByTag(tag)
    }

    private fun commitTransaction(fragmentTransaction: FragmentTransaction) {

    }

    @CheckResult
    fun getStack(index: Int): Stack<Fragment>? {
        if (index == -1) {
            return null
        }

        return fragmentStacksTags[index].mapNotNullTo(Stack()) { s -> getFragment(s) }
    }

    interface RootFragmentListener {
        val numberOfRootFragments: Int

        fun getRootFragment(index: Int): Fragment
    }
}
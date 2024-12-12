package com.onenote.android

import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton

abstract class BaseActivity : AppCompatActivity() {
    private lateinit var themeToggleFab: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        setupThemeToggleFab()
    }

    private fun setupThemeToggleFab() {
        themeToggleFab = FloatingActionButton(this).apply {
            layoutParams = CoordinatorLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.END
                marginEnd = resources.getDimensionPixelSize(R.dimen.fab_margin)
                bottomMargin = resources.getDimensionPixelSize(R.dimen.fab_margin)
            }

            // Set the FAB properties
            setImageResource(
                if (ThemeUtils.isDarkMode(context))
                    R.drawable.ic_light_mode
                else
                    R.drawable.ic_dark_mode
            )

            // Match your theme colors
            backgroundTintList = ContextCompat.getColorStateList(
                context,
                if (ThemeUtils.isDarkMode(context)) R.color.dark_pink_primary else R.color.pink_primary
            )

            setOnClickListener {
                val newDarkMode = !ThemeUtils.isDarkMode(context)
                ThemeUtils.setTheme(context, newDarkMode)
                recreate()
            }
        }

        // Find the root layout
        val rootLayout = findViewById<ViewGroup>(android.R.id.content).getChildAt(0)

        // If the root layout is not a CoordinatorLayout, wrap it in one
        if (rootLayout !is CoordinatorLayout) {
            val originalLayout = findViewById<ViewGroup>(android.R.id.content)
            val originalChild = originalLayout.getChildAt(0)
            originalLayout.removeView(originalChild)

            val coordinatorLayout = CoordinatorLayout(this).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

            coordinatorLayout.addView(originalChild)
            originalLayout.addView(coordinatorLayout)
            coordinatorLayout.addView(themeToggleFab)
        } else {
            // If it's already a CoordinatorLayout, just add the FAB
            rootLayout.addView(themeToggleFab)
        }
    }
}
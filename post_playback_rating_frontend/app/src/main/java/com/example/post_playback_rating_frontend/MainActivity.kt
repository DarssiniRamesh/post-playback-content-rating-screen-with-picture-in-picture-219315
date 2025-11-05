package com.example.post_playback_rating_frontend

import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.FragmentActivity
import com.example.post_playback_rating_frontend.ui.IndiceFragment

/**
 * Main Activity for Android TV.
 * Hosts the app's fragments and manages back navigation.
 */
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Load Índice on first launch
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, IndiceFragment.newInstance())
                .commitNow()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Let focused fragment handle key if needed, else default behavior
        return super.onKeyDown(keyCode, event)
    }

    override fun onBackPressed() {
        // Predictable back behavior
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            finish()
        }
    }
}

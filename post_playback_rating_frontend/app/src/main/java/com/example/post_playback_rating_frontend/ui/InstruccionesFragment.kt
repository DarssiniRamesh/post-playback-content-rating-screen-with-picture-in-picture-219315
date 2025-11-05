package com.example.post_playback_rating_frontend.ui

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.post_playback_rating_frontend.R

/**
 * Instrucciones screen: scrollable anchor sections with DPAD up/down navigation.
 * For brevity, this stub provides anchor buttons that simulate jumps.
 */
class InstruccionesFragment : Fragment() {

    private lateinit var anchorUp: Button
    private lateinit var anchorDown: Button
    private lateinit var webDemoBtn: Button

    companion object {
        // PUBLIC_INTERFACE
        /** Create a new instance of Instrucciones fragment. */
        fun newInstance() = InstruccionesFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_instrucciones, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        anchorUp = view.findViewById(R.id.anchor_up)
        anchorDown = view.findViewById(R.id.anchor_down)
        webDemoBtn = view.findViewById(R.id.btn_web_demo)

        anchorUp.setOnClickListener {
            // simulate moving to previous anchor: do nothing beyond focus feedback
        }
        anchorDown.setOnClickListener {
            // simulate moving to next anchor: do nothing beyond focus feedback
        }
        webDemoBtn.setOnClickListener { openWebDemo() }

        anchorUp.requestFocus()

        view.setOnKeyListener { _, keyCode, event ->
            if (event?.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_BACK -> {
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                        true
                    }
                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                        if (anchorUp.hasFocus()) anchorDown.requestFocus() else webDemoBtn.requestFocus()
                        true
                    }
                    KeyEvent.KEYCODE_DPAD_UP -> {
                        if (webDemoBtn.hasFocus()) anchorDown.requestFocus() else anchorUp.requestFocus()
                        true
                    }
                    else -> false
                }
            } else false
        }
    }

    private fun openWebDemo() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, WebDemoFragment.newInstance())
            .addToBackStack("web_demo")
            .commit()
    }
}

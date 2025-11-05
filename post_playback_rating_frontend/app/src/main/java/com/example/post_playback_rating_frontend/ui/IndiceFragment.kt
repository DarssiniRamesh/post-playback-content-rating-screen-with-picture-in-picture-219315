package com.example.post_playback_rating_frontend.ui

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.post_playback_rating_frontend.R

/**
 * Índice screen with three CTA buttons and a menu button to Instrucciones.
 * DPAD Up/Down to navigate, Enter to select, Back handled by Activity.
 * Figma parity:
 * - CTA sizes: 1680x480dp demo fidelity; helper spacing ~32dp.
 * - Divider thickness: 2dp (style_41).
 */
class IndiceFragment : Fragment() {

    private lateinit var title: TextView
    private lateinit var cta1: Button
    private lateinit var cta2: Button
    private lateinit var cta3: Button
    private lateinit var helper1: TextView
    private lateinit var helper2: TextView
    private lateinit var helper3: TextView
    private lateinit var instruccionesBtn: ImageButton

    companion object {
        // PUBLIC_INTERFACE
        /** Create a new instance of Índice fragment. */
        fun newInstance() = IndiceFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_indice, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        title = view.findViewById(R.id.title_text)
        cta1 = view.findViewById(R.id.cta1)
        cta2 = view.findViewById(R.id.cta2)
        cta3 = view.findViewById(R.id.cta3)
        helper1 = view.findViewById(R.id.helper1)
        helper2 = view.findViewById(R.id.helper2)
        helper3 = view.findViewById(R.id.helper3)
        instruccionesBtn = view.findViewById(R.id.btn_instrucciones)

        cta1.text = getString(R.string.indice_cta1)
        cta2.text = getString(R.string.indice_cta2)
        cta3.text = getString(R.string.indice_cta3)
        helper1.text = getString(R.string.indice_helper)
        helper2.text = getString(R.string.indice_helper)
        helper3.text = getString(R.string.indice_helper)

        // Navigation handlers
        cta1.setOnClickListener { openPrototype(1) }
        cta2.setOnClickListener { openPrototype(2) }
        cta3.setOnClickListener { openPrototype(3) }
        instruccionesBtn.setOnClickListener { openInstrucciones() }

        // DPAD ordering: focus moves vertically among CTAs; initial focus cta1
        cta1.requestFocus()
        setupFocusTransitions()

        // Handle Enter via default button; Back handled by Activity
        view.isFocusableInTouchMode = true
        view.requestFocus()
        view.setOnKeyListener { _, keyCode, event ->
            if (event?.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                        when {
                            cta1.hasFocus() -> cta2.requestFocus()
                            cta2.hasFocus() -> cta3.requestFocus()
                        }
                        true
                    }
                    KeyEvent.KEYCODE_DPAD_UP -> {
                        when {
                            cta3.hasFocus() -> cta2.requestFocus()
                            cta2.hasFocus() -> cta1.requestFocus()
                        }
                        true
                    }
                    else -> false
                }
            } else false
        }
    }

    private fun setupFocusTransitions() {
        // Keep focus flow strictly between CTAs to match vertical action stack.
        cta1.nextFocusDownId = R.id.cta2
        cta1.nextFocusUpId = R.id.cta1

        cta2.nextFocusDownId = R.id.cta3
        cta2.nextFocusUpId = R.id.cta1

        cta3.nextFocusUpId = R.id.cta2
        cta3.nextFocusDownId = R.id.cta3
    }

    private fun openPrototype(idx: Int) {
        val frag: Fragment = when (idx) {
            1 -> PrototypeOneFragment.newInstance()
            2 -> PrototypeTwoFragment.newInstance()
            else -> PrototypeThreeFragment.newInstance()
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, frag)
            .addToBackStack("prototype_$idx")
            .commit()
    }

    private fun openInstrucciones() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, InstruccionesFragment.newInstance())
            .addToBackStack("instrucciones")
            .commit()
    }
}

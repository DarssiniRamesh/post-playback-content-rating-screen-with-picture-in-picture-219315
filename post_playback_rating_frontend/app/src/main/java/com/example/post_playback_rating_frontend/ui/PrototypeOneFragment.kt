package com.example.post_playback_rating_frontend.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.post_playback_rating_frontend.R

/**
 * Prototype 1 stub screen.
 */
class PrototypeOneFragment : Fragment() {

    companion object {
        // PUBLIC_INTERFACE
        /** Create new instance for Prototype 1 stub. */
        fun newInstance() = PrototypeOneFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_prototype_stub, container, false)
    }
}

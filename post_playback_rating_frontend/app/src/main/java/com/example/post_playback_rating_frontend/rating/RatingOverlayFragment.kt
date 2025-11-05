package com.example.post_playback_rating_frontend.rating

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.example.post_playback_rating_frontend.R

/**
 * DialogFragment overlay for post-playback content rating.
 * Provides: poster background, PIP placeholder, title, helper, buttons and countdown.
 */
class RatingOverlayFragment : DialogFragment() {

    private val vm: PostPlaybackRatingViewModel by activityViewModels()

    private lateinit var poster: ImageView
    private lateinit var pipPlaceholder: View
    private lateinit var title: TextView
    private lateinit var helper: TextView
    private lateinit var likeBtn: Button
    private lateinit var loveBtn: Button
    private lateinit var dislikeBtn: Button
    private lateinit var closeBtn: Button
    private lateinit var countdown: TextView

    private var host: RatingOverlayHost? = null

    companion object {
        // PUBLIC_INTERFACE
        /** Create a new instance of RatingOverlayFragment. */
        fun newInstance(): RatingOverlayFragment = RatingOverlayFragment()
        const val TAG = "RatingOverlay"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        host = if (context is RatingOverlayHost) context else null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.AppTheme)
    }

    override fun onStart() {
        super.onStart()
        // Fullscreen dialog
        dialog?.window?.apply {
            setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_rating_overlay, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        poster = view.findViewById(R.id.poster_bg)
        pipPlaceholder = view.findViewById(R.id.pip_placeholder)
        title = view.findViewById(R.id.rating_title)
        helper = view.findViewById(R.id.rating_helper)
        likeBtn = view.findViewById(R.id.btn_like)
        loveBtn = view.findViewById(R.id.btn_love)
        dislikeBtn = view.findViewById(R.id.btn_dislike)
        closeBtn = view.findViewById(R.id.btn_close)
        countdown = view.findViewById(R.id.countdown_label)

        // Accessibility content descriptions
        likeBtn.contentDescription = getString(R.string.rating_like)
        loveBtn.contentDescription = getString(R.string.rating_love)
        dislikeBtn.contentDescription = getString(R.string.rating_dislike)
        closeBtn.contentDescription = getString(R.string.rating_close)

        // Focus: default on Close
        closeBtn.isFocusable = true
        closeBtn.requestFocus()

        setupDpadOrder()

        likeBtn.setOnClickListener { vm.onLike(); dismissAllowingStateLoss() }
        loveBtn.setOnClickListener { vm.onLove(); dismissAllowingStateLoss() }
        dislikeBtn.setOnClickListener { vm.onDislike(); dismissAllowingStateLoss() }
        closeBtn.setOnClickListener { vm.onClosePressed(); dismissAllowingStateLoss() }

        // Observe state updates
        vm.state.observe(viewLifecycleOwner, Observer { s ->
            when (s) {
                is PostPlaybackRatingViewModel.OverlayState.Visible -> {
                    title.text = s.title
                    helper.text = getString(R.string.rating_message)
                    countdown.text = getString(R.string.rating_countdown, s.countdownSeconds)
                    // Using solid bg placeholder for poster; no external loader to avoid deps.
                    poster.setBackgroundColor(Color.parseColor("#222222"))
                }
                is PostPlaybackRatingViewModel.OverlayState.AutoClosing -> {
                    countdown.text = getString(R.string.rating_countdown, s.remainingSeconds)
                }
                is PostPlaybackRatingViewModel.OverlayState.Closed -> {
                    dismissAllowingStateLoss()
                    host?.showEndPlayer()
                }
                is PostPlaybackRatingViewModel.OverlayState.NavigateToVcard -> {
                    dismissAllowingStateLoss()
                    host?.navigateToVcard()
                }
                else -> {
                    // Hidden/Loading no-ops
                }
            }
        })

        // Key handling for Back to close overlay
        view.isFocusableInTouchMode = true
        view.requestFocus()
        view.setOnKeyListener { _, keyCode, event ->
            if (event?.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_BACK -> {
                        vm.onClosePressed()
                        dismissAllowingStateLoss()
                        true
                    }
                    else -> false
                }
            } else false
        }
    }

    private fun setupDpadOrder() {
        // Default DPAD ordering: Close (focused) -> Like -> Love -> Dislike
        closeBtn.nextFocusRightId = R.id.btn_like
        likeBtn.nextFocusRightId = R.id.btn_love
        loveBtn.nextFocusRightId = R.id.btn_dislike

        // Reverse left order
        dislikeBtn.nextFocusLeftId = R.id.btn_love
        loveBtn.nextFocusLeftId = R.id.btn_like
        likeBtn.nextFocusLeftId = R.id.btn_close

        // Vertical: PIP up, actions below
        // Keep it simple; Close can go to Like on right or remain static on down
        closeBtn.nextFocusDownId = R.id.btn_like
        likeBtn.nextFocusDownId = R.id.btn_love
        loveBtn.nextFocusDownId = R.id.btn_dislike
        dislikeBtn.nextFocusDownId = R.id.btn_close
    }
}

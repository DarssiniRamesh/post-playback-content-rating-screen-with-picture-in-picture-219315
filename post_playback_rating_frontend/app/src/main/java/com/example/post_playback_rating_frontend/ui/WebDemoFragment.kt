package com.example.post_playback_rating_frontend.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.example.post_playback_rating_frontend.R

/**
 * WebView demo to load provided HTML/CSS/JS assets from android_asset.
 * You can toggle between indice and instrucciones demo pages by changing initialUrl.
 */
class WebDemoFragment : Fragment() {

    private lateinit var webView: WebView

    companion object {
        // PUBLIC_INTERFACE
        /** Create new instance of the Web Demo screen. */
        fun newInstance() = WebDemoFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_web_demo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        webView = view.findViewById(R.id.webview)
        val settings: WebSettings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        webView.webViewClient = object : WebViewClient() {}
        // Load the Instrucciones HTML page from assets folder.
        webView.loadUrl("file:///android_asset/instrucciones-41-206.html")
    }

    override fun onDestroyView() {
        webView.destroy()
        super.onDestroyView()
    }
}

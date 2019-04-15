package com.toast.android.mywebviewtest

import android.annotation.TargetApi
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.webkit.*
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*


const val TAG = "MainActivity"
// TODO: IP는 바뀔 수 있으니 변경 할 것.
const val LOCAL_URL = "http://10.77.95.34:5500/public/index.html"
class MainActivity : AppCompatActivity() {
    companion object {
        const val SCHEME_INTENT_OPEN = "intent://"
        const val SCHEME_MARKET_OPEN = "market://"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        webView.webViewClient = MyWebViewClient()
        webView.webChromeClient= MyWebChromeClient()
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
        }

        webView.loadUrl(LOCAL_URL)
    }

    override fun onBackPressed() {
        when(webView.canGoBack()) {
            true -> {
                webView.goBack()
            }
        }
    }

    inner class MyWebViewClient: WebViewClient() {
        override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
            Log.e(TAG, "[onReceivedError] url: $failingUrl")
            Log.e(TAG, "[onReceivedError] description/errorCode: $description/$errorCode")

            super.onReceivedError(view, errorCode, description, failingUrl)
        }

        @TargetApi(Build.VERSION_CODES.M)
        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
            onReceivedError(view, error!!.errorCode, error.description.toString(), request!!.url.toString())
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            Log.e(TAG, "[onPageStarted] url: $url")
            super.onPageStarted(view, url, favicon)
        }

        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            Log.e(TAG, "[shouldOverrideUrlLoading] url: $url")

            if (url!!.startsWith(SCHEME_INTENT_OPEN)) {
                Log.e(TAG, "[shouldOverrideUrlLoading] intent://")

                val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                val packageName = intent.`package`
3
                if (packageName != null) {
                    Log.e(TAG, "[shouldOverrideUrlLoading] packageName=$packageName")

                    val installedPackage = this@MainActivity.packageManager.getLaunchIntentForPackage(packageName)
                    if (installedPackage != null) {
                        Log.e(TAG, "[shouldOverrideUrlLoading] startActivity with intent($installedPackage).")
                        this@MainActivity.startActivity(intent)
                    } else {
                        val marketUri = Uri.parse("market://details?id=$packageName")
                        val marketIntent = Intent(Intent.ACTION_VIEW).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            data = marketUri
                        }
                        Log.e(TAG, "[shouldOverrideUrlLoading] startActivity with market($marketUri).")
                        this@MainActivity.startActivity(marketIntent)
                    }
                } else {
                    Log.e(TAG, "[shouldOverrideUrlLoading] No Package Name Found.")
                    Toast.makeText(this@MainActivity, "결제앱이 없습니다. 앱을 설치해주세요.", Toast.LENGTH_SHORT).show()
                }

                return true
            }

            if (url.startsWith(SCHEME_MARKET_OPEN)) {
                Log.e(TAG, "[shouldOverrideUrlLoading] market://")
                val marketUri = Uri.parse(url)
                val marketIntent = Intent(Intent.ACTION_VIEW).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    data = marketUri
                }
                Log.e(TAG, "[shouldOverrideUrlLoading] startActivity with market($marketUri).")
                this@MainActivity.startActivity(marketIntent)

                return true
            }

            return false;
        }

        @TargetApi(Build.VERSION_CODES.N)
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            return shouldOverrideUrlLoading(view, request!!.url.toString())
        }
    }

    inner class MyWebChromeClient: WebChromeClient()
}

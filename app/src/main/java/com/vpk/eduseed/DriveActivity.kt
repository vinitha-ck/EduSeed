package com.vpk.eduseed

import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.webkit.DownloadListener
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class DriveActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var spinnerFolders: Spinner
    private val folderLinks = mapOf(
        "Folder 1" to "https://drive.google.com/drive/folders/1ZhtYI2ENpk00reSpLSrtjZH2QJkt8bc0?usp=drive_link",
        "Folder 2" to "https://drive.google.com/drive/folders/1WAh6Fn0pB5KbcWl-4VdtsKErfPn4VuGt?usp=drive_link",
        "Folder 3" to "https://drive.google.com/file/d/1-MmAG9dT1mZKETyRuPS-ve1wHBZKgVqL/view?usp=drive_link"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drive)

        webView = findViewById(R.id.webView)
        spinnerFolders = findViewById(R.id.lotSpinner)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, folderLinks.keys.toList())
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFolders.adapter = adapter

        spinnerFolders.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val folderName = parent.getItemAtPosition(position).toString()
                val folderUrl = folderLinks[folderName]
                folderUrl?.let { webView.loadUrl(it) }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true
        webView.settings.javaScriptCanOpenWindowsAutomatically = true

        webView.setDownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
            val fileName = Regex("filename=\"([^\"]+)\"").find(contentDisposition ?: "")?.groupValues?.get(1)
                ?: "downloaded_file"

            val request = DownloadManager.Request(Uri.parse(url)).apply {
                setMimeType(mimeType)
                addRequestHeader("User-Agent", userAgent)
                setDescription("Downloading file...")
                setTitle(fileName)
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                        put(MediaStore.Downloads.MIME_TYPE, mimeType)
                        put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }

                    val resolver = contentResolver
                    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    setDestinationUri(uri!!)
                } else {
                    setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                }
            }
            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
            Toast.makeText(this, "Downloading File...", Toast.LENGTH_SHORT).show()
        }
    }
}

package com.askgps.autoupdate

import android.app.DownloadManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.util.Base64
import androidx.annotation.WorkerThread
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.PasswordAuthentication

class DownloadController(private val context: Context) : Service(){
    companion object {
        private const val url = "https://gitlab.com/wokkalokka/watchtrackerapk/-/raw/master/patientTracker.apk"
        private const val FILE_NAME = "patientTracker.apk"
        private const val FILE_BASE_PATH = "file://"
        private const val MIME_TYPE = "application/vnd.android.package-archive"
        private const val PROVIDER_PATH = ".provider"
        private const val APP_INSTALL_PATH = "\"application/vnd.android.package-archive\""

        const val TAG = "loc_upd"
    }
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    @WorkerThread
    suspend fun enqueueDownload() {
        withContext(Dispatchers.IO) {
            var destination =
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/"
            destination += FILE_NAME
            val uri = Uri.parse("$FILE_BASE_PATH$destination")
            val file = File(destination)
            if (file.exists()) file.delete()
            val downloadManager =
                context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadUri = Uri.parse(url)

            val request = DownloadManager.Request(downloadUri)

//         val charset = Charsets.UTF_8
//         val byteArray = "clevergoods:fde1e41c03b1db76c11ddd160679249e19bfda62".toByteArray(charset)
//         request.addRequestHeader(
//             "Authorization",
//             "Basic " + Base64.encodeToString(byteArray, Base64.NO_WRAP)
//         );

            request.setMimeType(MIME_TYPE)
            //request.setTitle(context.getString(R.string.title_file_download))
            //request.setDescription(context.getString(R.string.downloading))
            // set destination
            request.setDestinationUri(uri)
            showInstallOption(destination, uri)
            // Enqueue a new download and same the referenceId
            downloadManager.enqueue(request)
        }

}
    private fun showInstallOption(
        destination: String,
        uri: Uri
    ) {
        // set BroadcastReceiver to install app when .apk is downloaded
        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(
                context: Context,
                intent: Intent
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                    val contentUri = FileProvider.getUriForFile(
                        context,
                        BuildConfig.APPLICATION_ID + PROVIDER_PATH,
                        File(destination)
                    )
                    val install = Intent(Intent.ACTION_VIEW)
                    install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    install.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    install.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
                    install.data = contentUri
                    context.startActivity(install)
                    context.unregisterReceiver(this)
                    // finish()
                } else {
                    val install = Intent(Intent.ACTION_VIEW)
                    install.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    install.setDataAndType(
                        uri,
                        APP_INSTALL_PATH
                    )
                    context.startActivity(install)
                    context.unregisterReceiver(this)
                    // finish()
                }
            }
        }
        context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        "onStart".toLog()
        coroutineScope.launch(Dispatchers.IO) {
            enqueueDownload()
        }

        return START_STICKY
    }
}
package com.askgps.autoupdate

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.WorkerThread
import androidx.core.app.ActivityCompat

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {


    companion object {
        const val REQUEST_PERMISSION = 1
        private val permissions = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    lateinit var downloadController: DownloadController
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        requestPermission()
        val apkUrl ="https://github.com/clevergoods/RoundForPatient/raw/master/app-debug.apk"
        downloadController = DownloadController(this)

        val updateIntent = Intent(this, DownloadController::class.java)

        fab.setOnClickListener { view ->
            this.startService(updateIntent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun requestPermission() = ActivityCompat.requestPermissions(this,
        permissions,
        REQUEST_PERMISSION)


}

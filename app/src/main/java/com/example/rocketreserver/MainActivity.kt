package com.example.rocketreserver

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.apollographql.apollo3.api.Optional
import com.example.rocketreserver.type.MediaType
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val query = MediaListQuery(
            userId = Optional.presentIfNotNull(262143),
            mediaId = Optional.presentIfNotNull(99263),
            type = Optional.presentIfNotNull(MediaType.ANIME)
        )

        lifecycleScope.launch {
            apolloClient(this@MainActivity).query(query).toFlow()
                .catch {
                    Log.wtf(MainActivity::class.java.simpleName, it)
                    Toast.makeText(this@MainActivity, "error", Toast.LENGTH_SHORT).show()
                }
                .collect {
                    Toast.makeText(this@MainActivity, "successs", Toast.LENGTH_SHORT).show()
                    it.data?.mediaList?.mediaListEntry?.let {
                        findViewById<TextView>(R.id.title).text = it.media?.title?.english ?: ""
                    }
                }
        }
    }
}

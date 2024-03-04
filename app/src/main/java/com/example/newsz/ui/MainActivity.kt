package com.example.newsz.ui

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build

import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newsz.model.ArticlesItem
import com.example.newsz.adapter.NewsAdapter
import com.example.newsz.R

import com.example.newsz.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: NewsAdapter
    private val NOTIFICATION_PERMISSION_REQUEST_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.myToolbar)
        progressBar = binding.progressBar

        adapter = NewsAdapter { url ->
            openUrlInBrowser(url)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        binding.recyclerView.adapter = adapter

        fetchNews()
        checkNotificationPermission()

// FOR HORIZONTAL LAYOUT

//        val layoutManager = LinearLayoutManager(this).apply {
//            orientation = LinearLayoutManager.HORIZONTAL
//        }
//        binding.recyclerView.layoutManager = layoutManager


    }


    private fun fetchNews() {
        progressBar.visibility = View.VISIBLE
        val url =
            "https://candidate-test-data-moengage.s3.amazonaws.com/Android/news-api-feed/staticResponse.json"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = URL(url).readText()
                if (response.isNotEmpty()) {
                    val jsonObject = JSONObject(response)
                    val articles = jsonObject.getJSONArray("articles")
                    val newsList = mutableListOf<ArticlesItem>()

                    for (i in 0 until articles.length()) {
                        val article = articles.getJSONObject(i)
                        val headline = article.optString("title", "")
                        val author = article.optString("author", "")
                        val description = article.optString("description", "")
                        val articleUrl = article.optString("url", "")
                        val urlToImage = article.optString("urlToImage", "")
                        val publishedAt = article.optString("publishedAt", "")




                        if (headline.isNotEmpty() && articleUrl.isNotEmpty() && publishedAt.isNotEmpty() && urlToImage.isNotEmpty()) {
                            newsList.add(
                                ArticlesItem(
                                    publishedAt,
                                    author,
                                    urlToImage,
                                    description,
                                    null,
                                    headline,
                                    articleUrl,
                                    ""
                                )
                            )
                        }
                    }
                    updateUI(newsList)
                } else {

                    runOnUiThread {
                        progressBar.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()

            } finally {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                }
            }
        }


    }

    private fun updateUI(newsList: List<ArticlesItem>) {
        runOnUiThread {
            adapter.setArticlesList(newsList)
        }
    }


    private fun openUrlInBrowser(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sort_old_to_new -> {
                adapter.sortByOldToNew()
                true
            }

            R.id.action_sort_new_to_old -> {
                adapter.sortByNewToOld()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun checkNotificationPermission() {
        val notificationPermission =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.USE_FULL_SCREEN_INTENT
            } else {
                Manifest.permission.RECEIVE_BOOT_COMPLETED
            }

        if (ContextCompat.checkSelfPermission(
                this,
                notificationPermission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission not granted, request it
            ActivityCompat.requestPermissions(
                this,
                arrayOf(notificationPermission),
                NOTIFICATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // Permission already granted
            // Proceed with your logic here
        }
    }


    // Handle permission request result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                // Proceed with your logic here

            } else {
                // Permission denied
                // Handle accordingly, maybe show explanation to the user
            }
        }
    }

}

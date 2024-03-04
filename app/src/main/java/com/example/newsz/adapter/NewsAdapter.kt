package com.example.newsz.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.newsz.R
import com.example.newsz.model.ArticlesItem
import com.example.newsz.databinding.ItemNewsBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("NotifyDataSetChanged")
class NewsAdapter(private val onItemClick: (String) -> Unit) :
    RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    private var articlesList = mutableListOf<ArticlesItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val binding = ItemNewsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewsViewHolder(binding, parent.context)
    }


    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val articleItem = articlesList[position]
        holder.bind(articleItem)
    }

    override fun getItemCount(): Int {
        return articlesList.size
    }


    fun setArticlesList(articles: List<ArticlesItem>) {
        articlesList.clear()
        articlesList.addAll(articles)
        notifyDataSetChanged()
    }

    fun sortByOldToNew() {
        articlesList.sortBy { it.publishedAt?.let { it1 -> formatDateForNewsCard(it1) } }
        notifyDataSetChanged()
    }

    fun sortByNewToOld() {
        articlesList.sortByDescending { it.publishedAt?.let { it1 -> formatDateForNewsCard(it1) } }
        notifyDataSetChanged()
    }


    fun formatDateForNewsCard(dateString: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())

        val date: Date = inputFormat.parse(dateString) ?: return ""

        return outputFormat.format(date)
    }

    inner class NewsViewHolder(private val binding: ItemNewsBinding, private val context: Context) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(articleItem: ArticlesItem) {
            binding.title.text = articleItem.title ?: "Unknown Title"
            binding.author.text = articleItem.author ?: "Unknown Author"
            binding.description.text = articleItem.description ?: "No description available"
            binding.date.text = articleItem.publishedAt?.let { formatDateForNewsCard(it) } ?: "No date available"


            Glide
                .with(context)
                .load(articleItem.urlToImage)
                .centerCrop()
                 .placeholder(R.drawable.news_icon_placeholder)
                .into(binding.thumbnail)

            binding.readMoreButton.setOnClickListener {
                onItemClick(articleItem.url ?: "")
            }
        }
    }
}

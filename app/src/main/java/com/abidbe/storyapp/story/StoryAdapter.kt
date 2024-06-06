package com.abidbe.storyapp.story

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.RecyclerView
import com.abidbe.storyapp.api.ListStoryItem
import com.abidbe.storyapp.databinding.ItemStoryBinding
import com.bumptech.glide.Glide
import androidx.core.util.Pair

class StoryAdapter : RecyclerView.Adapter<StoryAdapter.StoryViewHolder>() {

    private val stories = mutableListOf<ListStoryItem>()

    fun updateStories(newStories: List<ListStoryItem>) {
        stories.clear()
        stories.addAll(newStories)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        holder.bind(stories[position])
    }

    override fun getItemCount(): Int = stories.size

    inner class StoryViewHolder(private val binding: ItemStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private var imgPhoto: ImageView = binding.ivItemPhoto
        private var tvName: TextView = binding.tvItemName
        private var tvDescription: TextView = binding.tvItemDescription
        fun bind(story: ListStoryItem) {

            tvName.text = story.name
            tvDescription.text = story.description


            Glide.with(binding.root.context).load(story.photoUrl).into(imgPhoto)

            binding.root.setOnClickListener {

                val intent = Intent(binding.root.context, DetailStoryActivity::class.java)
                intent.putExtra("STORY_ID", story.id)
                val optionsCompat: ActivityOptionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        itemView.context as Activity,
                        Pair(tvName, "transName"),
                        Pair(imgPhoto, "transPhoto"),
                        Pair(tvDescription, "transDescription")
                    )
                binding.root.context.startActivity(intent, optionsCompat.toBundle())
            }
        }
    }
}
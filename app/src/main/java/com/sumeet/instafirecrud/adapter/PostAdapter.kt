package com.sumeet.instafirecrud.adapter

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sumeet.instafirecrud.R
import com.sumeet.instafirecrud.models.Post

private const val TAG = "PostAdapter"
class PostAdapter(options: FirestoreRecyclerOptions<Post>, val listener:OnItemClicked): FirestoreRecyclerAdapter<Post, PostAdapter.PostViewHolder>(
    options
) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post,parent,false)
        val viewHolder = PostViewHolder(view)
        viewHolder.ivLikeButton.setOnClickListener {
            listener.onLikeClicked(snapshots.getSnapshot(viewHolder.adapterPosition).id)
        }
        viewHolder.deleteButton.setOnClickListener {
            listener.onDeleteClicked(snapshots.getSnapshot(viewHolder.adapterPosition).id)
        }
        viewHolder.updateButton.setOnClickListener {
            listener.onUpdateClicked(snapshots.getSnapshot(viewHolder.adapterPosition).id)
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int, model: Post) {
        holder.tvUserName.text = model.createdBy.username
        holder.tvPostDescription.text = model.description
        holder.tvCreatedAt.text = DateUtils.getRelativeTimeSpanString(model.createdAt)
        holder.textEdited.text = model.textEdit
        holder.tvLikeCount.text = model.likedBy.size.toString()
        Glide.with(holder.ivProfileImage.context).load(model.createdBy.imageUrl).apply(
            RequestOptions().transform(
                CenterCrop(), RoundedCorners(100)

            )
        ).into(holder.ivProfileImage)

        Glide.with(holder.ivUploadedImage.context).load(model.imageUrlPost).apply(
            RequestOptions().transform(
                CenterCrop(), RoundedCorners(20)

            )
        ).into(holder.ivUploadedImage)


        //Handling of Like Functionality
        val auth = Firebase.auth
        val currentUserId = auth.currentUser!!.uid
        val isLiked = model.likedBy.contains(currentUserId)

        if(isLiked) {
            holder.ivLikeButton.setImageDrawable(ContextCompat.getDrawable(holder.ivLikeButton.context,R.drawable.ic_liked))
        }
        else {
            holder.ivLikeButton.setImageDrawable(ContextCompat.getDrawable(holder.ivLikeButton.context,R.drawable.ic_unliked))
        }



    }
    inner class PostViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val textEdited = itemView.findViewById<TextView>(R.id.tvEdited)
        val tvPostDescription = itemView.findViewById<TextView>(R.id.tvDescription)
        val tvUserName = itemView.findViewById<TextView>(R.id.tvUserName)
        val tvCreatedAt = itemView.findViewById<TextView>(R.id.tvCreatedAt)
        val ivProfileImage = itemView.findViewById<ImageView>(R.id.ivProfileImage)
        val ivUploadedImage = itemView.findViewById<ImageView>(R.id.ivImageUploaded)
        val tvLikeCount = itemView.findViewById<TextView>(R.id.tvLikeCount)
        val deleteButton = itemView.findViewById<ImageView>(R.id.ivDelete)
        val updateButton = itemView.findViewById<ImageView>(R.id.ivEdit)
        val ivLikeButton = itemView.findViewById<ImageView>(R.id.ivLikeButton)

    }
}

interface OnItemClicked {

    fun onLikeClicked(postId:String)

    fun onDeleteClicked(postId:String)

    fun onUpdateClicked(postId:String)

}
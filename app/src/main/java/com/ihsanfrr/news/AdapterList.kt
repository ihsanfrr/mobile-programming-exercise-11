package com.ihsanfrr.news


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class AdapterList(private val itemList: kotlin.collections.List<ItemList>): RecyclerView.Adapter<AdapterList.ViewHolder>() {
    private var listener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(item: ItemList)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val subTitle: TextView = itemView.findViewById(R.id.subTitle)
        val image: ImageView = itemView.findViewById(R.id.image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_data, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        holder.title.text = item.title
        holder.subTitle.text = item.subTitle
        Glide.with(holder.image.context).load(item.imageUrl).into(holder.image)

        holder.itemView.setOnClickListener {
            listener?.onItemClick(item)
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}
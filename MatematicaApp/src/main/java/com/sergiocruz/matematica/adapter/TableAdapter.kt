package com.sergiocruz.matematica.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.sergiocruz.matematica.R
import kotlinx.android.synthetic.main.table_item.view.*

class TableAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var fullTableData: Map<Int, Pair<String, Boolean>>? = null
    private var primesTableData: Map<Int, Pair<String, Boolean>>? = null
    private var showAll: Boolean = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.table_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as ItemViewHolder
        if (showAll) {
            holder.item.text = fullTableData?.get(position)?.first
            paintCell(holder.root, fullTableData?.get(position)?.second)
        } else {
            holder.item.text = primesTableData?.get(position)?.first
            holder.root.setCardBackgroundColor(Color.parseColor("#9769bc4d"))
        }
    }

    private fun paintCell(card: CardView, isPrime: Boolean?) {
        if (isPrime == true) {
            card.setCardBackgroundColor(Color.parseColor("#9769bc4d"))
        } else {
            card.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
        }
    }

    override fun getItemCount(): Int {
        return if (showAll) {
            fullTableData?.size ?: 0
        } else {
            primesTableData?.size ?: 0
        }
    }

    /** Stupid android recyclerview mixes up items without this */
    override fun getItemId(position: Int) = position.toLong()

    override fun getItemViewType(position: Int) = position

    fun swap(
        full: Map<Int, Pair<String, Boolean>>,
        primes: Map<Int, Pair<String, Boolean>>,
        showAll: Boolean
    ) {
        this.fullTableData = full
        this.primesTableData = primes
        this.showAll = showAll
        notifyDataSetChanged()
    }

    fun reloadAdapter(showAll: Boolean) {
        this.showAll = showAll
        notifyDataSetChanged()
    }

    inner class ItemViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        internal val root: CardView = itemView.cardViewItem
        internal val item: TextView = itemView.textViewItem
    }
}

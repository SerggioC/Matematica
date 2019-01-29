package com.sergiocruz.MatematicaPro.fragment

import android.graphics.Color
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.sergiocruz.MatematicaPro.R
import kotlinx.android.synthetic.main.table_item.view.*

class TableAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.table_item, parent, false)
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

    private var fullTableData: Map<Int, Pair<String, Boolean>>? = null
    private var primesTableData: Map<Int, Pair<String, Boolean>>? = null
    private var showAll: Boolean = true

    fun swap(
        full: Map<Int, Pair<String, Boolean>>,
        primes: Map<Int, Pair<String, Boolean>>,
        showAll: Boolean
    ) {
        this.fullTableData = full
        this.primesTableData = primes
        this.showAll = showAll
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



package com.squad.chat.commons

import android.view.View

import androidx.recyclerview.widget.RecyclerView

/**
 * Base ViewHolder
 */
abstract class ViewHolder<DATA>(itemView: View) : RecyclerView.ViewHolder(itemView) {

    abstract fun onBind(data: DATA)

}

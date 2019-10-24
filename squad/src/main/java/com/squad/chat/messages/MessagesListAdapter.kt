package com.squad.chat.messages

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squad.chat.commons.ImageLoader
import com.squad.chat.commons.ViewHolder
import com.squad.chat.commons.models.IMessage
import com.squad.chat.utils.DateFormatter
import java.util.*


/**
 * For default list item layout and view holder.
 *
 * @param senderId    identifier of sender.
 * @param holders     custom layouts and view holders. See [MessageHolders] documentation for details
 * @param imageLoader image loading method.
 */

open class MessagesListAdapter<MESSAGE : IMessage>(private val senderId: String, private val holders: MessageHolders,
                                                   private val imageLoader: ImageLoader) : RecyclerView.Adapter<ViewHolder<*>>() {

    private var items: MutableList<Wrapper<*>> = mutableListOf()

    private var selectedItemsCount: Int = 0
    private var selectionListener: SelectionListener? = null
    var layoutManager: RecyclerView.LayoutManager? = null
        set(value) {
            field = value
        }
    private var dateHeadersFormatter: DateFormatter.Formatter? = null

    /**
     * Returns the list of selected messages.
     *
     * @return list of selected messages. Empty list if nothing was selected or selection mode is disabled.
     */
    private val selectedMessages: ArrayList<MESSAGE>
        get() {
            val selectedMessages = ArrayList<MESSAGE>()
            for (wrapper in items) {
                if (wrapper.item is IMessage && wrapper.isSelected) {
                    selectedMessages.add(wrapper.item as MESSAGE)
                }
            }
            return selectedMessages
        }

    /**
     * For default list item layout and view holder.
     *
     * @param senderId    identifier of sender.
     * @param imageLoader image loading method.
     */
    constructor(senderId: String, imageLoader: ImageLoader) : this(senderId, MessageHolders(), imageLoader)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<*> {
        return holders.getHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: ViewHolder<*>, position: Int) {
        val wrapper = items[position] as Wrapper<MESSAGE>
        holders.bind(holder, wrapper.item, wrapper.isSelected, imageLoader,
                getMessageClickListener(wrapper),
                getMessageLongClickListener(wrapper),
                dateHeadersFormatter)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return holders.getViewType(items[position].item, senderId)
    }


    /*
     * PUBLIC METHODS
     * */

    /**
     * Adds message to bottom of list and scroll if needed.
     *
     * @param message message to add.
     * @param scroll  `true` if need to scroll list to bottom when message added.
     */
    fun addToStart(message: MESSAGE) {
        val isNewMessageToday = !isPreviousSameDate(0, message.createdAt)
        if (isNewMessageToday) {
            items.add(0, Wrapper(message.createdAt))
        }
        val element = Wrapper(message)
        items.add(0, element)
        notifyItemRangeInserted(0, if (isNewMessageToday) 2 else 1)
        if (layoutManager != null) {
            layoutManager!!.scrollToPosition(0)
        }
    }

    /**
     * Deletes messages list.
     *
     * @param messages messages list to delete.
     */
    private fun delete(messages: List<MESSAGE>) {
        var result = false
        for (message in messages) {
            val index = getMessagePositionById(message.id)
            if (index >= 0) {
                items.removeAt(index)
                notifyItemRemoved(index)
                result = true
            }
        }
        if (result) {
            recountDateHeaders()
        }
    }

    /**
     * Enables selection mode.
     *
     * @param selectionListener listener for selected items count. To get selected messages use [.getSelectedMessages].
     */
    fun enableSelectionMode(selectionListener: SelectionListener?) {
        requireNotNull(selectionListener) { "SelectionListener must not be null. Use `disableSelectionMode()` if you want tp disable selection mode" }
        this.selectionListener = selectionListener
    }

    /**
     * Copies text to device clipboard and returns selected messages text. Also it does [.unselectAllItems] for you.
     *
     * @param context   The context.
     * @param formatter The formatter that allows you to format your message model when copying.
     * @param reverse   Change ordering when copying messages.
     * @return formatted text by [Formatter]. If it's `null` - `MESSAGE#toString()` will be used.
     */
    fun copySelectedMessagesText(context: Context, formatter: Formatter<MESSAGE>, reverse: Boolean): String {
        val copiedText = getSelectedText(formatter, reverse)
        copyToClipboard(context, copiedText)
        unselectAllItems()
        return copiedText
    }

    /**
     * Unselect all of the selected messages. Notifies [SelectionListener] with zero count.
     */
    fun unselectAllItems() {
        for (i in items.indices) {
            val wrapper = items[i]
            if (wrapper.isSelected) {
                wrapper.isSelected = false
                notifyItemChanged(i)
            }
        }
        isSelectionModeEnabled = false
        selectedItemsCount = 0
        notifySelectionChanged()
    }

    /**
     * Deletes all of the selected messages and disables selection mode.
     * Call [.getSelectedMessages] before calling this method to delete messages from your data source.
     */
    fun deleteSelectedMessages() {
        val selectedMessages = selectedMessages
        delete(selectedMessages)
        unselectAllItems()
    }

    /**
     * Sets custom [DateFormatter.Formatter] for text representation of date headers.
     */
    fun setDateHeadersFormatter(dateHeadersFormatter: DateFormatter.Formatter) {
        this.dateHeadersFormatter = dateHeadersFormatter
    }

    /*
     * PRIVATE METHODS
     * */
    private fun recountDateHeaders() {
        val indicesToDelete = ArrayList<Int>()

        for (i in items.indices) {
            val wrapper = items[i]
            if (wrapper.item is Date) {
                if (i == 0) {
                    indicesToDelete.add(i)
                } else {
                    if (items[i - 1].item is Date) {
                        indicesToDelete.add(i)
                    }
                }
            }
        }

        indicesToDelete.reverse()
        for (i in indicesToDelete) {
            items.removeAt(i)
            notifyItemRemoved(i)
        }
    }

    internal fun setLayoutManager(layoutManager: RecyclerView.LayoutManager) {
        this.layoutManager = layoutManager
    }

    private fun getMessagePositionById(id: String): Int {
        for (i in items.indices) {
            val wrapper = items[i]
            if (wrapper.item is IMessage) {
                val message = wrapper.item as MESSAGE
                if (message.id.contentEquals(id)) {
                    return i
                }
            }
        }
        return -1
    }

    private fun isPreviousSameDate(position: Int, dateToCompare: Date): Boolean {
        if (items.size <= position) return false
        return if (items[position].item is IMessage) {
            val previousPositionDate = (items[position].item as MESSAGE).createdAt
            DateFormatter.isSameDay(dateToCompare, previousPositionDate)
        } else
            false
    }

    private fun isPreviousSameAuthor(id: String, position: Int): Boolean {
        val prevPosition = position + 1
        return if (items.size <= prevPosition)
            false
        else
            items[prevPosition].item is IMessage && (items[prevPosition].item as MESSAGE).user.id.contentEquals(id)
    }

    private fun incrementSelectedItemsCount() {
        selectedItemsCount++
        notifySelectionChanged()
    }

    private fun decrementSelectedItemsCount() {
        selectedItemsCount--
        isSelectionModeEnabled = selectedItemsCount > 0

        notifySelectionChanged()
    }

    private fun notifySelectionChanged() {
        if (selectionListener != null) {
            selectionListener!!.onSelectionChanged(selectedItemsCount)
        }
    }

    private fun getMessageClickListener(wrapper: Wrapper<MESSAGE>): View.OnClickListener {
        return View.OnClickListener { view ->
            if (selectionListener != null && isSelectionModeEnabled) {
                wrapper.isSelected = !wrapper.isSelected

                if (wrapper.isSelected)
                    incrementSelectedItemsCount()
                else
                    decrementSelectedItemsCount()

                val message = wrapper.item
                notifyItemChanged(getMessagePositionById(message.id))
            }
        }
    }

    private fun getMessageLongClickListener(wrapper: Wrapper<MESSAGE>): View.OnLongClickListener {
        return View.OnLongClickListener { view ->
            if (selectionListener == null) {
                true
            } else {
                isSelectionModeEnabled = true
                view.performClick()
                true
            }
        }
    }

    private fun getSelectedText(formatter: Formatter<MESSAGE>?, reverse: Boolean): String {
        val builder = StringBuilder()

        val selectedMessages = selectedMessages
        if (reverse) selectedMessages.reverse()

        for (message in selectedMessages) {
            builder.append(formatter?.format(message) ?: message.toString())
            builder.append("\n\n")
        }
        builder.replace(builder.length - 2, builder.length, "")

        return builder.toString()
    }

    private fun copyToClipboard(context: Context, copiedText: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(copiedText, copiedText)
        clipboard.setPrimaryClip(clip)
    }


    /*
     * LISTENERS
     * */

    /**
     * Interface definition for a callback to be invoked when selected messages count is changed.
     */
    interface SelectionListener {

        /**
         * Fires when selected items count is changed.
         *
         * @param count count of selected items.
         */
        fun onSelectionChanged(count: Int)
    }


    /**
     * Interface used to format your message model when copying.
     */
    interface Formatter<MESSAGE> {

        /**
         * Formats an string representation of the message object.
         *
         * @param message The object that should be formatted.
         * @return Formatted text.
         */
        fun format(message: MESSAGE): String
    }

    /*
     * WRAPPER
     * */
    inner class Wrapper<DATA> internal constructor(var item: DATA) {
        var isSelected: Boolean = false
    }

    companion object {

        protected var isSelectionModeEnabled: Boolean = false
    }
}

package com.squad.chat.ui

import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.squad.chat.R
import com.squad.chat.commons.ImageLoader
import com.squad.chat.databinding.ActivityMainBinding
import com.squad.chat.messages.DummyData
import com.squad.chat.messages.Message
import com.squad.chat.messages.MessagesListAdapter
import com.squad.chat.utils.DateFormatter
import com.squad.chat.utils.DoubleClickListener
import com.squad.chat.utils.onChange
import com.squareup.picasso.Picasso
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import java.text.SimpleDateFormat
import java.util.*


open class MainActivity : AppCompatActivity(), DateFormatter.Formatter, MessagesListAdapter.SelectionListener, Toolbar.OnMenuItemClickListener {

    private val senderId = "0" //We will get this from chat list activity
    private lateinit var imageLoader: ImageLoader

    private lateinit var messagesAdapter: MessagesListAdapter<Message>
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by lazy { ViewModelProviders.of(this)[MainViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent?.extras?.let {
            //Assuming we get the number of users and info of users as argument
            //dummy data in viewmodel
        }

        imageLoader = object : ImageLoader {
            override fun loadImage(imageView: ImageView, url: String?) {
                Picasso.get().load(url).into(imageView)
            }
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.toolbar.setOnMenuItemClickListener(this)

        initAdapter()
        binding.send.setOnClickListener(object : DoubleClickListener() {
            override fun onSingleClick() {
                outgoingMessage()
            }

            override fun onDoubleClick() {
                randomIncomingMessage()
            }

        })
        binding.image.setOnClickListener { onAddAttachments() }


        //Change toolbar menu based on selection or not
        viewModel.selectionCount.observe(this, Observer {
            if (it == null || it == 0) {
                binding.toolbar.apply {
                    menu.clear()
                    inflateMenu(R.menu.menu_home)
                }
            } else binding.toolbar.apply {
                menu.clear()
                inflateMenu(R.menu.menu_selection)
            }
        })

        binding.messageInput.onChange {
            if (it.isNullOrBlank()) binding.send.setImageResource(R.drawable.ic_send_disabled)
            else binding.send.setImageResource(R.drawable.ic_send)
        }

        //Take a picture feature
        binding.camera.setOnClickListener {
            Toast.makeText(this@MainActivity, "Photo Feature", Toast.LENGTH_SHORT).show()
        }
    }

    fun outgoingMessage(): Boolean {
        if (binding.messageInput.text.isNotBlank()) {
            messagesAdapter.addToStart(DummyData.getOutgoingMessage(binding.messageInput.text.toString()))
            binding.messageInput.text.clear()
            return true
        }
        return false
    }

    fun randomIncomingMessage(): Boolean {
        messagesAdapter.addToStart(DummyData.incomingMessage)
        return true
    }

    private fun onAddAttachments() {
        messagesAdapter.addToStart(DummyData.imageMessage)
    }

    override fun format(date: Date): String {
        return when {
            DateFormatter.isToday(date) -> DateFormatter.format(date, DateFormatter.Template.TIME)
            DateFormatter.isYesterday(date) -> getString(R.string.date_header_yesterday)
            else -> DateFormatter.format(date, DateFormatter.Template.STRING_DAY_MONTH_YEAR)
        }
    }

    private fun initAdapter() {
        val layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, true)

        binding.messagesList.itemAnimator = SlideInUpAnimator()
        binding.messagesList.layoutManager = layoutManager

        messagesAdapter = MessagesListAdapter(senderId, imageLoader)
        messagesAdapter.let {
            it.enableSelectionMode(this)
            it.setDateHeadersFormatter(this)
            it.setLayoutManager(layoutManager)
        }
        binding.messagesList.adapter = messagesAdapter

    }

    private val messageStringFormatter: MessagesListAdapter.Formatter<Message> = object : MessagesListAdapter.Formatter<Message> {
        override fun format(message: Message): String {
            val createdAt = SimpleDateFormat("MMM d, EEE 'at' h:mm a", Locale.getDefault())
                    .format(message.createdAt)

            var text = message.text
            if (text == null) text = "[attachment]"

            return String.format(Locale.getDefault(), "%s: %s (%s)",
                    message.user.name, text, createdAt)
        }

    }

    override fun onStart() {
        super.onStart()
        //Add dummy messages
        messagesAdapter.addToStart(DummyData.outgoingMessage)
        messagesAdapter.addToStart(DummyData.incomingMessage)
        messagesAdapter.addToStart(DummyData.incomingMessage)
        messagesAdapter.addToStart(DummyData.outgoingMessage)
        messagesAdapter.addToStart(DummyData.incomingMessage)
        messagesAdapter.addToStart(DummyData.incomingMessage)
    }


    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.phone -> Toast.makeText(this@MainActivity, "Call Feature", Toast.LENGTH_SHORT).show()
            R.id.video -> Toast.makeText(this@MainActivity, "Video Feature", Toast.LENGTH_SHORT).show()
            R.id.menu_delete -> messagesAdapter.deleteSelectedMessages()
            R.id.menu_copy -> {
                messagesAdapter.copySelectedMessagesText(this, messageStringFormatter, true)
                Toast.makeText(this@MainActivity, R.string.copied_message, Toast.LENGTH_SHORT).show()
            }
        }
        return true
    }

    override fun onBackPressed() {
        //Close if no selections, else clear the selections
        if (viewModel.selectionCount.value == 0) {
            finish()
        } else {
            messagesAdapter.unselectAllItems()
        }
    }

    override fun onSelectionChanged(count: Int) {
        viewModel.selectionCount.value = count
    }

}

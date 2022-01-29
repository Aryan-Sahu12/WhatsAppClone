package com.example.whatsappclone.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsappclone.R
import com.example.whatsappclone.modals.ChatEvent
import com.example.whatsappclone.modals.DateHeader
import com.example.whatsappclone.modals.Message
import com.puldroid.whatsappclone.utils.formatAsTime
import kotlinx.android.synthetic.main.list_item_chat_sent_message.view.*
import kotlinx.android.synthetic.main.list_item_date_header.view.*
import java.time.DateTimeException

class ChatAdapter(private val list: MutableList<ChatEvent>,private val mCurrentUid: String)
    :RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val inflate = { layout: Int ->
            LayoutInflater.from(parent.context).inflate(layout,parent,false)
        }

        return when(viewType){
            TEXT_MESSAGE_RECEIVED ->{
                MessageViewHolder(inflate(R.layout.list_item_chat_recv_message))
            }
            TEXT_MESSAGE_SENT ->{
                MessageViewHolder(inflate(R.layout.list_item_chat_sent_message))
            }
            DATE_HEADER ->{
                DateViewHolder(inflate(R.layout.list_item_date_header))
            }
            else -> MessageViewHolder(inflate(R.layout.list_item_chat_recv_message))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(val item = list[position]){
            is DateHeader -> {
                holder.itemView.textView.text = item.date
            }
            is Message -> {
                holder.itemView.apply {
                    content.text = item.msg
                    time.text = item.sentAt.formatAsTime()
                }
            }
        }
    }

    override fun getItemCount():Int = list.size

    //This function is for - at which position which view we have to show
    override fun getItemViewType(position: Int): Int {
        return when (val event = list[position]){
            is Message -> {
                if(event.senderId == mCurrentUid){
                    TEXT_MESSAGE_SENT
                }else{
                    TEXT_MESSAGE_RECEIVED
                }
            }
            is DateHeader -> DATE_HEADER
            else -> UNSUPPORTED
        }
    }

    class DateViewHolder(view: View) : RecyclerView.ViewHolder(view)

    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view)



    //It's like static variable , we can access it outside the class as well
    companion object{
        private const val UNSUPPORTED = -1
        private const val TEXT_MESSAGE_RECEIVED = 0
        private const val TEXT_MESSAGE_SENT = 1
        private const val DATE_HEADER = 2

    }
}
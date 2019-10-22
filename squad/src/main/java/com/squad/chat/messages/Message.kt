package com.squad.chat.messages

import com.squad.chat.commons.models.IMessage
import com.squad.chat.commons.models.IUser
import com.squad.chat.commons.models.MessageContentType
import java.util.*

class Message @JvmOverloads constructor(sid: String,
                                        suser: IUser,
                                        stext: String? = "",
                                        screatedAt: Date = Date()) : IMessage, MessageContentType.Image, MessageContentType {
    override val imageUrl: String?
        get() = image?.url
    private var image: Image? = null


    override var id: String = sid
    override var text: String? = stext
    override var user: IUser = suser
    override var createdAt: Date = screatedAt

    fun setImage(image: Image) {
        this.image = image
    }

    class Image(val url: String)

}



package com.squad.chat.commons

import android.widget.ImageView

/**
 * Callback for implementing images loading in message list
 */
interface ImageLoader {

    fun loadImage(imageView: ImageView, url: String?)

}

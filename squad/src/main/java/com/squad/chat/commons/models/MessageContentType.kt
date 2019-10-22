

package com.squad.chat.commons.models

import com.squad.chat.messages.MessageHolders

/**
 * Interface used to mark messages as custom content types. For its representation see [MessageHolders]
 */

interface MessageContentType : IMessage {

    /**
     * Default media type for image message.
     */
    interface Image : IMessage {
        val imageUrl: String?
    }

    // other default types will be here

}

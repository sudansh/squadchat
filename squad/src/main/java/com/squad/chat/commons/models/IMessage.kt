

package com.squad.chat.commons.models

import java.util.*

/**
 * For implementing by real message model
 */
interface IMessage {

    /**
     * Returns message identifier
     *
     * @return the message id
     */
    val id: String

    /**
     * Returns message text
     *
     * @return the message text
     */
    val text: String?

    /**
     * Returns message author. See the [IUser] for more details
     *
     * @return the message author
     */
    val user: IUser

    /**
     * Returns message creation date
     *
     * @return the message creation date
     */
    val createdAt: Date
}

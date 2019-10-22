

package com.squad.chat.commons.models

/**
 * For implementing by real user model
 */
interface IUser {

    /**
     * Returns the user's id
     *
     * @return the user's id
     */
    val id: String

    /**
     * Returns the user's name
     *
     * @return the user's name
     */
    val name: String

    /**
     * Returns the user's avatar image url
     *
     * @return the user's avatar image url
     */
    val avatar: String
}

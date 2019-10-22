package com.squad.chat.messages

import com.squad.chat.commons.models.SenderUser
import com.squad.chat.commons.models.User
import java.security.SecureRandom
import java.util.*


class DummyData private constructor() {

    companion object {
        private var rnd = SecureRandom()
        private val names: ArrayList<String> = object : ArrayList<String>() {
            init {
                add("Celina Rojas")
                add("Carlos Shepard")
                add("Candace Frazier")
                add("Anita Hall")
                add("Mario Pruitt")
                add("Coleman Bell")
            }
        }

        private val incomingUser2 = User(
                "2",
                names[2],
                "https://picsum.photos/id/2/500/300")

        private val incomingUser1 = User(
                "1",
                names[1],
                "https://picsum.photos/id/1/500/300")

        //Returns either single or double imcoming users
        val incomingUsers = listOf(incomingUser1, incomingUser2)

        //Uncomment this to check three users
//        val incomingUsers = listOf(incomingUser1)

        val imageMessage: Message
            get() {
                val message = Message(randomId, outgoingUser, null)
                message.setImage(Message.Image(randomImage))
                return message
            }

        val outgoingMessage: Message
            get() = getOutgoingMessage(randomMessage)

        val incomingMessage: Message
            get() = Message(randomId, incomingUsers.random(), randomMessage)

        fun getOutgoingMessage(text: String): Message {
            return Message(randomId, outgoingUser, text)
        }

        private val outgoingUser = SenderUser()


        private val messages: ArrayList<String> = object : ArrayList<String>() {
            init {
                add("He turned in the research paper on Friday; otherwise, he would have not passed the class.")
                add("Where do random thoughts come from?")
                add("The mysterious diary records the voice.")
                add("Yeah, I think it's a good environment for learning English.")
                add("Someone I know recently combined Maple Syrup & buttered Popcorn thinking it would taste like caramel popcorn. It didn’t and they don’t recommend anyone else do it either.")
                add("We have a lot of rain in June.")

            }
        }

        private val randomId: String
            get() = UUID.randomUUID().leastSignificantBits.toString()

        private val randomMessage: String
            get() = messages[rnd.nextInt(messages.size)]

        private val randomImage: String
            get() = "https://picsum.photos/id/${rnd.nextInt(100)}/500/300"

    }
}

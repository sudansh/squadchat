package com.squad.chat.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.squad.chat.commons.models.User
import com.squad.chat.messages.DummyData

class MainViewModel : ViewModel() {
    var incomingUsers: List<User> = DummyData.incomingUsers
    var selectionCount = MutableLiveData<Int>()
    var toolbarTitle = MutableLiveData<String>()
    var avatar1 = MutableLiveData<String>()
    var avatar2 = MutableLiveData<String>()

    init {
        //Dummy info of incoming users
        toolbarTitle.value = incomingUsers.joinToString(separator = ", ") { it.name.split(" ")[0] }
        if (incomingUsers.size == 1) {
            avatar1.value = incomingUsers[0].avatar
        } else {
            avatar1.value = incomingUsers[0].avatar
            avatar2.value = incomingUsers[1].avatar
        }
    }

}

package com.squad.chat.commons.models

class User(sid: String,
           sname: String,
           savatar: String) : IUser {
    override val id = sid
    override val name = sname
    override val avatar = savatar

}
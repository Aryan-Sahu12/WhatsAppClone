package com.example.whatsappclone.modals

import java.util.*

data class Inbox(
    val msg:String,
    var from:String,
    var name:String,
    var image:String,
    val time:Date = Date(),
    var count:Int
){
    //For firebase we need to create empty constructor
    constructor() : this("","","","",Date() ,0)
}

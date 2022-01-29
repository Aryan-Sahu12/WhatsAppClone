package com.example.whatsappclone.modals

import com.google.firebase.firestore.FieldValue

data class User(
    val name:String,
    val imageUrl:String,
    val thumbImage:String,
    val uid:String,     //uid is a user ID that we get from the authentication
    val deviceToken:String,
    val status:String,
    val onlineStatus: String
) {
    /**
     * Empty [Constructor] for firebase
     */
    constructor():this("","","","","","", "")

    constructor(name: String,imageUrl: String,thumbImage: String,uid: String):this(
        name,
        imageUrl,
        thumbImage,
        uid,
        "",
        "Created by Aryan Sahu",
        ""
    )


}
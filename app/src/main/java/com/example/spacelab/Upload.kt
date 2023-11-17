package com.example.spacelab

class Upload {
    private var mName: String? = null
    private var mImageUrl: String? = null

    fun Upload() {
        //empty constructor needed
    }

    fun Upload(name: String, imageUrl: String?) {
        var name = name
        if (name.trim { it <= ' ' } == "") {
            name = "No Name"
        }
        mName = name
        mImageUrl = imageUrl
    }

    fun getName(): String? {
        return mName
    }

    fun setName(name: String?) {
        mName = name
    }

    fun getImageUrl(): String? {
        return mImageUrl
    }

    fun setImageUrl(imageUrl: String?) {
        mImageUrl = imageUrl
    }
}
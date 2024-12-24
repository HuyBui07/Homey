package com.example.homey.data.model

import android.os.Parcel
import android.os.Parcelable

data class Post(
    val type: String,
    val imageMain: Int,
    val smallImage1: Int,
    val smallImage2: Int,
    val smallImage3: Int,
    val title: String,
    val desc: String,
    val price: Long,
    val area: Int,
    val pricePerArea: String,
    val beds: Int,
    val baths: Int,
    val frontage: Int,
    val direction: String,
    val legal: String,
    val furniture: String,
    val location: String,
    val userName: String,
    val postTime: String,
    val phoneNumber: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readLong(),
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(type)
        parcel.writeInt(imageMain)
        parcel.writeInt(smallImage1)
        parcel.writeInt(smallImage2)
        parcel.writeInt(smallImage3)
        parcel.writeString(title)
        parcel.writeString(desc)
        parcel.writeLong(price)
        parcel.writeInt(area)
        parcel.writeString(pricePerArea)
        parcel.writeInt(beds)
        parcel.writeInt(baths)
        parcel.writeInt(frontage)
        parcel.writeString(direction)
        parcel.writeString(legal)
        parcel.writeString(furniture)
        parcel.writeString(location)
        parcel.writeString(userName)
        parcel.writeString(postTime)
        parcel.writeString(phoneNumber)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Post> {
        override fun createFromParcel(parcel: Parcel): Post {
            return Post(parcel)
        }

        override fun newArray(size: Int): Array<Post?> {
            return arrayOfNulls(size)
        }
    }
}
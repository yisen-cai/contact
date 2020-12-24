package com.glancebar.contact.persistence.entity

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "t_contact")
data class Contact(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = 0,

    @NonNull
    @ColumnInfo(name = "username")
    var username: String,

    @NonNull
    @ColumnInfo(name = "number")
    var number: String,

    @ColumnInfo(name = "avatar")
    var avatar: String?,

    @ColumnInfo(name = "company")
    var company: String? = null,

    @ColumnInfo(name = "tags")
    var tags: String = "",

    @ColumnInfo(name = "is_marked")
    var isMarked: Boolean = false
) {
}
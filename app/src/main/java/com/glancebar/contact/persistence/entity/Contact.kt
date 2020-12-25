package com.glancebar.contact.persistence.entity

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


/**
 * The Phone call entity
 * @author Ethan Gary
 * @date 2020/12/24
 */
@Entity(tableName = "t_contact")
data class Contact(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0,

    @NonNull
    @ColumnInfo(name = "username")
    var username: String? = null,

    @NonNull
    @ColumnInfo(name = "number")
    var number: String? = null,

    @ColumnInfo(name = "telephone")
    var telephone: String? = null,

    @ColumnInfo(name = "avatar")
    var avatar: String? = null,

    @ColumnInfo(name = "email")
    var email: String? = null,

    @ColumnInfo(name = "company")
    var company: String? = null,

    @ColumnInfo(name = "location")
    var location: String? = null,

    @ColumnInfo(name = "tags")
    var tags: String? = null,

    @ColumnInfo(name = "is_marked")
    var isMarked: Boolean = false
) {
}
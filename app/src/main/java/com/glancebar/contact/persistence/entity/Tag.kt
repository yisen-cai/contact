package com.glancebar.contact.persistence.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The contact tag entity
 * @author Ethan Gary
 * @date 2020/12/25
 */
@Entity(tableName = "t_tag")
data class Tag(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
)

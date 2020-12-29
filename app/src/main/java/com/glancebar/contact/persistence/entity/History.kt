package com.glancebar.contact.persistence.entity

import androidx.room.*


/**
 * The Phone call history entity
 * @author Ethan Gary
 * @date 2020/12/25
 */
@Entity(tableName = "t_history")
class History(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    val last: Int? = 0,
    val ringLast: Int? = 0,
    var contactId: Long? = 0,
    val isCall: Boolean = false,
    val isMissedCall: Boolean = false,
    @ColumnInfo(name = "number")
    val number: String? = null,
    @ColumnInfo(name = "create_time")
    val createTime: Long = 0,
)

/**
 * One to Many relation
 * https://developer.android.com/training/data-storage/room/relationships
 */
data class ContactWithHistory(
    @Embedded
    val contact: Contact,
    @Relation(
        parentColumn = "id",
        entityColumn = "contactId"
    )
    val histories: List<History>
)

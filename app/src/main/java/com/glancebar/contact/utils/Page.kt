package com.glancebar.contact.utils


/**
 *
 * @author Ethan Gary
 * @date 2020/12/28
 */
data class Page(
    var page: Int = 0,
    val size: Int = 20,
    var offset: Int = 0,
    var total: Int = 0
) {
    fun nextPage() {
        if (offset < total) {
            page += 1
            offset += size
        }
    }
}
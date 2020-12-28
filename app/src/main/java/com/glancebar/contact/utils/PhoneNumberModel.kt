package com.glancebar.contact.utils

/**
 * @author Ethan Gary
 * @date 2020/12/28
 */
data class PhoneNumberModel(
    var provinceName: String? = null,
    var cityName: String? = null,
    var carrier: String? = null,
) {
    override fun toString(): String {
        // TODO: 优化逻辑判断
        return "$provinceName$cityName$carrier"
    }
}
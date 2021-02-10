package com.glancebar.contact.utils

import com.google.i18n.phonenumbers.PhoneNumberToCarrierMapper
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber
import com.google.i18n.phonenumbers.geocoding.PhoneNumberOfflineGeocoder
import java.util.*

object PhoneUtil {
    /**
     * 直辖市
     */
    private val MUNICIPALITY = arrayOf("北京", "天津", "上海", "重庆")

    /**
     * 自治区
     */
    private val AUTONOMOUS_REGION = arrayOf(
        "新疆", "内蒙古", "西藏",
        "宁夏", "广西"
    )

    /**
     * 中国大陆区区号
     */
    private const val COUNTRY_CODE = 86
    private val phoneNumberUtil = PhoneNumberUtil.getInstance()

    /**
     * 提供与电话号码相关的运营商信息
     */
    private val carrierMapper = PhoneNumberToCarrierMapper.getInstance()

    /**
     * 提供与电话号码有关的地理信息
     */
    private val geocoder = PhoneNumberOfflineGeocoder.getInstance()

    /**
     * 根据手机号 判断手机号是否有效
     *
     * @param phoneNumber 手机号码
     * @return true-有效 false-无效
     */
    private fun checkPhoneNumber(phoneNumber: String): Boolean {
        val phone = phoneNumber.toLong()
        val pn = PhoneNumber()
        pn.countryCode = COUNTRY_CODE
        pn.nationalNumber = phone
        return phoneNumberUtil.isValidNumber(pn)
    }

    /**
     * 根据手机号 判断手机运营商
     *
     * @param phoneNumber 手机号码
     * @return 如：广东省广州市移动
     */
    private fun getCarrier(phoneNumber: String): String {
        val phone = phoneNumber.toLong()
        val pn = PhoneNumber()
        pn.countryCode = COUNTRY_CODE
        pn.nationalNumber = phone
        // 返回结果只有英文，自己转成成中文
        val carrierEn = carrierMapper.getNameForNumber(pn, Locale.ENGLISH)
        var carrierZh = ""
        when (carrierEn) {
            "China Mobile" -> carrierZh += "移动"
            "China Unicom" -> carrierZh += "联通"
            "China Telecom" -> carrierZh += "电信"
            else -> {
            }
        }
        return carrierZh
    }

    /**
     * 根据手机号 获取手机归属地
     *
     * @param phoneNumber 手机号码
     * @return 如：广东省广州市
     */
    private fun getGeo(phoneNumber: String): String {
        val phone = phoneNumber.toLong()
        val pn = PhoneNumber()
        pn.countryCode = COUNTRY_CODE
        pn.nationalNumber = phone
        return geocoder.getDescriptionForNumber(pn, Locale.CHINESE)
    }

    /**
     * 根据手机号 获取手机信息模型
     *
     * <pre>
     * 若返回值为null，则说明该号码无效
    </pre> *
     *
     * @param phoneNumber 手机号码
     * @return 手机信息模型PhoneModel
     */
    fun getPhoneModel(phoneNumber: String): PhoneNumberModel? {
        if (checkPhoneNumber(phoneNumber)) {
            val geo = getGeo(phoneNumber)
            val phoneModel = PhoneNumberModel()
            val carrier = getCarrier(phoneNumber)
            phoneModel.carrier = carrier

            // 直辖市
            for (value in MUNICIPALITY) {
                if (geo == value) {
//                    phoneModel.provinceName = value.replace("市", "")
                    phoneModel.cityName = value
                    return phoneModel
                }
            }
            // 自治区
            for (value in AUTONOMOUS_REGION) {
                if (geo.startsWith(value)) {
                    phoneModel.provinceName = value
                    phoneModel.cityName = geo.replace(value, "")
                    return phoneModel
                }
            }

            // 其它
            val splitArr = geo.split("省").toTypedArray()
            if (splitArr.size == 2) {
                phoneModel.provinceName = splitArr[0]
                phoneModel.cityName = splitArr[1].replace("市", "")
                return phoneModel
            }
        }
        return null
    }
}
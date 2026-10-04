package com.luckyzyx.luckytool.utils

import android.os.Build
import com.luckyzyx.luckytool.BuildConfig
import com.luckyzyx.luckytool.hook.utils.OplusBuildUtlils
import com.oplus.os.OplusBuild

/**
 * SDK_INT
 * @see [Build.VERSION.SDK_INT]
 */
val SDK get() = Build.VERSION.SDK_INT

/**
 * Android 11 30 R
 * @see [Build.VERSION_CODES.R]
 * */
val A11 get() = Build.VERSION_CODES.R

/**
 * Android 12 31 S
 * @see [Build.VERSION_CODES.S]
 * */
val A12 get() = Build.VERSION_CODES.S

/**
 * Android 13 33 TIRAMISU
 * @see [Build.VERSION_CODES.TIRAMISU]
 * */
val A13 get() = Build.VERSION_CODES.TIRAMISU

/**
 * Android 14 34 UPSIDE_DOWN_CAKE
 * @see [Build.VERSION_CODES.UPSIDE_DOWN_CAKE]
 * */
val A14 get() = Build.VERSION_CODES.UPSIDE_DOWN_CAKE

/**
 * Android 15 35 VANILLA_ICE_CREAM
 * @see [Build.VERSION_CODES.VANILLA_ICE_CREAM]
 * */
val A15 get() = Build.VERSION_CODES.VANILLA_ICE_CREAM

/**
 * Android 16 36 BAKLAVA
 * @see [Build.VERSION_CODES.BAKLAVA]
 * */
val A16 get() = Build.VERSION_CODES.BAKLAVA

/**
 * Android 17 37 CINNAMON_BUN
 * @see [Build.VERSION_CODES.CINNAMON_BUN]
 * */
val A17 get() = Build.VERSION_CODES.CINNAMON_BUN

/**
 * 获取构建版本名/版本号
 * @return [String]
 */
val getVersionName get() = BuildConfig.VERSION_NAME
val getVersionCode get() = BuildConfig.VERSION_CODE

/**
 * 获取OS版本名
 *
 * V12
 *
 * V12.1
 *
 * V12.2
 *
 * V13.0
 *
 * V13.1
 *
 * V13.1.1
 *
 * V13.2
 *
 * V14.0
 *
 * V14.0.1
 *
 * V14.0.2
 *
 * V14.1.0
 *
 * V15.0.0
 *
 * V15.0.1
 *
 * V15.0.2
 *
 * V16.0
 */
val getOSVersionName get() = getOSVersionName(OplusBuild.getOplusOSVERSION())

/**
 * 获取指定OS版本名
 * @param osCode Int
 * @return String
 */
fun getOSVersionName(osCode: Int): String {
    val buildUtils = OplusBuildUtlils(null)
    return try {
        buildUtils.getOSVersions?.get(osCode - 1) ?: throw Throwable()
    } catch (_: Throwable) {
        try {
            buildUtils.OSVERSIONS[osCode - 1]
        } catch (_: Throwable) {
            "null"
        }
    }
}

/**
 * 获取OS版本号
 *
 * 23 -> (c12)
 *
 * 24 -> (c12.1)
 *
 * 25 -> (c12.2)
 *
 * 26 -> (c13.0)
 *
 * 27 -> (c13.1)
 *
 * 28 -> (c13.1.1)
 *
 * 29 -> (c13.2)
 *
 * 30 -> (c14.0)
 *
 * 31 -> (c14.0.1)
 *
 * 32 -> (c14.0.2)
 *
 * 33 -> (c14.1.0)
 *
 * 34 -> (c15.0.0)
 *
 * 35 -> (c15.0.1)
 *
 * 36 -> (c15.0.2)
 *
 * 37 -> (c16.0.0)
 *
 * 38 -> (c16.1.0)
 *
 * 39 -> (c16.2.0)
 *
 * 40 -> (c17.0.0)
 */
val getOSVersionCode get() = safeOf(0) { OplusBuild.getOplusOSVERSION() }

/**
 * 获取指纹厂商
 * @return String
 */
val getFingerPrintBrand: String get() = safeOf(Build.MODEL) { Build.FINGERPRINT.split("/")[0] }


/**
 * 获取指纹机型
 * @return String
 */
val getFingerPrintModel get(): String = safeOf(Build.BRAND) { Build.FINGERPRINT.split("/")[1] }


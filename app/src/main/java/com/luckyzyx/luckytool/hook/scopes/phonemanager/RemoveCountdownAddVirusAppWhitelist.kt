package com.luckyzyx.luckytool.hook.scopes.phonemanager

import android.os.CountDownTimer
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.classOf
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.luckyzyx.luckytool.utils.DexkitUtils.checkDataList
import org.lsposed.lsparanoid.Obfuscate
import org.luckypray.dexkit.DexKitBridge
import org.luckypray.dexkit.result.MethodData

@Obfuscate
class RemoveCountdownAddVirusAppWhitelist(val dexKitBridge: DexKitBridge) : YukiBaseHooker() {
    override fun onHook() {
        //Source DialogCrossActivity
        dexKitBridge.findClass {
            matcher {
                className("com.oplus.phonemanager.common.DialogCrossActivity")
            }
        }.apply {
            checkDataList("DialogCrossActivity")

            val resolver = single().name.toClass().resolve()
            resolver.firstFieldOrNull { type = classOf<CountDownTimer>() } ?: return

            findMethod {
                matcher {
                    paramCount(2..3)
                    addUsingField {
                        type(classOf<CountDownTimer>())
                    }
                    //Only the dialog onShow listener: countDownButton.setEnabled(false)
                    //and countDownTimer.start(); the negative button listener calls
                    //cancel() instead and must not be intercepted
                    addInvoke {
                        declaredClass(classOf<CountDownTimer>())
                        name("start")
                    }
                }
            }.apply {
                checkDataList("CountDownTimer start", onlyOne = false)

                forEachIndexed { _: Int, methodData: MethodData ->
                    resolver.firstMethod {
                        name = methodData.methodName
                        parameterCount { it in 2..3 }
                    }.hook {
                        //Block the onShow listener: the button is never disabled and
                        //the countdown never starts, stays clickable immediately
                        intercept()
                    }
                }
            }
        }
    }
}
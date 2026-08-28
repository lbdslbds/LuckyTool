package com.luckyzyx.luckytool.hook.scopes.launcher

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.luckyzyx.luckytool.utils.ModulePrefs
import com.luckyzyx.luckytool.utils.getOSVersionCode
import org.lsposed.lsparanoid.Obfuscate

object HookLauncherFeature : YukiBaseHooker() {
    override fun onHook() {
        val osCode = getOSVersionCode
        loadHooker(HookFeatureOption)
        loadHooker(HookLauncherSettings)
        if (osCode >= 34) loadHooker(HookAppFeature)
    }

    @Obfuscate
    object HookAppFeature : YukiBaseHooker() {
        override fun onHook() {
            val disableAutoSwitch =
                prefs(ModulePrefs).getBoolean("disable_auto_switch_last_task", false)
            if (!disableAutoSwitch) return

            //Source AppFeatureUtils (all versions)
            //Legacy gate, new Launcher still consults it in StackPagedViewEx,
            //getToRecentsFocusPage, AppToOverviewAnimationProvider and
            //StandardInterruptHelper.canFocusToNextPage
            "com.android.common.util.AppFeatureUtils".toClass().resolve().apply {
                //Source OplusGridRecentsConfig isEnable
                firstMethodOrNull {
                    name = "isSupportAutoFocusToNextPageInOverviewState"
                    parameterCount = 1
                }?.hook {
                    replaceToFalse()
                }
                firstMethod {
                    name = "isSupportAutoFocusToNextPageInOverviewState"
                    emptyParameters()
                }.hook {
                    replaceToFalse()
                }
            }

            //Source RecentInterruptAnimUtilKt (new Launcher only)
            //computeNonInterruptFocusToNextPageTarget dropped the gate and returns
            //runningTaskIndex + 1, which StackRecentsViewDelegate.updateStackLayoutNextPage
            //uses to overwrite the hooked value; -1 falls back to getNextPage()
            "com.oplus.quickstep.utils.RecentInterruptAnimUtilKt".toClassOrNull()?.resolve()
                ?.apply {
                    firstMethodOrNull {
                        name = "computeNonInterruptFocusToNextPageTarget"
                        parameterCount = 1
                    }?.hook {
                        replaceTo(-1)
                    }
                }

            //Source TileCardFirstInterruptFocusPolicy (new Launcher only, card-first path)
            //resolveFocusPageFallback decides the card-first settle page without the gate;
            //-1 falls back to getNextPage()
            "com.oplus.quickstep.utils.tilecardfirst.policy.TileCardFirstInterruptFocusPolicy"
                .toClassOrNull()?.resolve()?.apply {
                    firstMethodOrNull {
                        name = "resolveFocusPageFallback"
                        parameterCount = 2
                    }?.hook {
                        replaceTo(-1)
                    }
                }
        }
    }

    @Obfuscate
    object HookFeatureOption : YukiBaseHooker() {
        override fun onHook() {
            val appUpdateDot = prefs(ModulePrefs).getBoolean("enable_display_app_update_dot", false)
            val disableDockerMax =
                prefs(ModulePrefs).getBoolean("remove_docker_max_number_limit", false)

            //Source FeatureOption
            "com.android.common.config.FeatureOption".toClass().resolve().apply {
                firstMethod { name = "initFeature" }.hook {
                    after {
                        if (appUpdateDot) {
                            firstFieldOrNull { name = "isSupportAppUpdateDotSwitch" }?.set(true)
                        }
                    }
                }
                if (disableDockerMax) {
                    firstMethodOrNull { name = "isDockerMax5" }?.hook {
                        replaceToFalse()
                    }
                }
            }
        }
    }

    @Obfuscate
    object HookLauncherSettings : YukiBaseHooker() {
        override fun onHook() {
            val appUpdateDot = prefs(ModulePrefs).getBoolean("enable_display_app_update_dot", false)

            //Source LauncherSettingsUtils
            "com.android.launcher.settings.LauncherSettingsUtils".toClass().resolve().apply {
                if (appUpdateDot) {
                    firstMethodOrNull { name = "isSupportAppUpdateDot" }?.hook {
                        replaceToTrue()
                    }
                }
            }
        }
    }
}

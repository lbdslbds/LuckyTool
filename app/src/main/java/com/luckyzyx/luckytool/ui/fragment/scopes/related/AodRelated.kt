package com.luckyzyx.luckytool.ui.fragment.scopes.related

import android.content.Context
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.DropDownPreference
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.SwitchPreference
import com.luckyzyx.luckytool.R
import com.luckyzyx.luckytool.ui.activity.MainActivity
import com.luckyzyx.luckytool.ui.fragment.base.BaseScopePreferenceFeagment
import com.luckyzyx.luckytool.utils.A13
import com.luckyzyx.luckytool.utils.FileUtils
import com.luckyzyx.luckytool.utils.ModulePrefs
import com.luckyzyx.luckytool.utils.SDK
import com.luckyzyx.luckytool.utils.arraySummaryDot
import com.luckyzyx.luckytool.utils.arraySummaryLine
import com.luckyzyx.luckytool.utils.checkPackName
import com.luckyzyx.luckytool.utils.getString
import com.luckyzyx.luckytool.utils.putString
import com.luckyzyx.luckytool.utils.setPrefsIconRes
import com.luckyzyx.luckytool.utils.setSummaryProvider
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
class AodRelated : BaseScopePreferenceFeagment() {
    override val scopes = arrayOf("com.android.systemui", "com.oplus.aod", "com.oplus.uiengine")

    override val isEnableRestartMenu: Boolean = true

    override val currentPrefsName: String = ModulePrefs

    override val navigateFragmentId: Int = R.id.aod

    private val pickFile = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null) {
            val path = FileUtils.getDocumentPath(requireActivity(), it)
                ?: return@registerForActivityResult
            requireActivity().putString(ModulePrefs, "custom_random_text_file", path)
        }
        (activity as MainActivity).restart()
    }

    override fun Context.loadRootPreference(): Preference {
        return Preference(this).apply {
            key = "com.oplus.aod"
            setPrefsIconRes(key) { resource, show ->
                icon = resource
                isIconSpaceReserved = show
            }
            title = getString(R.string.AodRelated)
            summary = arraySummaryDot(
                getString(R.string.remove_aod_music_whitelist),
                getString(R.string.remove_aod_notification_icon_whitelist)
            )
            isVisible = SDK >= A13 && checkPackName(key)
        }
    }

    override fun Context.loadPreferences(): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            //息屏相关
            add(PreferenceCategory(this@loadPreferences).apply {
                title = getString(R.string.AodRelated)
                key = "AodRelated"
                isIconSpaceReserved = false
            })
            add(SwitchPreference(this@loadPreferences).apply {
                title = getString(R.string.remove_aod_music_whitelist)
                key = "remove_aod_music_whitelist"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(this@loadPreferences).apply {
                title = getString(R.string.remove_aod_notification_icon_whitelist)
                key = "remove_aod_notification_icon_whitelist"
                setDefaultValue(false)
                isVisible = SDK == A13
                isIconSpaceReserved = false
            })
            add(DropDownPreference(this@loadPreferences).apply {
                title = getString(R.string.set_aod_notification_icon_style)
                summary = getString(R.string.current_mode) + ": %s"
                key = "set_aod_notification_icon_style"
                setEntries(R.array.set_aod_notification_icon_style_entries)
                entryValues = arrayOf("0", "1", "2")
                setDefaultValue("0")
                isVisible = SDK >= A13
                isIconSpaceReserved = false
            })
            add(SwitchPreference(this@loadPreferences).apply {
                title = getString(R.string.force_enable_screen_off_music_support)
                summary = getString(R.string.force_enable_screen_off_music_support_summary)
                key = "force_enable_screen_off_music_support"
                setDefaultValue(false)
                isVisible = osCode in 26..33
                isIconSpaceReserved = false
            })
            add(SwitchPreference(this@loadPreferences).apply {
                title = getString(R.string.hide_panoramic_aod_status_bar)
                summary = getString(R.string.hide_panoramic_aod_status_bar_summary)
                key = "hide_panoramic_aod_status_bar"
                setDefaultValue(false)
                isVisible = osCode >= 37
                isIconSpaceReserved = false
            })
            //随机一言
            if (osCode >= 26) {
                add(PreferenceCategory(this@loadPreferences).apply {
                    title = getString(R.string.AodRandomText)
                    key = "AodRandomText"
                    isIconSpaceReserved = false
                })
                add(DropDownPreference(this@loadPreferences).apply {
                    title = getString(R.string.set_random_text_display_mode)
                    summary = arraySummaryLine(
                        getString(R.string.current_mode) + ": %s",
                        getString(R.string.set_random_text_display_mode_tips1),
                        getString(R.string.set_random_text_display_mode_tips2)
                    )
                    key = "set_random_text_display_mode"
                    setEntries(R.array.set_random_text_display_mode_entries)
                    entryValues = arrayOf("0", "1", "2")
                    setDefaultValue("0")
                    isIconSpaceReserved = false
                    setOnPreferenceChangeListener { _, _ ->
                        (activity as MainActivity).restart()
                        true
                    }
                })
                when (getString(ModulePrefs, "set_random_text_display_mode", "0")) {
                    "1" -> add(Preference(this@loadPreferences).apply {
                        title = getString(R.string.custom_random_text_file)
                        key = "custom_random_text_file"
                        val path = getString(ModulePrefs, key, "")
                        summary = path.ifBlank { "Null" }
                        isCopyingEnabled = path.isNotBlank()
                        isIconSpaceReserved = false
                        setOnPreferenceClickListener {
                            pickFile.launch("text/plain")
                            true
                        }
                    })

                    "2" -> add(EditTextPreference(this@loadPreferences).apply {
                        title = getString(R.string.custom_random_text_api)
                        dialogTitle = title
                        key = "custom_random_text_api"
                        setDefaultValue("")
                        setSummaryProvider(this)
                        isIconSpaceReserved = false
                    })
                }
            }
            //字体样式
            if (osCode >= 26) {
                add(PreferenceCategory(this@loadPreferences).apply {
                    title = getString(R.string.AodTypface)
                    key = "AodTypface"
                    isIconSpaceReserved = false
                })
                add(DropDownPreference(this@loadPreferences).apply {
                    title = getString(R.string.set_aod_typeface_mode)
                    summary = getString(R.string.current_mode) + ": %s"
                    key = "set_aod_typeface_mode"
                    setEntries(R.array.set_aod_typeface_mode_entries)
                    entryValues = arrayOf("0", "1", "2")
                    setDefaultValue("0")
                    isIconSpaceReserved = false
                    setOnPreferenceChangeListener { _, _ ->
                        (activity as MainActivity).restart()
                        true
                    }
                })
                if (getString(ModulePrefs, "set_aod_typeface_mode") != "0") {
                    add(SwitchPreference(this@loadPreferences).apply {
                        title = getString(R.string.apply_aod_clock_typeface)
                        key = "apply_aod_clock_typeface"
                        setDefaultValue(false)
                        isIconSpaceReserved = false
                    })
                }
            }
        }
    }
}
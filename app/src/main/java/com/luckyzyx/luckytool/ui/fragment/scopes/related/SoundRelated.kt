package com.luckyzyx.luckytool.ui.fragment.scopes.related

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import androidx.preference.DropDownPreference
import androidx.preference.Preference
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreference
import com.luckyzyx.colorpicker.ColorPickerPreference
import com.luckyzyx.luckytool.R
import com.luckyzyx.luckytool.ui.activity.MainActivity
import com.luckyzyx.luckytool.ui.fragment.base.BaseScopePreferenceFeagment
import com.luckyzyx.luckytool.utils.A12
import com.luckyzyx.luckytool.utils.A13
import com.luckyzyx.luckytool.utils.ModulePrefs
import com.luckyzyx.luckytool.utils.SDK
import com.luckyzyx.luckytool.utils.arraySummaryDot
import com.luckyzyx.luckytool.utils.arraySummaryLine
import com.luckyzyx.luckytool.utils.getBoolean
import com.luckyzyx.luckytool.utils.sendPrefsValue
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
class SoundRelated : BaseScopePreferenceFeagment() {
    override val scopes = arrayOf("com.android.systemui", "com.android.settings")

    override val isEnableRestartMenu: Boolean = true

    override val currentPrefsName: String = ModulePrefs

    override val navigateFragmentId: Int = R.id.soundRelated

    override fun Context.loadRootPreference(): Preference {
        return Preference(this).apply {
            title = getString(R.string.SoundRelated)
            summary = arraySummaryDot(
                getString(R.string.media_volume_level),
                getString(R.string.minimum_volume_level_can_be_zero)
            )
            key = "SoundRelated"
            isIconSpaceReserved = false
        }
    }

    @SuppressLint("ResourceType")
    override fun Context.loadPreferences(): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(SwitchPreference(this@loadPreferences).apply {
                title = getString(R.string.disable_headphone_high_volume_warning)
                summary = getString(R.string.disable_headphone_high_volume_warning_summary)
                key = "disable_headphone_high_volume_warning"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SeekBarPreference(this@loadPreferences).apply {
                title = getString(R.string.media_volume_level)
                summary = getString(R.string.media_volume_level_summary)
                key = "media_volume_level"
                setDefaultValue(0)
                max = 50
                min = 0
                showSeekBarValue = true
                updatesContinuously = false
                isIconSpaceReserved = false
            })
            add(SwitchPreference(this@loadPreferences).apply {
                title = getString(R.string.enable_super_volume_mode)
                key = "enable_super_volume_mode"
                setDefaultValue(false)
                isVisible = SDK >= A13
                isIconSpaceReserved = false
            })
            add(SwitchPreference(this@loadPreferences).apply {
                title = getString(R.string.enable_super_volume_mode_for_calls)
                key = "enable_super_volume_mode_for_calls"
                setDefaultValue(false)
                isVisible = osCode >= 27
                isIconSpaceReserved = false
            })
            add(SwitchPreference(this@loadPreferences).apply {
                title = getString(R.string.minimum_volume_level_can_be_zero)
                key = "minimum_volume_level_can_be_zero"
                setDefaultValue(false)
                isVisible = SDK >= A12
                isIconSpaceReserved = false
            })
            add(SwitchPreference(this@loadPreferences).apply {
                title = getString(R.string.enable_app_specific_media_volume)
                summary = arraySummaryLine(
                    getString(R.string.need_restart_system),
                    getString(R.string.enable_app_specific_media_volume_summary),
                    getString(R.string.enable_app_specific_media_volume_tips)
                )
                key = "enable_app_specific_media_volume"
                setDefaultValue(false)
                isVisible = osCode >= 27
                isIconSpaceReserved = false
            })
            add(SwitchPreference(this@loadPreferences).apply {
                title = getString(R.string.disable_volume_bar_thickness_effect)
                key = "disable_volume_bar_thickness_effect"
                setDefaultValue(false)
                isVisible = osCode >= 30
                isIconSpaceReserved = false
            })
            add(DropDownPreference(this@loadPreferences).apply {
                title = getString(R.string.set_volume_bar_display_position)
                summary = getString(R.string.current_mode) + ": %s"
                key = "set_volume_bar_display_position"
                setEntries(R.array.set_volume_bar_display_position_entries)
                entryValues = arrayOf("0", "1", "2")
                setDefaultValue("0")
                isVisible = SDK >= A13
                isIconSpaceReserved = false
            })
            add(SeekBarPreference(this@loadPreferences).apply {
                title = getString(R.string.custom_volume_dialog_background_transparency)
                summary = getString(R.string.force_enable_systemui_blur_feature_tips)
                key = "custom_volume_dialog_background_transparency"
                setDefaultValue(-1)
                max = 10
                min = -1
                showSeekBarValue = true
                updatesContinuously = false
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, newValue ->
                    sendPrefsValue("com.android.systemui", key, newValue)
                    true
                }
            })
            add(SwitchPreference(this@loadPreferences).apply {
                title = getString(R.string.enable_volume_bar_percent_display)
                key = "enable_volume_bar_percent_display"
                setDefaultValue(false)
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, _ ->
                    (activity as MainActivity).restart()
                    true
                }
            })
            if (getBoolean(ModulePrefs, "enable_volume_bar_percent_display", false)) {
                add(ColorPickerPreference(this@loadPreferences).apply {
                    title = getString(R.string.custom_volume_bar_percent_color)
                    key = "custom_volume_bar_percent_color"
                    setDefaultValue(Color.WHITE)
                    isIconSpaceReserved = false
                    setOnPreferenceChangeListener { _, newValue ->
                        sendPrefsValue("com.android.systemui", key, newValue)
                        true
                    }
                })
            }
            add(SwitchPreference(this@loadPreferences).apply {
                title = getString(R.string.disable_audio_focus)
                key = "disable_audio_focus"
                summary = getString(R.string.need_restart_system)
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
        }
    }
}
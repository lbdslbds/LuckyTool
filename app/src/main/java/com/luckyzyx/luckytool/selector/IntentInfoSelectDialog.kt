package com.luckyzyx.luckytool.selector

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ResolveInfo
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.drake.net.utils.scope
import com.drake.net.utils.withDefault
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.highcapable.betterandroid.ui.component.adapter.factory.bindAdapter
import com.highcapable.betterandroid.ui.component.adapter.recycler.factory.notifyDataSetChangedIgnore
import com.highcapable.betterandroid.ui.extension.view.layoutInflater
import com.luckyzyx.luckytool.R
import com.luckyzyx.luckytool.data.AppIntentInfo
import com.luckyzyx.luckytool.databinding.DialogActivityInfoSelectorLayoutBinding
import com.luckyzyx.luckytool.databinding.LayoutActivityinfoCheckboxItemBinding
import com.luckyzyx.luckytool.databinding.LayoutActivityinfoItemBinding
import com.luckyzyx.luckytool.enums.IntentType
import com.luckyzyx.luckytool.listener.OnSelectIntentInfoListener
import com.luckyzyx.luckytool.utils.dialogCentered
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import org.lsposed.lsparanoid.Obfuscate

@SuppressLint("SetTextI18n")
@Obfuscate
class IntentInfoSelectDialog(
    context: Context, val multiMode: Boolean,
    val appIntentInfos: List<AppIntentInfo>,
    val appResolveInfos: Map<AppIntentInfo, ResolveInfo>
) : MaterialAlertDialogBuilder(context, dialogCentered) {

    private val TAG = "IntentInfoSelectDialog"

    private val binding =
        DialogActivityInfoSelectorLayoutBinding.inflate(context.layoutInflater)

    private lateinit var dialog: AlertDialog

    private var allIntentInfos = ArrayList<AppIntentInfo>()
    private var filterIntentInfos = ArrayList<AppIntentInfo>()
    private var allEnabledInfos = ArrayList<AppIntentInfo>()
    private var enabledList = ArrayList<AppIntentInfo>()

    private var onSelectIntentInfoListener: OnSelectIntentInfoListener? = null
    private var selectAllMode = false
    private var showAppIcon = true

    init {
        setView(binding.root)

        binding.searchViewLayout.apply {
            hint = "ActivityName"
            endIconMode = TextInputLayout.END_ICON_CLEAR_TEXT
        }
        binding.searchView.apply {
            addTextChangedListener(onTextChanged = { text: CharSequence?, _: Int, _: Int, _: Int ->
                val query = text?.toString() ?: ""
                filterIntentInfos = if (query.isBlank()) allIntentInfos
                else {
                    val newList = allIntentInfos.filter {
                        val resolveInfo = appResolveInfos[it]!!
                        resolveInfo.loadLabel(context.packageManager).contains(query) ||
                                resolveInfo.activityInfo.name.lowercase().contains(query)
                    }
                    ArrayList(newList)
                }
                binding.recyclerView.adapter?.notifyDataSetChangedIgnore()
            })
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadData()
        }
        binding.btnOk.apply {
            isVisible = multiMode
            setOnClickListener {
                dialog.dismiss()
                onSelectIntentInfoListener?.resultSelectIntentInfos(allEnabledInfos)
            }
        }

        binding.recyclerView.apply {
            adapter = bindAdapter<AppIntentInfo> {
                onBindData { filterIntentInfos }
                if (!multiMode) {
                    onBindItemView<LayoutActivityinfoItemBinding> { item, info, _ ->
                        item.activityIcon.isVisible = showAppIcon

                        val resolveInfo = appResolveInfos[info]!!
                        val appIcon = resolveInfo.loadIcon(context.packageManager)
                        val label = resolveInfo.loadLabel(context.packageManager)
                        val name = resolveInfo.activityInfo.name
                        val type = info.type

                        item.activityIcon.setImageDrawable(appIcon)
                        item.activityLabel.text = "$label $type"
                        item.activityName.text = name
                    }
                } else {
                    onBindItemView<LayoutActivityinfoCheckboxItemBinding> { item, info, _ ->
                        item.activityIcon.isVisible = showAppIcon

                        val resolveInfo = appResolveInfos[info]!!
                        val appIcon = resolveInfo.loadIcon(context.packageManager)
                        val label = resolveInfo.loadLabel(context.packageManager)
                        val name = resolveInfo.activityInfo.name
                        val type = info.type

                        item.activityIcon.setImageDrawable(appIcon)
                        item.activityLabel.text = "$label ${formatType(type)}"
                        item.activityName.text = name

                        item.checkboxView.setOnCheckedChangeListener(null)
                        item.checkboxView.isChecked = allEnabledInfos.contains(info)
                        item.checkboxView.setOnCheckedChangeListener { _, isChecked ->
                            allEnabledInfos.remove(info)
                            if (isChecked) allEnabledInfos.add(info)
                        }
                    }
                }
                onItemViewClick { view, info, _ ->
                    if (multiMode) {
                        view.findViewById<MaterialCheckBox>(R.id.checkbox_view)?.toggle()
                    } else {
                        dialog.dismiss()
                        onSelectIntentInfoListener?.resultSelectIntentInfos(arrayListOf(info))
                    }
                }
            }
            FastScrollerBuilder(this).useMd2Style().build()
        }
    }

    override fun show(): AlertDialog {
        if (allIntentInfos.isEmpty()) loadData()
        dialog = super.show()
        return dialog
    }

    fun setOnSelectIntentInfoListener(onSelectIntentInfoListener: OnSelectIntentInfoListener) {
        this.onSelectIntentInfoListener = onSelectIntentInfoListener
    }

    fun setEnabledList(list: ArrayList<AppIntentInfo>) {
        enabledList = list
    }

    fun setShowIcon(mode: Boolean) {
        showAppIcon = mode
    }

    fun setSelectAllMode(mode: Boolean) {
        selectAllMode = mode

        binding.btnSelectAll.apply {
            isVisible = multiMode && selectAllMode
            setOnClickListener {
                val isAll = allIntentInfos.size == allEnabledInfos.size
                if (isAll) allEnabledInfos.clear() else allEnabledInfos = allIntentInfos
                filterIntentInfos = allIntentInfos
                binding.recyclerView.adapter?.notifyDataSetChangedIgnore()
            }
        }
    }

    private fun loadData() {
        scope {
            allIntentInfos.clear()
            filterIntentInfos.clear()
            allEnabledInfos.clear()

            binding.swipeRefreshLayout.isRefreshing = true
            binding.searchViewLayout.isEnabled = false
            binding.searchView.text = null

            withDefault {
                allIntentInfos.addAll(appIntentInfos)
                allIntentInfos.forEach {
                    if (enabledList.contains(it)) {
                        allEnabledInfos.add(it)
                    }
                }
                allIntentInfos.removeAll(allEnabledInfos.toSet())
                allIntentInfos.addAll(0, allEnabledInfos)
                filterIntentInfos = allIntentInfos
            }

            binding.recyclerView.adapter?.notifyDataSetChangedIgnore()

            binding.swipeRefreshLayout.isRefreshing = false
            binding.searchViewLayout.isEnabled = true
        }
    }

    private fun formatType(type: IntentType): String {
        return when (type) {
            IntentType.SINGLE_SHARE -> context.getString(R.string.intent_single_share)
            IntentType.MULTI_SHARE -> context.getString(R.string.intent_multi_share)
            IntentType.PROCESS_TEXT -> context.getString(R.string.intent_long_press_text)
            IntentType.CONTENT -> context.getString(R.string.intent_open_content)
            IntentType.FILE -> context.getString(R.string.intent_open_file)
            IntentType.HTTP_LINK -> context.getString(R.string.intent_http_link)
            IntentType.HTTPS_LINK -> context.getString(R.string.intent_https_link)
            else -> IntentType.UNKNOWN.toString()
        }
    }
}
package com.luckyzyx.luckytool.selector

import android.content.Context
import android.content.pm.ActivityInfo
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
import com.luckyzyx.luckytool.databinding.DialogActivityInfoSelectorLayoutBinding
import com.luckyzyx.luckytool.databinding.LayoutActivityinfoCheckboxItemBinding
import com.luckyzyx.luckytool.databinding.LayoutActivityinfoItemBinding
import com.luckyzyx.luckytool.listener.OnSelectActivityInfoListener
import com.luckyzyx.luckytool.utils.dialogCentered
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
class ActivityInfoSelectDialog(
    context: Context, val multiMode: Boolean, val activitys: Array<ActivityInfo>?
) : MaterialAlertDialogBuilder(context, dialogCentered) {

    private val TAG = "ActivityInfoSelectDialog"

    private val binding =
        DialogActivityInfoSelectorLayoutBinding.inflate(context.layoutInflater)

    private lateinit var dialog: AlertDialog

    private var allActivityInfos = ArrayList<ActivityInfo>()
    private var filterActivityInfos = ArrayList<ActivityInfo>()
    private var allEnabledInfos = ArrayList<ActivityInfo>()
    private var enabledList = ArrayList<String>()

    private var onSelectActivityInfoListener: OnSelectActivityInfoListener? = null
    private var selectAllMode = false

    init {
        setView(binding.root)

        binding.searchViewLayout.apply {
            hint = "ActivityName"
            endIconMode = TextInputLayout.END_ICON_CLEAR_TEXT
        }
        binding.searchView.apply {
            addTextChangedListener(onTextChanged = { text: CharSequence?, _: Int, _: Int, _: Int ->
                val query = text?.toString() ?: ""
                filterActivityInfos = if (query.isBlank()) allActivityInfos
                else {
                    val newList = allActivityInfos.filter {
                        val label = it.loadLabel(context.packageManager)
                        it.name.contains(query) ||
                                label.toString().lowercase().contains(query)
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
                onSelectActivityInfoListener?.resultSelectActivityInfos(allEnabledInfos)
            }
        }

        binding.recyclerView.apply {
            adapter = bindAdapter<ActivityInfo> {
                onBindData { filterActivityInfos }
                if (!multiMode) {
                    onBindItemView<LayoutActivityinfoItemBinding> { item, info, _ ->
                        val appIcon = info.loadIcon(context.packageManager)

                        item.activityIcon.setImageDrawable(appIcon)
                        item.activityLabel.text = info.loadLabel(context.packageManager)
                        item.activityName.text = info.name
                    }
                } else {
                    onBindItemView<LayoutActivityinfoCheckboxItemBinding> { item, info, _ ->
                        val appIcon = info.loadIcon(context.packageManager)

                        item.activityIcon.setImageDrawable(appIcon)
                        item.activityLabel.text = info.loadLabel(context.packageManager)
                        item.activityName.text = info.name

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
                        onSelectActivityInfoListener?.resultSelectActivityInfos(arrayListOf(info))
                    }
                }
            }
            FastScrollerBuilder(this).useMd2Style().build()
        }
    }

    override fun show(): AlertDialog {
        if (allActivityInfos.isEmpty()) loadData()
        dialog = super.show()
        return dialog
    }

    fun setOnSelectActivityListener(onSelectActivityInfoListener: OnSelectActivityInfoListener) {
        this.onSelectActivityInfoListener = onSelectActivityInfoListener
    }

    fun setSelectAllMode(mode: Boolean) {
        selectAllMode = mode

        binding.btnSelectAll.apply {
            isVisible = multiMode && selectAllMode
            setOnClickListener {
                val isAll = allActivityInfos.size == allEnabledInfos.size
                if (isAll) allEnabledInfos.clear() else allEnabledInfos = allActivityInfos
                binding.recyclerView.adapter?.notifyDataSetChangedIgnore()
            }
        }
    }

    fun setEnabledList(list: ArrayList<String>) {
        enabledList = list
    }

    private fun loadData() {
        scope {
            allActivityInfos.clear()
            filterActivityInfos.clear()
            allEnabledInfos.clear()

            binding.swipeRefreshLayout.isRefreshing = true
            binding.searchViewLayout.isEnabled = false
            binding.searchView.text = null

            withDefault {
                allActivityInfos.addAll(activitys?.toList() ?: arrayListOf())
                allActivityInfos.forEach {
                    if (enabledList.contains(it.packageName)) allEnabledInfos.add(it)
                }
                allActivityInfos.removeAll(allEnabledInfos.toSet())
                allActivityInfos.addAll(0, allEnabledInfos)
                filterActivityInfos = allActivityInfos
            }

            binding.recyclerView.adapter?.notifyDataSetChangedIgnore()

            binding.swipeRefreshLayout.isRefreshing = false
            binding.searchViewLayout.isEnabled = true
        }
    }
}
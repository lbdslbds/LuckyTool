package com.luckyzyx.luckytool.ui.fragment.extension

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.ArraySet
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.core.widget.addTextChangedListener
import com.drake.net.utils.scopeLife
import com.drake.net.utils.withDefault
import com.google.android.material.materialswitch.MaterialSwitch
import com.highcapable.betterandroid.ui.component.adapter.factory.bindAdapter
import com.highcapable.betterandroid.ui.component.adapter.recycler.factory.notifyDataSetChangedIgnore
import com.luckyzyx.luckytool.R
import com.luckyzyx.luckytool.data.AppInfo
import com.luckyzyx.luckytool.databinding.FragmentMutliAppApplistLayoutBinding
import com.luckyzyx.luckytool.databinding.LayoutAppinfoSwitchItemBinding
import com.luckyzyx.luckytool.selector.SortFilterBottomSheetDialog
import com.luckyzyx.luckytool.ui.fragment.base.BaseFragment
import com.luckyzyx.luckytool.utils.IntentUtils
import com.luckyzyx.luckytool.utils.ModulePrefs
import com.luckyzyx.luckytool.utils.PackageUtils
import com.luckyzyx.luckytool.utils.ThemeUtils
import com.luckyzyx.luckytool.utils.getStringSet
import com.luckyzyx.luckytool.utils.putStringSet
import com.luckyzyx.luckytool.utils.sendPrefsKey
import com.luckyzyx.luckytool.utils.setupMenuProvider
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
class MultiAppFragment : BaseFragment<FragmentMutliAppApplistLayoutBinding>(), MenuProvider {

    private val TAG = "MultiAppFragment"

    private var allAppInfos = ArrayList<AppInfo>()
    private var filterAppInfos = ArrayList<AppInfo>()
    private var allEnabledInfos = ArrayList<AppInfo>()

    private val supportListKey = "multi_app_custom_list"

    private var isReverse = false
    private var sortMode = 0

    private lateinit var sortFilterBottomSheetDialog: SortFilterBottomSheetDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        setupMenuProvider(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sortFilterBottomSheetDialog = SortFilterBottomSheetDialog(requireActivity()).apply {
            setReverse(true) { _, isChecked ->
                isReverse = isChecked
                loadData()
            }
            setSortChips(
                true, context.resources.getStringArray(R.array.sort_selector_chips)
            ) { _, checkedIds ->
                sortMode = checkedIds.firstOrNull() ?: 0
                loadData()
            }
        }
        binding.searchViewLayout.apply {
            hint = "Name / PackageName"
            setEndIconOnClickListener {
                sortFilterBottomSheetDialog.show()
            }
        }
        binding.searchView.apply {
            addTextChangedListener(onTextChanged = { text: CharSequence?, _: Int, _: Int, _: Int ->
                val query = text?.toString() ?: ""
                filterAppInfos = if (query.isBlank()) allAppInfos
                else {
                    val newList = allAppInfos.filter {
                        it.name.lowercase().contains(query)
                                || it.packageName.lowercase().contains(query)
                    }
                    ArrayList(newList)
                }
                binding.recyclerView.adapter?.notifyDataSetChangedIgnore()
            })
        }
        binding.swipeRefreshLayout.apply {
            setOnRefreshListener {
                loadData()
            }
        }

        binding.recyclerView.apply {
            adapter = bindAdapter<AppInfo> {
                onBindData { filterAppInfos }
                onBindItemView<LayoutAppinfoSwitchItemBinding> { item, info, _ ->
                    item.appIcon.setImageDrawable(info.icon)
                    item.appName.text = info.name
                    item.packName.text = info.packageName

                    item.switchview.setOnCheckedChangeListener(null)
                    item.switchview.isChecked = allEnabledInfos.contains(info)
                    item.switchview.setOnCheckedChangeListener { _, isChecked ->
                        allEnabledInfos.remove(info)
                        if (isChecked) allEnabledInfos.add(info)
                        saveEnableList()
                    }
                }
                onItemViewClick { view, _, _ ->
                    view.findViewById<MaterialSwitch>(R.id.switchview)?.toggle()
                }
            }
            FastScrollerBuilder(this).useMd2Style().build()
        }

        if (allAppInfos.isEmpty()) loadData()
    }

    private fun loadData() {
        scopeLife {
            allAppInfos.clear()
            filterAppInfos.clear()
            allEnabledInfos.clear()

            binding.swipeRefreshLayout.isRefreshing = true
            binding.searchViewLayout.isEnabled = false
            binding.searchView.text = null

            withDefault {
                val enableData = requireActivity().getStringSet(ModulePrefs, supportListKey, ArraySet())

                val packageManager = requireActivity().packageManager
                allAppInfos = PackageUtils(packageManager).getInstalledAppInfos(0)
                allAppInfos.removeIf { it.isOverlay }

                enableData.forEach { its ->
                    val find = allAppInfos.find { it.packageName == its }
                    if (find != null) allEnabledInfos.add(find)
                }
                allAppInfos.apply {
                    when (sortMode) {
                        0 -> sortBy { it.name }
                        1 -> sortBy { it.packageName }
                        2 -> sortBy { it.size }
                        3 -> sortBy { it.installTime }
                        4 -> sortBy { it.lastInstallTime }
                        5 -> sortBy { it.target }
                    }
                    if (isReverse) reverse()
                }
                allEnabledInfos.apply {
                    when (sortMode) {
                        0 -> sortBy { it.name }
                        1 -> sortBy { it.packageName }
                        2 -> sortBy { it.size }
                        3 -> sortBy { it.installTime }
                        4 -> sortBy { it.lastInstallTime }
                        5 -> sortBy { it.target }
                    }
                    if (isReverse) reverse()
                }

                filterAppInfos = allAppInfos
                filterAppInfos.removeAll(allEnabledInfos.toSet())
                filterAppInfos.addAll(0, allEnabledInfos)
            }

            binding.recyclerView.adapter?.notifyDataSetChangedIgnore()

            binding.swipeRefreshLayout.isRefreshing = false
            binding.searchViewLayout.isEnabled = true
        }
    }

    private fun saveEnableList() {
        val data = allEnabledInfos.map { it.packageName }
        requireActivity().putStringSet(ModulePrefs, supportListKey, data.toSet())
        requireActivity().sendPrefsKey("android", supportListKey)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.add(0, 1, 0, getString(R.string.open)).apply {
            setIcon(R.drawable.baseline_open_in_new_24)
            setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            if (ThemeUtils.isNightMode(resources.configuration)) {
                iconTintList = ColorStateList.valueOf(Color.WHITE)
            }
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == 1) IntentUtils(requireActivity()).jumpMultiApp()
        return true
    }
}
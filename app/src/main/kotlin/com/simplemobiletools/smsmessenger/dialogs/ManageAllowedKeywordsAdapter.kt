package com.simplemobiletools.smsmessenger.dialogs

import android.view.*
import android.widget.PopupMenu
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.adapters.MyRecyclerViewAdapter
import com.simplemobiletools.commons.extensions.copyToClipboard
import com.simplemobiletools.commons.extensions.getPopupMenuTheme
import com.simplemobiletools.commons.extensions.getProperTextColor
import com.simplemobiletools.commons.extensions.setupViewBackground
import com.simplemobiletools.commons.interfaces.RefreshRecyclerViewListener
import com.simplemobiletools.commons.views.MyRecyclerView
import com.simplemobiletools.smsmessenger.R
import com.simplemobiletools.smsmessenger.extensions.config
import kotlinx.android.synthetic.main.item_manage_allowed_keyword.view.manage_allowed_keyword_holder
import kotlinx.android.synthetic.main.item_manage_allowed_keyword.view.manage_allowed_keyword_title
import kotlinx.android.synthetic.main.item_manage_blocked_keyword.view.manage_blocked_keyword_holder
import kotlinx.android.synthetic.main.item_manage_blocked_keyword.view.manage_blocked_keyword_title
import kotlinx.android.synthetic.main.item_manage_blocked_keyword.view.overflow_menu_anchor
import kotlinx.android.synthetic.main.item_manage_blocked_keyword.view.overflow_menu_icon

class ManageAllowedKeywordsAdapter(
    activity: BaseSimpleActivity, var allowedKeywords: ArrayList<String>, val listener: RefreshRecyclerViewListener?,
    recyclerView: MyRecyclerView, itemClick: (Any) -> Unit
) : MyRecyclerViewAdapter(activity, recyclerView, itemClick) {
    init {
        setupDragListener(true)
    }

    override fun getActionMenuId() = R.menu.cab_allowed_keywords

    override fun prepareActionMode(menu: Menu) {
        menu.apply {
            findItem(R.id.cab_copy_keyword).isVisible = isOneItemSelected()
        }
    }

    override fun actionItemPressed(id: Int) {
        if (selectedKeys.isEmpty()) {
            return
        }

        when (id) {
            R.id.cab_copy_keyword -> copyKeywordToClipboard()
            R.id.cab_delete -> deleteSelection()
        }
    }

    override fun getSelectableItemCount() = allowedKeywords.size

    override fun getIsItemSelectable(position: Int) = true

    override fun getItemSelectionKey(position: Int) = allowedKeywords.getOrNull(position)?.hashCode()

    override fun getItemKeyPosition(key: Int) = allowedKeywords.indexOfFirst { it.hashCode() == key }

    override fun onActionModeCreated() {}

    override fun onActionModeDestroyed() {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = createViewHolder(R.layout.item_manage_allowed_keyword, parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val allowedKeyword = allowedKeywords[position]
        holder.bindView(allowedKeyword, true, true) { itemView, _ ->
            setupView(itemView, allowedKeyword)
        }
        bindViewHolder(holder)
    }

    override fun getItemCount() = allowedKeywords.size

    private fun getSelectedItems() = allowedKeywords.filter { selectedKeys.contains(it.hashCode()) }

    private fun setupView(view: View, allowedKeyword: String) {
        view.apply {
            setupViewBackground(activity)
            manage_allowed_keyword_holder?.isSelected = selectedKeys.contains(allowedKeyword.hashCode())
            manage_allowed_keyword_title.apply {
                text = allowedKeyword
                setTextColor(textColor)
            }

            overflow_menu_icon.drawable.apply {
                mutate()
                setTint(activity.getProperTextColor())
            }

            overflow_menu_icon.setOnClickListener {
                showPopupMenu(overflow_menu_anchor, allowedKeyword)
            }
        }
    }

    private fun showPopupMenu(view: View, allowedKeyword: String) {
        finishActMode()
        val theme = activity.getPopupMenuTheme()
        val contextTheme = ContextThemeWrapper(activity, theme)

        PopupMenu(contextTheme, view, Gravity.END).apply {
            inflate(getActionMenuId())
            setOnMenuItemClickListener { item ->
                val allowedKeywordId = allowedKeyword.hashCode()
                when (item.itemId) {
                    R.id.cab_copy_keyword -> {
                        executeItemMenuOperation(allowedKeywordId) {
                            copyKeywordToClipboard()
                        }
                    }

                    R.id.cab_delete -> {
                        executeItemMenuOperation(allowedKeywordId) {
                            deleteSelection()
                        }
                    }
                }
                true
            }
            show()
        }
    }

    private fun executeItemMenuOperation(allowedKeywordId: Int, callback: () -> Unit) {
        selectedKeys.add(allowedKeywordId)
        callback()
        selectedKeys.remove(allowedKeywordId)
    }

    private fun copyKeywordToClipboard() {
        val selectedKeyword = getSelectedItems().firstOrNull() ?: return
        activity.copyToClipboard(selectedKeyword)
        finishActMode()
    }

    private fun deleteSelection() {
        val deleteAllowedKeywords = HashSet<String>(selectedKeys.size)
        val positions = getSelectedItemPositions()

        getSelectedItems().forEach {
            deleteAllowedKeywords.add(it)
            activity.config.removeAllowedKeyword(it)
        }

        allowedKeywords.removeAll(deleteAllowedKeywords)
        removeSelectedItems(positions)
        if (allowedKeywords.isEmpty()) {
            listener?.refreshItems()
        }
    }
}

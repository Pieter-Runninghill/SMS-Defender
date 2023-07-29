package com.simplemobiletools.smsmessenger.activities

import android.os.Bundle
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.beVisibleIf
import com.simplemobiletools.commons.extensions.getProperPrimaryColor
import com.simplemobiletools.commons.extensions.underlineText
import com.simplemobiletools.commons.extensions.updateTextColors
import com.simplemobiletools.commons.helpers.APP_ICON_IDS
import com.simplemobiletools.commons.helpers.APP_LAUNCHER_NAME
import com.simplemobiletools.commons.helpers.NavigationIcon
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.commons.interfaces.RefreshRecyclerViewListener
import com.simplemobiletools.smsmessenger.R
import com.simplemobiletools.smsmessenger.dialogs.AddAllowedKeywordDialog
import com.simplemobiletools.smsmessenger.dialogs.AddBlockedKeywordDialog
import com.simplemobiletools.smsmessenger.dialogs.ManageAllowedKeywordsAdapter
import com.simplemobiletools.smsmessenger.dialogs.ManageBlockedKeywordsAdapter
import com.simplemobiletools.smsmessenger.extensions.config
import com.simplemobiletools.smsmessenger.extensions.toArrayList
import kotlinx.android.synthetic.main.activity_manage_allowed_keywords.*
import kotlinx.android.synthetic.main.activity_manage_blocked_keywords.*

class ManageAllowedKeywordsActivity : BaseSimpleActivity(), RefreshRecyclerViewListener {
    override fun getAppIconIDs() = intent.getIntegerArrayListExtra(APP_ICON_IDS) ?: ArrayList()

    override fun getAppLauncherName() = intent.getStringExtra(APP_LAUNCHER_NAME) ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        isMaterialActivity = true
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_allowed_keywords)
        updateAllowedKeywords()
        setupOptionsMenu()

        updateMaterialActivityViews(allow_keywords_coordinator, manage_allowed_keywords_list, useTransparentNavigation = true, useTopSearchMenu = false)
        setupMaterialScrollListener(manage_allowed_keywords_list, allow_keywords_toolbar)
        updateTextColors(manage_allowed_keywords_wrapper)

        manage_allowed_keywords_placeholder_2.apply {
            underlineText()
            setTextColor(getProperPrimaryColor())
            setOnClickListener {
                addOrEditAllowedKeyword()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setupToolbar(allow_keywords_toolbar, NavigationIcon.Arrow)
    }

    private fun setupOptionsMenu() {
        allow_keywords_toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.add_allowed_keyword -> {
                    addOrEditAllowedKeyword()
                    true
                }

                else -> false
            }
        }
    }

    override fun refreshItems() {
        updateAllowedKeywords()
    }

    private fun updateAllowedKeywords() {
        ensureBackgroundThread {
            val allowedKeywords = config.allowedKeywords
            runOnUiThread {
                ManageAllowedKeywordsAdapter(this, allowedKeywords.toArrayList(), this, manage_allowed_keywords_list) {
                    addOrEditAllowedKeyword(it as String)
                }.apply {
                    manage_allowed_keywords_list.adapter = this
                }

                manage_allowed_keywords_placeholder.beVisibleIf(allowedKeywords.isEmpty())
                manage_allowed_keywords_placeholder_2.beVisibleIf(allowedKeywords.isEmpty())
            }
        }
    }

    private fun addOrEditAllowedKeyword(keyword: String? = null) {
        AddAllowedKeywordDialog(this, keyword) {
            updateAllowedKeywords()
        }
    }
}

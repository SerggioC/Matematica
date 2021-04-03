package com.sergiocruz.matematica.fragment

import android.os.Bundle
import android.widget.LinearLayout
import androidx.annotation.StringRes
import com.sergiocruz.matematica.R

class HomeFragment : BaseFragment() {

    @StringRes
    override var title: Int = R.string.app_inicio

    override var pageIndex: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun getLayoutIdForFragment() = R.layout.fragment_home

    override fun getHelpTextId(): Int? = null

    override fun getHelpMenuTitleId(): Int? = null

    override fun getHistoryLayout(): LinearLayout? = null

    override fun optionsMenu(): Int? = null

}

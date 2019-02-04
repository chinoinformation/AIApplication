package com.example.mitake.aiapplication.character

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.support.v4.view.ViewPager
import com.example.mitake.aiapplication.custom_layout.character.CustomPagerAdapter
import android.support.design.widget.TabLayout
import com.example.mitake.aiapplication.R
import com.example.mitake.aiapplication.data.DataManagement


@Suppress("DEPRECATION", "DEPRECATED_IDENTITY_EQUALS")
class OrganizationFragment : Fragment() {
    private var mViewPager: ViewPager? = null
    private var data: DataManagement? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_organization, container, false)

        val adapter = CustomPagerAdapter(fragmentManager!!)

        mViewPager = root.findViewById(R.id.pager) as ViewPager
        mViewPager!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        mViewPager!!.adapter = adapter

        // プリファレンスで初期ページを読み出し
        data = DataManagement(this.context!!)
        mViewPager!!.currentItem = data!!.readData("organization_page", "0")[0].toInt()

        val tabLayout = root.findViewById(R.id.indicator) as TabLayout
        tabLayout.setupWithViewPager(mViewPager, true)

        return root
    }

    override fun onPause() {
        super.onPause()
        data!!.saveData("organization_page", mViewPager!!.currentItem.toString())
    }

    override fun onDestroy() {
        super.onDestroy()

        mViewPager!!.adapter = null
        mViewPager!!.setBackgroundResource(0)
        mViewPager = null
        data = null
    }

    companion object {
        fun newInstance(): OrganizationFragment {
            // Fragment インスタンス生成
            val organizationFragment = OrganizationFragment()
            return organizationFragment
        }
    }

}

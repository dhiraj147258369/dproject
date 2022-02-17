package com.rsl.foodnairesto.ui.main_screen.tables_and_tabs.add_to_tab

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.rsl.foodnairesto.data.cart.models.CartProductModel
import com.rsl.foodnairesto.data.database_download.models.ProductModel
import com.rsl.foodnairesto.ui.main_screen.tables_and_tabs.add_to_tab.tabs.ProductDescriptionFragment
import com.rsl.foodnairesto.ui.main_screen.tables_and_tabs.add_to_tab.tabs.Type1ModifierFragment
import com.rsl.foodnairesto.ui.main_screen.tables_and_tabs.add_to_tab.tabs.modifier_2.Type2ModifierFragment
import com.rsl.foodnairesto.ui.main_screen.tables_and_tabs.add_to_tab.tabs.modifier_3.Type3ModifierFragment
import com.rsl.foodnairesto.ui.main_screen.tables_and_tabs.edit_cart_product.tabs.EditType2ModifierFragment
import com.rsl.foodnairesto.ui.main_screen.tables_and_tabs.edit_cart_product.tabs.EditType3ModifierFragment
import com.rsl.foodnairesto.utils.AppConstants.API_CART_PRODUCT_ID
import com.rsl.foodnairesto.utils.AppConstants.CATEGORY_ID
import com.rsl.foodnairesto.utils.AppConstants.GROUP_ID
import com.rsl.foodnairesto.utils.AppConstants.PRODUCT_ID

class ProductTabPagerAdapter(
    fm: FragmentManager,
    private val mProductModel: ProductModel?,
    private val mCartProductModel: CartProductModel?,
    private val mType: Int
) :
    FragmentStatePagerAdapter(fm) {

    private val mArray = arrayOf("Modifiers", "Description")

    private var typeFragment : Fragment? = null

    override fun getItem(position: Int): Fragment {

        val mBundle = Bundle()
        if(mType == 1){
            mBundle.putString(GROUP_ID, mProductModel!!.mGroupID)
            mBundle.putString(CATEGORY_ID, mProductModel.mCategoryID)
            mBundle.putString(PRODUCT_ID, mProductModel.mProductID)
        }else{
            mBundle.putString(GROUP_ID, mCartProductModel!!.mProductGroupID)
            mBundle.putString(CATEGORY_ID, mCartProductModel.mProductCategoryID)
            mBundle.putString(PRODUCT_ID, mCartProductModel.mProductID)
            mBundle.putString(API_CART_PRODUCT_ID, mCartProductModel.mCartProductID)
        }


        return when (position) {
            0 -> {

                if(mType == 1){
                    when {
                        mProductModel!!.mProductType == 1 -> typeFragment = Type2ModifierFragment()
                        mProductModel.mProductType == 2 -> typeFragment = Type2ModifierFragment()
                        mProductModel.mProductType == 3 -> typeFragment = Type3ModifierFragment()
                    }

                    typeFragment!!.arguments = mBundle
                    typeFragment!!
                }else {

                    when {
                        mCartProductModel!!.mProductType == 1 -> typeFragment = Type1ModifierFragment()
                        mCartProductModel.mProductType == 2 -> typeFragment = EditType2ModifierFragment()
                        mCartProductModel.mProductType == 3 -> typeFragment = EditType3ModifierFragment()
                    }

                    typeFragment!!.arguments = mBundle
                    typeFragment!!
                }

            }
            1 -> {
                val productDescriptionFragment = ProductDescriptionFragment()
                productDescriptionFragment.arguments = mBundle
                productDescriptionFragment
            }
            else -> {
                Type1ModifierFragment()
            }
        }
    }

    fun getTypeFragment(): Fragment {
        return typeFragment!!
    }

    override fun getCount() = mArray.size

    override fun getPageTitle(position: Int): CharSequence {
        return mArray[position]
    }

}
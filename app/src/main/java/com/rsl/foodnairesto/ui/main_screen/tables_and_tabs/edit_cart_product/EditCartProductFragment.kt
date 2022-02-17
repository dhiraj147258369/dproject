package com.rsl.foodnairesto.ui.main_screen.tables_and_tabs.edit_cart_product


import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log.e
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.rsl.foodnairesto.App
import com.rsl.foodnairesto.R
import com.rsl.foodnairesto.data.cart.models.CartProductModel
import com.rsl.foodnairesto.data.database_download.models.*
import com.rsl.foodnairesto.databinding.DialogSpecialInstructionBinding
import com.rsl.foodnairesto.databinding.FragmentEditCartProductBinding
import com.rsl.foodnairesto.ui.main_screen.MainScreenActivity
import com.rsl.foodnairesto.ui.main_screen.main_product_flow.NewProductViewModel
import com.rsl.foodnairesto.ui.main_screen.tables_and_tabs.add_to_tab.ProductTabPagerAdapter
import com.rsl.foodnairesto.ui.main_screen.tables_and_tabs.add_to_tab.ShowCartEvent
import com.rsl.foodnairesto.ui.main_screen.tables_and_tabs.add_to_tab.tabs.modifier_2.ModifierType2Event
import com.rsl.foodnairesto.ui.main_screen.tables_and_tabs.edit_cart_product.tabs.EditCartDialog
import com.rsl.foodnairesto.utils.AppConstants
import com.rsl.foodnairesto.utils.AppConstants.DIALOG_TYPE_OTHER
import com.rsl.foodnairesto.utils.AppConstants.LOCATION_SERVICE_TYPE
import com.rsl.foodnairesto.utils.AppConstants.SERVICE_QUICK_SERVICE
import com.rsl.foodnairesto.utils.custom_dialog.CustomProgressDialog
import com.rsl.foodnairesto.utils.custom_views.CustomToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass.
 *
 */
@SuppressLint("LogNotTimber")
class EditCartProductFragment : Fragment() {

    private lateinit var mBinding: FragmentEditCartProductBinding
    private lateinit var mSharedPrefs: SharedPreferences
    private lateinit var mTableID: String
    private var mTableNO: Int = 0
    private lateinit var mLocationID: String
    private var mGroupName: String? = null
    private val productViewModel: NewProductViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_cart_product, container, false)
        val mView = mBinding.root

        mSharedPrefs = requireActivity().getSharedPreferences(AppConstants.MY_PREFERENCES, Context.MODE_PRIVATE)
        mTableID = mSharedPrefs.getString(AppConstants.SELECTED_TABLE_ID, "")!!
        mTableNO = mSharedPrefs.getInt(AppConstants.SELECTED_TABLE_NO, 0)
        mLocationID = mSharedPrefs.getString(AppConstants.SELECTED_LOCATION_ID, "")!!
        mGroupName = EditCartProductFragmentArgs.fromBundle(requireArguments()).groupName
        (activity as MainScreenActivity?)?.checktablesfrag()
        e("TAG", "onCreateView: " )
        setupProduct()

        return mView
    }

    private var mQuantity = BigDecimal(1)

    private lateinit var mCartProductModel: CartProductModel
    private lateinit var mTabAdapter: ProductTabPagerAdapter

    private fun setupProduct() {
        val mCartProductID = EditCartProductFragmentArgs.fromBundle(
            requireArguments()
        ).cartProductId

        lifecycleScope.launch {
            mCartProductModel = withContext(Dispatchers.IO) {
                productViewModel.getCartProductByID(mCartProductID)
            }

            e("TAG", "setupProduct: mCartProductModel: $mCartProductModel" )

            mBinding.textViewProductName.text = mCartProductModel.mProductName
            mQuantity = mCartProductModel.mProductQuantity

            when (mCartProductModel.mProductType) {
                2 -> mTotalProductPrice = mCartProductModel.mProductUnitPrice * mQuantity
                1 -> mTotalProductPrice = (mCartProductModel.mProductUnitPrice * mQuantity)
                else -> {}
            }

            mCartProductModel.mShowModifierList?.map {
                mTotalProductPrice += (it.mIngredientPrice * mQuantity)
            }


            val mProductPrice = "${requireActivity().resources.getString(R.string.string_currency_sign)} " +
                    String.format(Locale.ENGLISH, "%.2f", mTotalProductPrice)
            mBinding.textViewProductPrice.text = mProductPrice


            mBinding.textViewProductQuantity.text = String.format("%d", mQuantity.toInt())

            mTabAdapter = ProductTabPagerAdapter(childFragmentManager, null, mCartProductModel, 2)
            mBinding.viewPagerAddToTab.adapter = mTabAdapter

            val limit = when {
                mTabAdapter.count > 1 -> mTabAdapter.count - 1
                else -> 1
            }
            mBinding.viewPagerAddToTab.offscreenPageLimit = limit
        }

        manageQuantity()


        mBinding.imageViewInstruction.setOnClickListener {
            showInstructionPopup()
        }

        mBinding.textViewEditDialogUpdate.setOnClickListener {
           updateProduct()
        }


        mBinding.textViewEditDialogCancel.setOnClickListener {
           if (App.isTablet) {
               ((parentFragment as NavHostFragment).parentFragment as EditCartDialog).dismissDialog()
           } else {
               findNavController().navigate(R.id.cartFragment)
           }
        }

    }

    private var selectedAddOnList = ArrayList<IngredientsModel>()

    private fun manageQuantity() {

        mBinding.imageViewLeftQtyArrow.setOnClickListener {
            if (mQuantity > BigDecimal(1)) {
                mQuantity--
                mBinding.textViewProductQuantity.text = String.format("%d", mQuantity.toInt())
                EventBus.getDefault()
                    .post(ModifierType2Event(mCartProductModel.mProductType, null, null))
            }
        }

        mBinding.imageViewRightQtyArrow.setOnClickListener {
            mQuantity++
            mBinding.textViewProductQuantity.text = String.format("%d", mQuantity.toInt())
            EventBus.getDefault()
                .post(ModifierType2Event(mCartProductModel.mProductType, null, null))
        }

    }

    private var mTotalProductPrice = BigDecimal(0)

    @Subscribe
    fun onProductPriceUpdate(mEvent: ModifierType2Event) {

        if (mEvent.mVariantList != null) selectedAddOnList = mEvent.mVariantList

        when (mEvent.mProductType) {
            2 -> {
                var ingredientPrice = BigDecimal(0.0)
                for (j in 0 until selectedAddOnList.size) {
                    if (selectedAddOnList[j].isSelected) {
                        ingredientPrice += selectedAddOnList[j].mIngredientPrice
                    }
                }

                mTotalProductPrice = (mCartProductModel.mProductUnitPrice + ingredientPrice) * mQuantity

                mTotalProductPrice = mTotalProductPrice.setScale(2, RoundingMode.HALF_UP)
            }
            1 -> when {
                mSharedPrefs.getInt(LOCATION_SERVICE_TYPE, 0) == AppConstants.SERVICE_DINE_IN -> mTotalProductPrice = mCartProductModel.mProductUnitPrice * mQuantity
                mSharedPrefs.getInt(LOCATION_SERVICE_TYPE, 0) == SERVICE_QUICK_SERVICE -> mTotalProductPrice = mCartProductModel.mProductUnitPrice * mQuantity
            }
        }


        val mProductPriceString = "${requireActivity().resources.getString(R.string.string_currency_sign)} " + String.format(
            Locale.ENGLISH,
            "%.2f",
            mTotalProductPrice
        )
        mBinding.textViewProductPrice.text = mProductPriceString
    }

    private fun showInstructionPopup() {

        val mInstructionDialog = Dialog(requireActivity())

        val mInstructionBinding = DataBindingUtil.inflate<DialogSpecialInstructionBinding>(
            LayoutInflater.from(context),
            R.layout.dialog_special_instruction,
            null,
            false
        )

        mInstructionDialog.setContentView(mInstructionBinding.root)


        mInstructionDialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        mInstructionDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        mInstructionDialog.window!!.setWindowAnimations(R.style.DialogScaleAnimation)
        mInstructionDialog.show()

        //show previous special instruction
        mInstructionBinding.editTextSpecialInstructions.setText(mCartProductModel.mSpecialInstruction)
        if (mCartProductModel.mSpecialInstructionPrice > BigDecimal(0))
            mInstructionBinding.editTextSpecialInstructionsPrice.setText(  String.format(
                "%.2f",
                mCartProductModel.mSpecialInstructionPrice
            ))

        mInstructionBinding.textViewApply.setOnClickListener {

            if (mInstructionBinding.editTextSpecialInstructions.text.toString().isEmpty() &&
                mInstructionBinding.editTextSpecialInstructionsPrice.text.toString().isNotEmpty()
            ) {

                CustomToast.makeText(requireActivity(), "Cannot add price without instruction", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            mCartProductModel.mSpecialInstruction = mInstructionBinding.editTextSpecialInstructions.text.toString()

            if (mInstructionBinding.editTextSpecialInstructionsPrice.text.toString().isNotEmpty())
                mCartProductModel.mSpecialInstructionPrice =
                    BigDecimal(mInstructionBinding.editTextSpecialInstructionsPrice.text.toString())
            else
                mCartProductModel.mSpecialInstructionPrice =
                    BigDecimal(0)

            mInstructionDialog.dismiss()
        }

        mInstructionBinding.editTextSpecialInstructionsPrice.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                e(javaClass.simpleName, "afterTextChanged : $s")
                if (s.toString() == ".") {
                    mInstructionBinding.editTextSpecialInstructionsPrice.setText("0.")
                    mInstructionBinding.editTextSpecialInstructionsPrice.setSelection(2)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                /*Not Required*/
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                /*Not Required*/
            }
        })
    }

    private var mProgressDialog: CustomProgressDialog? = null

    private fun updateProduct() {
        mProgressDialog = CustomProgressDialog.newInstance(
            "Updating Product",
            "Please Wait..",
            DIALOG_TYPE_OTHER
        )
        mProgressDialog!!.show(childFragmentManager, AppConstants.CUSTOM_PROGRESS_DIALOG_FRAGMENT)
        mProgressDialog!!.isCancelable = true

        mCartProductModel.mProductTotalPrice = mTotalProductPrice
        mCartProductModel.mProductQuantity = mQuantity
        mCartProductModel.mShowModifierList = selectedAddOnList

        productViewModel.submitCartProduct(mCartProductModel)

        productViewModel.cartData.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let {
                if (it.status){
                    ((parentFragment as NavHostFragment).parentFragment as EditCartDialog).dismissDialog()
                    EventBus.getDefault().post(ShowCartEvent(true))
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }
}

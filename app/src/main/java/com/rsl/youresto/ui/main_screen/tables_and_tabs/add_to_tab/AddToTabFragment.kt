package com.rsl.youresto.ui.main_screen.tables_and_tabs.add_to_tab


import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log.e
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.rsl.youresto.App
import com.rsl.youresto.R
import com.rsl.youresto.data.cart.models.CartProductModel
import com.rsl.youresto.data.database_download.models.*
import com.rsl.youresto.databinding.DialogSpecialInstructionBinding
import com.rsl.youresto.databinding.FragmentAddToTabBinding
import com.rsl.youresto.ui.main_screen.main_product_flow.NewProductViewModel
import com.rsl.youresto.ui.main_screen.tables_and_tabs.AddToTabDialog
import com.rsl.youresto.ui.main_screen.tables_and_tabs.add_to_tab.tabs.modifier_2.ModifierType2Event
import com.rsl.youresto.ui.main_screen.tables_and_tabs.tables.TablesFragment
import com.rsl.youresto.utils.AppConstants
import com.rsl.youresto.utils.AppConstants.LOCATION_SERVICE_TYPE
import com.rsl.youresto.utils.AppConstants.LOGGED_IN_SERVER_ID
import com.rsl.youresto.utils.AppConstants.LOGGED_IN_SERVER_NAME
import com.rsl.youresto.utils.AppConstants.SEAT_SELECTION_ENABLED
import com.rsl.youresto.utils.AppConstants.SELECTED_LOCATION_ID
import com.rsl.youresto.utils.AppConstants.SELECTED_TABLE_ID
import com.rsl.youresto.utils.AppConstants.SELECTED_TABLE_NO
import com.rsl.youresto.utils.AppConstants.SERVICE_DINE_IN
import com.rsl.youresto.utils.AppConstants.SERVICE_QUICK_SERVICE
import com.rsl.youresto.utils.AppPreferences
import com.rsl.youresto.utils.custom_views.CustomToast
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@SuppressLint("LogNotTimber")
class AddToTabFragment : Fragment() {

    private lateinit var mBinding: FragmentAddToTabBinding
    private lateinit var mSharedPrefs: SharedPreferences
    private lateinit var mTableID: String
    private var mTableNO: Int = 0
    private lateinit var mLocationID: String
    private var isSeatSelectionEnabled: Boolean = false
    private var mSelectedLocationType: Int? = null

    private var isTablet: Boolean = false
    private val productViewModel: NewProductViewModel by viewModel()
    private val prefs: AppPreferences by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_to_tab, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isTablet = resources.getBoolean(R.bool.isTablet)

        mSharedPrefs = requireActivity().getSharedPreferences(AppConstants.MY_PREFERENCES, Context.MODE_PRIVATE)

        isSeatSelectionEnabled = mSharedPrefs.getBoolean(SEAT_SELECTION_ENABLED, false)
        mSelectedLocationType = mSharedPrefs.getInt(LOCATION_SERVICE_TYPE, 0)

        mLocationID = mSharedPrefs.getString(SELECTED_LOCATION_ID, "")!!

        mTableID = mSharedPrefs.getString(SELECTED_TABLE_ID, "")!!
        mTableNO = mSharedPrefs.getInt(SELECTED_TABLE_NO, 0)

        setupProduct()
    }

    private lateinit var mProductModel: ProductModel
    private lateinit var mTabAdapter: ProductTabPagerAdapter

    private var mQuantity = BigDecimal(1)

    private var mGroupID = ""
    private var mCategoryID = ""

    private fun setupProduct() {

        val mProductID = AddToTabFragmentArgs.fromBundle(requireArguments()).productId
        mCategoryID = AddToTabFragmentArgs.fromBundle(requireArguments()).categoryId
        mGroupID = AddToTabFragmentArgs.fromBundle(requireArguments()).groupId

        productViewModel.getProduct(mProductID).observe(viewLifecycleOwner) {
            it?.let {
                mProductModel = it

                mBinding.textViewProductName.text = mProductModel.mProductName

                val mProductPrice: String = "${requireActivity().resources.getString(R.string.string_currency_sign)} " + String.format(
                            Locale.ENGLISH,
                            "%.2f",
                            mProductModel.mDineInPrice
                        )

                mBinding.textViewProductPrice.text = mProductPrice

                mTabAdapter = ProductTabPagerAdapter(childFragmentManager, mProductModel, null, 1)
                mBinding.viewPagerAddToTab.adapter = mTabAdapter

                val limit =
                    when {
                        mTabAdapter.count > 1 -> mTabAdapter.count - 1
                        else -> 1
                    }
                mBinding.viewPagerAddToTab.offscreenPageLimit = limit
            }
        }

        mBinding.textViewModifierDialogCancel.setOnClickListener {
            when (mSelectedLocationType) {
                SERVICE_DINE_IN -> goToDineInMainProducts()
                SERVICE_QUICK_SERVICE -> goToQuickMainProducts()
            }
        }

        mBinding.textViewModifierDialogAddToTab.setOnClickListener {

            mBinding.textViewModifierDialogAddToTab.isEnabled = false

            Handler(Looper.getMainLooper()).postDelayed({
                mBinding.textViewModifierDialogAddToTab.isEnabled = true
            }, 1000)

            addProduct()

        }

        mBinding.imageViewInstruction.setOnClickListener {
            showInstructionPopup()
            mBinding.imageViewInstruction.isEnabled = false
        }

        manageQuantity()
    }


    private fun addProduct() {

        val mProductName = mProductModel.mProductName
        val mProductID = mProductModel.mProductID

        val mProductUnitPrice = when (mSelectedLocationType) {
            SERVICE_DINE_IN -> mProductModel.mDineInPrice
            SERVICE_QUICK_SERVICE -> mProductModel.mDineInPrice
            else -> mProductModel.mDeliveryPrice
        }

        val now = Calendar.getInstance()
        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH)
        val nowTime = simpleDateFormat.format(now.time)

        var cartId = "0"

        if (mSelectedLocationType == SERVICE_QUICK_SERVICE){
            cartId = prefs.selectedQuickServiceCartId()
        }

        mTotalProductPrice = (mProductUnitPrice * mQuantity)

        selectedAddOnList.map { addOn ->
            mTotalProductPrice += addOn.mIngredientPrice * mQuantity
        }

        val mCartProduct = CartProductModel(
            mSharedPrefs.getString(SELECTED_LOCATION_ID, "")!!,
            mTableID,
            mTableNO,
            "z",
            0,
            ArrayList(),
            cartId,
            cartId,
            "",
            mSharedPrefs.getInt(LOCATION_SERVICE_TYPE, 1),
            mSharedPrefs.getString(LOGGED_IN_SERVER_ID, "")!!,
            mSharedPrefs.getString(LOGGED_IN_SERVER_NAME, "")!!,
            mProductID,
            mProductModel.mCategoryID ?: "",
            mProductModel.mCategoryName,
            mProductModel.mGroupID ?: "",
            mProductModel.mGroupName,
            mProductModel.mGroupName,
            mProductModel.mProductType,
            mProductName,
            mProductUnitPrice,
            mQuantity,
            mProductTotalPrice = mTotalProductPrice,
            mSpecialInstruction,
            mSpecialInstructionPrice,
            ArrayList(),
            selectedAddOnList,
            ArrayList(),
            ArrayList(),
            Date(),
            nowTime,
            0,
            mProductModel.mPrinterID,
            0,
            true,
            taxName = mProductModel.taxName,
            taxPercentage = mProductModel.taxPercentage.toDouble()
        )

        productViewModel.submitCartProduct(mCartProduct)

        productViewModel.cartData.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let {
                if (it.status){
                    if (App.isTablet){
                        ((parentFragment as? NavHostFragment)?.parentFragment as? AddToTabDialog)?.dismissDialog()
                        EventBus.getDefault().post(ShowCartEvent(true))
                    } else {
                        findNavController().navigateUp()
                    }
                } else {
                    CustomToast.makeText(requireActivity(), it.msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun manageQuantity() {
        mBinding.textViewProductQuantity.text = String.format("%d", mQuantity.toInt())

        mBinding.imageViewLeftQtyArrow.setOnClickListener {
            if (mQuantity > BigDecimal(1)) {
                mQuantity--
                mBinding.textViewProductQuantity.text = String.format("%d", mQuantity.toInt())
                EventBus.getDefault()
                    .post(ModifierType2Event(mProductModel.mProductType, null, null))
            }
        }

        mBinding.imageViewRightQtyArrow.setOnClickListener {
            mQuantity++
            mBinding.textViewProductQuantity.text = String.format("%d", mQuantity.toInt())
            EventBus.getDefault().post(ModifierType2Event(mProductModel.mProductType, null, null))
        }


    }

    private var mTotalProductPrice = BigDecimal(0)
    private var selectedAddOnList = ArrayList<IngredientsModel>()

    @Subscribe
    fun onProductPriceUpdate(mEvent: ModifierType2Event) {

        if (mEvent.mVariantList != null) selectedAddOnList = mEvent.mVariantList

        when (mEvent.mProductType) {
            2 ->
            {
                var ingredientPrice = BigDecimal(0.0)
                for (j in 0 until selectedAddOnList.size) {
                    if (selectedAddOnList[j].isSelected) {
                        ingredientPrice += selectedAddOnList[j].mIngredientPrice
                    }
                }

                mTotalProductPrice = (mProductModel.mDineInPrice + ingredientPrice) * mQuantity

                mTotalProductPrice = mTotalProductPrice.setScale(2, RoundingMode.HALF_UP)
            }
            1 ->
                when {
                    mSharedPrefs.getInt(LOCATION_SERVICE_TYPE, 0) == SERVICE_DINE_IN -> mTotalProductPrice =
                        mProductModel.mDineInPrice * mQuantity
                    mSharedPrefs.getInt(LOCATION_SERVICE_TYPE, 0) == SERVICE_QUICK_SERVICE -> mTotalProductPrice =
                        mProductModel.mQuickServicePrice * mQuantity
                }
        }


        val mProductPriceString = "${requireActivity().resources.getString(R.string.string_currency_sign)} " + String.format(
            Locale.ENGLISH,
            "%.2f",
            mTotalProductPrice
        )
        mBinding.textViewProductPrice.text = mProductPriceString
    }


    private var mSpecialInstruction = ""
    private var mSpecialInstructionPrice = BigDecimal(0)

    private fun showInstructionPopup() {
        val mInstructionDialog = Dialog(requireActivity())
        val mInstructionBinding = DataBindingUtil.inflate<DialogSpecialInstructionBinding>(
            LayoutInflater.from(context),
            R.layout.dialog_special_instruction,
            null,
            false
        )

        mInstructionDialog.setContentView(mInstructionBinding.root)

        mInstructionDialog.window!!.setLayout(MATCH_PARENT, MATCH_PARENT)
        mInstructionDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        mInstructionDialog.window!!.setWindowAnimations(R.style.DialogScaleAnimation)
        mInstructionDialog.show()
        mInstructionDialog.setOnCancelListener { mBinding.imageViewInstruction.isEnabled = true }

        //show previous special instruction
        mInstructionBinding.editTextSpecialInstructions.setText(mSpecialInstruction)
        if (mSpecialInstructionPrice > BigDecimal(0))
            mInstructionBinding.editTextSpecialInstructionsPrice.setText(
                String.format(
                    "%.2f",
                    mSpecialInstructionPrice
                )
            )

        mInstructionBinding.textViewApply.setOnClickListener {


            if (mInstructionBinding.editTextSpecialInstructions.text.toString().isEmpty() &&
                mInstructionBinding.editTextSpecialInstructionsPrice.text.toString().isNotEmpty()
            ) {

                CustomToast.makeText(requireActivity(), "Cannot add price without instruction", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            mSpecialInstruction = mInstructionBinding.editTextSpecialInstructions.text.toString()

            mSpecialInstructionPrice = BigDecimal(0)
            if (mInstructionBinding.editTextSpecialInstructionsPrice.text.toString().isNotEmpty())
                mSpecialInstructionPrice =
                    BigDecimal(mInstructionBinding.editTextSpecialInstructionsPrice.text.toString())

            mInstructionDialog.dismiss()

            mBinding.imageViewInstruction.isEnabled = true
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

    private fun goToDineInMainProducts() {
        if (isTablet) {
            ((parentFragment as NavHostFragment).parentFragment as AddToTabDialog).dismissDialog()
        } else {
            val action = AddToTabFragmentDirections.actionAddToTabFragmentToMainProductFragment(
                mGroupID,
                mCategoryID,
                TablesFragment::class.java.simpleName
            )
            findNavController().navigate(action)
        }
    }

    private fun goToQuickMainProducts() {
        if (isTablet) {
            ((parentFragment as NavHostFragment).parentFragment as AddToTabDialog).dismissDialog()
        } else {
            val action = AddToTabFragmentDirections.actionAddToTabFragmentToQuickServiceFragment(
                mGroupID,
                mCategoryID
            )
            findNavController().navigate(action)
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

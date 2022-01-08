package com.rsl.youresto.ui.main_screen.cart


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log.e
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.rsl.youresto.App
import com.rsl.youresto.R
import com.rsl.youresto.data.cart.models.CartProductModel
import com.rsl.youresto.databinding.FragmentCartBinding
import com.rsl.youresto.ui.main_screen.cart.adapter.CartRecyclerAdapter
import com.rsl.youresto.ui.main_screen.cart.event.*
import com.rsl.youresto.ui.main_screen.checkout.CheckoutDialog
import com.rsl.youresto.ui.main_screen.checkout.events.DrawerEvent
import com.rsl.youresto.ui.main_screen.checkout.payment_options.events.PaymentCompletedEvent
import com.rsl.youresto.ui.main_screen.estimate_bill_print.EstimateBillPrint50Activity
import com.rsl.youresto.ui.main_screen.kitchen_print.KitchenPrint50Activity
import com.rsl.youresto.ui.main_screen.kitchen_print.KitchenPrint80Activity
import com.rsl.youresto.ui.main_screen.tables_and_tabs.edit_cart_product.tabs.EditCartDialog
import com.rsl.youresto.ui.tab_specific.QuickServiceTabFragment
import com.rsl.youresto.ui.tab_specific.TablesTabFragment
import com.rsl.youresto.utils.*
import com.rsl.youresto.utils.AppConstants.CUSTOM_DIALOG_FRAGMENT
import com.rsl.youresto.utils.AppConstants.GROUP_NAME
import com.rsl.youresto.utils.AppConstants.PAPER_SIZE_50
import com.rsl.youresto.utils.AppConstants.SELECTED_GROUP_NAME
import com.rsl.youresto.utils.AppConstants.SERVICE_DINE_IN
import com.rsl.youresto.utils.AppConstants.SERVICE_QUICK_SERVICE
import com.rsl.youresto.utils.custom_dialog.AlertDialogEvent
import com.rsl.youresto.utils.custom_dialog.CustomAlertDialogFragment
import com.rsl.youresto.utils.custom_views.CustomToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.math.BigDecimal
import java.util.*
import kotlin.collections.ArrayList


/**
 * A simple [Fragment] subclass.
 *
 */
@SuppressLint("LogNotTimber")
class CartFragment : Fragment() {

    private lateinit var mBinding: FragmentCartBinding
    private var mGroupName: String? = null
    private lateinit var mTableID: String
    private var mTableNO: Int = 0
    private var mCartNO: String? = null
    private var mSelectedLocationType: Int? = null

    private val cartViewModel: NewCartViewModel by viewModel()
    private val prefs: AppPreferences by inject()

    companion object {
        fun newInstance(mGroupName: String, mSelectedGroupName: String): CartFragment {
            val mBundle = Bundle()
            mBundle.putString(GROUP_NAME, mGroupName)
            mBundle.putString(SELECTED_GROUP_NAME, mSelectedGroupName)
            val cartFragment = CartFragment()
            cartFragment.arguments = mBundle

            return cartFragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_cart, container, false)
        val mView = mBinding.root

        mSelectedLocationType = prefs.getLocationServiceType()
        mTableID = prefs.getSelectedTableId()
        mTableNO = prefs.getSelectedTableNo()

        getCartDetails()
        initViews()

        return mView
    }

    private var mActionBarHiddenState = true

    private fun initViews() {
        mBinding.imageViewAction.setOnClickListener {
            if (mBinding.constraintLayoutActionOptions.isVisible) {
                hideActionBarOptions()
            } else {
                showActionBarOptions()
            }
        }

        if (!App.isTablet) {
            mBinding.imageViewAddProduct.visibility = VISIBLE
            mBinding.imageViewAddProduct.setOnClickListener { findNavController().navigate(
                if (mSelectedLocationType == SERVICE_QUICK_SERVICE) R.id.quickServiceFragment
                else R.id.mainProductFragment
            ) }
        }
    }

    private fun hideActionBarOptions() {
        Animations.rotateAntiClockwise(mBinding.imageViewAction)
        mBinding.constraintLayoutActionOptions.visibility = View.INVISIBLE
        mBinding.viewBackgroundBlur.visibility = View.INVISIBLE
        Animations.slideDown(mBinding.constraintLayoutActionOptions)
        mActionBarHiddenState = true
        Utils.disableAllViews(mBinding.recyclerViewCartList, false)
        EventBus.getDefault().post(CartFragmentTouchEvent(false))
        EventBus.getDefault().post(DrawerEvent(false, javaClass.simpleName))
        mBinding.imageViewAddProduct.isEnabled = true
    }

    private fun showActionBarOptions() {
        Animations.rotateClockwise(mBinding.imageViewAction)
        mBinding.constraintLayoutActionOptions.visibility = View.VISIBLE
        mBinding.viewBackgroundBlur.visibility = View.VISIBLE
        Animations.slideUp(mBinding.constraintLayoutActionOptions)
        mActionBarHiddenState = false
        Utils.disableAllViews(mBinding.recyclerViewCartList, true)
        EventBus.getDefault().post(CartFragmentTouchEvent(true))
        EventBus.getDefault().post(DrawerEvent(true, javaClass.simpleName))
        mBinding.imageViewAddProduct.isEnabled = false
    }

    private var mCartAdapter: CartRecyclerAdapter? = null
    private var cartId = ""

    private fun getCartDetails() {

        if (mSelectedLocationType == SERVICE_DINE_IN) {
            cartViewModel.getCarts(mTableID).observe(viewLifecycleOwner) {
                    loadAdapter(it)
                }
        } else if (mSelectedLocationType == SERVICE_QUICK_SERVICE){
            val cartId = prefs.selectedQuickServiceCartId()
            cartViewModel.getCartDataById(cartId).observe(viewLifecycleOwner) {
                    loadAdapter(it)
                }
        }




        cartViewModel.deleteCartData.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                if (!App.isTablet){
                    findNavController().navigate(R.id.tablesFragment)
                }
            }
        }

        mBinding.constraintLayoutOptionDone.setOnClickListener {
            kotSend()
            if (mBinding.constraintLayoutActionOptions.isVisible) hideActionBarOptions()
        }

        mBinding.constraintLayoutOptionClose.setOnClickListener {
            close()
            if (mBinding.constraintLayoutActionOptions.isVisible) hideActionBarOptions()
        }

        mBinding.constraintLayoutOptionPrint.setOnClickListener {
            estimateBillPrint()
            if (mBinding.constraintLayoutActionOptions.isVisible) hideActionBarOptions()
        }
    }

    private fun loadAdapter(it: List<CartProductModel>) {
        if (it.isNotEmpty()) {
            mCartAdapter = CartRecyclerAdapter(requireActivity(), ArrayList(it))
            mBinding.recyclerViewCartList.adapter = mCartAdapter

            cartId = it[0].mCartNO
            val mCartNOString = "Cart ID: ${it[0].mCartNO}"
            mBinding.textViewCartOrderNo.text = mCartNOString

            var mCartTotal = BigDecimal(0.0)
            for (element in it)
                mCartTotal += element.mProductTotalPrice

            mBinding.textViewCartTotalCartAmount.text =
                String.format(Locale.ENGLISH, "%.2f", mCartTotal)
        } else {
            if (mSelectedLocationType == SERVICE_DINE_IN){
                ((parentFragment as? NavHostFragment)?.parentFragment as? TablesTabFragment)?.hideCart()
            } else {
                ((parentFragment as? NavHostFragment)?.parentFragment as? QuickServiceTabFragment)?.hideCart()
            }
        }
    }

    private fun estimateBillPrint() {
        val mEstimate50Intent = Intent(requireActivity(), EstimateBillPrint50Activity::class.java)
        mEstimate50Intent.putExtra(AppConstants.TABLE_NO, mTableNO)
        mEstimate50Intent.putExtra(AppConstants.ORDER_NO, mCartNO)
//        mEstimate50Intent.putExtra(AppConstants.API_CART_ID, mCartID)
        if (mTableNO != 100)
            mEstimate50Intent.putExtra(AppConstants.ORDER_TYPE, 1)
        else
            mEstimate50Intent.putExtra(AppConstants.ORDER_TYPE, 2)

        startActivity(mEstimate50Intent)
    }

    private fun kotSend() {

        if (prefs.getKitchenPrintEnableFlag()){
            lifecycleScope.launch {
                val kitchens = withContext(Dispatchers.IO){
                    cartViewModel.getKitchens()
                }

                if (kitchens.isNotEmpty()){
                    val mKitchenPrintIntent = if (kitchens[0].mSelectedKitchenPrinterSize == PAPER_SIZE_50){
                        Intent(requireActivity(), KitchenPrint50Activity::class.java)
                    } else {
                        Intent(requireActivity(), KitchenPrint80Activity::class.java)
                    }
                    mKitchenPrintIntent.putExtra("CART_ID", cartId)
                    startActivity(mKitchenPrintIntent)
                }
            }
        } else {
            CustomToast.makeText(requireActivity(), "Please enable kitchen print from settings", Toast.LENGTH_SHORT).show()
        }
    }

    private fun close() {
        val dialog = CheckoutDialog()
        dialog.isCancelable = true
        dialog.show(childFragmentManager, "CheckoutDialog")
    }

    @Subscribe
    fun checkInternetConnectionOnProductQtyChange(mEvent: CheckNetworkCartEvent) {

        if (mEvent.mCartModel.mGroupName != mGroupName) {
            return
        }

        Network.isNetworkAvailableWithInternetAccess(requireActivity()).observe(viewLifecycleOwner,
            {
                if (it) mCartAdapter!!.onNetworkChecked(true, mEvent.mPosition, mEvent.mChangeType, mEvent.mCartModel)
                else CustomToast.makeText(requireActivity(), getString(R.string.network_error), Toast.LENGTH_SHORT).show()
            })
    }

    @Subscribe
    fun onProductQuantityChanged(mEvent: UpdateCartProductQuantityEvent) {
        cartViewModel.updateQuantity(mEvent.mRowID, mEvent.mQuantity, mEvent.mTotalProductPrice, mTableID)
    }

    @Subscribe
    fun onCartProductNameClick(mEvent: EditCartProductClickEvent) {
        if (App.isTablet) {
            val dialog = EditCartDialog(mEvent.mCartModel.mCartProductID)
            dialog.isCancelable = true
            dialog.show(childFragmentManager, "EditCartDialog")
        } else {
            //todo: editCart fragment
        }

    }

    private var mCartModel: CartProductModel? = null
    private var mCartProductPosition = -1

    @Subscribe
    fun onCartProductDelete(mEvent: DeleteCartProductEvent) {

        mCartModel = mEvent.mCartModel
        mCartProductPosition = mEvent.mPosition

        val mCustomDialogFragment = CustomAlertDialogFragment.newInstance(
            1, javaClass.simpleName, R.drawable.ic_delete_forever_primary_36dp,
            "Delete Product", "Are you sure you want to delete this product?",
            "Yes, Delete", "No, Don't", R.drawable.ic_check_black_24dp, R.drawable.ic_close_black_24dp
        )
        mCustomDialogFragment.show(childFragmentManager, CUSTOM_DIALOG_FRAGMENT)

    }

    @Subscribe
    fun onAlertDialogEvent(mEvent: AlertDialogEvent) {

        e(javaClass.simpleName, "mEvent: AlertDialogEvent")
        if (mEvent.mSource == javaClass.simpleName && mEvent.mActionType)
            cartViewModel.deleteCartItem(mCartModel!!)
    }

    @Subscribe
    fun refreshCart(mEvent: RefreshCartEvent) {
        e(javaClass.simpleName, "mEvent: ${mEvent.mGroupName}")
        getCartDetails()
    }

    @Subscribe
    fun paymentComplete(mEvent: PaymentCompletedEvent) {
        findNavController().navigate(R.id.tablesFragment)
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

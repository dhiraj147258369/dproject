package com.rsl.youresto.ui.main_screen.checkout.payment_options


import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log.e
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.rsl.youresto.R
import com.rsl.youresto.data.checkout.model.CheckoutModel
import com.rsl.youresto.data.database_download.models.PaymentMethodModel
import com.rsl.youresto.databinding.DialogSelectWalletBinding
import com.rsl.youresto.databinding.FragmentPaymentMethodBinding
import com.rsl.youresto.ui.main_screen.app_settings.AppSettingsViewModel
import com.rsl.youresto.ui.main_screen.checkout.CheckoutViewModel
import com.rsl.youresto.ui.main_screen.checkout.CheckoutViewModelFactory
import com.rsl.youresto.utils.AppConstants
import com.rsl.youresto.utils.AppConstants.CARD
import com.rsl.youresto.utils.AppConstants.CASH
import com.rsl.youresto.utils.AppConstants.QUICK_SERVICE_CART_ID
import com.rsl.youresto.utils.AppConstants.QUICK_SERVICE_TABLE_ID
import com.rsl.youresto.utils.AppConstants.SELECTED_TABLE_ID
import com.rsl.youresto.utils.AppConstants.SERVICE_CHARGE
import com.rsl.youresto.utils.AppConstants.TIP
import com.rsl.youresto.utils.AppConstants.VOUCHER
import com.rsl.youresto.utils.AppConstants.WALLET
import com.rsl.youresto.utils.AppConstants.ZERO_SEAT_LIST_COUNT
import com.rsl.youresto.utils.InjectorUtils
import com.rsl.youresto.utils.custom_dialog.CustomAlertDialogFragment
import com.rsl.youresto.utils.custom_views.CustomToast
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.math.BigDecimal

/**
 * A simple [Fragment] subclass.
 *
 */
class PaymentMethodFragment : Fragment() {

    private lateinit var mBinding: FragmentPaymentMethodBinding
    private lateinit var mCheckoutViewModel: CheckoutViewModel
    private lateinit var mAppSettingViewModel: AppSettingsViewModel
    private lateinit var mSharedPrefs: SharedPreferences
    private var mGroupName = ""
    private var mSelectedLocationType: Int? = null
    private var mTableNO = 0
    private var mTableID = ""

    companion object{
        private const val PAYMENT_ALERT = "Payment Alert!"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_payment_method, container, false)
        val mView = mBinding.root

        mSharedPrefs = requireActivity().getSharedPreferences(AppConstants.MY_PREFERENCES, Context.MODE_PRIVATE)

        mSelectedLocationType = mSharedPrefs.getInt(AppConstants.LOCATION_SERVICE_TYPE, 0)

        val checkoutFactory: CheckoutViewModelFactory = InjectorUtils.provideCheckoutViewModelFactory(requireActivity())
        mCheckoutViewModel = ViewModelProviders.of(this, checkoutFactory).get(CheckoutViewModel::class.java)

        val factory = InjectorUtils.provideAppSettingsViewModelFactory(requireActivity())
        mAppSettingViewModel = ViewModelProviders.of(this, factory).get(AppSettingsViewModel::class.java)

        if (mSelectedLocationType == AppConstants.SERVICE_DINE_IN) {
            mTableID = mSharedPrefs.getString(SELECTED_TABLE_ID, "")!!
            mTableNO = mSharedPrefs.getInt(AppConstants.SELECTED_TABLE_NO, 0)
            mGroupName = mSharedPrefs.getString(AppConstants.GROUP_NAME, "")!!
        } else if (mSelectedLocationType == AppConstants.SERVICE_QUICK_SERVICE) {
            mTableID = mSharedPrefs.getString(QUICK_SERVICE_TABLE_ID, "")!!
            mTableNO = mSharedPrefs.getInt(AppConstants.QUICK_SERVICE_TABLE_NO, 0)
            mGroupName = "Q"
        }

        setPaymentMethodRecyclerView()
        setOtherMethodRecyclerView()

        return mView
    }

    private var mPaymentObserver: Observer<List<PaymentMethodModel>>? = null

    private fun setPaymentMethodRecyclerView() {
        val mPaymentMethodData = mCheckoutViewModel.getPaymentMethods()
        mPaymentObserver = Observer {
            if (it.isNotEmpty()) {
                val mPaymentMethodList: ArrayList<PaymentMethodModel> = ArrayList()
                for (i in 0 until it.size) {
                    mPaymentMethodList.add(it[i])
                }

                val mPaymentAdapter = PaymentMethodAdapter(mPaymentMethodList)
                mBinding.recyclerViewPaymentMethods.layoutManager = GridLayoutManager(requireActivity(), 4)
                mBinding.recyclerViewPaymentMethods.adapter = mPaymentAdapter

                mPaymentMethodData.removeObserver(mPaymentObserver!!)
            }
        }
        mPaymentMethodData.observe(viewLifecycleOwner, mPaymentObserver!!)
    }

    private fun setOtherMethodRecyclerView() {
        val mOtherMethodList: ArrayList<PaymentMethodModel> = ArrayList()
        val paymentMethodModel = PaymentMethodModel("1", SERVICE_CHARGE, 5, R.drawable.ic_payment)
        mOtherMethodList.add(paymentMethodModel)

        val mPaymentAdapter = PaymentMethodAdapter(mOtherMethodList)
        mBinding.recyclerViewOthers.layoutManager = GridLayoutManager(requireActivity(), 4)
        mBinding.recyclerViewOthers.adapter = mPaymentAdapter
    }

    @Subscribe
    fun onPaymentMethodClick(mMethod: PaymentMethodModel) {
        when {
            mMethod.mPaymentMethodName.equals(CASH, ignoreCase = true) -> {
                findNavController().navigate(R.id.cashFragment2)
//                Navigation.findNavController(
//                    requireActivity(),
//                    R.id.checkout_host_fragment
//                ).navigate(R.id.action_paymentMethodFragment_to_cashFragment)
            }
            mMethod.mPaymentMethodName.contains(CARD, ignoreCase = true) -> {
                findNavController().navigate(R.id.cardFragment2)
//                mAppSettingViewModel.getPaymentDevice(mSharedPrefs.getString(SELECTED_LOCATION_ID,"")!!).observe(viewLifecycleOwner,
//                    {
//                        if(it != null) {
//                            Navigation.findNavController(requireActivity(), R.id.checkout_host_fragment)
//                                .navigate(R.id.action_paymentMethodFragment_to_cardFragment)
//                        } else {
//                            CustomToast.makeText(requireActivity(), "Select payment terminal device from general settings to continue", Toast.LENGTH_SHORT).show()
//                        }
//                    })
            }
            mMethod.mPaymentMethodName.equals(WALLET, ignoreCase = true) -> openWalletDialog()
            mMethod.mPaymentMethodName.equals(TIP, ignoreCase = true) -> checkTip().observe(viewLifecycleOwner,
                { tipApplied ->
                    if (!tipApplied) {

                        Navigation.findNavController(
                            requireActivity(),
                            R.id.checkout_host_fragment
                        ).navigate(R.id.action_paymentMethodFragment_to_tipFragment)
                    }
                })
            mMethod.mPaymentMethodName.equals(SERVICE_CHARGE, ignoreCase = true) -> Navigation.findNavController(
                requireActivity(),
                R.id.checkout_host_fragment
            ).navigate(R.id.action_paymentMethodFragment_to_serviceChargeFragment)
            mMethod.mPaymentMethodName.equals(VOUCHER, ignoreCase = true) -> CustomToast.makeText(
                requireActivity(),
                "Voucher payment is not available now",
                Toast.LENGTH_SHORT
            ).show()
            else -> {
                CustomToast.makeText(
                    requireActivity(),
                    "Not implemented yet",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
    }

    private var mCheckoutObserver: Observer<CheckoutModel>? = null

    private fun checkTip(): LiveData<Boolean> {

        val mCheckoutTipData = MutableLiveData<Boolean>()

        val mCheckoutRowID = mSharedPrefs.getInt(AppConstants.CHECKOUT_ROW_ID, 0)
        val mCheckoutData = mCheckoutViewModel.getCheckoutDataByRowID(mCheckoutRowID)

        mCheckoutObserver = Observer {
            if (it != null) {
                if (it.mCheckoutTransaction[it.mCheckoutTransaction.size - 1].mAmountPaid > BigDecimal(0)) {
                    CustomToast.makeText(
                        requireActivity(),
                        "Some amount is paid already, cannot apply Tip now",
                        Toast.LENGTH_SHORT
                    ).show()
                    mCheckoutTipData.postValue(true)
                } else {
                    mCheckoutTipData.postValue(false)
                }

                mCheckoutData.removeObserver(mCheckoutObserver!!)
            }
        }

        mCheckoutData.observe(viewLifecycleOwner, mCheckoutObserver!!)

        return mCheckoutTipData
    }


    private var mWalletDialog: Dialog? = null
    private lateinit var mWalletBinding: DialogSelectWalletBinding

    @SuppressLint("LogNotTimber")
    private fun checkPaymentTypeToProceedWithYoyo() {
        when (mSelectedLocationType) {
            AppConstants.SERVICE_DINE_IN -> mCheckoutViewModel.getCheckoutDataByTableAndGroup(mTableID, mGroupName).observe(viewLifecycleOwner,
                {
                    when {
                        it != null ->
                            when {
                                it.mCheckoutTransaction.size == 1 && it.mCheckoutTransaction[0].mPaymentTransaction.size == 0 -> {

                                    e(javaClass.simpleName,"ZERO_SEAT_LIST_COUNT: ${mSharedPrefs.getInt(ZERO_SEAT_LIST_COUNT, 0)}")

                                    val mSeatListSize =
                                        it.mCheckoutTransaction[0].mSeatList.size + mSharedPrefs.getInt(ZERO_SEAT_LIST_COUNT, 0)

                                    mCheckoutViewModel.getTableGroupAndSeats(mTableNO, mGroupName)
                                        .observe(viewLifecycleOwner, { localTableGroupModel ->
                                            when {
                                                localTableGroupModel != null ->
                                                    when (mSeatListSize) {
                                                        localTableGroupModel.mSeatList!!.size -> Navigation.findNavController(requireActivity(), R.id.checkout_host_fragment)
                                                            .navigate(R.id.action_paymentMethodFragment_to_yoyoWalletFragment)
                                                        else -> {
                                                            val mCustomDialogFragment = CustomAlertDialogFragment.newInstance(
                                                                1,
                                                                javaClass.simpleName,
                                                                R.drawable.ic_delete_forever_primary_36dp,
                                                                PAYMENT_ALERT,
                                                                "Partial seat wise payment is not allowed with yoyo wallet",
                                                                "Okay",
                                                                "",
                                                                R.drawable.ic_close_black_24dp,
                                                                0
                                                            )
                                                            mCustomDialogFragment.show(childFragmentManager, AppConstants.CUSTOM_DIALOG_FRAGMENT)
                                                        }
                                                    }
                                            }
                                        })
                                }
                                else -> {
                                    val mCustomDialogFragment = CustomAlertDialogFragment.newInstance(
                                        2,
                                        javaClass.simpleName,
                                        R.drawable.ic_delete_forever_primary_36dp,
                                        PAYMENT_ALERT,
                                        "Some amount already paid for this order, you cannot pay with yoyo wallet",
                                        "",
                                        "Okay",
                                        0,
                                        R.drawable.ic_close_black_24dp
                                    )
                                    mCustomDialogFragment.show(childFragmentManager, AppConstants.CUSTOM_DIALOG_FRAGMENT)
                                }
                            }
                    }
                })
            AppConstants.SERVICE_QUICK_SERVICE -> mCheckoutViewModel.getCheckoutDataByTableAndCartID(mSharedPrefs.getString(QUICK_SERVICE_TABLE_ID,"")!!,
                mSharedPrefs.getString(QUICK_SERVICE_CART_ID,"")!!).observe(viewLifecycleOwner, {

                when {
                    it != null -> {
                        e(javaClass.simpleName,"it.mCheckoutTransaction[0].mPaymentTransaction.size: ${it.mCheckoutTransaction[0].mPaymentTransaction.size}")
                        when {
                            it.mCheckoutTransaction.size == 1 && it.mCheckoutTransaction[0].mPaymentTransaction.size == 0 -> Navigation.findNavController(requireActivity(), R.id.checkout_host_fragment)
                                .navigate(R.id.yoyoWalletFragment)
                            else -> {
                                val mCustomDialogFragment = CustomAlertDialogFragment.newInstance(
                                    3,
                                    javaClass.simpleName,
                                    R.drawable.ic_delete_forever_primary_36dp,
                                    PAYMENT_ALERT,
                                    "Some amount already paid for this order, you cannot pay with yoyo wallet",
                                    "",
                                    "Okay",
                                    0,
                                    R.drawable.ic_close_black_24dp
                                )
                                mCustomDialogFragment.show(childFragmentManager, AppConstants.CUSTOM_DIALOG_FRAGMENT)
                            }
                        }
                    }
                }
            })
        }

    }

    private fun openWalletDialog() {
        mWalletDialog = Dialog(requireActivity())

        mWalletBinding =
            DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_select_wallet, null, false)
        mWalletDialog!!.setContentView(mWalletBinding.root)

        mWalletBinding.buttonYoyoWallet.setOnClickListener {
            checkPaymentTypeToProceedWithYoyo()
            mWalletDialog!!.dismiss()
        }

        mWalletBinding.buttonPaytmWallet.setOnClickListener {
            mWalletDialog!!.dismiss()
            CustomToast.makeText(requireActivity(), "This wallet is coming soon", Toast.LENGTH_SHORT).show()
        }

        mWalletDialog!!.show()
        mWalletDialog!!.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        mWalletDialog!!.window!!.setBackgroundDrawableResource(android.R.color.transparent)
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

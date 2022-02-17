package com.rsl.foodnairesto.ui.main_screen.tables_and_tabs.add_to_tab.tabs.modifier_2


import android.annotation.SuppressLint
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log.e
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.rsl.foodnairesto.R
import com.rsl.foodnairesto.data.database_download.models.IngredientsModel
import com.rsl.foodnairesto.data.database_download.models.ProductModel
import com.rsl.foodnairesto.databinding.FragmentType2ModifierBinding
import com.rsl.foodnairesto.ui.main_screen.main_product_flow.MainProductViewModel
import com.rsl.foodnairesto.ui.main_screen.main_product_flow.NewProductViewModel
import com.rsl.foodnairesto.utils.AppConstants.MY_PREFERENCES
import com.rsl.foodnairesto.utils.AppConstants.PRODUCT_ID
import com.rsl.foodnairesto.utils.InjectorUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.koin.androidx.viewmodel.ext.android.viewModel

@SuppressLint("LogNotTimber")
class Type2ModifierFragment : Fragment() {

    private lateinit var mBinding : FragmentType2ModifierBinding
    private lateinit var mViewModel: MainProductViewModel
    private lateinit var mSharedPrefs: SharedPreferences
    private val productViewModel: NewProductViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_type2_modifier, container, false)
        val mView = mBinding.root

        mSharedPrefs = requireActivity().getSharedPreferences(MY_PREFERENCES, MODE_PRIVATE)

        val factory = InjectorUtils.provideMainProductViewModelFactory(requireActivity())
        mViewModel = ViewModelProviders.of(this, factory).get(MainProductViewModel::class.java)

        getProduct()
        initViews()

        mBinding.recyclerViewGeneric1.isNestedScrollingEnabled = false
        mBinding.recyclerViewGeneric2.isNestedScrollingEnabled = false
        return mView
    }

    private lateinit var mProductModel : ProductModel

    private fun getProduct(){
        val mProductID = arguments?.getString(PRODUCT_ID)

        productViewModel.getProduct(mProductID?: "").observe(viewLifecycleOwner) {
            mProductModel = it
            setupIngredients()

        }

        mVariantList = ArrayList()
    }

    private fun setupIngredients() {
        lifecycleScope.launch {

            val ingredients = withContext(Dispatchers.IO) {
                productViewModel.getProductIngredients(mProductModel.mIngredientsList)
            }
            if(ingredients.size==0){
               mBinding.addOnsConstraintLayout.visibility=View.GONE
            }

//            mVariantList = ArrayList(ingredients)

            val mVariantsRecyclerAdapter =
                Type2VariantsRecyclerAdapter(
                    ArrayList(ingredients)
                )
            mBinding.recyclerViewGeneric1.adapter = mVariantsRecyclerAdapter
        }
    }

    private fun initViews(){
        mBinding.edittextViewInstruction.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                EventBus.getDefault().post(ModifierType2Event2(""+s))
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
        mBinding.textViewChangeVariant.setOnClickListener {
            hideModifierViews()
        }
    }

    private var mVariant: IngredientsModel? = null
    private var mVariantList = ArrayList<IngredientsModel>()

    @Subscribe
    fun onVariantChecked(mVariant: IngredientsModel){

        if (!mVariantList.contains(mVariant)) mVariantList.add(mVariant) else mVariantList.remove(mVariant)

        Handler(Looper.getMainLooper()).postDelayed({
            this.mVariant = mVariant
            mBinding.textViewSelectedVariant.text = mVariant.mIngredientName

            EventBus.getDefault().post(ModifierType2Event(2, mVariant, mVariantList))
        }, 100)


    }

    @Subscribe
    fun onModifierClicked(mEvent: ModifierClickedEvent){
        EventBus.getDefault().post(ModifierType2Event(2, mVariant, mVariantList))
    }

//    fun getSelectedVariant() : GenericProducts?{
//        return mVariant
//    }

    private fun showModifiers(mVariant: IngredientsModel){
        e(javaClass.simpleName, "showModifiers:")

//        val mModifierCategoryList = mVariant.mIngredientCategoryList

//        mModifierCategoryList.sortWith({ o1, o2 ->  o1.mCategorySequence - o2.mCategorySequence})
//
//        var mSingleSelectionCategoryCount = 0
//
//        for (i in 0 until mModifierCategoryList.size)
//            if (mModifierCategoryList[i].mModifierSelection == SINGLE_SELECTION)
//                mSingleSelectionCategoryCount++
//
//        val mMultipleSelectionCount = mVariant.mIngredientLimit - mSingleSelectionCategoryCount
//
//        val mModifierCategoryAdapter =
//            VariantModifierCategoryATTRecyclerAdapter(requireActivity(), mModifierCategoryList, mMultipleSelectionCount)
//        mBinding.recyclerViewGeneric2.adapter = mModifierCategoryAdapter
//
//        val mEditor = mSharedPrefs.edit()
//        mEditor.putInt(MULTIPLE_SELECTION_COUNT, 0)
//        mEditor.apply()

    }

    private fun hideModifierViews(){
        mBinding.textViewSelectedVariant.visibility = GONE
        mBinding.textViewChangeVariant.visibility = GONE
        mBinding.recyclerViewGeneric2.visibility = GONE

        mBinding.cardViewGenericProducts.visibility = VISIBLE
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

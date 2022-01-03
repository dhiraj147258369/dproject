package com.rsl.youresto.ui.main_screen.app_settings.printer_settings.kitchen_printer

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rsl.youresto.R
import com.rsl.youresto.data.database_download.models.KitchenModel
import com.rsl.youresto.databinding.RecyclerItemKitchenPrinterBinding
import com.rsl.youresto.ui.main_screen.app_settings.printer_settings.event.SelectPrinterEvent
import com.rsl.youresto.utils.AppConstants
import com.rsl.youresto.utils.AppConstants.KITCHEN_PRINTER
import com.rsl.youresto.utils.AppConstants.NO_TYPE
import com.rsl.youresto.utils.AppConstants.PAPER_SIZE_50
import com.rsl.youresto.utils.AppConstants.PAPER_SIZE_80
import com.rsl.youresto.utils.AppConstants.SELECTED_KITCHEN_PRINTER_ID
import org.greenrobot.eventbus.EventBus
import java.util.*

@SuppressLint("LogNotTimber")
class KitchenPrinterAdapter(private val mContext: Context, private val mKitchenList: ArrayList<KitchenModel>):
    RecyclerView.Adapter<KitchenPrinterAdapter.KitchenPrinterViewHolder>() {

    private var i = 0
    private val noOfKitchens: Int = mKitchenList.size
    private var mSharedPrefs: SharedPreferences = mContext.getSharedPreferences(AppConstants.MY_PREFERENCES, Context.MODE_PRIVATE)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KitchenPrinterViewHolder {
        val mBinding = RecyclerItemKitchenPrinterBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return KitchenPrinterViewHolder(mBinding)
    }

    override fun getItemCount(): Int {
        return mKitchenList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: KitchenPrinterViewHolder, position: Int) {
        val mKitchenPrinter = mKitchenList[holder.adapterPosition]
        holder.bindData(mKitchenPrinter)

        if(mKitchenPrinter.mSelectedKitchenPrinterSize == 80)
            holder.mBinding.spinnerKitchenPaperSize.setSelection(1)
        else
            holder.mBinding.spinnerKitchenPaperSize.setSelection(0)

        holder.mBinding.checkboxKitchenPrinter.isChecked = mKitchenPrinter.mSelectedKitchenPrinterName != NO_TYPE

        if(mKitchenPrinter.mNetworkPrinterIP.isNotEmpty() && mKitchenPrinter.mPrinterType == 2) {
            holder.mBinding.textViewNetworkIp.visibility = VISIBLE
            holder.mBinding.textViewNetworkIp.text = "Printer: ${mKitchenPrinter.mNetworkPrinterIP}:${mKitchenPrinter.mNetworkPrinterPort}"
        } else {
            holder.mBinding.textViewNetworkIp.visibility = GONE
        }

        if(mKitchenPrinter.mLogWoodServerIP.isNotEmpty()) {
            holder.mBinding.textViewLogwoodServerId.visibility = VISIBLE
            holder.mBinding.textViewLogwoodServerId.text = "Logwood: ${mKitchenPrinter.mLogWoodServerIP}:${mKitchenPrinter.mLogWoodServerPort}"
        } else {
            holder.mBinding.textViewLogwoodServerId.visibility = GONE
        }

        holder.mBinding.checkboxKitchenPrinter.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                EventBus.getDefault().post(
                    SelectPrinterEvent(
                        true
                    )
                )
                val mEditor = mSharedPrefs.edit()
                mEditor.putString(SELECTED_KITCHEN_PRINTER_ID, mKitchenPrinter.mKitchenID)
                mEditor.putString(AppConstants.BILL_PRINTER_OR_KITCHEN_PRINTER, KITCHEN_PRINTER)
                mEditor.apply()
            } else {
                EventBus.getDefault().post(KitchenPrinterEvent(false,mKitchenPrinter,80))    //clear printer
            }
        }

        holder.mBinding.buttonPrinterEdit.setOnClickListener {
            val mEditor = mSharedPrefs.edit()
            mEditor.putString(SELECTED_KITCHEN_PRINTER_ID, mKitchenPrinter.mKitchenID)
            mEditor.putString(AppConstants.BILL_PRINTER_OR_KITCHEN_PRINTER, KITCHEN_PRINTER)
            mEditor.apply()

            EventBus.getDefault().post(KitchenPrinterEditEvent(true))
        }

        setPaperSizeSpinner(holder, mKitchenPrinter)
    }

    private fun setPaperSizeSpinner(holder: KitchenPrinterViewHolder, mKitchenPrinter: KitchenModel) {
        val mPaperSize = ArrayList<String>()
        mPaperSize.add("50mm")
        mPaperSize.add("80mm")

        val mTypeAdapter =
            ArrayAdapter(mContext, R.layout.spinner_item_printer_paper_size, mPaperSize)
        mTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        holder.mBinding.spinnerKitchenPaperSize.adapter = mTypeAdapter

        val mListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>, view: View, mPosition: Int, id: Long) {
                Log.e(javaClass.simpleName, "paperSize: no: $noOfKitchens")
                if (i >= noOfKitchens) {
                    when (mPosition) {
                        0 -> {
                            EventBus.getDefault().post(KitchenPrinterEvent(true,mKitchenPrinter, PAPER_SIZE_50))
                            Log.e(javaClass.simpleName, "onPaperSelection: $PAPER_SIZE_50")
                        }
                        1 -> {
                            EventBus.getDefault().post(KitchenPrinterEvent(true,mKitchenPrinter, PAPER_SIZE_80))
                            Log.e(javaClass.simpleName, "onPaperSelection: $PAPER_SIZE_80")
                        }
                    }
                }
                i++
            }

            override fun onNothingSelected(parent: AdapterView<*>) = Unit
        }

        holder.mBinding.spinnerKitchenPaperSize.onItemSelectedListener = mListener
    }

    inner class KitchenPrinterViewHolder(val mBinding: RecyclerItemKitchenPrinterBinding) :
        RecyclerView.ViewHolder(mBinding.root) {

        fun bindData(kitchenModel: KitchenModel) {
            mBinding.kitchenModel = kitchenModel
        }
    }
}
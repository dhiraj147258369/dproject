package com.rsl.foodnairesto.ui.main_screen.app_settings.printer_settings.bluetooth_printer

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rsl.foodnairesto.R
import com.rsl.foodnairesto.databinding.RecyclerItemPrinterBinding
import java.util.ArrayList

@Suppress("DEPRECATION")
class BluetoothPrinterAdapter(mBluetoothDeviceList: ArrayList<BluetoothDevice>, private val mContext: Context) :
    RecyclerView.Adapter<BluetoothPrinterAdapter.BluetoothPrinterViewHolder>() {

    private val mBluetoothList: ArrayList<BluetoothDevice> = mBluetoothDeviceList
    private var mSelectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BluetoothPrinterViewHolder {
        val mBinding = RecyclerItemPrinterBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return BluetoothPrinterViewHolder(mBinding)
    }

    override fun getItemCount(): Int {
        return mBluetoothList.size
    }

    override fun onBindViewHolder(holder: BluetoothPrinterViewHolder, position: Int) {
        val mBluetooth: BluetoothDevice = mBluetoothList[holder.adapterPosition]
        holder.mBinding.textViewPrinterName.text = mBluetooth.name

        if (mSelectedPosition == holder.adapterPosition) {
            holder.mBinding.constraintLayoutRecyclerPrinter.setBackgroundDrawable(mContext.getDrawable(R.drawable.new_item_selected))
            holder.mBinding.textViewPrinterName.setTextColor(mContext.resources.getColor(R.color.colorWhite))
        }
        else {
            holder.mBinding.constraintLayoutRecyclerPrinter.setBackgroundDrawable(mContext.getDrawable(R.drawable.new_item_not_selected))
            holder.mBinding.textViewPrinterName.setTextColor(mContext.resources.getColor(R.color.colorBlack))
        }

        holder.mBinding.constraintLayoutRecyclerPrinter.setOnClickListener {
            mSelectedPosition = holder.adapterPosition
            notifyDataSetChanged()
        }
    }

    fun getSelectedBluetooth(): BluetoothDevice? {
        return if (mSelectedPosition > -1) mBluetoothList[mSelectedPosition]
        else null

    }

    fun setSelectedBluetooth(mSelectedPosition: Int) {
        this.mSelectedPosition = mSelectedPosition
        notifyDataSetChanged()
    }

    class BluetoothPrinterViewHolder(val mBinding: RecyclerItemPrinterBinding) : RecyclerView.ViewHolder(mBinding.root)
}
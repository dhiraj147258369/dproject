package com.rsl.youresto.ui.main_screen.checkout.payment_options.cash

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rsl.youresto.databinding.RecyclerItemCurrencyNoteBinding
import com.rsl.youresto.ui.main_screen.checkout.payment_options.events.CurrencyNoteClickEvent
import org.greenrobot.eventbus.EventBus

class CurrencyNoteAdapter(private var mNoteList: ArrayList<Int>): RecyclerView.Adapter<CurrencyNoteAdapter.NoteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val mBinding = RecyclerItemCurrencyNoteBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return NoteViewHolder(mBinding)
    }

    override fun getItemCount(): Int {
        return mNoteList.size
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val mNote =  mNoteList[holder.adapterPosition]
        holder.setNote(mNote)
    }

    fun onNoteClicked(mNote: Int){
        EventBus.getDefault().post(CurrencyNoteClickEvent(mNote))
    }

    inner class NoteViewHolder(var mBinding: RecyclerItemCurrencyNoteBinding) : RecyclerView.ViewHolder(mBinding.root) {
        fun setNote(mNote: Int) {
            mBinding.adapter = this@CurrencyNoteAdapter
            mBinding.note = mNote
        }
    }
}
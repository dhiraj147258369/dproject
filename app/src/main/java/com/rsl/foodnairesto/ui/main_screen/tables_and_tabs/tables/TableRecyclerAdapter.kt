package com.rsl.foodnairesto.ui.main_screen.tables_and_tabs.tables

import android.view.*
import androidx.recyclerview.widget.RecyclerView
import com.rsl.foodnairesto.R
import com.rsl.foodnairesto.data.database_download.models.TablesModel
import com.rsl.foodnairesto.databinding.RecyclerItemTablesBinding
import org.greenrobot.eventbus.EventBus

class TableRecyclerAdapter(private val mTablesList: ArrayList<TablesModel>) :
    RecyclerView.Adapter<TableRecyclerAdapter.TablesViewHolder>(), View.OnCreateContextMenuListener {

    private var mTableLongPress: TablesModel? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TablesViewHolder {
        val mBinding = RecyclerItemTablesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TablesViewHolder(mBinding)
    }

    override fun getItemCount(): Int {
        return mTablesList.size
    }

    override fun onBindViewHolder(holder: TablesViewHolder, position: Int) {
        val mTable = mTablesList[holder.adapterPosition]
        holder.bindData(mTable)

        holder.itemView.setOnLongClickListener {
            mTableLongPress = mTable
            return@setOnLongClickListener false
        }
    }

    override fun onViewRecycled(holder: TablesViewHolder) {
        holder.itemView.setOnLongClickListener(null)
        super.onViewRecycled(holder)
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        if(mTableLongPress!!.mTableNoOfOccupiedChairs > 0) {
            menu!!.setHeaderTitle("Selected Table " + mTableLongPress!!.mTableNo)
            menu.add(Menu.NONE, R.id.menu_print_bill, Menu.NONE, R.string.print_bill)
            /*menu.add(Menu.NONE, R.id.menu_done, Menu.NONE, R.string.done)*/
            menu.add(Menu.NONE, R.id.menu_checkout, Menu.NONE, R.string.checkout)
//        menu.add(Menu.NONE, R.id.menu_move_table, Menu.NONE, R.string.move_table)
            menu.add(Menu.NONE, R.id.menu_clear_table, Menu.NONE, R.string.clear_table)
        }
    }

    fun getTableLongPressed(): TablesModel {
        return mTableLongPress!!
    }

    fun onTableClicked(mTable: TablesModel) {
        EventBus.getDefault().post(mTable)
    }

    inner class TablesViewHolder(private val mBinding: RecyclerItemTablesBinding) : RecyclerView.ViewHolder(mBinding.root) {

        init {
            mBinding.root.setOnCreateContextMenuListener(this@TableRecyclerAdapter)
        }

        fun bindData(tablesModel: TablesModel) {
            mBinding.tablesModel = tablesModel

            mBinding.tableAdapter = this@TableRecyclerAdapter
        }

    }
}
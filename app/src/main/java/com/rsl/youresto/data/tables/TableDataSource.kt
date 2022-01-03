package com.rsl.youresto.data.tables

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.rsl.youresto.data.database_download.models.TablesModel
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.Exception
import kotlin.collections.ArrayList

@SuppressLint("LogNotTimber")
class TableDataSource(val context: Context) : TablesNetworkUtils.TableNetworkInterface {

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var sInstance: TableDataSource? = null

        fun getInstance(context: Context) :TableDataSource?{
            val tempInstance = sInstance
            if (tempInstance != null)
                return tempInstance

            sInstance ?: synchronized(this){
                sInstance = TableDataSource(context)
            }
            return sInstance
        }
    }

    private var mSyncTablesData: MutableLiveData<List<TablesModel>>? = null
    private var mLocationID: String? = null

    fun syncTables(mLocationID: String): LiveData<List<TablesModel>>? {
        mSyncTablesData = MutableLiveData()
        this.mLocationID = mLocationID
        fetchTablesData()
        return mSyncTablesData
    }

    private fun fetchTablesData() {
        val getTablesURL = TablesNetworkUtils.getAllTableUrl(context)
        TablesNetworkUtils.getResponseFromAPIForAllTables(context, getTablesURL!!, this)
    }

    override fun onTableResponse(mResponse: String) {

        Log.e(javaClass.simpleName, "onTableResponse: $mResponse")

        try {
            val mJSONObject = JSONObject(mResponse)
            mSyncTablesData!!.postValue(TablesNetworkUtils.
                getRestaurantTablesData(mJSONObject.getJSONArray("data"), mLocationID))
        }catch (e: Exception){
            e.printStackTrace()
            mSyncTablesData!!.postValue(ArrayList())
        }


    }

    private var mOccupyData: MutableLiveData<TablesModel>? = null
    private var mTableModel: TablesModel? = null

    fun occupyTable(mTableModel: TablesModel): LiveData<TablesModel>? {
        this.mTableModel = mTableModel
        mOccupyData = MutableLiveData()
        occupyTableOnServer()
        return mOccupyData
    }

    private fun occupyTableOnServer() {
        TablesNetworkUtils.getResponseFromOccupyTable(context, mTableModel, this)
    }

    private var mUpdateTableData: MutableLiveData<Int>? = null
    fun updateTableData(mTableModel: TablesModel, mGroupName: String): LiveData<Int> {
        mUpdateTableData = MutableLiveData()
        val mGroupList = mTableModel.mGroupList

        for (i in 0 until mGroupList!!.size){
            updateTableOnServer(mTableModel, mGroupList[i].mGroupName)
        }

        return mUpdateTableData!!
    }

    private fun updateTableOnServer(mTableModel: TablesModel, mGroupName: String) {
        TablesNetworkUtils.getResponseFromUpdateTable(context, mTableModel, mGroupName, this)
    }

    override fun updateTableResponse(mResponse: String) {
        Log.e(javaClass.simpleName, "updateTableResponse: $mResponse")

        try {
            val mUpdateObject = JSONObject(mResponse)

            if (mUpdateObject.getString("status").equals("OK", ignoreCase = true)) {
                mUpdateTableData!!.postValue(1)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }


    override fun occupyTableAPIResponse(mResponse: String) {

        Log.e(javaClass.simpleName, "occupyTableAPIResponse: $mResponse")

        try {
            val mJsonObject = JSONObject(mResponse)
            val mJSONArray = mJsonObject.getJSONArray("data")
            val mTable = TablesNetworkUtils.getTableModel(mJSONArray.getJSONObject(0), mTableModel!!)
            mOccupyData!!.postValue(mTable)
        }catch (e: Exception){
            e.printStackTrace()
            mOccupyData!!.postValue(null)
        }



    }

    private var mClearTableData: MutableLiveData<Int>? = null
    fun clearTable(mTableID: String): LiveData<Int> {
        mClearTableData = MutableLiveData()

        TablesNetworkUtils.getResponseFromClearTable(context, mTableID, this)

        return mClearTableData!!
    }

    override fun onClearTable(isException: Boolean, mResponse: String) {
        Log.e(javaClass.simpleName, "onClearTable: $mResponse")
        if(!isException) {
            val mJSONObject = JSONObject(mResponse)
            if (mJSONObject.getString("status").lowercase(Locale.getDefault()) == "ok")
                mClearTableData!!.postValue(1)
        } else {
            mClearTableData!!.postValue(-1)
        }
    }
}
package com.rsl.foodnairesto.utils

import android.util.Log
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rsl.foodnairesto.data.cart.models.CartSubProductModel
import com.rsl.foodnairesto.data.checkout.model.*
import com.rsl.foodnairesto.data.database_download.models.*
import com.rsl.foodnairesto.data.tables.models.LocalTableGroupModel
import com.rsl.foodnairesto.data.tables.models.LocalTableSeatModel
import com.rsl.foodnairesto.data.tables.models.ServerTableGroupModel
import com.rsl.foodnairesto.data.tables.models.ServerTableSeatModel
import com.rsl.foodnairesto.ui.main_screen.favorite_items.model.FavoriteProductModel
import java.math.BigDecimal
import java.util.*
import kotlin.collections.ArrayList

class Converters {

    @TypeConverter
    fun fromCategoryString(value: String): ArrayList<ProductCategoryModel> {
        val listType = object : TypeToken<ArrayList<ProductCategoryModel>>() {

        }.getType()
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromCategoryArrayList(list: ArrayList<ProductCategoryModel>): String {
        val gson = Gson()
        return gson.toJson(list)
    }

    @TypeConverter
    fun fromProductString(value: String?): ArrayList<ProductModel> {
        Log.e("TAG", "fromProductString: $value" )
        if (value == null || value.isBlank()) return ArrayList()
        val listType = object : TypeToken<ArrayList<ProductModel>>() {}.getType()
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromProductArrayList(list: ArrayList<ProductModel>?): String {
        val gson = Gson()

        if (list == null) return ""
        return gson.toJson(list)
    }

    @TypeConverter
    fun fromIntegerString(value: String): ArrayList<Int> {
        val listType = object : TypeToken<ArrayList<Int>>() {

        }.getType()
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromIntegerArrayList(list: ArrayList<Int>): String {
        val gson = Gson()
        return gson.toJson(list)
    }

    @TypeConverter
    fun fromStringString(value: String): ArrayList<String> {
        val listType = object : TypeToken<ArrayList<String>>() {

        }.getType()
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromStringArrayList(list: ArrayList<String>): String {
        val gson = Gson()
        return gson.toJson(list)
    }

    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return if (timestamp == null) null else Date(timestamp)
    }

    @TypeConverter
    fun toTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromBigDecimalToString(bigDecimal: BigDecimal?) : String {
        if (bigDecimal == null) return "0"
        return bigDecimal.toString()
    }

    @TypeConverter
    fun fromStringToBigDecimal(value: String) : BigDecimal {
        return BigDecimal(value)
    }

    @TypeConverter
    fun fromLocationTypeString(value: String): ArrayList<LocationTypeModel>? {
        val listType = object : TypeToken<ArrayList<LocationTypeModel>>() {

        }.type
        return Gson().fromJson<ArrayList<LocationTypeModel>>(value, listType)
    }

    @TypeConverter
    fun fromLocationTypeArrayList(list: ArrayList<LocationTypeModel>): String {
        val gson = Gson()
        return gson.toJson(list)
    }

    @TypeConverter
    fun fromLocalTableGroupString(value: String): ArrayList<LocalTableGroupModel>? {
        val listType = object : TypeToken<ArrayList<LocalTableGroupModel>>() {

        }.type
        return Gson().fromJson<ArrayList<LocalTableGroupModel>>(value, listType)
    }

    @TypeConverter
    fun fromLocalTableGroupArrayList(list: ArrayList<LocalTableGroupModel>): String {
        val gson = Gson()
        return gson.toJson(list)
    }

    @TypeConverter
    fun fromLocalTableSeatString(value: String): ArrayList<LocalTableSeatModel>? {
        val listType = object : TypeToken<ArrayList<LocalTableSeatModel>>() {

        }.type
        return Gson().fromJson<ArrayList<LocalTableSeatModel>>(value, listType)
    }

    @TypeConverter
    fun fromLocalTableSeatArrayList(list: ArrayList<LocalTableSeatModel>): String {
        val gson = Gson()
        return gson.toJson(list)
    }

    @TypeConverter
    fun fromServerTableGroupString(value: String): ArrayList<ServerTableGroupModel>? {
        val listType = object : TypeToken<ArrayList<ServerTableGroupModel>>() {

        }.type
        return Gson().fromJson<ArrayList<ServerTableGroupModel>>(value, listType)
    }

    @TypeConverter
    fun fromServerTableGroupArrayList(list: ArrayList<ServerTableGroupModel>?): String {
        val gson = Gson()
        if (list == null) return gson.toJson(ArrayList<ServerTableGroupModel>())
        return gson.toJson(list)
    }

    @TypeConverter
    fun fromFavoriteProductModelString(value: String): ArrayList<FavoriteProductModel>? {
        val listType = object : TypeToken<ArrayList<FavoriteProductModel>>() {

        }.type
        return Gson().fromJson<ArrayList<FavoriteProductModel>>(value, listType)
    }

    @TypeConverter
    fun fromFavoriteProductModelArrayList(list: ArrayList<FavoriteProductModel>): String {
        val gson = Gson()
        return gson.toJson(list)
    }

    @TypeConverter
    fun fromServerTableSeatModelString(value: String): ArrayList<ServerTableSeatModel>? {
        val listType = object : TypeToken<ArrayList<ServerTableSeatModel>>() {

        }.type
        return Gson().fromJson<ArrayList<ServerTableSeatModel>>(value, listType)
    }

    @TypeConverter
    fun fromServerTableSeatModelArrayList(list: ArrayList<ServerTableSeatModel>): String {
        val gson = Gson()
        return gson.toJson(list)
    }

    @TypeConverter
    fun fromIngredientCategoryString(value: String): ArrayList<IngredientCategoryModel>? {
        val listType = object : TypeToken<ArrayList<IngredientCategoryModel>>() {

        }.type
        return Gson().fromJson<ArrayList<IngredientCategoryModel>>(value, listType)
    }

    @TypeConverter
    fun fromIngredientCategoryArrayList(list: ArrayList<IngredientCategoryModel>): String {
        val gson = Gson()
        return gson.toJson(list)
    }

    @TypeConverter
    fun fromTaxString(value: String): ArrayList<TaxModel>? {
        val listType = object : TypeToken<ArrayList<TaxModel>>() {

        }.type
        return Gson().fromJson<ArrayList<TaxModel>>(value, listType)
    }

    @TypeConverter
    fun fromTaxArrayList(list: ArrayList<TaxModel>): String {
        val gson = Gson()
        return gson.toJson(list)
    }

    @TypeConverter
    fun fromIngredientModelString(value: String): ArrayList<IngredientsModel>? {
        val listType = object : TypeToken<ArrayList<IngredientsModel>>() {

        }.type
        return Gson().fromJson<ArrayList<IngredientsModel>>(value, listType)
    }

    @TypeConverter
    fun fromIngredientModelArrayList(list: ArrayList<IngredientsModel>): String {
        val gson = Gson()
        return gson.toJson(list)
    }

    @TypeConverter
    fun fromCheckoutTransactionString(value: String): ArrayList<CheckoutTransaction>? {
        val listType = object : TypeToken<ArrayList<CheckoutTransaction>>() {

        }.type
        return Gson().fromJson<ArrayList<CheckoutTransaction>>(value, listType)
    }

    @TypeConverter
    fun fromCheckoutTransactionArrayList(list: ArrayList<CheckoutTransaction>): String {
        val gson = Gson()
        return gson.toJson(list)
    }

    @TypeConverter
    fun fromSubProductCategoryString(value: String): ArrayList<SubProductCategoryModel>? {
        val listType = object : TypeToken<ArrayList<SubProductCategoryModel>>() {

        }.type
        return Gson().fromJson<ArrayList<SubProductCategoryModel>>(value, listType)
    }

    @TypeConverter
    fun fromSubProductCategoryArrayList(list: ArrayList<SubProductCategoryModel>): String {
        val gson = Gson()
        return gson.toJson(list)
    }

    @TypeConverter
    fun fromCartSubProductString(value: String): ArrayList<CartSubProductModel>? {
        val listType = object : TypeToken<ArrayList<CartSubProductModel>>() {

        }.type
        return Gson().fromJson<ArrayList<CartSubProductModel>>(value, listType)
    }

    @TypeConverter
    fun fromCartSubProductArrayList(list: ArrayList<CartSubProductModel>): String {
        val gson = Gson()
        return gson.toJson(list)
    }

    @TypeConverter
    fun fromReportPaymentString(value: String): ArrayList<ReportPaymentModel>? {
        val listType = object : TypeToken<ArrayList<ReportPaymentModel>>() {

        }.type
        return Gson().fromJson<ArrayList<ReportPaymentModel>>(value, listType)
    }

    @TypeConverter
    fun fromReportPaymentArrayList(list: ArrayList<ReportPaymentModel>): String {
        val gson = Gson()
        return gson.toJson(list)
    }

    @TypeConverter
    fun fromReportProductString(value: String): ArrayList<ReportProductModel>? {
        val listType = object : TypeToken<ArrayList<ReportProductModel>>() {

        }.type
        return Gson().fromJson<ArrayList<ReportProductModel>>(value, listType)
    }

    @TypeConverter
    fun fromReportProductArrayList(list: ArrayList<ReportProductModel>): String {
        val gson = Gson()
        return gson.toJson(list)
    }

    ////////////////////////////////////////////////////
    @TypeConverter
    fun fromNewPaymentMethodArrayList(list: ArrayList<NewPaymentMethodModel>): String {
        val gson = Gson()
        return gson.toJson(list)
    }
    @TypeConverter
    fun fromNewPaymentMethodString(value: String): ArrayList<NewPaymentMethodModel>? {
        val listType = object : TypeToken<ArrayList<NewPaymentMethodModel>>() {

        }.type
        return Gson().fromJson<ArrayList<NewPaymentMethodModel>>(value, listType)
    }

    @TypeConverter
    fun fromTableWisePriceArrayList(list: ArrayList<TableWisePrice>): String {
        val gson = Gson()
        return gson.toJson(list)
    }
    @TypeConverter
    fun fromTableWisePriceString(value: String): ArrayList<TableWisePrice>? {
        val listType = object : TypeToken<ArrayList<TableWisePrice>>() {

        }.type
        return Gson().fromJson<ArrayList<TableWisePrice>>(value, listType)
    }

    @TypeConverter
    fun fromLocationUserListArrayList(list: ArrayList<LocationUserList>): String {
        val gson = Gson()
        return gson.toJson(list)
    }
    @TypeConverter
    fun fromLocationUserListString(value: String): ArrayList<LocationUserList>? {
        val listType = object : TypeToken<ArrayList<LocationUserList>>() {

        }.type
        return Gson().fromJson<ArrayList<LocationUserList>>(value, listType)
    }


}
package com.rsl.foodnairesto.utils.new_print

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.rsl.foodnairesto.repositories.LoginRepository
import com.rsl.foodnairesto.repositories.NewCartRepository
import com.rsl.foodnairesto.ui.main_screen.checkout.SharedCheckoutViewModel
import com.rsl.foodnairesto.utils.AppPreferences
import com.rsl.foodnairesto.utils.Utils
import com.rsl.foodnairesto.utils.custom_views.CustomToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.math.BigDecimal
import java.util.*
import kotlin.collections.ArrayList

class BillPrint(val lifecycleScope: CoroutineScope, val activity: Activity, private val cartId: String): KoinComponent {

    private val prefs: AppPreferences by inject()
    private val cartRepo: NewCartRepository by inject()
    private val loginRepo: LoginRepository by inject()

    var finalBill = false
    lateinit var sharedViewModel: SharedCheckoutViewModel

    fun print50(){
        val printString = StringBuilder()
        val mDottedLine = "\n--------------------------------"

        lifecycleScope.launch {
            val storeModel = withContext(Dispatchers.IO){
                loginRepo.getRestaurantData()
            }

            printString.append("\n").append(storeModel.name)

            if (storeModel.address.isNotEmpty())
                printString.append("\n").append(storeModel.address)

            if (storeModel.city.isNotEmpty())
                printString.append("\n").append(storeModel.city)

            if (storeModel.postcode.isNotEmpty())
                printString.append("\n").append(storeModel.postcode)

            if (storeModel.contactNumber.isNotEmpty())
                printString.append("\n").append(storeModel.contactNumber)

            if (storeModel.email.isNotEmpty())
                printString.append("\n").append(storeModel.email)

            printString.append(mDottedLine)

            val mTime = "\nTime: " + Utils.getStringFromDate("dd/MM/yyyy HH:mm", Date())
            printString.append(mTime)

            printString.append("\n")

            val mLocation = "LOC: " + prefs.getSelectedLocationName()

            val mServer = "Waiter: " + prefs.getSelectedWaiterName()


            val totalLength1 = mLocation.length + mServer.length

            printString.append(mLocation)

            for (i in 0 until 32 - totalLength1) {
                printString.append(" ")
            }

            printString.append(mServer)

            val carts = withContext(Dispatchers.IO) {
                cartRepo.getCartsById(cartId)
            }

            if (carts.isEmpty()){
                CustomToast.makeText(activity, "Cart empty", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val orderNOString = "\nOrder NO: ${carts[0].mCartID}"
            printString.append(orderNOString)
            printString.append(mDottedLine)

            val header = "\nProducts       " + "QTY  " + "Price  " + "Total"
            printString.append(header)
            printString.append(mDottedLine)

            var cartSubTotal = BigDecimal(0.0)
            var taxTotal = BigDecimal(0.0)
            for (cart in carts) {
                val productName = "\n${cart.mProductName}"
                val productQty = "${cart.mProductQuantity}"
                val productPrice = cart.mProductUnitPrice
                val productTotalPrice = cart.mProductTotalPrice
                cartSubTotal += productTotalPrice
                taxTotal += (productTotalPrice * BigDecimal(cart.taxPercentage)) / BigDecimal(100)

                printString.append("\n").append(productName)

                val length: Int = productName.length

                if (length >= 15) {
                    val mLength = 31 - length
                    for (j in 0 until mLength) {
                        printString.append(" ")
                    }
                    printString.append("\n")
                    for (j in 0..14) {
                        printString.append(" ")
                    }
                } else {
                    val mLength = 15 - length
                    for (j in 0 until mLength) {
                        printString.append(" ")
                    }
                }

                val productPriceString = String.format(Locale.ENGLISH, "%.2f", productPrice)
                val productTotalPriceString = String.format(Locale.ENGLISH, "%.2f", productTotalPrice)

                if (productQty.length == 1) {
                    var spaceInBetween = ""
                    when (productPriceString.length) {
                        4 -> spaceInBetween = "    "
                        5 -> spaceInBetween = "   "
                        6 -> spaceInBetween = "  "
                    }
                    printString.append(productQty).append(spaceInBetween).append(productPriceString)

                    when (productTotalPriceString.length) {
                        4 -> spaceInBetween = "    "
                        5 -> spaceInBetween = "   "
                        6 -> spaceInBetween = "  "
                    }
                    printString.append(spaceInBetween).append(productTotalPriceString)
                } else {
                    var spaceInBetween = ""
                    when (productPriceString.length) {
                        4 -> spaceInBetween = "   "
                        5 -> spaceInBetween = "  "
                        6 -> spaceInBetween = " "
                    }
                    printString.append(productQty).append(spaceInBetween).append(productPriceString)

                    when (productTotalPriceString.length) {
                        4 -> spaceInBetween = "   "
                        5 -> spaceInBetween = "  "
                        6 -> spaceInBetween = " "
                    }
                    printString.append(spaceInBetween).append(productTotalPriceString)
                }

                for (modifier in cart.mShowModifierList ?: ArrayList()){

                    printString.append("\n  - ").append(modifier.mIngredientName)

                    for (k in 0 until 16 - modifier.mIngredientName.length) {
                        printString.append(" ")
                    }

                    printString.append(String.format(Locale.ENGLISH, "%.2f", modifier.mIngredientPrice))

                    printString.append("        ")
                }
            }

            printString.append(mDottedLine)

            val cartSubTotalString = String.format(Locale.ENGLISH, "%.2f", cartSubTotal)

            var spaceInBetween = ""
            when (cartSubTotalString.length) {
                4 -> spaceInBetween = "                  "
                5 -> spaceInBetween = "                 "
                6 -> spaceInBetween = "                "
                7 -> spaceInBetween = "               "
            }
            val subTotalString = "\nSub Total:${spaceInBetween}${cartSubTotalString}"
            printString.append(subTotalString)


            if (taxTotal > BigDecimal(0)){
                val taxTotalString = String.format(Locale.ENGLISH, "%.2f", taxTotal)
                spaceInBetween = ""
                when (taxTotalString.length) {
                    4 -> spaceInBetween = "                        "
                    5 -> spaceInBetween = "                       "
                    6 -> spaceInBetween = "                      "
                    7 -> spaceInBetween = "                     "
                }
                printString.append("\nTax:${spaceInBetween}${taxTotalString}")
            }

            if (finalBill) {
                if (sharedViewModel.postCheckout.disTotal > 0) {
                    val totalDiscountString = String.format(Locale.ENGLISH, "%.2f", sharedViewModel.postCheckout.disTotal)
                    spaceInBetween = ""
                    when (totalDiscountString.length) {
                        4 -> spaceInBetween = "                   "
                        5 -> spaceInBetween = "                  "
                        6 -> spaceInBetween = "                 "
                        7 -> spaceInBetween = "                "
                    }
                    printString.append("\nDiscount:${spaceInBetween}${totalDiscountString}")
                }

                //delivery

                //net total
                if (sharedViewModel.postCheckout.netTotal > 0) {
                    val totalString = String.format(Locale.ENGLISH, "%.2f", sharedViewModel.postCheckout.netTotal)
                    spaceInBetween = ""
                    when (totalString.length) {
                        4 -> spaceInBetween = "               "
                        5 -> spaceInBetween = "              "
                        6 -> spaceInBetween = "             "
                        7 -> spaceInBetween = "            "
                    }
                    printString.append("\nTotal Amount:${spaceInBetween}${totalString}")
                }
            } else {
                val total = cartSubTotal + taxTotal
                val totalString = String.format(Locale.ENGLISH, "%.2f", total)
                spaceInBetween = ""
                when (totalString.length) {
                    4 -> spaceInBetween = "               "
                    5 -> spaceInBetween = "              "
                    6 -> spaceInBetween = "             "
                    7 -> spaceInBetween = "            "
                }
                printString.append("\nTotal Amount:${spaceInBetween}${totalString}")
            }
            printText(printString.toString())

            Log.e("TAG", "print50: \n$printString")
        }
    }

    fun print80(){
        val printString = StringBuilder()
        val mDottedLine = "\n------------------------------------------------"

        lifecycleScope.launch {
            val storeModel = withContext(Dispatchers.IO){
                loginRepo.getRestaurantData()
            }

            printString.append("\n").append(storeModel.name)

            if (storeModel.address.isNotEmpty())
                printString.append("\n").append(storeModel.address)

            if (storeModel.city.isNotEmpty())
                printString.append("\n").append(storeModel.city)

            if (storeModel.postcode.isNotEmpty())
                printString.append("\n").append(storeModel.postcode)

            if (storeModel.contactNumber.isNotEmpty())
                printString.append("\n").append(storeModel.contactNumber)

            if (storeModel.email.isNotEmpty())
                printString.append("\n").append(storeModel.email)

            printString.append(mDottedLine)

            val mTime = "\nTime: " + Utils.getStringFromDate("dd/MM/yyyy HH:mm", Date())
            printString.append(mTime)

            printString.append("\n")

            val mLocation = "LOC: " + prefs.getSelectedLocationName()

            val mServer = "Waiter: " + prefs.getSelectedWaiterName()


            val totalLength1 = mLocation.length + mServer.length

            printString.append(mLocation)

            for (i in 0 until 48 - totalLength1) {
                printString.append(" ")
            }

            printString.append(mServer)

            val carts = withContext(Dispatchers.IO) {
                cartRepo.getCartsById(cartId)
            }

            val orderNOString = "\nOrder NO: ${carts[0].mCartID}"
            printString.append(orderNOString)
            printString.append(mDottedLine)

            val header = "\nProducts                   " + "QTY    " + "Price    " + "Total"
            printString.append(header)
            printString.append(mDottedLine)

            var cartSubTotal = BigDecimal(0.0)
            var taxTotal = BigDecimal(0.0)
            for (cart in carts) {
                val productName = "\n${cart.mProductName}"
                val productQty = "${cart.mProductQuantity}"
                val productPrice = cart.mProductUnitPrice
                val productTotalPrice = cart.mProductTotalPrice
                cartSubTotal += productTotalPrice
                taxTotal += (productTotalPrice * BigDecimal(cart.taxPercentage)) / BigDecimal(100)

                printString.append(productName)

                val length: Int = productName.length

                if (length >= 24) {
                    val mLength = 47 - length
                    for (j in 0 until mLength) {
                        printString.append(" ")
                    }
                    printString.append("\n")
                    for (j in 0..27) {
                        printString.append(" ")
                    }
                } else {
                    val mLength = 29 - length
                    for (j in 0 until mLength) {
                        printString.append(" ")
                    }
                }

                val productPriceString = String.format(Locale.ENGLISH, "%.2f", productPrice)
                val productTotalPriceString = String.format(Locale.ENGLISH, "%.2f", productTotalPrice)

                if (productQty.length == 1) {
                    var spaceInBetween = ""
                    when (productPriceString.length) {
                        4 -> spaceInBetween = "      "
                        5 -> spaceInBetween = "     "
                        6 -> spaceInBetween = "    "
                        7 -> spaceInBetween = "   "
                    }

                    printString.append(productQty).append(spaceInBetween).append(productPriceString)

                    when (productTotalPriceString.length) {
                        4 -> spaceInBetween = "     "
                        5 -> spaceInBetween = "    "
                        6 -> spaceInBetween = "   "
                        7 -> spaceInBetween = "  "
                    }
                    printString.append(spaceInBetween).append(productTotalPriceString)
                } else {
                    var spaceInBetween = ""
                    when (productPriceString.length) {
                        4 -> spaceInBetween = "     "
                        5 -> spaceInBetween = "    "
                        6 -> spaceInBetween = "   "
                        7 -> spaceInBetween = "  "
                    }
                    printString.append(productQty).append(spaceInBetween).append(productPriceString)

                    when (productTotalPriceString.length) {
                        4 -> spaceInBetween = "     "
                        5 -> spaceInBetween = "    "
                        6 -> spaceInBetween = "   "
                        7 -> spaceInBetween = "  "
                    }
                    printString.append(spaceInBetween).append(productTotalPriceString)
                }

                for (modifier in cart.mShowModifierList ?: ArrayList()){

                    printString.append("\n  - ").append(modifier.mIngredientName)

                    val priceString = String.format(Locale.ENGLISH, "%.2f", modifier.mIngredientPrice)

                    val spaceLength = when (priceString.length) {
                        4 -> 31
                        5 -> 30
                        6 -> 29
                        else -> 28
                    }

                    for (k in 0 until spaceLength - modifier.mIngredientName.length) {
                        printString.append(" ")
                    }

                    printString.append(priceString)

                    printString.append("        ")
                }
            }

            printString.append(mDottedLine)

            val cartSubTotalString = String.format(Locale.ENGLISH, "%.2f", cartSubTotal)

            var spaceInBetween = ""
            when (cartSubTotalString.length) {
                4 -> spaceInBetween = "                                  "
                5 -> spaceInBetween = "                                 "
                6 -> spaceInBetween = "                                "
                7 -> spaceInBetween = "                               "
            }
            val subTotalString = "\nSub Total:${spaceInBetween}${cartSubTotalString}"
            printString.append(subTotalString)


            if (taxTotal > BigDecimal(0)){
                val taxTotalString = String.format(Locale.ENGLISH, "%.2f", taxTotal)
                spaceInBetween = ""
                when (taxTotalString.length) {
                    4 -> spaceInBetween = "                                        "
                    5 -> spaceInBetween = "                                       "
                    6 -> spaceInBetween = "                                      "
                    7 -> spaceInBetween = "                                     "
                }
                printString.append("\nTax:${spaceInBetween}${taxTotalString}")
            }

            if (finalBill) {
                if (sharedViewModel.postCheckout.disTotal > 0) {
                    val totalDiscountString = String.format(Locale.ENGLISH, "%.2f", sharedViewModel.postCheckout.disTotal)
                    spaceInBetween = ""
                    when (totalDiscountString.length) {
                        4 -> spaceInBetween = "                                   "
                        5 -> spaceInBetween = "                                  "
                        6 -> spaceInBetween = "                                 "
                        7 -> spaceInBetween = "                                "
                    }
                    printString.append("\nDiscount:${spaceInBetween}${totalDiscountString}")
                }

                //delivery

                //net total
                if (sharedViewModel.postCheckout.netTotal > 0) {
                    val totalString = String.format(Locale.ENGLISH, "%.2f", sharedViewModel.postCheckout.netTotal)
                    spaceInBetween = ""
                    when (totalString.length) {
                        4 -> spaceInBetween = "                               "
                        5 -> spaceInBetween = "                              "
                        6 -> spaceInBetween = "                             "
                        7 -> spaceInBetween = "                            "
                    }
                    printString.append("\nTotal Amount:${spaceInBetween}${totalString}")
                }
            } else {
                val total = cartSubTotal + taxTotal
                val totalString = String.format(Locale.ENGLISH, "%.2f", total)
                spaceInBetween = ""
                when (totalString.length) {
                    4 -> spaceInBetween = "                               "
                    5 -> spaceInBetween = "                              "
                    6 -> spaceInBetween = "                             "
                    7 -> spaceInBetween = "                            "
                }
                printString.append("\nTotal Amount:${spaceInBetween}${totalString}")
            }
            printText(printString.toString())

            Log.e("TAG", "print50: \n$printString")
        }
    }

    private fun printText(text: String){
        CustomToast.makeText(activity, "Printing Bill", Toast.LENGTH_SHORT).show()
        lifecycleScope.launch {
            withContext(Dispatchers.IO){
                val printUtils = PrintUtils(activity, prefs.getSelectedBillPrinterName())
                printUtils.printText(text, prefs.getSelectedBillPrinterType())
            }
        }
    }

}
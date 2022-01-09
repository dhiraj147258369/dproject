package com.rsl.youresto.utils.new_print

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.rsl.youresto.repositories.NewCartRepository
import com.rsl.youresto.utils.AppPreferences
import com.rsl.youresto.utils.Utils
import com.rsl.youresto.utils.custom_views.CustomToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*
import kotlin.collections.ArrayList

class KitchenPrint(val lifecycleScope: CoroutineScope, val activity: Activity, private val cartId: String): KoinComponent {

    private val prefs: AppPreferences by inject()
    private val cartRepo: NewCartRepository by inject()

    fun print50() {
        val printString = StringBuilder()
        val dotLine = "\n--------------------------------"

        val mTime = "Time: " + Utils.getStringFromDate("dd/MM/yyyy HH:mm", Date())
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
        printString.append(dotLine)

        val header = "\nProducts                  " + " QTY  "
        printString.append(header)
        printString.append(dotLine)

        lifecycleScope.launch{
            val carts = withContext(Dispatchers.IO) {
                cartRepo.getCartsById(cartId)
            }

           for (cart in carts) {
               val productName = "\n${cart.mProductName}"
               val productQty = "${cart.mProductQuantity}  "
               printString.append(productName)

               val productLength = productName.length + productQty.length

               for (j in 0 until 32 - productLength) {
                   printString.append(" ")
               }

               printString.append(productQty)

               for (ingredient in cart.mShowModifierList ?: ArrayList()){
                   val ingredientName = "\n  - ${ingredient.mIngredientName}"
                   printString.append(ingredientName)

                   val ingredientLength = ingredientName.length
                   for (j in 0 until 32 - ingredientLength) {
                       printString.append(" ")
                   }
               }
           }

            printString.append(dotLine)

            val mCartNO = "\nCartNO: $cartId"

            printString.append(mCartNO)
            printText(printString.toString())

            Log.e("TAG", "print50: \n$printString")
        }
    }

    fun print80() {
        val printString = StringBuilder()
        val dotLine = "\n------------------------------------------------"

        val mTime = "Time: " + Utils.getStringFromDate("dd/MM/yyyy HH:mm", Date())
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
        printString.append(dotLine)

        val header = "\nProducts                               " + " QTY  "
        printString.append(header)
        printString.append(dotLine)

        lifecycleScope.launch{
            val carts = withContext(Dispatchers.IO) {
                cartRepo.getCartsById(cartId)
            }

            for (cart in carts) {
                val productName = "\n${cart.mProductName}"
                val productQty = "${cart.mProductQuantity}  "
                printString.append(productName)

                val productLength = productName.length + productQty.length

                for (j in 0 until 48 - productLength) {
                    printString.append(" ")
                }

                printString.append(productQty)

                for (ingredient in cart.mShowModifierList ?: ArrayList()){
                    val ingredientName = "\n  - ${ingredient.mIngredientName}"
                    printString.append(ingredientName)

                    val ingredientLength = ingredientName.length
                    for (j in 0 until 48 - ingredientLength) {
                        printString.append(" ")
                    }
                }
            }

            printString.append(dotLine)

            val mCartNO = "\nCartNO: $cartId"

            printString.append(mCartNO)
            printText(printString.toString())

            Log.e("TAG", "print50: \n$printString")
        }
    }

    private fun printText(text: String){
        CustomToast.makeText(activity, "Printing KOT", Toast.LENGTH_SHORT).show()
       lifecycleScope.launch {
           withContext(Dispatchers.IO){
               val printUtils = PrintUtils(activity, prefs.getSelectedKitchenPrinterName())
               printUtils.printText(text, prefs.getSelectedKitchenPrinterType())
           }
       }
    }

}
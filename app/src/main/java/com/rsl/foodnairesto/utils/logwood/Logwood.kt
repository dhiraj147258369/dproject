package com.rsl.foodnairesto.utils.logwood

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log.e
import com.rsl.foodnairesto.ui.main_screen.cart.event.LogwoodEvent
import org.greenrobot.eventbus.EventBus
import java.io.*
import java.net.Socket
import java.net.UnknownHostException
import java.util.regex.Pattern

@SuppressLint("LogNotTimber")
class Logwood(var mContext: Context) {
    private val fileName = "tickets.dat"
    private val path: File = ContextWrapper(mContext).getDir("Notes", Context.MODE_PRIVATE)
//    internal val path = Environment.getExternalStorageDirectory().toString() + "/Android/data/mpos_logwood/"
    private var mCheckOrder = true
    private var mValidOrder = true
    private val FILE_TO_RECEIVED =
        Environment.getExternalStorageDirectory().toString() + "/Android/data/mpos_logwood/Notes/tickets.dat"

    fun sendData(mLogwoodString: String, mServerIP: String, mServerPort: String) {
        val mSeparatedString = mLogwoodString.split("\n")
        for (i in 0 until mSeparatedString.size - 1) {
            if (i == 0) {
                if (mSeparatedString[i] == "$&,") {
                    e(javaClass.simpleName, "valid start order")
                } else {
                    e(javaClass.simpleName, "Invalid start order")
                    mValidOrder = false
                    EventBus.getDefault().post(LogwoodEvent(false, "Invalid Order"))
                }
            } else if (i == 1) {
                if (isValidDate(mSeparatedString[i])) {
                    e(javaClass.simpleName, "valid date")
                } else {
                    e(javaClass.simpleName, "please enter valid date")
                    mValidOrder = false
                    EventBus.getDefault().post(LogwoodEvent(false, "Invalid Date"))
                    break
                }
            } else {
                if (fixedInformation(mSeparatedString[i])) {
                    e(javaClass.simpleName, "valid fixed Information")
                } else if (itemInformation(mSeparatedString[i])) {
                    e(javaClass.simpleName, "valid Item Information")
                } else if (toppingInformation(mSeparatedString[i])) {
                    e(javaClass.simpleName, "valid itemInfo")
                } else {
                    e(javaClass.simpleName, "enter valid info ")
                    EventBus.getDefault().post(LogwoodEvent(false, "Invalid Order"))
                    mValidOrder = false
                    break
                }
            }

        }
        if (mValidOrder) {
            e(javaClass.simpleName, "mValidOrder $mValidOrder")
            try {
                val root = File(path, "Notes")
                if (!root.exists()) {
                    root.mkdirs()
                }
                val gpxFile = File(root, fileName)
                val writer = FileWriter(gpxFile)
                writer.append(mLogwoodString)
                writer.flush()
                writer.close()

                e(javaClass.simpleName, "mValidOrder abc")
                sendFileServer(mServerIP, mServerPort)
                //mCheckOrder = true
                mValidOrder = true
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            e(javaClass.simpleName, "Enter Valid Order")
        }
    }

    private fun sendFileServer(mServerIP: String, mSocketPort: String) {
        e(javaClass.simpleName,"sendFileServer: Server IP $mServerIP Socket Port $mSocketPort")
        val fos = arrayOf<FileOutputStream>()
        val bos = arrayOf<BufferedOutputStream>()
        val sock = arrayOf<Socket>()
        val thread = Thread({
            try {
                val f = File(FILE_TO_RECEIVED)
                val socket = Socket(mServerIP, mSocketPort.toInt())
                val input = FileInputStream(f)
                val out = DataOutputStream(socket.getOutputStream())
                val buf = ByteArray(1024)
                var len = input.read(buf)
                while (len > 0) {
                    out.write(buf, 0, len)
                    Handler(Looper.getMainLooper()).post {
                        EventBus.getDefault().post(LogwoodEvent(true, "Order send Kitchen successfully"))
                    }
                    len = input.read(buf)
                }

                e(javaClass.simpleName,"sendFileServer abc")

                input.close()
                out.close()

            } catch (e: UnknownHostException) {
                e.printStackTrace()
                e(javaClass.simpleName, e.localizedMessage)
                Handler(Looper.getMainLooper()).post {
                    EventBus.getDefault().post(LogwoodEvent(false, "Unknown Host Exception"))
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                Handler(Looper.getMainLooper()).post {
                    EventBus.getDefault().post(LogwoodEvent(false, "File Not Found Exception"))
                }
                e(javaClass.simpleName, e.localizedMessage)
            } catch (e: IOException) {
                e.printStackTrace()
                Handler(Looper.getMainLooper()).post {
                    EventBus.getDefault().post(LogwoodEvent(false, "Failed to connect to port and server"))
                }
                e(javaClass.simpleName, e.localizedMessage)
            }
        })
        thread.start()
    }

    private fun isValidDate(date: String): Boolean {
        val dateRegEx = "^ [0-9]{2}:[0-9]{2}:[0-9]{2}:[0-9]{2}:[0-9]{4},"
        val pattern: Pattern
        // Regex for a valid date
        // Compare the regex with the date
        pattern = Pattern.compile(dateRegEx)
        val matcher = pattern.matcher(date)
        return matcher.find()
    }


    private fun fixedInformation(information: String): Boolean {
        val informationRegEx = "^ [0-9]{1,3},[a-zA-Z ]{1,20},[0-9]{1,2},[0-9]{1,2},[0-9]{1,4},[0-9],[S|M|D|B],[0-9]"
        val pattern: Pattern
        // Regex for a valid fixed information
        // Compare the regex with the fixed information
        pattern = Pattern.compile(informationRegEx)
        val matcher = pattern.matcher(information)
        return matcher.find()
    }

    private fun itemInformation(itemInfo: String): Boolean {
        val itemInfoRegEx = "[PLU]{3},[0-9]{1,9},[0-9a-zA-Z&()\" ]{1,20},[0-9]{0,9},[0-9]{0,9}"
        val pattern: Pattern
        // Regex for a valid item information
        // Compare the regex with the item information
        pattern = Pattern.compile(itemInfoRegEx)
        val matcher = pattern.matcher(itemInfo)
        return matcher.find()
    }

    private fun toppingInformation(toppingInfo: String): Boolean {
        val toppingInfoRegEx = "[SI]{2},[0-9]{1,9},[0-9a-zA-z&()\" ]{1,20},[0-9]{0,9}]"
        val pattern: Pattern
        // Regex for a valid topping information
        // Compare the regex with the topping information
        pattern = Pattern.compile(toppingInfoRegEx)
        val matcher = pattern.matcher(toppingInfo)
        return matcher.find()
    }
}
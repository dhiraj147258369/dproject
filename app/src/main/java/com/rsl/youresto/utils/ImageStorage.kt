package com.rsl.youresto.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object ImageStorage {

    fun saveToInternalStorage(bitmapImage: Bitmap, mImageName: String, mContext: Context): String {

        val directory: File = ContextWrapper(mContext).getDir("pics", MODE_PRIVATE)
        val myFile = File(directory, "$mImageName.jpg")
        val fos = FileOutputStream(myFile)
        bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.close()

        return "$directory/$mImageName.jpg"
    }

    fun getImage(mImagePath: String?): Bitmap {
        return BitmapFactory.decodeStream(FileInputStream(File(mImagePath)))
    }
}
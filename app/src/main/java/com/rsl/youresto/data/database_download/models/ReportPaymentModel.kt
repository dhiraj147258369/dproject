package com.rsl.youresto.data.database_download.models

class ReportPaymentModel(
     val mTransactionID: String,
     val mPaymentMethodType: Int,
     val mAmount: Double,
     val mWalletName: String,
     val mEntryMode: String,
     val mPaymentMethodName: String,
     val mCashAmount: Double,
     val mCardType: String,
     val mCardProvider: String,
     val mPaidBySeats: String,
     val mChangeAmount: Double,
     val mPaymentMethodID: String,
     val mCardNO: String,
     val mReferenceNO: String
)
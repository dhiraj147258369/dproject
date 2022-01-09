package com.rsl.youresto.utils.printer_utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.Toast;

import com.rsl.youresto.R;
import woyou.aidlservice.jiuiv5.ICallback;
import woyou.aidlservice.jiuiv5.IWoyouService;

import java.util.ArrayList;
import java.util.List;



public class AidlUtil {
    private static final String SERVICE＿PACKAGE = "woyou.aidlservice.jiuiv5";
    private static final String SERVICE＿ACTION = "woyou.aidlservice.jiuiv5.IWoyouService";

    private Context mContext;
    private IWoyouService woyouService;
    private ICallback mICallback;
    private static AidlUtil mAidlUtil = new AidlUtil();


    public AidlUtil() {

    }

    public AidlUtil(Context context) {
        mContext = context;
        connectPrinterService(mContext);
    }

    public static AidlUtil getInstance() {
        return mAidlUtil;
    }

    public void connectPrinterService(Context context)
    {
        this.mContext = context.getApplicationContext();
        Intent intent = new Intent();
        intent.setPackage(SERVICE＿PACKAGE);
        intent.setAction(SERVICE＿ACTION);
        context.getApplicationContext().startService(intent);
        context.getApplicationContext().bindService(intent, connService, Context.BIND_AUTO_CREATE);
//        initPrinter();
    }

    public void initPrinter() {
        if (woyouService == null) {
//            Toast.makeText(context,R.string.toast_2,Toast.LENGTH_LONG).show();
            return;
        }

        try {
            woyouService.printerInit(null);
            Toast.makeText(mContext, "initialised", Toast.LENGTH_LONG).show();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    public interface PrinterCallback {
        String getResult();
        void onReturnString(String result);
    }

    private PrinterCallback mPrinterCallback;

    private ServiceConnection connService = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            woyouService = IWoyouService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            woyouService = null;
        }
    };

    public void disconnectPrinterService(Context context) {
        if (woyouService != null) {
            context.getApplicationContext().unbindService(connService);
            woyouService = null;
        }
    }

    public boolean isConnect() {
        return woyouService != null;
    }

//    public ICallback generateCB(final PrinterCallback printerCallback){
//        return new ICallback.Stub(){
//            @Override
//            public void  onRunResult(boolean isSuccess, int code, String msg) throws RemoteException {
//
//            }
//
//        };
//    }

    public void openDrawer(){
        if (woyouService == null) {
            return;
        }

        byte[] mOpenDrawerCommand = new byte[4];
        mOpenDrawerCommand[0] = 0x10;
        mOpenDrawerCommand[1] = 0x14;
        mOpenDrawerCommand[2] = 0x00;
        mOpenDrawerCommand[3] = 0x00;

        try {
            woyouService.sendRAWData(mOpenDrawerCommand, null);
            Toast.makeText(mContext, "Opening Cash Drawer", Toast.LENGTH_SHORT).show();

        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    public void cutPaper(){
        if (woyouService == null) {
            return;
        }

        byte[] mCutPaperCommand = new byte[4];
        mCutPaperCommand[0] = 0x1d;
        mCutPaperCommand[1] = 0x56;
        mCutPaperCommand[2] = 0x42;
        mCutPaperCommand[3] = 0x00;

        try {
            woyouService.sendRAWData(mCutPaperCommand, null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void printTextReceiptHeader(String content, float size, boolean isBold, boolean isUnderLine) {
        if (woyouService == null) {
            Toast.makeText(mContext, "Printing Receipt!", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            if (isBold) {
                woyouService.sendRAWData(ESCUtil.boldOn(), null);
            } else {
                woyouService.sendRAWData(ESCUtil.boldOff(), null);
            }

            if (isUnderLine) {
                woyouService.sendRAWData(ESCUtil.underlineWithOneDotWidthOn(), null);
            } else {
                woyouService.sendRAWData(ESCUtil.underlineOff(), null);
            }

            woyouService.sendRAWData(ESCUtil.alignCenter(), null);

            /*Bitmap icon = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.veeapos_square);
            woyouService.printBitmap(Bitmap.createScaledBitmap(icon, 170, 180, false), null);*/
            woyouService.printTextWithFont(content,"fonts/abel.ttf", size, null);
            //don't forget to change this
            woyouService.lineWrap(5, null);

        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    public void printTextReceiptContent(String content, float size, boolean isBold, boolean isUnderLine){
        try {

            if (woyouService == null) return;

            if (isBold) {
                woyouService.sendRAWData(ESCUtil.boldOn(), null);
            } else {
                woyouService.sendRAWData(ESCUtil.boldOff(), null);
            }

            if (isUnderLine) {
                woyouService.sendRAWData(ESCUtil.underlineWithOneDotWidthOn(), null);
            } else {
                woyouService.sendRAWData(ESCUtil.underlineOff(), null);
            }

            woyouService.sendRAWData(ESCUtil.alignCenter(), null);

            woyouService.printTextWithFont(content, null, size, null);
            //don't forget to change this
            woyouService.lineWrap(5, null);
            cutPaper();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    public void printBitmap(Bitmap bitmap, int mLineWrap) {
        if (woyouService == null) {
            Toast.makeText(mContext, "Printing Receipt!", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            /*if (isBold) {
                woyouService.sendRAWData(ESCUtil.boldOn(), null);
            } else {
                woyouService.sendRAWData(ESCUtil.boldOff(), null);
            }

            if (isUnderLine) {
                woyouService.sendRAWData(ESCUtil.underlineWithOneDotWidthOn(), null);
            } else {
                woyouService.sendRAWData(ESCUtil.underlineOff(), null);
            }*/

            woyouService.sendRAWData(ESCUtil.alignCenter(), null);

            //Bitmap icon = BitmapFactory.decodeResource(mContext.getResources(), bitmap);
            //woyouService.printBitmap(Bitmap.createScaledBitmap(bitmap, width, height, false), null);
            woyouService.printBitmap(bitmap, null);
            //woyouService.printTextWithFont(content, null, size, null);
            //don't forget to change this
            woyouService.lineWrap(mLineWrap, null);

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}

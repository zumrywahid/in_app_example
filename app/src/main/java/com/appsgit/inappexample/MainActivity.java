package com.appsgit.inappexample;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.UiThread;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.TextView;

import com.android.billingclient.api.Purchase;
import com.appsgit.inappexample.billing.BillingManager;
import com.appsgit.inappexample.billing.BillingProvider;
import com.appsgit.inappexample.skulist.PurchaseListFragment;

import java.util.List;

/**
 * Example App using Play Billing library.
 * */
public class MainActivity extends FragmentActivity implements BillingProvider {
    // Debug tag, for logging
    private static final String TAG = "MainActivity";

    // Tag for a dialog that allows us to find it when screen was rotated
    private static final String DIALOG_TAG = "dialog";

    private BillingManager mBillingManager;
    private PurchaseListFragment mPurchaseListFragment;
    private View mScreenWait, mScreenMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Try to restore dialog fragment if we were showing it prior to screen rotation
        if (savedInstanceState != null) {
            mPurchaseListFragment = (PurchaseListFragment) getSupportFragmentManager()
                    .findFragmentByTag(DIALOG_TAG);
        }

        // Create and initialize BillingManager which talks to BillingLibrary
        mBillingManager = new BillingManager(this);

        mScreenWait = findViewById(R.id.screen_wait);
        mScreenMain = findViewById(R.id.screen_main);

        findViewById(R.id.button_purchase).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPurchaseButtonClicked(view);
            }
        });

        showRefreshedUi();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBillingManager.destroy();
    }

    @Override
    public BillingManager getBillingManager() {
        return mBillingManager;
    }

    /**
     * User clicked purchased button - show a purchase dialog with all available SKUs
     */
    public void onPurchaseButtonClicked(final View arg0) {

        if (mPurchaseListFragment == null) {
            mPurchaseListFragment = new PurchaseListFragment();
        }

        if (!isPurchaseListFragmentShown()) {
            mPurchaseListFragment.show(getSupportFragmentManager(), DIALOG_TAG);
        }
    }

    /**
     * Remove loading spinner and refresh the UI
     */
    public void showRefreshedUi() {
        setWaitScreen(false);

        if (isPurchaseListFragmentShown()) {
            mPurchaseListFragment.refreshUI();
        }
    }

    /**
     * Show an alert dialog to the user
     * @param messageId String id to display inside the alert dialog
     */
    @UiThread
    void alert(@StringRes int messageId) {
        alert(messageId, null);
    }

    /**
     * Show an alert dialog to the user
     * @param messageId String id to display inside the alert dialog
     * @param optionalParam Optional attribute for the string
     */
    @UiThread
    void alert(@StringRes int messageId, @Nullable Object optionalParam) {
        if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
            throw new RuntimeException("Dialog could be shown only from the main thread");
        }

        AlertDialog.Builder bld = new AlertDialog.Builder(this);
        bld.setNeutralButton("OK", null);

        if (optionalParam == null) {
            bld.setMessage(messageId);
        } else {
            bld.setMessage(getResources().getString(messageId, optionalParam));
        }

        bld.create().show();
    }

    /**
     * Enables or disables the "please wait" screen.
     */
    private void setWaitScreen(boolean set) {
        mScreenMain.setVisibility(set ? View.GONE : View.VISIBLE);
        mScreenWait.setVisibility(set ? View.VISIBLE : View.GONE);
    }

    /**
     * Update UI to reflect model
     */
    @UiThread
    public void updateUi(List<Purchase> purchases) {
        String text = "";

        if (purchases != null && purchases.size() > 0) {
            for (Purchase purchase : purchases) {
                text += "Purchased Item : " + purchase.getSku() + "\n";
            }
        }

        ((TextView)findViewById(R.id.resultText)).setText(text);

        if (isPurchaseListFragmentShown()) {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.remove(mPurchaseListFragment);
            ft.commitAllowingStateLoss();
        }
    }

    public boolean isPurchaseListFragmentShown() {
        return mPurchaseListFragment != null && mPurchaseListFragment.isVisible();
    }
}

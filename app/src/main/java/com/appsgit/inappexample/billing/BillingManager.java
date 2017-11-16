package com.appsgit.inappexample.billing;

import android.app.Activity;

import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.PurchaseHistoryResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import android.util.Log;

import com.android.billingclient.api.BillingClient.BillingResponse;
import com.android.billingclient.api.BillingClient.SkuType;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.appsgit.inappexample.MainActivity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class BillingManager implements PurchasesUpdatedListener {
    private static final String TAG = BillingManager.class.getSimpleName();

    private final BillingClient mBillingClient;
    private final Activity mActivity;

    // Defining SKU constants from Google Play Developer Console
    private static final HashMap<String, List<String>> SKUS;
    static
    {
        SKUS = new HashMap<>();
        SKUS.put(SkuType.INAPP, Arrays.asList("theme1", "theme2"));
        SKUS.put(SkuType.SUBS, Arrays.asList("subscription_monthly", "subscription_yearly"));
    }

    public BillingManager(Activity activity) {
        mActivity = activity;
        mBillingClient = BillingClient.newBuilder(mActivity).setListener(this).build();
        startServiceConnectionIfNeeded(null);
    }

    @Override
    public void onPurchasesUpdated(int responseCode, List<Purchase> purchases) {
        Log.i(TAG, "onPurchasesUpdated() response: " + responseCode);

        if (responseCode == BillingResponse.OK) {
            ((MainActivity)mActivity).updateUi(purchases);
            consumePurchases(purchases);
        }

    }

    /*
    * By default you cannot buy the same product again and again...after you purchased the item you should cosume it.
    * For testing purpose i am going to consume all the product each and everytim i buy...
     */
    public void consumePurchases(List<Purchase> purchases) {
        try {
            if (purchases != null && purchases.size() > 0) {
                for (final Purchase purchase : purchases) {
                    if (purchase.getPurchaseToken() != null && !purchase.getPurchaseToken().isEmpty()) {
                        mBillingClient.consumeAsync(purchase.getPurchaseToken(), new ConsumeResponseListener() {
                            @Override
                            public void onConsumeResponse(int responseCode, String purchaseToken) {
                                Log.d(TAG, "onConsumeResponse: purchased SKU  " + purchase.getSku());
                                Log.d(TAG, "onConsumeResponse: purchased getPurchaseToken  " + purchase.getPurchaseToken());
                            }
                        });
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void startServiceConnectionIfNeeded(final Runnable executeOnSuccess) {
        if (mBillingClient.isReady()) {
            if (executeOnSuccess != null) {
                executeOnSuccess.run();
            }
        } else {
            mBillingClient.startConnection(new BillingClientStateListener() {
                @Override
                public void onBillingSetupFinished(@BillingResponse int billingResponse) {
                    if (billingResponse == BillingResponse.OK) {
                        Log.i(TAG, "onBillingSetupFinished() response: " + billingResponse);
                        if (executeOnSuccess != null) {
                            executeOnSuccess.run();
                        }
                    } else {
                        Log.w(TAG, "onBillingSetupFinished() error code: " + billingResponse);
                    }
                }

                @Override
                public void onBillingServiceDisconnected() {
                    Log.w(TAG, "onBillingServiceDisconnected()");
                }
            });
        }
    }

    public void querySkuDetailsAsync(@BillingClient.SkuType final String itemType,
            final List<String> skuList, final SkuDetailsResponseListener listener) {
        // Specify a runnable to start when connection to Billing client is established
        Runnable executeOnConnectedService = new Runnable() {
            @Override
            public void run() {
                SkuDetailsParams skuDetailsParams = SkuDetailsParams.newBuilder()
                        .setSkusList(skuList).setType(itemType).build();
                mBillingClient.querySkuDetailsAsync(skuDetailsParams,
                        new SkuDetailsResponseListener() {
                            @Override
                            public void onSkuDetailsResponse(int responseCode,
                                    List<SkuDetails> skuDetailsList) {
                                listener.onSkuDetailsResponse(responseCode, skuDetailsList);
                            }
                        });
            }
        };

        // If Billing client was disconnected, we retry 1 time and if success, execute the query
        startServiceConnectionIfNeeded(executeOnConnectedService);
    }

    public List<String> getSkus(@SkuType String type) {
        return SKUS.get(type);
    }

    public void startPurchaseFlow(final String skuId, final String billingType) {
        // Specify a runnable to start when connection to Billing client is established
        Runnable executeOnConnectedService = new Runnable() {
            @Override
            public void run() {
                BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                        .setType(billingType)
                        .setSku(skuId)
                        .build();
                mBillingClient.launchBillingFlow(mActivity, billingFlowParams);
            }
        };

        // If Billing client was disconnected, we retry 1 time and if success, execute the query
        startServiceConnectionIfNeeded(executeOnConnectedService);
    }

    /*
     * We shoould check purchased products and update them accordingly...
     * in my case, i am trying to consume as if they are not consumed...
     */
    public void checkPurchasedProducts(String billingType) {
        mBillingClient.queryPurchaseHistoryAsync(billingType,
                new PurchaseHistoryResponseListener() {
                    @Override
                    public void onPurchaseHistoryResponse(@BillingResponse int responseCode,
                                                          List<Purchase> purchasesList) {
                        if (responseCode == BillingResponse.OK
                                && purchasesList != null) {
//                            for (Purchase purchase : purchasesList) {
//                                // Process the result.
//                            }
                            consumePurchases(purchasesList);
                            Log.d(TAG, "onPurchaseHistoryResponse: purchased items..");
                        }
                    }
                });

    }

    public void destroy() {
        mBillingClient.endConnection();
    }
}
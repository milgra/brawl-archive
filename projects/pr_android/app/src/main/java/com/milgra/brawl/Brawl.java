package com.milgra.brawl;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import org.libsdl.app.SDLActivity;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClient.BillingResponse;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.ArrayList;
import java.util.List;


public class Brawl extends SDLActivity implements PurchasesUpdatedListener {

    static{
        System.loadLibrary("main");
    }

    private BillingClient mBillingClient;

    private String itemIds[];
    private static Brawl staticbrawl;

    private native void storeitem(int index , String name);
    private native void showdonate( );

    // Setup
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);

        // init billing client
        Log.i( "BRAWL", "BRAWL construct" );

        staticbrawl = this;

        itemIds = new String[3];

        mBillingClient = BillingClient.newBuilder(this).setListener(this).build();
        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@BillingResponse int billingResponseCode) {
                Log.i( "BRAWL" ,  "billing setup finished " + billingResponseCode );

                if (billingResponseCode == BillingResponse.OK) {
                    // The billing client is ready. You can query purchases here.
                    getDetails();
                }
            }
            @Override
            public void onBillingServiceDisconnected() {
                Log.i( "BRAWL" ,  "billing client disconnected" );
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });

    }

    public void getDetails( )
    {

        Log.i( "BRAWL" , "getting product details" );


        List skuList = new ArrayList<>( );

//        skuList.add("android.test.purchased");
//        skuList.add("android.test.purchased");
//        skuList.add("android.test.purchased");

        skuList.add("brawldonationsmall");
        skuList.add("brawldonationmedium");
        skuList.add("brawldonationnormal");

        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder( );
        params.setSkusList( skuList ).setType( BillingClient.SkuType.INAPP );

        mBillingClient.querySkuDetailsAsync( params.build( ) ,

                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(int responseCode, List<SkuDetails> skuDetailsList) {

                        Log.i( "BRAWL" , "SKUDETAULS responsecode " + responseCode );

                        if (responseCode == BillingResponse.OK && skuDetailsList != null)
                        {
                            for (SkuDetails skuDetails : skuDetailsList) {
                                String sku = skuDetails.getSku();
                                String price = skuDetails.getPrice();
                                String curr = skuDetails.getPriceCurrencyCode();
                                String title = skuDetails.getTitle();

                                Log.i( "BRAWL" , "sku : " + sku + " price : " + price + " title : " + title );

                                if ("brawldonationsmall".equals(sku)) {
                                    storeitem( 0 , price + " " + curr );
                                    itemIds[0]=sku;
                                } else if ("brawldonationmedium".equals(sku)) {
                                    storeitem( 1 , price + " " + curr  );
                                    itemIds[1]=sku;
                                } else if ("brawldonationnormal".equals(sku)) {
                                    storeitem( 2 , price + " " + curr  );
                                    itemIds[2]=sku;
                                }
                            }

                            if ( skuDetailsList.size() > 0 ) showdonate( );
                        }
                        else
                        {
                            Log.i( "BRAWL" , "no response" );

                        }
                        // Process the result.
                    }
                } );

    }

    @Override
    public void onPurchasesUpdated( int responseCode , @Nullable List < Purchase > purchases )
    {
        Log.i( "BRAWL" , "Purchases updated" );

        if (responseCode == BillingResponse.OK && purchases != null)
        {
            for (Purchase purchase : purchases)
            {
                Log.i( "BRAWL" , "Purchases completed " + purchase );
            }
        }
        else if (responseCode == BillingResponse.USER_CANCELED)
        {
            // Handle an error caused by a user cancelling the purchase flow.
            Log.i( "BRAWL" , "Purchases cancelled" );
        }
        else
            {
            // Handle any other error codes.
                Log.i( "BRAWL" , "Purchases error " + responseCode );
        }
    }

    public static void buyItem( int index )
    {

        staticbrawl.buyItemInst( index );

    }

    public void buyItemInst( int index )
    {

        Log.i( "BRAWL" , "buyItem : "  + index );

        String sku = itemIds[ index ];

        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                .setSku(sku)
                .setType(BillingClient.SkuType.INAPP) // SkuType.SUB for subscription
                .build();

        int responseCode = mBillingClient.launchBillingFlow( this, flowParams );

        Log.i( "BRAWL" , "purchase repsonsecode : "  + responseCode );
    }

    public static void openURL( String url )
    {
        staticbrawl.openURLInst( url );
    }


    public void openURLInst( String url )
    {
        Uri uriUrl = Uri.parse( url );
        Intent intent = new Intent(Intent.ACTION_VIEW, uriUrl);
        intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
        startActivity( intent );
    }
}

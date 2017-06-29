package com.martinrecipe.italian;

/**
 * Created by melvin on 25/09/2016.
 */
public class Configurations {

    //GENERAL---------------------------------------------------------------------------------------
    //the default number of servings
    public final static int DEFAULT_NUMBER_OF_SERVINGS = 4;

    //Server URL
    public static String SERVER_URL = "http://findajobsite.com/recipeapp/";



    //IN-APP PURCHASE-------------------------------------------------------------------------------
    //TO USE THE IN-APP PURCHASE FEATURE AN EXTENDED LICENSE NEEDS TO BE PURCHASED.
    //ONLY PUT IN A PUBLIC KEY IF YOU PURCHASED AN EXTENDED LICENSE FROM
    // CODECANYON: http://codecanyon.net/user/neurondigital/portfolio?ref=neurondigital

    //OPTIONAL - Leave 'PUBLIC_KEY' empty to disable in-app purchase.
    final static String PUBLIC_KEY = "";

    // For testing use:  "android.test.purchased";  to make the purchase always accepted without an actual payment
    //Needs to be the same as the product id used in the Google Play Dashboard
    final static String SKU_PREMIUM = "your_premium_upgrade_sku_here";



    //DEEP LINK SHARE-------------------------------------------------------------------------------
    //in android manifest don't forget:
    //<data android:host="recipeapp.neurondigital.com" android:scheme="http"/>
    //<data android:host="recipeapp.neurondigital.com" android:scheme="https"/>
    final static String DEEP_LINK_TO_SHARE = "http://recipeapp.neurondigital.com";


    //LIST TYPE------------------------------------------------------------------------------------
    public final static int LIST_FULLWIDTH = 0, LIST_2COLUMNS = 1;
    public final static int LIST_MENU_TYPE = LIST_2COLUMNS;


    //CATEGORIES------------------------------------------------------------------------------------
    public final static int CATEGORY_TEXT_ONLY = 0, CATEGORY_TEXT_AND_IMAGE = 1;
    public final static int CATEGORY_MENU_TYPE = CATEGORY_TEXT_AND_IMAGE;

    public final static boolean DISPLAY_CATEGORIES_IN_NAVIGATION_DRAWER= true;



    //FIREBASE PUSH NOTIFICATION--------------------------------------------------------------------
    public final static String FIREBASE_PUSH_NOTIFICATION_TOPIC = "news";


}

package com.martinrecipe.italian;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by melvin on 25/09/2016.
 */
public class Recipe {
    public int id;
    public String name;
    public String directions;
    public String[] ingredients;
    public int servings;
    public int category;
    public String imageUrl[];
    public int viewed, shared, favorited;

    interface onRecipeDownloadedListener {
        void onRecipeDownloaded(Recipe recipe);
    }

    interface onRecipesDownloadedListener {
        void onRecipesDownloaded(List<Recipe> recipes);
    }

    public Recipe(int id, String name, String directions, String[] ingredients, int servings, int category, String[] imageUrl) {
        this.id = id;
        this.name = name;
        this.directions = directions;
        this.ingredients = ingredients;
        this.servings = servings;
        this.category = category;
        this.imageUrl = imageUrl;
    }

    public Recipe(String name) {
        this.name = name;
    }


    /**
     * Decode recipe from JSON
     *
     * @param JSONRecipe
     */
    public Recipe(JSONObject JSONRecipe) {
        try {
            id = JSONRecipe.getInt("id");
            name = JSONRecipe.getString("name");
            directions = JSONRecipe.getString("directions");
           // imageUrl = Configurations.SERVER_URL + "uploads/" + JSONRecipe.getString("image");
            servings = JSONRecipe.getInt("servings");
            category = JSONRecipe.getInt("category");
            viewed = JSONRecipe.getInt("viewed");
            shared = JSONRecipe.getInt("shared");
            favorited = JSONRecipe.getInt("favorited");
            JSONArray JSONingredients = new JSONArray(JSONRecipe.getString("ingredients"));
            ingredients = new String[JSONingredients.length()];
            for (int i = 0; i < JSONingredients.length(); i++) {
                ingredients[i] = JSONingredients.getString(i);
            }

            JSONArray JSONimageURL = new JSONArray(JSONRecipe.getString("image"));
            imageUrl = new String[JSONimageURL.length()];
            for (int i = 0; i < JSONimageURL.length(); i++) {
                imageUrl[i] = Configurations.SERVER_URL + "uploads/" + JSONimageURL.getString(i);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Calculate servings. Replace {2} with 2*servings
     *
     * @param servings
     * @return
     */
    public String[] calculateServings(int servings) {
        if (ingredients == null)
            return null;
        String[] ingredientsWithServings = new String[ingredients.length];
        for (int i = 0; i < ingredients.length; i++) {
            Pattern replaceBetweenBrackets = Pattern.compile("\\{(.*?)\\}");
            ingredientsWithServings[i] = replace(ingredients[i], replaceBetweenBrackets, servings);
        }

        return ingredientsWithServings;
    }

    /**
     * Replace and multiply
     *
     * @param input
     * @param regex
     * @param multiply
     * @return
     */
    public static String replace(String input, Pattern regex, int multiply) {
        StringBuffer resultString = new StringBuffer();
        Matcher regexMatcher = regex.matcher(input);
        while (regexMatcher.find()) {
            String s = regexMatcher.group();
            String number = s.substring(1, s.length() - 1);
            try {
                int num = Integer.parseInt(number);
                regexMatcher.appendReplacement(resultString, "" + (num * multiply));
            } catch (Exception e) {
                return input;
            }

        }
        regexMatcher.appendTail(resultString);

        return resultString.toString();
    }


    //LOAD MULTIPLE RECIPES-------------------------------------------------------------------------

    /**
     * Load Recipes from server
     *
     * @param activity
     * @param offset
     * @param limit
     * @param search
     * @param recipesDownloadedListener
     */
    public static void loadRecipes(FragmentActivity activity, int offset, int limit, String search, final onRecipesDownloadedListener recipesDownloadedListener) {
        loadRecipes(activity, offset, limit, search, "", recipesDownloadedListener);
    }

    /**
     * Load recipes from server
     *
     * @param activity
     * @param offset
     * @param limit
     * @param search
     * @param category
     * @param recipesDownloadedListener
     */
    public static void loadRecipes(final FragmentActivity activity, int offset, int limit, String search, String category, final onRecipesDownloadedListener recipesDownloadedListener) {

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(activity);
        final String url = Configurations.SERVER_URL + "api/recipes/" + offset + "/" + limit + "?search=" + Uri.encode(search) + "&category=" + category;
        final Cache cache = new Cache(activity);
        StringRequest arrayreq = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String responseStr) {
                //save to cache
                cache.store(url, responseStr);
                //decode and return recipes
                decodeRecipes(responseStr, recipesDownloadedListener);
            }
        }, new Response.ErrorListener() {
            @Override
            // Handles errors that occur due to Volley
            public void onErrorResponse(VolleyError error) {
                //No connection to server. Probably no internet
                Log.e("Volley", "Error");
                error.printStackTrace();
                //Try to load from cache else view warning
                String responseStr = cache.load(url);
                if (responseStr != null) {
                    System.out.println("loading cached data: " + responseStr);
                    decodeRecipes(responseStr, recipesDownloadedListener);
                } else
                    Functions.noInternetAlert(activity);

            }
        });
        // Add the request to the RequestQueue.
        queue.add(arrayreq);
    }

    /**
     * Load recipes by ids (for favorites)
     *
     * @param activity
     * @param ids
     * @param recipesDownloadedListener
     */
    public static void loadRecipes(final FragmentActivity activity, final int[] ids, final onRecipesDownloadedListener recipesDownloadedListener) {

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(activity);
        final String url = Configurations.SERVER_URL + "api/recipes/";

        final Cache cache = new Cache(activity);
        JSONObject requestBody = new JSONObject();
        StringRequest arrayreq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String responseStr) {
                //save to cache
                cache.store(url, responseStr);
                //decode and return recipes
                decodeRecipes(responseStr, recipesDownloadedListener);
            }
        }, new Response.ErrorListener() {
            @Override
            // Handles errors that occur due to Volley
            public void onErrorResponse(VolleyError error) {
                //No connection to server. Probably no internet
                Log.e("Volley", "Error");
                error.printStackTrace();
                //Try to load from cache else view warning
                String responseStr = cache.load(url);
                if (responseStr != null) {
                    System.out.println("loading cached data: " + responseStr);
                    decodeRecipes(responseStr, recipesDownloadedListener);
                } else
                    Functions.noInternetAlert(activity);
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                JSONArray idsJSON = new JSONArray();
                for (int i = 0; i < ids.length; i++) {
                    idsJSON.put(ids[i]);
                }
                // System.out.println(idsJSON.toString());
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("ids", idsJSON.toString());
                return params;
            }
        };

        // Add the request to the RequestQueue.
        queue.add(arrayreq);
    }

    /**
     * Decode recipes received from server or cache
     *
     * @param responseStr
     * @param recipesDownloadedListener
     */
    public static void decodeRecipes(String responseStr, final onRecipesDownloadedListener recipesDownloadedListener) {
        try {
            JSONArray response = new JSONArray(responseStr);
            System.out.println(response.toString(2));
            List<Recipe> recipes = new ArrayList<>();
            for (int i = 0; i < response.length(); i++) {
                JSONObject jsonObject = response.getJSONObject(i);
                recipes.add(new Recipe(jsonObject));
            }
            recipesDownloadedListener.onRecipesDownloaded(recipes);
        }
        // Try and catch are included to handle any errors due to JSON
        catch (JSONException e) {
            e.printStackTrace();
        }
    }


    //LOAD SINGLE RECIPES---------------------------------------------------------------------------

    /**
     * Load a single recipe by id
     *
     * @param activity
     * @param id
     * @param recipeDownloadedListener
     */
    public static void loadRecipe(final FragmentActivity activity, int id, final onRecipeDownloadedListener recipeDownloadedListener) {

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(activity);
        final String url = Configurations.SERVER_URL + "api/recipe/" + id;
        final Cache cache = new Cache(activity);
        StringRequest req = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String responseStr) {
                //save to cache
                cache.store(url, responseStr);
                //decode and return recipes
                decodeRecipe(responseStr, recipeDownloadedListener);
            }
        }, new Response.ErrorListener() {
            @Override
            // Handles errors that occur due to Volley
            public void onErrorResponse(VolleyError error) {
                //No connection to server. Probably no internet
                Log.e("Volley", "Error");
                error.printStackTrace();
                //Try to load from cache else view warning
                String responseStr = cache.load(url);
                if (responseStr != null) {
                    System.out.println("loading cached data: " + responseStr);
                    decodeRecipe(responseStr, recipeDownloadedListener);
                } else
                    Functions.noInternetAlert(activity);
            }
        });
        // Add the request to the RequestQueue.
        queue.add(req);
    }

    /**
     * Decode a single recipe
     *
     * @param responseStr
     * @param recipeDownloadedListener
     */
    public static void decodeRecipe(String responseStr, final onRecipeDownloadedListener recipeDownloadedListener) {
        try {
            JSONObject response = new JSONObject(responseStr);
            System.out.println(response.toString(2));
            Recipe recipe = new Recipe(response);
            recipeDownloadedListener.onRecipeDownloaded(recipe);
        }
        // Try and catch are included to handle any errors due to JSON
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //RECIPE STATISTICS-----------------------------------------------------------------------------

    public void favorite(Context context) {
        send(context, "favorite/" + id);
    }

    public void shared(Context context) {
        send(context, "shared/" + id);
    }

    public void viewed(Context context) {
        send(context, "viewed/" + id);
    }


    public static void send(Context context, String data) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = Configurations.SERVER_URL + "api/recipe/" + data;

        StringRequest arrayreq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String responseStr) {

            }
        }, new Response.ErrorListener() {
            @Override
            // Handles errors that occur due to Volley
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        // Add the request to the RequestQueue.
        queue.add(arrayreq);
    }


    //RECIPE FAVORITE-------------------------------------------------------------------------------

    public void setFavorite(Context context, boolean isFavorite) {

        if (isFavorite) {
            favorite(context);
            Save.addToArray(id, "favorites", context);
        } else {
            int[] allFavorites = Save.loadIntArray("favorites", context);
            for (int i = 0; i < allFavorites.length; i++) {
                if (allFavorites[i] == this.id) {
                    Save.removeFromIntArray(i, "favorites", context);
                    return;
                }
            }
        }
    }

    public boolean isFavorite(Context context) {
        //get favorites
        int[] allFavorites = Save.loadIntArray("favorites", context);
        for (int i = 0; i < allFavorites.length; i++) {
            if (allFavorites[i] == this.id) {
                return true;
            }
        }
        return false;
    }


    public static void getFavoriteRecipes(FragmentActivity activity, final onRecipesDownloadedListener recipesDownloadedListener) {
        int[] allFavorites = Save.loadIntArray("favorites", activity);
        if (allFavorites.length > 0)
            loadRecipes(activity, allFavorites, recipesDownloadedListener);
        else
            recipesDownloadedListener.onRecipesDownloaded(new ArrayList<Recipe>());
    }

    //RECIPE FROM INTENT----------------------------------------------------------------------------

    /**
     * Gets a recipe id from intent.
     *
     * @param intent
     * @param savedInstanceState
     * @return
     */
    public static int getRecipeIdFromIntent(Intent intent, Bundle savedInstanceState) {
        String action = intent.getAction();
        String data = intent.getDataString();
        if (Intent.ACTION_VIEW.equals(action) && data != null) {
            String recipeIdstr = data.substring(data.lastIndexOf("/") + 1);
            try {
                int recipeId = Integer.parseInt(recipeIdstr);
                return recipeId;
            } catch (Exception e) {

            }
        }

        //from bundle
        if (savedInstanceState == null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                return extras.getInt("RECIPE_ID", -10);
            }
        } else {
            if (savedInstanceState.containsKey("RECIPE_ID"))
                return (int) savedInstanceState.getSerializable("RECIPE_ID");
        }

        //no id found
        return -10;
    }

    //RECIPE SHARE----------------------------------------------------------------------------------

    /**
     * Share on social media
     */
    public void share(Activity activity) {
        shared(activity);
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, activity.getResources().getString(R.string.share_message) + " " + Configurations.DEEP_LINK_TO_SHARE + "/" + id);
        sendIntent.setType("text/plain");
        activity.startActivity(Intent.createChooser(sendIntent, "Share via"));
    }

}

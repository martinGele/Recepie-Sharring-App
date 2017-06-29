package com.martinrecipe.italian;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

/**
 * Created by melvin on 28/09/2016.
 * A Fragment to Display ingredients. Uses a WebView to show Directions in HTML format.
 * Supports videos, images, javascript
 */
public class DirectionsFragment extends Fragment {

    WebView webViewDirections;


    /**
     * Required empty public constructor
     */
    public DirectionsFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //get recipe id
        int recipeId = Recipe.getRecipeIdFromIntent(getActivity().getIntent(), savedInstanceState);

        //get rootview
        View rootView = inflater.inflate(R.layout.fragment_directions, container, false);

        //get webView Resource
        webViewDirections = (WebView) rootView.findViewById(R.id.webview_directions);

        //enable javascript
        webViewDirections.getSettings().setJavaScriptEnabled(true);

        //load recipe
        Recipe.loadRecipe(getActivity(), recipeId, new Recipe.onRecipeDownloadedListener() {
            @Override
            public void onRecipeDownloaded(Recipe recipe) {
                webViewDirections.loadData(Functions.HTMLTemplate(recipe.directions), "text/html; charset=utf-8", "utf-8");
            }
        });

        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        webViewDirections.destroy();
    }


}

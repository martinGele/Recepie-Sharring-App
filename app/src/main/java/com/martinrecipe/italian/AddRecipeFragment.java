package com.martinrecipe.italian;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.mikepenz.iconics.view.IconicsCompatButton;
import com.mikepenz.iconics.view.IconicsImageView;
import com.martinrecipe.neurondigital_listview.NDListview;
import com.martinrecipe.neurondigital_listview.SingleLineItem;
import com.martinrecipe.neurondigital_listview.SingleLineListAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.wasabeef.richeditor.RichEditor;

import static android.app.Activity.RESULT_OK;

/**
 * Created by melvin on 08/09/2016.
 */
public class AddRecipeFragment extends Fragment {
    Context context;
    private RichEditor mEditor;
    Spinner categorySpinner;
    List<Category> categ;
    EditText newIngredient, recipeTitle;
    IconicsCompatButton addIngredient;
    NDListview ingredientList;
    SingleLineListAdapter ingredientListViewAdapter;
    IconicsImageView imageView;

    Bitmap image;
    LoadImage loadImageHelper;
    Uri selectedImage;

    //requestCodes
    final int PICK_IMAGE = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_recipe, container, false);

        //submit button
        Button SubmitBtn = (Button) rootView.findViewById(R.id.submit);
        SubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadRecipe();
            }
        });

        //image laoder
        loadImageHelper = new LoadImage(getActivity());
        imageView = (IconicsImageView) rootView.findViewById(R.id.image);
        imageView.setPaddingDp(30);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (image != null) {
                    //delete
                    image = null;
                    imageView.setIcon("faw-picture-o");
                    imageView.setPaddingDp(30);
                } else {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
                }
            }
        });

        //recipe title
        recipeTitle = (EditText) rootView.findViewById(R.id.title);

        //ingridient list
        newIngredient = (EditText) rootView.findViewById(R.id.new_ingredient);
        addIngredient = (IconicsCompatButton) rootView.findViewById(R.id.add_ingredient);
        addIngredient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ingredientListViewAdapter.add(new SingleLineItem(newIngredient.getText().toString()));
                newIngredient.setText("");
                ingredientListViewAdapter.notifyDataSetChanged();
                ingredientList.setHeightBasedOnChildren();
            }
        });
        ingredientList = (NDListview) rootView.findViewById(R.id.listview_ingredients);
        //create List Adapter
        ingredientListViewAdapter = new SingleLineListAdapter(getContext());
        ingredientListViewAdapter.add(new SingleLineItem("test"));
        //set the adapter
        ingredientList.setAdapter(ingredientListViewAdapter);
        ingredientList.setHeightBasedOnChildren();
        ingredientList.enableSwipeUndo(new OnDismissCallback() {
            @Override
            public void onDismiss(@NonNull ViewGroup listView, @NonNull int[] reverseSortedPositions) {
                for (int i = 0; i < reverseSortedPositions.length; i++) {
                    ingredientListViewAdapter.remove(reverseSortedPositions[i]);
                }
                ingredientListViewAdapter.notifyDataSetChanged();
                ingredientList.setHeightBasedOnChildren();
            }
        });


        //directions html editor
        mEditor = (RichEditor) rootView.findViewById(R.id.description);
        mEditor.setEditorHeight(200);
        mEditor.setEditorFontColor(Color.BLACK);
        mEditor.setPadding(10, 10, 10, 10);
        mEditor.setPlaceholder(getString(R.string.recipe_directions));
        mEditor.setOnTextChangeListener(new RichEditor.OnTextChangeListener() {
            @Override
            public void onTextChange(String text) {
            }
        });

        //editor buttons

        rootView.findViewById(R.id.action_undo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.undo();
            }
        });

        rootView.findViewById(R.id.action_redo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.redo();
            }
        });

        rootView.findViewById(R.id.action_bold).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setBold();
            }
        });

        rootView.findViewById(R.id.action_italic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setItalic();
            }
        });

        rootView.findViewById(R.id.action_subscript).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setSubscript();
            }
        });

        rootView.findViewById(R.id.action_superscript).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setSuperscript();
            }
        });

        rootView.findViewById(R.id.action_strikethrough).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setStrikeThrough();
            }
        });

        rootView.findViewById(R.id.action_underline).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setUnderline();
            }
        });

        rootView.findViewById(R.id.action_bg_color).setOnClickListener(new View.OnClickListener() {
            private boolean isChanged;

            @Override
            public void onClick(View v) {
                mEditor.setTextBackgroundColor(isChanged ? Color.TRANSPARENT : Color.YELLOW);
                isChanged = !isChanged;
            }
        });

        rootView.findViewById(R.id.action_indent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setIndent();
            }
        });

        rootView.findViewById(R.id.action_outdent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setOutdent();
            }
        });

        rootView.findViewById(R.id.action_align_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setAlignLeft();
            }
        });

        rootView.findViewById(R.id.action_align_center).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setAlignCenter();
            }
        });

        rootView.findViewById(R.id.action_align_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setAlignRight();
            }
        });

        rootView.findViewById(R.id.action_blockquote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setBlockquote();
            }
        });

        rootView.findViewById(R.id.action_insert_bullets).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setBullets();
            }
        });

        rootView.findViewById(R.id.action_insert_numbers).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setNumbers();
            }
        });

        rootView.findViewById(R.id.action_insert_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Alert AddItemAlert = new Alert();
                AddItemAlert.DisplayEditText(getString(R.string.html_editor_insert_image_title), getString(R.string.html_editor_insert_image_description), getString(R.string.html_editor_insert_image_hint), getString(R.string.Alert_accept), getString(R.string.Alert_cancel), getActivity());
                AddItemAlert.setPositiveButtonListener(new Alert.PositiveButtonListener() {
                    @Override
                    public void onPositiveButton(String input) {
                        mEditor.insertImage(input, "");
                    }
                });
                AddItemAlert.show(getActivity().getSupportFragmentManager(), getString(R.string.add_item_dialog_title));

            }
        });

        rootView.findViewById(R.id.action_insert_link).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Alert AddItemAlert = new Alert();
                AddItemAlert.DisplayEditText2(getString(R.string.html_editor_insert_link_title), getString(R.string.html_editor_insert_link_description), getString(R.string.html_editor_insert_link_title_hint), getString(R.string.html_editor_insert_link_url_hint), getString(R.string.Alert_accept), getString(R.string.Alert_cancel), getActivity());
                AddItemAlert.setPositiveButtonListener2(new Alert.PositiveButtonListener2() {
                    @Override
                    public void onPositiveButton(String inputTitle, String inputURL) {
                        mEditor.insertLink(inputURL, inputTitle);
                    }
                });
                AddItemAlert.show(getActivity().getSupportFragmentManager(), getString(R.string.add_item_dialog_title));

            }
        });
        rootView.findViewById(R.id.action_insert_checkbox).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.insertTodo();
            }
        });

        categorySpinner = (Spinner) rootView.findViewById(R.id.category);

        Category.loadCategories(getActivity(), "", new Category.onCategoriesDownloadedListener() {
            @Override
            public void onCategoriesDownloaded(List<Category> categories) {
                categ = categories;
                String[] categoryNames = new String[categories.size()];
                for (int i = 0; i < categories.size(); i++) {
                    categoryNames[i] = categories.get(i).name;
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, categoryNames);
                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                categorySpinner.setAdapter(adapter);
            }
        });

        return rootView;
    }


    /**
     * Upload a recipe to server via POST HTTP request
     */
    public void uploadRecipe() {
        //url
        String url = Configurations.SERVER_URL + "api/recipe";

        //check if image loaded
        if (image == null) {
            Toast.makeText(getContext(), getString(R.string.add_image), Toast.LENGTH_LONG).show();
            return;
        }

        //upload
        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, url, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                //upload success
                String resultResponse = new String(response.data);
                System.out.println("Server says: " + resultResponse);
                Toast.makeText(getContext(), getString(R.string.recipe_submitted), Toast.LENGTH_LONG).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //error. Probably no internet
                Functions.noInternetAlert(getActivity());

                //show error
                if (error.networkResponse.data != null) {
                    try {
                        String body = new String(error.networkResponse.data, "UTF-8");
                        System.out.println(body);

                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        }) {


            @Override
            protected Map<String, String> getParams() {
                //get category id
                int categoryId = 0;
                if (categ.size() >= categorySpinner.getSelectedItemPosition())
                    categoryId = categ.get(categorySpinner.getSelectedItemPosition()).id;

                //fill up parameters
                Map<String, String> params = new HashMap<>();
                params.put("name", recipeTitle.getText().toString());
                params.put("directions", mEditor.getHtml());
                params.put("category", "" + categoryId);
                for (int i = 0; i < ingredientListViewAdapter.getItems().size(); i++) {
                    System.out.println(ingredientListViewAdapter.getItems().get(i).text1);
                    params.put("ingredients[" + i + "]", ingredientListViewAdapter.getItems().get(i).text1);
                }
                params.put("ingredients[" + ingredientListViewAdapter.getItems().size() + "]", "extra");

                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                //compress image to JPEG
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] byteArray = stream.toByteArray();

                //add image to parameters
                Map<String, DataPart> params = new HashMap<>();
                params.put("image", new DataPart("image.jpg", byteArray, "image/jpg"));

                return params;
            }
        };

        //send
        multipartRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(getActivity().getBaseContext()).addToRequestQueue(multipartRequest);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PICK_IMAGE:
                if (resultCode == RESULT_OK && null != data) {
                    selectedImage = data.getData();
                    Bitmap image = loadImageHelper.decodeSampledBitmapFromResource(selectedImage, 600, 300);
                    if (image != null) {
                        this.image = image;
                        imageView.setImageBitmap(image);
                        imageView.setPaddingDp(0);
                    }
                }
                break;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Bitmap image = loadImageHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (image != null) {
            this.image = image;
            imageView.setImageBitmap(image);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = getActivity();
        getActivity().setTitle(getString(R.string.add_recipe_title));
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}

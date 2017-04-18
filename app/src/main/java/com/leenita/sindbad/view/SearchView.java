package com.leenita.sindbad.view;


import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.leenita.sindbad.R;
import com.leenita.sindbad.SearchActivity;
import com.leenita.sindbad.SindbadApp;
import com.leenita.sindbad.fragments.DiagPickFilter;
import com.leenita.sindbad.model.SindCategory;

import java.util.ArrayList;


public class SearchView extends FrameLayout implements OnClickListener {

    String keyWord;
    EditText etSearch;
    View btnFilter;
    View btnMode;
    SearchViewCallback callback;
    ArrayList<SindCategory> selectedCategories;

    TextView.OnEditorActionListener callbackSearchQueryChange = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                keyWord = v.getText().toString();

                if (callback != null)
                    callback.onSearchForKeyword(keyWord);
                return true;
            }
            return false;
        }
    };

    TextWatcher textWatcherSearchChangeWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (editable == null || editable.toString() == null || editable.toString().isEmpty())
                keyWord = "";

            if (callback != null)
                callback.onSearchForKeyword(keyWord);
        }
    };


    public SearchView(Context context) {
        super(context);
        initialize();
    }

    public SearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public SearchView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    /**
     * Inflates and initializes the views and tool of this custom view
     */
    void initialize() {
        try {
            if (!isInEditMode()) {
                inflate(getContext(), R.layout.layout_search_bar, this);
                // views
                etSearch = (EditText) findViewById(R.id.etSearch);
                btnFilter = findViewById(R.id.btnFilter);
                btnMode = findViewById(R.id.btnMode);
                //listeners
                etSearch.setOnEditorActionListener(callbackSearchQueryChange);
                etSearch.addTextChangedListener(textWatcherSearchChangeWatcher);
                btnFilter.setOnClickListener(this);
                btnMode.setOnClickListener(this);
            }
        } catch (Exception e) {
        }
    }

    /**
     * Updates the view data
     */
    public void updateUI(String keyWord) {
        this.keyWord = keyWord;
    }


    private void switchSearchResultViewMode()
    {
        SearchActivity a = (SearchActivity) getContext();
        a.switchResultMode();
    }
    @Override
    public void onClick(View v) {
        try {
            switch (v.getId()) {
                case R.id.btnFilter:
                    DiagPickFilter diag = new DiagPickFilter(getContext(), selectedCategories, callback);
                    diag.show();
                    break;
                case R.id.btnMode:
                    switchSearchResultViewMode();
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeKeyBorad() {
        SindbadApp.hideKeyboard(etSearch);
    }

    public String getKeyWord() {
        if(etSearch != null)
            return etSearch.getText().toString();
        return "";
    }

    public void setSearchListener(SearchViewCallback listener) {
        this.callback = listener;
    }

    public void setSelectedCategories (ArrayList<SindCategory> categoriesFilters) {
        this.selectedCategories = categoriesFilters;
    }

    public ArrayList<SindCategory> getSelectedCategories()
    {
        return selectedCategories;
    }

    public interface SearchViewCallback {
        public void onFilterSelect(ArrayList<SindCategory> categories);
        public void onSearchForKeyword(String keyWord);
    }
}

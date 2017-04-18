package com.sithagi.countrycodepicker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency; 
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64; 
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class CountryPicker extends DialogFragment implements
		Comparator<Country> {

	private EditText searchEditText;
	private ListView countryListView;
	private LinearLayout indexLayout;
    Typeface fontFaceRobotoRegular;
	private CountryListAdapter adapter;

	private List<Country> allCountriesList;

	private List<Country> selectedCountriesList;

	private CountryPickerListener listener;

	public void setListener(CountryPickerListener listener) {
		this.listener = listener;
	}

	public EditText getSearchEditText() {
		return searchEditText;
	}

	public ListView getCountryListView() {
		return countryListView;
	}

	public static Currency getCurrencyCode(String countryCode) {
		try {
			return Currency.getInstance(new Locale("en", countryCode));
		} catch (Exception e) {

		}
		return null;
	}

	private List<Country> getAllCountries() {
		if (allCountriesList == null) {
			try {
				allCountriesList = new ArrayList<Country>();
				
				String allCountriesCode = readEncodedJsonString(getActivity());
				
				JSONArray countrArray = new JSONArray(allCountriesCode);
				
				for (int i = 0; i < countrArray.length(); i++) {
					JSONObject jsonObject = countrArray.getJSONObject(i);
					String countryName = jsonObject.getString("name");
					String countryDialCode = jsonObject.getString("dial_code");
					String countryCode = jsonObject.getString("code");

					//Alpha
					Locale l = new Locale("", countryCode);
					countryName = l.getDisplayCountry();
					//!Alpha

					Country country = new Country();
					country.setCode(countryCode);
					country.setName(countryName);
					country.setDialCode(countryDialCode);
					allCountriesList.add(country);
				}

				Collections.sort(allCountriesList, this);

				selectedCountriesList = new ArrayList<Country>();
				selectedCountriesList.addAll(allCountriesList);

				// Return
				return allCountriesList;

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private static String readEncodedJsonString(Context context)
			throws java.io.IOException {
		String base64 = context.getResources().getString(R.string.countries_code);
		byte[] data = Base64.decode(base64, Base64.DEFAULT);
		return new String(data, "UTF-8");
	}

	/**
	 * To support show as dialog
	 * 
	 * @param dialogTitle
	 * @return
	 */
	public static CountryPicker newInstance(String dialogTitle) {
		CountryPicker picker = new CountryPicker();
		Bundle bundle = new Bundle();
		bundle.putString("dialogTitle", dialogTitle);
		picker.setArguments(bundle);
		return picker;
	}
 
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) { 
		View view = inflater.inflate(R.layout.country_picker, null);
        hideKeyboardWhenTouchOut(getActivity() , view);
        fontFaceRobotoRegular = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
		getAllCountries();
 
		Bundle args = getArguments();
		if (args != null) {
			String dialogTitle = args.getString("dialogTitle");
			getDialog().setTitle(dialogTitle);

			int width = getResources().getDimensionPixelSize(
					R.dimen.cp_dialog_width);
			int height = getResources().getDimensionPixelSize(
					R.dimen.cp_dialog_height);
			getDialog().getWindow().setLayout(width, height);
		}
 
		searchEditText = (EditText) view
				.findViewById(R.id.country_code_picker_search);
        searchEditText.setTypeface(fontFaceRobotoRegular);

        countryListView = (ListView) view
				.findViewById(R.id.country_code_picker_listview);

		indexLayout = (LinearLayout) view.findViewById(R.id.side_index);

		adapter = new CountryListAdapter(getActivity(), selectedCountriesList ,countryListView, indexLayout);
		countryListView.setAdapter(adapter);
        countryListView.setFastScrollEnabled(true);

		countryListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (listener != null) {
					Country country = selectedCountriesList.get(position);
                    view.setSelected(true);
					listener.onSelectCountry(country.getName(),
							country.getCode(),country.getDialCode());
				}
			}
		});
 
		searchEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				search(s.toString());
				//Show hide the letters index when search is active
				if(s.toString().length() > 0)
					indexLayout.setVisibility(View.GONE);
				else
					indexLayout.setVisibility(View.VISIBLE);
			}
		});

		return view;
	} 
	
	@SuppressLint("DefaultLocale")
	private void search(String text) {
		selectedCountriesList.clear();

		for (Country country : allCountriesList) {
			if (country.getName().toLowerCase(Locale.ENGLISH)
					.contains(text.toLowerCase())) {
				selectedCountriesList.add(country);
			}
		}

		adapter.notifyDataSetChanged();
	}
 
	@Override
	public int compare(Country lhs, Country rhs) {
		return lhs.getName().compareTo(rhs.getName());
	}



    public static void hideKeyboard(View v) {
        try {
            InputMethodManager imm = (InputMethodManager) v.getContext().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }catch (Exception ignored){}
    }

    public static void hideKeyboardWhenTouchOut(Activity activity , View parentView)
    {
        //Set up touch listener for non-text box views to hide keyboard.
        if(!(parentView instanceof EditText)) {

            parentView.setOnTouchListener(new View.OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {
                    hideKeyboard(v);
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (parentView instanceof ViewGroup) {

            for (int i = 0; i < ((ViewGroup) parentView).getChildCount(); i++) {

                View innerView = ((ViewGroup) parentView).getChildAt(i);

                hideKeyboardWhenTouchOut(activity , innerView);
            }
        }

    }

}

package com.sithagi.countrycodepicker;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;


import com.sithagi.countrycodepicker.R.drawable;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;
 

public class CountryListAdapter extends BaseAdapter implements SectionIndexer , View.OnClickListener {

	private Context context;
	List<Country> countries;
	LayoutInflater inflater;
    Typeface fontFaceRobotoRegular;
    LinearLayout indexLayout;
    ListView lvCountries;
    private HashMap<String, Integer> alphaIndexer;
    private String[] sections;


	private int getResId(String drawableName) {

		try {
			Class<drawable> res = R.drawable.class;
			Field field = res.getField(drawableName);
			int drawableId = field.getInt(null);
			return drawableId;
		} catch (Exception e) {
			Log.e("CountryCodePicker", "Failure to get drawable id.", e);
		}
		return -1;
	}

	public CountryListAdapter(Context context, List<Country> countries , ListView lvCountries ,LinearLayout indexLayout) {
		super();
        this.indexLayout = indexLayout;
        this.lvCountries = lvCountries;
		this.context = context;
		this.countries = countries;
        alphaIndexer = new HashMap<String, Integer>();
        fontFaceRobotoRegular = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Regular.ttf");
        for (int i = 0; i < countries.size(); i++)
        {
            String s = countries.get(i).getName().substring(0, 1).toUpperCase();
            if (!alphaIndexer.containsKey(s))
                alphaIndexer.put(s, i);
        }
        Set<String> sectionLetters = alphaIndexer.keySet();
        ArrayList<String> sectionList = new ArrayList<String>(sectionLetters);
        Collections.sort(sectionList);
        sections = new String[sectionList.size()];
        for (int i = 0; i < sectionList.size(); i++)
            sections[i] = sectionList.get(i);

        inflater = (LayoutInflater) this.context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        displayIndex();
	}

	private void displayIndex() {

		TextView textView;
		for (String index : sections) {
			textView = (TextView) inflater.inflate(
					R.layout.side_index_item, null);
            textView.setTypeface(fontFaceRobotoRegular);
			textView.setText(index);
			textView.setOnClickListener(this);
			indexLayout.addView(textView);
		}
	}

	@Override
	public int getCount() {
		return countries.size();
	}

	@Override
	public Country getItem(int arg0) {
		return countries.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}
 
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View cellView = convertView;
		Cell cell;
		Country country = countries.get(position);

		if (convertView == null) {
			cell = new Cell();
			cellView = inflater.inflate(R.layout.row, null);
			cell.textView = (TextView) cellView.findViewById(R.id.row_title);
            cell.textViewCode = (TextView) cellView.findViewById(R.id.row_code);
            cell.textViewCode.setTypeface(fontFaceRobotoRegular);
            cell.textView.setTypeface(fontFaceRobotoRegular);
			cell.imageView = (ImageView) cellView.findViewById(R.id.row_icon);
			cellView.setTag(cell);
		} else {
			cell = (Cell) cellView.getTag();
		}

		cell.textView.setText(country.getName());
        cell.textViewCode.setText(country.getDialCode());
		String drawableName = "flag_"
				+ country.getCode().toLowerCase(Locale.ENGLISH);
		cell.imageView.setImageResource(getResId(drawableName));
        cellView.setSelected(false);
		return cellView;
	}

    public int getPositionForSection(int section)
    {
        return alphaIndexer.get(sections[section]);
    }

    public int getSectionForPosition(int position)
    {
        return 1;
    }

    public Object[] getSections()
    {
        return sections;
    }

    @Override
    public void onClick(View view) {
            TextView selectedIndex = (TextView) view;
            lvCountries.setSelection(alphaIndexer.get(selectedIndex.getText()));
    }

    static class Cell {
		public TextView textView;
        public TextView textViewCode;
		public ImageView imageView;
	}

}
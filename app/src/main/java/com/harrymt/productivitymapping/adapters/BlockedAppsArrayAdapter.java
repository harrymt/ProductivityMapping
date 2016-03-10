package com.harrymt.productivitymapping.adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.harrymt.productivitymapping.BlockedApps;
import com.harrymt.productivitymapping.R;

import java.util.ArrayList;


/**
 *
 * Array Adapter for a listview with a checkbox in its row.
 *
 * Aimed at the list of blocked apps.
 *
 */
public class BlockedAppsArrayAdapter extends ArrayAdapter<BlockedApps> {

    public static class BlockedAppsViewHolder {
        public TextView name;
        public TextView package_name;
        public CheckBox isSelected;
        public ImageView icon;
    }

    ArrayList<Boolean> itemChecked = new ArrayList<>();

    /**
     * Constructor
     *
     * @param context The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     */
    public BlockedAppsArrayAdapter(Context context, @LayoutRes int resource, ArrayList<BlockedApps> values, ArrayList<Boolean> checkedItems) {
        super(context, resource, 0, values);
        itemChecked = checkedItems;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row = convertView;
        BlockedAppsViewHolder holder;

        if(row == null) {
            // Inflate for each row
            row = LayoutInflater.from(getContext()).inflate(R.layout.list_apps_row, null);

            // View Holder Object to contain row elements
            holder = new BlockedAppsViewHolder();
            holder.name = (TextView) row.findViewById(R.id.tvAppName);
            holder.package_name = (TextView) row.findViewById(R.id.tvPackageName);
            holder.isSelected = (CheckBox) row.findViewById(R.id.cbAppSelected);
            holder.icon = (ImageView) row.findViewById(R.id.ivIcon);

            holder.isSelected.setTag(position);

            // Set holder with LayoutInflater
            row.setTag(holder);

        } else {
            holder = (BlockedAppsViewHolder) row.getTag();
        }

        // Get each Model object from Arraylist
        BlockedApps app = getItem( position );

        // Set holder
        holder.name.setText(app.name + (app.isPopular ? " ★" : ""));
        holder.package_name.setText(app.package_name);
        holder.icon.setImageDrawable(app.icon);

        holder.isSelected.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                CheckBox cb = (CheckBox) v.findViewById(R.id.cbAppSelected);

                if (cb.isChecked()) {
                    itemChecked.set(position, true);
                } else {
                    itemChecked.set(position, false);
                }
            }
        });
        holder.isSelected.setChecked(itemChecked.get(position)); // this will Check or Uncheck the

        return row;
    }

    /**
     * Get the package names of all the selected items.
     *
     * @return a string array of package names.
     */
    public String[] getSelectedItemsPackageNames() {
        ArrayList<String> packages = new ArrayList<>();
        for(int i = 0; i < itemChecked.size(); i++) {
            if(itemChecked.get(i)) { // if this item is checked
                packages.add(this.getItem(i).package_name);
            }
        }

        return packages.toArray(new String[packages.size()]);
    }
}
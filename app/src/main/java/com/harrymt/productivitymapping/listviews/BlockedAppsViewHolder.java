package com.harrymt.productivitymapping.listviews;

import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * View Holder for a row in the blocked apps list.
 */
public class BlockedAppsViewHolder {

    // Information about the app
    public TextView name;
    public TextView package_name;

    // If the user has selected the app
    public CheckBox isSelected;

    // Image of app
    public ImageView icon;
}
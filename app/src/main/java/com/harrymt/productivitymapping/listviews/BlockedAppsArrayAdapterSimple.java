package com.harrymt.productivitymapping.listviews;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.harrymt.productivitymapping.coredata.BlockedApps;
import com.harrymt.productivitymapping.PROJECT_GLOBALS;
import com.harrymt.productivitymapping.R;
import java.util.ArrayList;

/**
 * Array Adapter for a list view that displays an image and 2 text views.
 * Aimed to be showing some blocked apps. Not selecting them.
 */
public class BlockedAppsArrayAdapterSimple extends ArrayAdapter<BlockedApps> {
    private static final String TAG = PROJECT_GLOBALS.LOG_NAME + "BAppsAASimple";

    /**
     * Constructor.
     *
     * @param context The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     */
    public BlockedAppsArrayAdapterSimple(Context context, @LayoutRes int resource, ArrayList<BlockedApps> values) {
        super(context, resource, 0, values);
    }

    /**
     * Gets the view at the current position. Recycle the view if its already
     * been created, if not, create it.
     *
     * @param position row position.
     * @param convertView View of current item.
     * @param parent Parent view (list view).
     * @return The view at the position.
     */
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row = convertView;
        BlockedAppsViewHolder holder;

        if(row == null) {
            // Inflate for each row
            row = LayoutInflater.from(getContext()).inflate(R.layout.list_apps_row_simple, null);

            // View Holder Object to contain row elements
            holder = new BlockedAppsViewHolder();
            holder.name = (TextView) row.findViewById(R.id.tvAppName);
            holder.package_name = (TextView) row.findViewById(R.id.tvPackageName);
            holder.icon = (ImageView) row.findViewById(R.id.ivIcon);

            // Set holder with LayoutInflater
            row.setTag(holder);

        } else {
            holder = (BlockedAppsViewHolder) row.getTag();
        }

        // Get each Model object from ArrayList
        BlockedApps app = getItem( position );

        // Set holder
        holder.name.setText(app.name + (app.isPopular ? " ★" : ""));
        holder.package_name.setText(app.package_name);
        holder.icon.setImageDrawable(app.icon);

        return row;
    }
}
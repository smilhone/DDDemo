package com.smilhone.doordashdemo.adapters;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.smilhone.doordashdemo.R;
import com.smilhone.doordashdemo.database.MetadataDatabase;

/**
 * RestaurantList adapter.
 *
 * Created by smilhone on 11/22/2017.
 */

public class RestaurantListAdapter extends RecyclerView.Adapter<RestaurantListAdapter.ViewHolder> {
    private Cursor mCursor;
    private int mRestaurantNameColumnIndex;
    private int mRestaurantDescriptionColumnIndex;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.restaurant_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        String name = mCursor.getString(mRestaurantNameColumnIndex);
        String description = mCursor.getString(mRestaurantDescriptionColumnIndex);
        Log.v("Adapter:", "Name: " + name + ": " + description);
        holder.mRestaurantNameTextView.setVisibility(View.VISIBLE);
        holder.mRestaurantNameTextView.setText(mCursor.getString(mRestaurantNameColumnIndex));

        holder.mRestaurantDescriptionTextView.setVisibility(View.VISIBLE);
        holder.mRestaurantDescriptionTextView.setText(mCursor.getString(mRestaurantDescriptionColumnIndex));
    }

    @Override
    public int getItemCount() {
        return mCursor != null ? mCursor.getCount() : 0;
    }

    public void swapCursor(Cursor cursor) {
        loadColumnIndices(cursor);
        mCursor = cursor;
        notifyDataSetChanged();
    }

    private void loadColumnIndices(Cursor cursor) {
        if (cursor != null) {
            mRestaurantNameColumnIndex = cursor.getColumnIndex(MetadataDatabase.RestaurantsTableColumns.NAME);
            mRestaurantDescriptionColumnIndex = cursor.getColumnIndex(
                    MetadataDatabase.RestaurantsTableColumns.DESCRIPTION);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView mRestaurantNameTextView;
        TextView mRestaurantDescriptionTextView;
        ViewHolder(View itemView) {
            super(itemView);
            mRestaurantNameTextView = itemView.findViewById(R.id.restaurant_name);
            mRestaurantDescriptionTextView = itemView.findViewById(R.id.restaurant_description);
        }
    }
}

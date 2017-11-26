package com.smilhone.doordashdemo.adapters;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.smilhone.doordashdemo.OnFavoriteButtonListener;
import com.smilhone.doordashdemo.R;
import com.smilhone.doordashdemo.common.CursorUtils;
import com.smilhone.doordashdemo.database.MetadataDatabase;

import java.lang.ref.WeakReference;

/**
 * RestaurantList adapter.
 *
 * Created by smilhone on 11/22/2017.
 */

public class RestaurantListAdapter extends RecyclerView.Adapter<RestaurantListAdapter.ViewHolder> {
    private Cursor mCursor;
    private int mRestaurantNameColumnIndex;
    private int mRestaurantDescriptionColumnIndex;
    private int mCoverImgUrlColumnIndex;
    private int mStatusColumnIndex;

    private View.OnClickListener mOnButtonClickListener = new OnFavoriteButtonClick();
    private WeakReference<OnFavoriteButtonListener> mFavoriteButtonListener;

    public void setFavoriteButtonListener(OnFavoriteButtonListener listener) {
        mFavoriteButtonListener = new WeakReference<OnFavoriteButtonListener>(listener);
    }

    public OnFavoriteButtonListener getFavoriteButtonListener() {
        return mFavoriteButtonListener == null ? null : mFavoriteButtonListener.get();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.restaurant_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        holder.mRestaurantNameTextView.setVisibility(View.VISIBLE);
        holder.mRestaurantNameTextView.setText(mCursor.getString(mRestaurantNameColumnIndex));

        holder.mRestaurantDescriptionTextView.setVisibility(View.VISIBLE);
        holder.mRestaurantDescriptionTextView.setText(mCursor.getString(mRestaurantDescriptionColumnIndex));

        holder.mStatusTextView.setText(mCursor.getString(mStatusColumnIndex));
        holder.mParentView.setTag(R.id.tag_adapter_position, mCursor.getPosition());

        holder.mFavoriteButtom.setOnClickListener(mOnButtonClickListener);
        Integer isFavorite = mCursor.getInt(mCursor.getColumnIndex(MetadataDatabase.RestaurantsTableColumns.IS_FAVORITE));
        holder.mFavoriteButtom.setChecked(isFavorite != 0);

        Glide.with(holder.mImageView.getContext()).load(mCursor.getString(mCoverImgUrlColumnIndex)).into(holder.mImageView);
    }

    @Override
    public void onViewRecycled(ViewHolder viewHolder) {
        viewHolder.mFavoriteButtom.setOnClickListener(null);
        Glide.clear(viewHolder.mImageView);
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
            mCoverImgUrlColumnIndex = cursor.getColumnIndex(MetadataDatabase.RestaurantsTableColumns.COVER_IMG_URL);
            mStatusColumnIndex = cursor.getColumnIndex(MetadataDatabase.RestaurantsTableColumns.STATUS);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView mRestaurantNameTextView;
        TextView mRestaurantDescriptionTextView;
        TextView mStatusTextView;
        ImageView mImageView;
        CheckBox mFavoriteButtom;
        View mParentView;

        ViewHolder(View itemView) {
            super(itemView);

            mParentView = itemView.findViewById(R.id.restaurant_item);
            mRestaurantNameTextView = itemView.findViewById(R.id.restaurant_name);
            mRestaurantDescriptionTextView = itemView.findViewById(R.id.restaurant_description);
            mImageView = itemView.findViewById(R.id.restaurant_thumbnail);
            mStatusTextView = itemView.findViewById(R.id.status);
            mFavoriteButtom = itemView.findViewById(R.id.favorite_button);
        }
    }

    private class OnFavoriteButtonClick implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            View parentItem = findParentWithId(view, R.id.restaurant_item);
            if (parentItem != null) {
                int adapterPosition = (Integer) parentItem.getTag(R.id.tag_adapter_position);
                mCursor.moveToPosition(adapterPosition);
                ContentValues itemClicked = new ContentValues();
                DatabaseUtils.cursorRowToContentValues(mCursor, itemClicked);

                OnFavoriteButtonListener listener = getFavoriteButtonListener();
                if (listener != null) {
                    listener.onFavoriteButtonClicked(parentItem.getContext(), itemClicked);
                }
            }
        }

        private View findParentWithId(View view, int id) {
            View result = view;
            while (result != null && result.getId() != id && result.getParent() instanceof View) {
                result = (View) result.getParent();
            }
            return result == null || result.getId() != id ? null : result;
        }
    }
}

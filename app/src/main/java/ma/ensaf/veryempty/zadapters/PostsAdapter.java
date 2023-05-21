package ma.ensaf.veryempty.zadapters;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Html;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import ma.ensaf.veryempty.R;
import ma.ensaf.veryempty.databinding.ItemPostBinding;
import ma.ensaf.veryempty.models.Posts;
import ma.ensaf.veryempty.utils.DateTimeUtils;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    private List<Posts> filtered_items;

    private Context ctx;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, int position, Posts obj);
    }

    public void SetOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ItemPostBinding binding;
        ViewHolder(final ItemPostBinding itemBinding) {
            super(itemBinding.getRoot());
            this.binding = itemBinding;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public PostsAdapter(Context ctx, List<Posts> items) {
        this.ctx = ctx;
        filtered_items = items;
    }

    public void setPostsList(List<Posts> items) {
        // then update the items
        filtered_items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ItemPostBinding binding = DataBindingUtil.inflate(layoutInflater, R.layout.item_post, parent, false);
        return new ViewHolder(binding);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        final Posts obj = filtered_items.get(position);

        // set the views
        if(obj.getUser().getImage() != null) {

            holder.binding.userNameTextView.setText(obj.getUser().getName());
            byte[] bytes = Base64.decode(obj.getUser().getImage(), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            holder.binding.userImageView.setImageBitmap(bitmap);
        }
        if(obj.getPostImage() != null) {

            byte[] bytes2 = Base64.decode(obj.getPostImage(), Base64.DEFAULT);
            Bitmap bitmap2 = BitmapFactory.decodeByteArray(bytes2, 0, bytes2.length);
            holder.binding.postImageView.setImageBitmap(bitmap2);

        }
        holder.binding.postContentTextView.setText(Html.fromHtml(obj.getPostContent()));
        holder.binding.userLocationTextView.setText(obj.getUser().getLocation());

        // show the time ago
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date datePostCreated = dateFormat.parse(obj.getTimeAgo());
            holder.binding.postTimeTextView.setText(DateTimeUtils.timeAgoTimeDiff(datePostCreated));
        } catch (ParseException e) {
            holder.binding.postTimeTextView.setText(obj.getTimeAgo());
            e.printStackTrace();
        }

        // to show and hide the various layouts
//        if(position % 2 != 0){
//            holder.binding.postImageView.setVisibility(View.VISIBLE);
//            holder.binding.userVerifiedImageView.setVisibility(View.GONE);
//        }else{
//            holder.binding.postImageView.setVisibility(View.GONE);
//            holder.binding.userVerifiedImageView.setVisibility(View.VISIBLE);
//            holder.binding.actionShareImageView.setVisibility(View.GONE);
//        }
//
//        if(position % 3 == 0 && position !=0){
//            holder.binding.postContentTextView.setVisibility(View.GONE);
//        }

        // Here you apply the animation when the view is bound
        setAnimation(holder.itemView, position);

        // click listeners
        holder.binding.actionLikeImageView.setOnClickListener(view -> {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(view, position, obj);
            }
        });
        holder.binding.actionShareImageView.setOnClickListener(view -> {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(view, position, obj);
            }
        });
        /* holder.binding.actionCommentImageView.setOnClickListener(view -> {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(view, position, obj);
            }
        });*/
    }

    /**
     * Here is the key method to apply the animation
     */
    private int lastPosition = -1;
    private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(ctx, R.anim.slide_in_bottom);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return filtered_items.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}

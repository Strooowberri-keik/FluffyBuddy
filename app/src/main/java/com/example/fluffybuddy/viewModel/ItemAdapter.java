package com.example.fluffybuddy.viewModel;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fluffybuddy.R;
import com.example.fluffybuddy.model.itemModel;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {
    private List<itemModel> itemList;
    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public ItemAdapter(List<itemModel> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_of_animal, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        if (itemList == null || itemList.isEmpty()) {
            return;
        }
        itemModel currentItem = itemList.get(position);

        holder.bind(currentItem);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call the onItemClick method of the listener when an item is clicked
                if (listener != null) {
                    listener.onItemClick(currentItem);
                }
            }
        });
    }

    public interface OnItemClickListener {
        void onItemClick(itemModel item);
    }

    @Override
    public int getItemCount() {
        return itemList != null ? itemList.size() : 0;
    }

    public void setItems(List<itemModel> itemList) {
        this.itemList = itemList;
        notifyDataSetChanged();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        private TextView nameTextView;
        private TextView descriptionTextView;
        private TextView locationTextView;
        private ImageView imageView;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.animalName);
            descriptionTextView = itemView.findViewById(R.id.animalAge);
            locationTextView = itemView.findViewById(R.id.animalLocation);
            imageView = itemView.findViewById(R.id.animalPhoto);
        }

        public void bind(itemModel item) {
            nameTextView.setText(item.getName());
            descriptionTextView.setText(item.getDescription());
            locationTextView.setText(item.getLocation());

            // Load the image using Glide
            if (item.getImageUrl() != null) {
                Glide.with(itemView.getContext())
                        .load(item.getImageUrl()) // The image URL from itemModel
                        .placeholder(R.drawable.ic_image) // Placeholder while loading
                        .error(R.drawable.ic_error_image) // Error image if loading fails
                        .into(imageView);
            } else {
                // If there's no image URL, you can set a default image or hide the ImageView
                imageView.setImageResource(R.drawable.default_image);
                // imageView.setVisibility(View.GONE); // If you want to hide the ImageView
            }
        }

    }
}

package com.example.freshfinds;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public ProductAdapter(Context context, List<Product> productList, OnProductClickListener listener) {
        this.context = context;
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.product_item, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        
        holder.productName.setText(product.getName());
        holder.productPrice.setText(product.getFormattedPrice());
        holder.productFarm.setText(product.getFarm());
        
        // Set the product image
        holder.productImage.setImageResource(product.getImageResourceId());
        
        // Show organic badge if applicable
        if (product.isOrganic()) {
            holder.organicBadge.setVisibility(View.VISIBLE);
            holder.organicBadge.setImageResource(R.drawable.ic_leaf);
        } else {
            holder.organicBadge.setVisibility(View.GONE);
        }
        
        // Show favorite icon if item is in favorites
        if (product.isFavorite()) {
            holder.favoriteIcon.setVisibility(View.VISIBLE);
        } else {
            holder.favoriteIcon.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage, organicBadge, favoriteIcon;
        TextView productName, productPrice, productFarm;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            
            productImage = itemView.findViewById(R.id.imageViewProduct);
            organicBadge = itemView.findViewById(R.id.imageViewOrganic);
            favoriteIcon = itemView.findViewById(R.id.imageViewFavorite);
            productName = itemView.findViewById(R.id.textViewProductName);
            productPrice = itemView.findViewById(R.id.textViewProductPrice);
            productFarm = itemView.findViewById(R.id.textViewFarmName);
            
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onProductClick(productList.get(position));
                    }
                }
            });
        }
    }
}

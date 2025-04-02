package com.example.project.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.ExpenseDetailActivity;
import com.example.project.R;
import com.example.project.models.Spending;

import java.util.List;

public class SpendingAdapter extends RecyclerView.Adapter<SpendingAdapter.ViewHolder> {

    private List<Spending> spendingList;
    private Context context;

    public SpendingAdapter(List<Spending> spendingList) {
        this.spendingList = spendingList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textPurpose, textAmount, textDate, textType;
        View itemContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textPurpose = itemView.findViewById(R.id.item_purpose);
            textAmount = itemView.findViewById(R.id.item_amount);
            textDate = itemView.findViewById(R.id.item_date);
            textType = itemView.findViewById(R.id.item_type);
            itemContainer = itemView;
        }
    }

    @NonNull
    @Override
    public SpendingAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_spending, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SpendingAdapter.ViewHolder holder, int position) {
        Spending spending = spendingList.get(position);
        holder.textPurpose.setText(spending.getPurpose());
        holder.textAmount.setText("CAD " + spending.getAmount());
        holder.textDate.setText(spending.getDate());
        holder.textType.setText(spending.getType());

        // Add click listener to navigate to detail view
        holder.itemContainer.setOnClickListener(v -> {
            Intent intent = new Intent(context, ExpenseDetailActivity.class);
            intent.putExtra("categoryName", spending.getPurpose());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return spendingList.size();
    }
}
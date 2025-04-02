package com.example.project.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.R;
import com.example.project.models.ExpenseDetail;

import java.util.List;

public class ExpenseDetailAdapter extends RecyclerView.Adapter<ExpenseDetailAdapter.ViewHolder> {

    private List<ExpenseDetail> expenseList;
    private OnDeleteClickListener deleteListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(ExpenseDetail expense, int position);
    }

    public ExpenseDetailAdapter(List<ExpenseDetail> expenseList, OnDeleteClickListener deleteListener) {
        this.expenseList = expenseList;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExpenseDetail expense = expenseList.get(position);

        holder.amountText.setText("CAD " + expense.getAmount());
        holder.dateText.setText(expense.getDate());

        holder.deleteIcon.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(expense, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    public void removeItem(int position) {
        expenseList.remove(position);
        notifyItemRemoved(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView amountText, dateText;
        ImageView deleteIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            amountText = itemView.findViewById(R.id.item_detail_amount);
            dateText = itemView.findViewById(R.id.item_detail_date);
            deleteIcon = itemView.findViewById(R.id.item_delete_icon);
        }
    }
}
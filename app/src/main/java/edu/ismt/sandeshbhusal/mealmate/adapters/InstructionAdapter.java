package edu.ismt.sandeshbhusal.mealmate.adapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.ismt.sandeshbhusal.mealmate.R;
import edu.ismt.sandeshbhusal.mealmate.model.InstructionStep;

public class InstructionAdapter extends RecyclerView.Adapter<InstructionAdapter.InstructionViewHolder> {

    private List<InstructionStep> instructionList;
    private OnDeleteClickListener onDeleteClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    public InstructionAdapter(List<InstructionStep> instructionList, OnDeleteClickListener listener) {
        this.instructionList = instructionList;
        this.onDeleteClickListener = listener;
    }

    @NonNull
    @Override
    public InstructionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_instruction, parent, false);
        return new InstructionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final InstructionViewHolder holder, final int position) {
        InstructionStep step = instructionList.get(position);
        holder.stepNumber.setText("" + (position + 1));
        holder.instructionInput.setText(step.getInstruction());

        // Update instruction text dynamically
        holder.instructionInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                instructionList.get(holder.getAdapterPosition()).setInstruction(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Delete step on click
        holder.deleteButton.setOnClickListener(v -> {
            if (onDeleteClickListener != null) {
                onDeleteClickListener.onDeleteClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return instructionList.size();
    }

    static class InstructionViewHolder extends RecyclerView.ViewHolder {
        TextView stepNumber;
        EditText instructionInput;
        Button deleteButton;

        InstructionViewHolder(View itemView) {
            super(itemView);
            stepNumber = itemView.findViewById(R.id.stepNumber);
            instructionInput = itemView.findViewById(R.id.instructionText);
            deleteButton = itemView.findViewById(R.id.deleteStepButton);
        }
    }
}

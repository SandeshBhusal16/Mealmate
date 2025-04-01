package edu.ismt.sandeshbhusal.mealmate.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

import edu.ismt.sandeshbhusal.mealmate.R;

public class InstructionDetailAdapter extends RecyclerView.Adapter<InstructionDetailAdapter.InstructionViewHolder> {

    private final Context context;
    private final List<Map<String, Object>> instructions;

    public InstructionDetailAdapter(Context context, List<Map<String, Object>> instructions) {
        this.context = context;
        this.instructions = instructions;
    }

    @NonNull
    @Override
    public InstructionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_instruction_step, parent, false);
        return new InstructionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InstructionViewHolder holder, int position) {
        Map<String, Object> step = instructions.get(position);

        // Set step number
        holder.stepNumber.setText("Step " + (position + 1));

        // Set instruction text
        String instruction = (String) step.get("instruction");
        holder.instructionText.setText(instruction);

        // Optional: Load step image if available
        // String imageUrl = (String) step.get("imageUrl");
        // Glide.with(context).load(imageUrl).into(holder.stepImage);
    }

    @Override
    public int getItemCount() {
        return instructions.size();
    }

    public static class InstructionViewHolder extends RecyclerView.ViewHolder {
        TextView stepNumber, instructionText;
        ImageView stepImage;

        public InstructionViewHolder(@NonNull View itemView) {
            super(itemView);
            stepNumber = itemView.findViewById(R.id.stepNumber);
            instructionText = itemView.findViewById(R.id.instructionText);

        }
    }
}
package com.example.memoria.ui.onboarding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memoria.R;
import com.example.memoria.data.model.entity.OnboardingItem;

import java.util.List;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {

    private final List<OnboardingItem> onboardingItems;

    public OnboardingAdapter(List<OnboardingItem> onboardingItems) {
        this.onboardingItems = onboardingItems;
    }

    @NonNull
    @Override
    public  OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new OnboardingViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_onboarding, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        holder.setOnboardingData(onboardingItems.get(position));
    }

    @Override
    public int getItemCount() {
        return onboardingItems.size();
    }

    public static class OnboardingViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageOnboarding;
        private final TextView textTitle;
        private final TextView textDescription;

        OnboardingViewHolder(@NonNull View view) {
            super(view);
            imageOnboarding = view.findViewById(R.id.imgOnboarding);
            textTitle = view.findViewById(R.id.tvTitle);
            textDescription = view.findViewById(R.id.tvDescription);
        }

        void setOnboardingData(OnboardingItem item) {
            imageOnboarding.setImageResource(item.getImage());
            textTitle.setText(item.getTitle());
            textDescription.setText(item.getDescription());
        }
    }
}
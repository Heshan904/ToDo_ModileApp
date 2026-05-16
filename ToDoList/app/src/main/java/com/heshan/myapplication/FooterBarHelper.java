package com.heshan.myapplication;

import android.content.Intent;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class FooterBarHelper {

    public static void setupFooter(AppCompatActivity activity) {
        ImageView profileIcon = activity.findViewById(R.id.iconProfile);
        ImageView homeIcon = activity.findViewById(R.id.iconHome);
        ImageView settingsIcon = activity.findViewById(R.id.iconSettings);

        Class<?> current = activity.getClass();

        if (profileIcon != null) {
            profileIcon.setOnClickListener(v -> navigateTo(activity, profile.class));
            styleNavIcon(profileIcon, current == profile.class);
        }

        if (homeIcon != null) {
            homeIcon.setOnClickListener(v -> navigateTo(activity, home.class));
            styleNavIcon(homeIcon, current == home.class);
        }

        if (settingsIcon != null) {
            settingsIcon.setOnClickListener(v -> navigateTo(activity, settings.class));
            styleNavIcon(settingsIcon, current == settings.class || current == dev_info.class);
        }
    }

    private static void styleNavIcon(ImageView icon, boolean selected) {
        float scale = selected ? 1.12f : 1f;
        icon.setScaleX(scale);
        icon.setScaleY(scale);
        icon.setAlpha(selected ? 1f : 0.72f);
    }

    private static void navigateTo(AppCompatActivity activity, Class<? extends AppCompatActivity> target) {
        if (activity.getClass().equals(target)) {
            return;
        }
        Intent intent = new Intent(activity, target);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activity.startActivity(intent);
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}

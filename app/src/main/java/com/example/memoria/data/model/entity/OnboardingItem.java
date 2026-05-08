package com.example.memoria.data.model.entity;

/**
 * A UI model class representing an item in the onboarding (introductory) screens.
 * * Note: This class is NOT annotated with @Entity because it is not stored in the Room database.
 * It is solely used for binding data to the User Interface (ViewPager/RecyclerView)
 * during the initial app launch.
 */
public class OnboardingItem {

    // ========================================================================
    // UI Information
    // ========================================================================

    /**
     * The drawable resource ID for the onboarding image.
     */
    private final int image;

    /**
     * The main title text displayed on the onboarding screen.
     */
    private final String title;

    /**
     * The detailed description text displayed below the title.
     */
    private final String description;

    // ========================================================================
    // Constructor & Getters
    // ========================================================================

    /**
     * Constructs a new OnboardingItem.
     *
     * @param image       The drawable resource ID for the image.
     * @param title       The main title text.
     * @param description The detailed description text.
     */
    public OnboardingItem(int image, String title, String description) {
        this.image = image;
        this.title = title;
        this.description = description;
    }

    /**
     * @return The drawable resource ID for the image.
     */
    public int getImage() { return image; }

    /**
     * @return The main title text.
     */
    public String getTitle() { return title; }

    /**
     * @return The detailed description text.
     */
    public String getDescription() { return description; }
}

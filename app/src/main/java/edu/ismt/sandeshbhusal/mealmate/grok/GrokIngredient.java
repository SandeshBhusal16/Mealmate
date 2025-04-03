package edu.ismt.sandeshbhusal.mealmate.grok;

public class GrokIngredient {
    public String name, date, category;
    public boolean isPurchased;
    public float price;

    public GrokIngredient(String name, String date, String category, boolean isPurchased, float price) {
        this.name = name;
        this.date = date;
        this.category = category;
        this.isPurchased = isPurchased;
        this.price = price;
    }
}
package edu.ismt.sandeshbhusal.mealmate.grok;

class GrokIngredient {
    String name, date, category;
    boolean isPurchased;
    float price;

    GrokIngredient(String name, String date, String category, boolean isPurchased, float price) {
        this.name = name;
        this.date = date;
        this.category = category;
        this.isPurchased = isPurchased;
        this.price = price;
    }
}
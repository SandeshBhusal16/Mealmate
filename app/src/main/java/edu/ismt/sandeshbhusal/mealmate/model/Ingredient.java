package edu.ismt.sandeshbhusal.mealmate.model;

public class Ingredient {
    private String name;
    private boolean selected;
    private String quantity = "1";
    private double price;

    // Constructor
    public Ingredient(String name) {
        this.name = name;
    }

    // Getters & Setters
    public String getName() { return name; }
    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }
    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}
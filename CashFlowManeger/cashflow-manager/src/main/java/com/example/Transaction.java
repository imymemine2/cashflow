package com.example; // あなたのパッケージ名に合わせてください

import java.time.LocalDate;

public class Transaction {
    private int id;
    private LocalDate date;
    private double amount; // 金額
    private String type;   // "INCOME" または "EXPENSE"
    private int categoryId;
    private String categoryName; // UI表示用にCategoryDAOから取得した名前を保持

    // コンストラクタ
    public Transaction(int id, LocalDate date, double amount, String type, int categoryId, String categoryName) {
        this.id = id;
        this.date = date;
        this.amount = amount;
        this.type = type;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    // ゲッター (JavaFXのTableViewで表示するために必要)
    public int getId() { return id; }
    public LocalDate getDate() { return date; }
    public double getAmount() { return amount; }
    public String getType() { return type; }
    public int getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    
    // 金額を整形して文字列で返すヘルパーメソッド（UI表示用）
    public String getFormattedAmount() {
        return String.format("%,.0f円", amount); // 例: 1,000円
    }
}

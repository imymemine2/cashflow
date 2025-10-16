// MonthlySummary.java
package com.example;

// UIのTableView表示用に使うデータモデル
public class MonthlySummary {
    private String month;
    private double income;
    private double expense;
    private double netProfit; // 純利益 (収入 - 支出)

    public MonthlySummary(String month, double income, double expense) {
        this.month = month;
        this.income = income;
        this.expense = expense;
        this.netProfit = income - expense;
    }

    // ゲッター (TableView表示に必要。MainControllerで使用します)
    public String getMonth() { return month; }
    public double getIncome() { return income; }
    public double getExpense() { return expense; }
    public double getNetProfit() { return netProfit; }

    // UI表示用に金額を整形するメソッド
    public String getFormattedIncome() { return String.format("%,.0f円", income); }
    public String getFormattedExpense() { return String.format("%,.0f円", expense); }
    public String getFormattedNetProfit() { 
        String sign = netProfit >= 0 ? "" : "-";
        return sign + String.format("%,.0f円", Math.abs(netProfit)); 
    }
}
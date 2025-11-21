package com.example.CashFlowWeb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBManager {

    private static final String URL = "jdbc:sqlite:cashflow.db";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            // 1. カテゴリテーブル
            String sqlCategory = "CREATE TABLE IF NOT EXISTS categories (" +
                                 "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                 "name TEXT NOT NULL," +
                                 "type TEXT NOT NULL CHECK(type IN ('INCOME', 'EXPENSE'))" +
                                 ");";
            stmt.execute(sqlCategory);

            // 2. 取引テーブル
            String sqlTransaction = "CREATE TABLE IF NOT EXISTS transactions (" +
                                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                    "date TEXT NOT NULL," +
                                    "amount REAL NOT NULL," +
                                    "type TEXT NOT NULL CHECK(type IN ('INCOME', 'EXPENSE'))," +
                                    "category_id INTEGER," +
                                    "is_future BOOLEAN NOT NULL DEFAULT FALSE," +
                                    "is_extraordinary BOOLEAN NOT NULL DEFAULT FALSE," +
                                    "FOREIGN KEY (category_id) REFERENCES categories(id)" +
                                    ");";
            stmt.execute(sqlTransaction);
            
            // 3. 資産テーブル
            String sqlAssets = "CREATE TABLE IF NOT EXISTS assets (" +
                               "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                               "name TEXT NOT NULL," +
                               "ticker_symbol TEXT," +
                               "quantity REAL NOT NULL," +
                               "purchase_price REAL," +
                               "current_price REAL," +
                               "asset_type TEXT NOT NULL" +
                               ");";
            stmt.execute(sqlAssets);
            
            // 4. 予算テーブル
            String sqlBudgets = "CREATE TABLE IF NOT EXISTS budgets (" +
                                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                "year_month TEXT NOT NULL," + // "YYYY-MM"形式
                                "category_id INTEGER NOT NULL," +
                                "amount REAL NOT NULL," +
                                "UNIQUE(year_month, category_id)," + // 同じ月、同じカテゴリの予算は1つだけ
                                "FOREIGN KEY (category_id) REFERENCES categories(id)" +
                                ");";
            stmt.execute(sqlBudgets);

            // 5. 目標テーブル
            stmt.execute("CREATE TABLE IF NOT EXISTS goals (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "name TEXT NOT NULL, " +
                         "target_amount REAL NOT NULL, " +
                         "current_amount REAL NOT NULL DEFAULT 0, " +
                         "target_date TEXT, " +
                         "image_url TEXT)");
                         
            // --- ここから追加 ---
            // 6. ユーザーテーブル (ログイン機能で新規追加)
            String sqlUsers = "CREATE TABLE IF NOT EXISTS users (" +
                              "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                              "username TEXT NOT NULL UNIQUE, " + // ユーザー名は重複不可
                              "password_hash TEXT NOT NULL, " + // ハッシュ化されたパスワード
                              "role TEXT NOT NULL DEFAULT 'USER'" +
                              ");";
            stmt.execute(sqlUsers);
            // --- ここまで追加 ---
            
        } catch (SQLException e) {
            System.err.println("データベース初期化エラー: " + e.getMessage());
        }
    }
}
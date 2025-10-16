package com.example; 

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBManager {

    private static final String URL = "jdbc:sqlite:cashflow.db"; 

    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL);
            // System.out.println("データベースに接続しました。");
        } catch (SQLException e) {
            System.err.println("データベース接続エラー: " + e.getMessage());
        }
        return conn;
    }

    /**
     * アプリケーションに必要なカテゴリと取引のテーブルを初期作成します。
     */
    public static void createNewTables() {
        // 1. カテゴリ管理テーブルのSQL定義
        String sqlCategory = "CREATE TABLE IF NOT EXISTS categories (\n"
                + " id INTEGER PRIMARY KEY,\n"
                + " name TEXT NOT NULL UNIQUE,\n"
                + " type TEXT NOT NULL CHECK(type IN ('INCOME', 'EXPENSE'))\n"
                + ");";

        // 2. 取引登録テーブルのSQL定義
        String sqlTransaction = "CREATE TABLE IF NOT EXISTS transactions (\n"
                + " id INTEGER PRIMARY KEY,\n"
                + " date TEXT NOT NULL,\n"
                + " amount REAL NOT NULL,\n"
                + " type TEXT NOT NULL CHECK(type IN ('INCOME', 'EXPENSE')),\n"
                + " category_id INTEGER,\n"
                + " FOREIGN KEY (category_id) REFERENCES categories(id)\n"
                + ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute(sqlCategory);
            stmt.execute(sqlTransaction);
            System.out.println("データベースの初期設定（テーブル作成）が完了しました。");
            
        } catch (SQLException e) {
            System.err.println("テーブル作成エラー: " + e.getMessage());
        }
    }
}
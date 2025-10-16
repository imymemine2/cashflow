package com.example; // あなたのパッケージ名に合わせてください

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CategoryDao {
    
    /**
     * カテゴリをDBに登録します (Create)
     */
    public boolean addCategory(String name, String type) {
        String sql = "INSERT INTO categories(name, type) VALUES(?, ?)";
        try (Connection conn = DBManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, name);
            pstmt.setString(2, type.toUpperCase()); // 大文字に変換して登録
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("カテゴリ登録エラー: " + e.getMessage());
            // 通常、カテゴリ名が重複した場合に発生
            return false;
        }
    }

    /**
     * 全てのカテゴリを取得します (Read)
     */
    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        // typeでソートしてからnameでソートします
        String sql = "SELECT id, name, type FROM categories ORDER BY type, name"; 
        
        try (Connection conn = DBManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                categories.add(new Category(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("type")
                ));
            }
        } catch (SQLException e) {
            System.err.println("カテゴリ取得エラー: " + e.getMessage());
        }
        return categories;
    }

    /**
     * IDを指定してカテゴリを削除します (Delete)
     */
    public boolean deleteCategory(int id) {
        String sql = "DELETE FROM categories WHERE id = ?";
        try (Connection conn = DBManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("カテゴリ削除エラー: " + e.getMessage());
            // 削除時に外部キー制約（取引に紐づいているなど）で失敗した場合を考慮
            return false;
        }
    }
}
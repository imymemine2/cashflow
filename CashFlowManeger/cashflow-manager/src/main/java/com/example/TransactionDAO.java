package com.example; // あなたのパッケージ名に合わせてください

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    /**
     * 取引をDBに登録します (Create)
     */
    public boolean addTransaction(LocalDate date, double amount, String type, int categoryId) {
        String sql = "INSERT INTO transactions(date, amount, type, category_id) VALUES(?, ?, ?, ?)";
        try (Connection conn = DBManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, date.toString()); // LocalDateを文字列（YYYY-MM-DD）で保存
            pstmt.setDouble(2, amount);
            pstmt.setString(3, type.toUpperCase());
            pstmt.setInt(4, categoryId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("取引登録エラー: " + e.getMessage());
            return false;
        }
    }

    /**
     * 全ての取引を取得します (Read)
     * カテゴリ名も結合して取得します。
     */
    public List<Transaction> getAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        // transactionsとcategoriesテーブルを結合してカテゴリ名を取得
        String sql = "SELECT t.id, t.date, t.amount, t.type, t.category_id, c.name AS category_name " +
                     "FROM transactions t JOIN categories c ON t.category_id = c.id " +
                     "ORDER BY t.date DESC, t.id DESC"; // 日付の新しい順に表示

        try (Connection conn = DBManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                transactions.add(new Transaction(
                        rs.getInt("id"),
                        LocalDate.parse(rs.getString("date")), // 文字列からLocalDateに変換
                        rs.getDouble("amount"),
                        rs.getString("type"),
                        rs.getInt("category_id"),
                        rs.getString("category_name")
                ));
            }
        } catch (SQLException e) {
            System.err.println("取引取得エラー: " + e.getMessage());
        }
        return transactions;
    }

    /**
     * IDを指定して取引を削除します (Delete)
     */
    public boolean deleteTransaction(int id) {
        String sql = "DELETE FROM transactions WHERE id = ?";
        try (Connection conn = DBManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("取引削除エラー: " + e.getMessage());
            return false;
        }
    }

    /**
     * 月ごとの収入合計と支出合計を集計します。
     * @return 月別集計結果 (MonthlySummary) のリスト
     */
    public List<MonthlySummary> getMonthlySummaries() {
        List<MonthlySummary> summaries = new ArrayList<>();
        
        // SQLiteのsubstr関数を使って日付から年-月を抽出
        // CASE文を使って収入と支出を分けて合計（SUM）を計算
        String sql = "SELECT substr(date, 1, 7) AS month, " + 
                     "SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END) AS total_income, " +
                     "SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END) AS total_expense " +
                     "FROM transactions " +
                     "GROUP BY month " +
                     "ORDER BY month DESC"; // 新しい月から表示

        try (Connection conn = DBManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                summaries.add(new MonthlySummary( 
                    rs.getString("month"),
                    rs.getDouble("total_income"),
                    rs.getDouble("total_expense")
                ));
            }
        } catch (SQLException e) {
            System.err.println("月次集計エラー: " + e.getMessage());
        }
        return summaries;
    }
}
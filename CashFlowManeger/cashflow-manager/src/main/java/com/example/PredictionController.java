package com.example;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class PredictionController implements Initializable {

    @FXML private ComboBox<String> periodCombo;
    @FXML private TextField initialBalanceField;
    @FXML private Button generateButton;
    @FXML private LineChart<Number, Number> predictionChart;
    @FXML private Label messageLabel;

    private TransactionDAO transactionDAO = new TransactionDAO();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // 1. 予測期間の選択肢を設定
        periodCombo.getItems().addAll("1年 (12ヶ月)", "3年 (36ヶ月)", "5年 (60ヶ月)");
        periodCombo.getSelectionModel().selectFirst();
        
        // 2. 初期残高フィールドに現在の残高を自動で設定 (Optional)
        // ユーザーが手動で入力できるように、ここでは空欄にしておいても良い。
        // getCurrentBalance()メソッドをTransactionDAOに追加するか、MainControllerの残高を渡す必要があるが、ここでは簡略化。
    }

    /**
     * メニューバーからメイン画面に遷移する
     */
    @FXML
    private void showMainMenu() {
        try {
            App.setRoot("main-view"); 
        } catch (IOException e) {
            System.err.println("メイン画面への遷移中にエラーが発生しました: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 「予測グラフを生成」ボタンが押されたときの処理
     */
    @FXML
    private void handleGeneratePrediction() {
        // ここに予測ロジックを実装します
        
        // 1. 過去3ヶ月の平均純利益を計算する
        double averageNetProfit = calculateAverageNetProfit();
        if (averageNetProfit == 0) {
            messageLabel.setText("予測に必要な取引データがありません。");
            return;
        }

        // 2. 設定を読み取る
        int months = getPredictionMonths();
        double initialBalance;
        try {
            initialBalance = Double.parseDouble(initialBalanceField.getText());
        } catch (NumberFormatException e) {
            messageLabel.setText("開始残高に有効な数値を入力してください。");
            return;
        }
        
        // 3. グラフを生成
        generateGraph(months, initialBalance, averageNetProfit);
        messageLabel.setText(String.format("予測グラフを生成しました。（月平均純利益: %,.0f円）", averageNetProfit));
    }
    
    // --- 予測ロジックの実装 ---

    /**
     * 予測期間の月数をComboBoxから取得する
     */
    private int getPredictionMonths() {
        String selected = periodCombo.getSelectionModel().getSelectedItem();
        if (selected == null) return 0;
        if (selected.contains("1年")) return 12;
        if (selected.contains("3年")) return 36;
        if (selected.contains("5年")) return 60;
        return 0;
    }

    /**
     * 過去3ヶ月の取引データから月平均純利益を計算する
     */
    private double calculateAverageNetProfit() {
        List<MonthlySummary> summaries = transactionDAO.getMonthlySummaries();
        
        // 直近3ヶ月分のデータを取得
        int count = 0;
        double totalNetProfit = 0;
        for (int i = 0; i < summaries.size() && count < 3; i++) {
            totalNetProfit += summaries.get(i).getNetProfit();
            count++;
        }
        
        if (count == 0) return 0;
        
        return totalNetProfit / count;
    }

    /**
     * 予測グラフを生成し、LineChartに表示する
     */
    private void generateGraph(int months, double initialBalance, double monthlyNetProfit) {
        predictionChart.getData().clear();
        
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("予測資産残高");
        
        double currentBalance = initialBalance;
        
        // 0ヶ月目: 開始残高
        series.getData().add(new XYChart.Data<>(0, currentBalance));
        
        // 1ヶ月目から予測期間までデータを計算
        for (int i = 1; i <= months; i++) {
            currentBalance += monthlyNetProfit;
            series.getData().add(new XYChart.Data<>(i, currentBalance));
        }

        predictionChart.getData().add(series);
    }
}

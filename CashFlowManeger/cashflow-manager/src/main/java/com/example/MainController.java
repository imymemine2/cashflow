package com.example;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    // FXML 要素 (UIコンポーネント)
    // 取引登録/残高表示エリア
    @FXML private DatePicker datePicker;
    @FXML private TextField amountField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private ComboBox<Category> categoryCombo;
    @FXML private Label messageLabel;
    @FXML private Text balanceText;

    // 月別集計テーブル
    @FXML private TableView<MonthlySummary> summaryTable;
    @FXML private TableColumn<MonthlySummary, String> monthColumn;
    @FXML private TableColumn<MonthlySummary, String> totalIncomeColumn;
    @FXML private TableColumn<MonthlySummary, String> totalExpenseColumn;
    @FXML private TableColumn<MonthlySummary, String> netProfitColumn;

    // 取引一覧テーブル
    @FXML private TableView<Transaction> transactionTable;
    @FXML private TableColumn<Transaction, Integer> idColumn;
    @FXML private TableColumn<Transaction, LocalDate> dateColumn;
    @FXML private TableColumn<Transaction, String> amountColumn; 
    @FXML private TableColumn<Transaction, String> typeColumn;
    @FXML private TableColumn<Transaction, String> categoryNameColumn;

    // DAOインスタンス (CategoryDaoの命名規則に合わせる)
    private CategoryDao categoryDao = new CategoryDao(); // 👈 修正点: CategoryDao (小文字のd)
    private TransactionDAO transactionDAO = new TransactionDAO();
    
    // データリスト
    private ObservableList<Transaction> transactionList = FXCollections.observableArrayList();
    private ObservableList<MonthlySummary> summaryList = FXCollections.observableArrayList();

    /**
     * FXMLファイルロード時の初期化処理
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // 1. 日付を今日に設定
        datePicker.setValue(LocalDate.now());

        // 2. 種別コンボボックス（収入/支出）を設定
        typeCombo.getItems().addAll("収入", "支出");
        typeCombo.getSelectionModel().selectFirst();
        
        // 3. カテゴリコンボボックスの準備
        loadCategoriesToCombo();
        
        // 4. 取引一覧テーブルの列とTransactionクラスのフィールドを紐づけ
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("formattedAmount")); 
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        categoryNameColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));

        // 5. 月別集計テーブルの列とMonthlySummaryクラスのフィールドを紐づけ
        monthColumn.setCellValueFactory(new PropertyValueFactory<>("month"));
        totalIncomeColumn.setCellValueFactory(new PropertyValueFactory<>("formattedIncome")); 
        totalExpenseColumn.setCellValueFactory(new PropertyValueFactory<>("formattedExpense"));
        netProfitColumn.setCellValueFactory(new PropertyValueFactory<>("formattedNetProfit"));
        
        // 6. データロードの実行
        loadTransactions();
    }

    /**
     * DBからカテゴリを取得し、コンボボックスを更新する
     */
    private void loadCategoriesToCombo() {
        // ⬇️ 修正点: インスタンス（categoryDao）経由でメソッドを呼び出す ⬇️
        List<Category> categories = categoryDao.getAllCategories(); 
        categoryCombo.getItems().clear();
        categoryCombo.getItems().addAll(categories);
        
        if (!categories.isEmpty()) {
            categoryCombo.getSelectionModel().selectFirst();
        }
    }

    /**
     * DBから全取引を取得し、TableViewを更新する
     */
    private void loadTransactions() {
        List<Transaction> transactions = transactionDAO.getAllTransactions();
        transactionList.clear();
        transactionList.addAll(transactions);
        transactionTable.setItems(transactionList);
        
        // 取引が更新されたら残高と集計も更新
        calculateBalance();
        loadMonthlySummary(); 
    }
    
    /**
     * DBから月別集計データを取得し、summaryTableを更新する
     */
    private void loadMonthlySummary() {
        List<MonthlySummary> summaries = transactionDAO.getMonthlySummaries();
        summaryList.clear();
        summaryList.addAll(summaries);
        summaryTable.setItems(summaryList);
    }

    /**
     * UIの「取引を登録」ボタンが押されたときの処理
     */
    @FXML
    private void handleAddTransaction() {
        LocalDate date = datePicker.getValue();
        String amountText = amountField.getText().trim();
        Category category = categoryCombo.getSelectionModel().getSelectedItem();
        
        if (date == null || amountText.isEmpty() || category == null) {
            messageLabel.setText("日付、金額、カテゴリをすべて選択/入力してください。");
            return;
        }

        try {
            double amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                messageLabel.setText("金額は正の値を入力してください。");
                return;
            }
            
            // データベース登録
            if (transactionDAO.addTransaction(date, amount, category.getType(), category.getId())) {
                messageLabel.setText("取引を登録しました。");
                amountField.clear();
                loadTransactions(); // 取引一覧、残高、集計をすべて更新
            } else {
                messageLabel.setText("取引の登録に失敗しました。");
            }
        } catch (NumberFormatException e) {
            messageLabel.setText("金額には数値のみを入力してください。");
        }
    }

    /**
     * UIの「選択した取引を削除」ボタンが押されたときの処理
     */
    @FXML
    private void handleDeleteTransaction() {
        Transaction selectedTransaction = transactionTable.getSelectionModel().getSelectedItem();
        
        if (selectedTransaction == null) {
            messageLabel.setText("削除する取引を選択してください。");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, 
                                "取引ID：" + selectedTransaction.getId() + "を削除しますか？", 
                                ButtonType.YES, ButtonType.NO);
        alert.setTitle("確認");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                if (transactionDAO.deleteTransaction(selectedTransaction.getId())) {
                    messageLabel.setText("取引を削除しました。");
                    loadTransactions(); // リスト、残高、集計を更新
                } else {
                    messageLabel.setText("削除に失敗しました。");
                }
            }
        });
    }

    /**
     * 現在の残高を計算するロジック
     */
    private void calculateBalance() {
        double balance = 0;
        for (Transaction t : transactionList) {
            if (t.getType().equals("INCOME")) {
                balance += t.getAmount();
            } else if (t.getType().equals("EXPENSE")) {
                balance -= t.getAmount();
            }
        }
        
        // 残高表示の色の設定
        String color = balance >= 0 ? "GREEN" : "RED";
        String formattedBalance = String.format("%,.0f 円", balance);
        
        balanceText.setText(formattedBalance);
        balanceText.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-fill: " + color + ";");
    }

    // --- 画面遷移ロジック ---

    /**
     * メニューバーからカテゴリ管理画面に遷移する
     */
    @FXML
    private void showCategoryManagement() {
        try {
            App.setRoot("category-view"); 
        } catch (IOException e) {
            System.err.println("カテゴリ管理画面への遷移中にエラーが発生しました: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * メニューバーから予測シミュレーション画面に遷移する
     */
    @FXML
    private void showPredictionScreen() {
        try {
            App.setRoot("prediction-view"); 
        } catch (IOException e) {
            System.err.println("予測画面への遷移中にエラーが発生しました: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
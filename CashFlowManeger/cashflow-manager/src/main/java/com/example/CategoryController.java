package com.example;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class CategoryController implements Initializable {

    @FXML private TextField nameField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private TableView<Category> categoryTable;
    @FXML private TableColumn<Category, Integer> idColumn;
    @FXML private TableColumn<Category, String> nameColumn;
    @FXML private TableColumn<Category, String> typeColumn;
    @FXML private Label messageLabel;

    // ⬇️ 修正箇所 1: あなたの命名規則に合わせてインスタンスを定義します ⬇️
    private CategoryDao categoryDao = new CategoryDao(); // CategoryDao (小文字のd)
    
    private ObservableList<Category> categoryList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // コンボボックスの設定
        typeCombo.getItems().addAll("INCOME", "EXPENSE");
        typeCombo.getSelectionModel().selectFirst();

        // テーブルの列とCategoryクラスのフィールドを紐づけ
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));

        // データをロード
        loadCategories();
    }

    /**
     * DBからカテゴリを取得し、TableViewを更新する
     */
    private void loadCategories() {
        // ⬇️ 修正箇所 2: インスタンス（categoryDao）経由でメソッドを呼び出す ⬇️
        categoryList.setAll(categoryDao.getAllCategories()); 
        categoryTable.setItems(categoryList);
    }

    /**
     * UIの「追加」ボタンが押されたときの処理
     */
    @FXML
    private void handleAddCategory() {
        String name = nameField.getText().trim();
        String type = typeCombo.getSelectionModel().getSelectedItem();

        if (name.isEmpty() || type == null) {
            messageLabel.setText("名称と種別を入力してください。");
            return;
        }
        
        // ⬇️ 修正箇所 3: インスタンス（categoryDao）経由でメソッドを呼び出す ⬇️
        if (categoryDao.addCategory(name, type)) {
            messageLabel.setText("カテゴリを登録しました。");
            nameField.clear();
            loadCategories(); // リストを更新
        } else {
            messageLabel.setText("登録に失敗しました。（名称が重複していませんか？）");
        }
    }

    /**
     * UIの「選択したカテゴリを削除」ボタンが押されたときの処理
     */
    @FXML
    private void handleDeleteCategory() {
        Category selectedCategory = categoryTable.getSelectionModel().getSelectedItem();
        
        if (selectedCategory == null) {
            messageLabel.setText("削除するカテゴリを選択してください。");
            return;
        }

        // 確認ダイアログ
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, 
                                "カテゴリ「" + selectedCategory.getName() + "」を削除しますか？\n関連する取引があると削除できません。", 
                                ButtonType.YES, ButtonType.NO);
        alert.setTitle("確認");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                // ⬇️ 修正箇所 4: インスタンス（categoryDao）経由でメソッドを呼び出す ⬇️
                if (categoryDao.deleteCategory(selectedCategory.getId())) {
                    messageLabel.setText("カテゴリを削除しました。");
                    loadCategories(); // リストを更新
                } else {
                    messageLabel.setText("削除に失敗しました。（このカテゴリに関連する取引が存在します）");
                }
            }
        });
    }

    // --- 画面遷移ロジック ---
    
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
}
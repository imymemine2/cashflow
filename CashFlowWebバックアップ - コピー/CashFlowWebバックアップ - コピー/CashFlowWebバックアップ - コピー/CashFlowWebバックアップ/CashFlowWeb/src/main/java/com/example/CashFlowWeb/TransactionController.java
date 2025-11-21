package com.example.CashFlowWeb;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO(); 

    public TransactionController() {
        DBManager.initializeDatabase();
        categoryDAO.initializeCache();
    }

    // ----------------------------------------
    // R (Read) - データ取得
    // ----------------------------------------
    
    @GetMapping
    public List<Transaction> getAllTransactions() {
        return transactionDAO.getAllTransactions();
    }
    
    @GetMapping("/filter")
    public List<Transaction> getFilteredTransactions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String type) {
        return transactionDAO.getFilteredTransactions(startDate, endDate, categoryId, type);
    }

    @GetMapping("/balance")
    public double getCurrentBalance() {
        return transactionDAO.calculateCurrentBalance();
    }

    @GetMapping("/summary")
    public List<MonthlySummary> getMonthlySummary() {
        return transactionDAO.getMonthlySummary();
    }
    
    @GetMapping("/summary/category")
    public List<CategorySummary> getCategorySummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam String type) {
        return transactionDAO.getCategorySummary(startDate, endDate, type);
    }

    // ----------------------------------------
    // C (Create) - 取引登録
    // ----------------------------------------

    @PostMapping
    public ResponseEntity<Boolean> addTransaction(@RequestBody Transaction transaction) {
        boolean isSuccess = transactionDAO.addTransaction(
            transaction.getDate(),
            transaction.getAmount(),
            transaction.getType(),
            transaction.getCategoryId(),
            transaction.getIsFuture(),
            transaction.getIsExtraordinary()
        );
        return isSuccess ? ResponseEntity.ok(true) : ResponseEntity.badRequest().body(false);
    }

    // ----------------------------------------
    // U (Update) - 取引更新
    // ----------------------------------------

    @PutMapping("/{id}")
    public ResponseEntity<Boolean> updateTransaction(@PathVariable int id, @RequestBody Transaction transaction) {
        boolean isSuccess = transactionDAO.updateTransaction(
            id,
            transaction.getDate(),
            transaction.getAmount(),
            transaction.getType(),
            transaction.getCategoryId(),
            transaction.getIsFuture(),
            transaction.getIsExtraordinary()
        );
        return isSuccess ? ResponseEntity.ok(true) : ResponseEntity.badRequest().body(false);
    }

    // ----------------------------------------
    // D (Delete) - 取引削除
    // ----------------------------------------

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteTransaction(@PathVariable int id) {
        boolean isSuccess = transactionDAO.deleteTransaction(id);
        return isSuccess ? ResponseEntity.ok(true) : ResponseEntity.badRequest().body(false);
    }

    // ----------------------------------------
    // 予測機能 API
    // ----------------------------------------
    
    @GetMapping("/predict")
    public ResponseEntity<PredictionResult> predictTargetAchievement(
        @RequestParam double targetAmount,
        @RequestParam int targetMonths) {

        double currentBalance = transactionDAO.calculateCurrentBalance();
        List<Double> pastNetProfits = transactionDAO.getPastThreeMonthsRegularNetProfits();
        
        if (pastNetProfits.size() < 3) {
            return ResponseEntity.ok(new PredictionResult(0, 0, "過去3ヶ月分のデータが不足しているため、予測できません。", currentBalance));
        }
        
        double averageProfit = pastNetProfits.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        
        List<Double> projectionPoints = new ArrayList<>();
        double projectedBalance = currentBalance;
        projectionPoints.add(projectedBalance);
        for (int i = 0; i < targetMonths; i++) {
            projectedBalance += averageProfit;
            projectionPoints.add(projectedBalance);
        }

        int estimatedMonths;
        if (currentBalance >= targetAmount) {
            estimatedMonths = 0;
        } else if (averageProfit <= 0) {
            estimatedMonths = -1;
        } else {
            estimatedMonths = (int) Math.ceil((targetAmount - currentBalance) / averageProfit);
        }
        
        String feedback;
        if (estimatedMonths == -1) {
            feedback = "毎月の純利益がマイナス傾向です。目標達成は不可能と推定されます。**支出の見直しが緊急で必要**です。";
        } else if (estimatedMonths > targetMonths) {
            int lateMonths = estimatedMonths - targetMonths;
            double requiredFinalBalance = currentBalance + averageProfit * targetMonths;
            double requiredIncrease = (targetAmount - requiredFinalBalance) / targetMonths;
            
            feedback = String.format(
                "目標達成は**%dヶ月遅れる**可能性があります。目標達成には今後**%dヶ月間**、月あたり純利益をあと**¥%s**増やす必要があります。",
                lateMonths,
                targetMonths,
                new java.text.DecimalFormat("#,###").format(Math.max(0, requiredIncrease))
            );
        } else {
            feedback = String.format(
                "現在のペースで推移すれば、**%dヶ月**で目標を達成できる見込みです。目標達成に向けて引き続き努力しましょう！",
                estimatedMonths
            );
        }

        PredictionResult result = new PredictionResult(averageProfit, estimatedMonths, feedback, currentBalance, projectionPoints);
        return ResponseEntity.ok(result);
    }
    /**
     * IDを指定して単一の取引データを取得します。
     */
    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable int id) {
        Transaction transaction = transactionDAO.getTransactionById(id);
        if (transaction != null) {
            return ResponseEntity.ok(transaction);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
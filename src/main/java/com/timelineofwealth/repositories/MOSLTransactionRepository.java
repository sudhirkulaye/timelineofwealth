package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.MOSLTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MOSLTransactionRepository extends JpaRepository<MOSLTransaction, MOSLTransaction.MOSLTransactionKey> {
    public int countByKeyMoslCodeAndKeyDateAndKeyScriptNameAndKeySellBuyAndKeyOrderNoAndKeyTradeNoAndKeyPortfolioid(String code, java.sql.Date date, String scriptName, String sellBuy, String orderNo, String tradeNo, int portfoliono);

}

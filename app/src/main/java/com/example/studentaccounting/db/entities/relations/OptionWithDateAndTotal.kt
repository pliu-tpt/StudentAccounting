package com.example.studentaccounting.db.entities.relations

data class OptionWithDateAndTotal(
    val option: String,
    val month: String,
    val total: Double
)

//  PS: absolute stupid way of getting a 3 letter month + year :
//  SELECT (SUBSTR('JanFebMarAprMayJunJulAugSepOctNovDec', 1 + 3*strftime('%m', transaction_date), -3) || strftime('. %Y', transaction_date)) as month

// SELECT strftime('%Y-%m', transaction_date) as month,
//
//transaction_category AS option, SUM(CASE WHEN isSpending = 1 THEN preferred_currency_amount ELSE -preferred_currency_amount END) AS total
//FROM (SELECT t.* ,transaction_amount * rate AS preferred_currency_amount, "EUR" AS preferred_currency
//FROM (SELECT * FROM transaction_table) AS t LEFT JOIN
//(SELECT currency_name, destination / departure AS rate FROM
//       (SELECT currency_name, USD_to_it AS departure,
//                   (SELECT USD_to_it FROM currency_table WHERE currency_name = "EUR") AS destination FROM currency_table)) AS c
//                                                                    ON t.transaction_currency = c.currency_name)
//
//
//                                                                   GROUP BY transaction_category, month
//                                                                   HAVING option = "Voyage" OUOUOUOUOU HAVING option = "Voyage" AND month >= "2022-01" AND month <= "2023-03" OUOUOUUOU
//                                                                   ORDER BY month ASC
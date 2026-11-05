package com.example.intrack

import com.example.intrack.notification.sms.TransactionParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class TransactionParserTest {

    @Test
    fun testCommercialBankPickMeRideSms() {
        val sms = "Combk: Paid LKR 850.00 to PICKME with Card ending 1234 on 05/11/2026. Bal: LKR 45,200.00"
        val tx = TransactionParser.parse("COMBANK", sms)

        assertNotNull(tx)
        assertEquals(850.0, tx!!.amountLkr, 0.001)
        assertEquals("PickMe", tx.merchantName)
        assertEquals("TRANSPORT", tx.category)
        assertEquals("DISCRETIONARY", tx.commitmentType)
        assertEquals("CARD_PRIMARY", tx.paymentAccount)
        assertEquals("ride", tx.typeLabel)
    }

    @Test
    fun testSampathBankUberEatsSms() {
        val sms = "Sampath Bank Alert: LKR 2,450.00 debited from A/C 0123 for UBER EATS on 05-Nov-2026"
        val tx = TransactionParser.parse("SAMPATH", sms)

        assertNotNull(tx)
        assertEquals(2450.0, tx!!.amountLkr, 0.001)
        assertEquals("Uber Eats", tx.merchantName)
        assertEquals("DINING_OUT", tx.category)
        assertEquals("DISCRETIONARY", tx.commitmentType)
        assertEquals("CARD_PRIMARY", tx.paymentAccount)
        assertEquals("food delivery", tx.typeLabel)
    }

    @Test
    fun testHnbPickMeRideSms() {
        val sms = "HNB Alert: Your card 4xxx-xxxx-xxxx-1234 was used for LKR 850.00 at PICKME on 05/11/2026"
        val tx = TransactionParser.parse("HNB", sms)

        assertNotNull(tx)
        assertEquals(850.0, tx!!.amountLkr, 0.001)
        assertEquals("PickMe", tx.merchantName)
        assertEquals("TRANSPORT", tx.category)
        assertEquals("DISCRETIONARY", tx.commitmentType)
    }

    @Test
    fun testPickMeAppNotificationReceipt() {
        val notifText = "Trip completed. Your fare is LKR 850.00. Thank you for riding with us!"
        val tx = TransactionParser.parse("com.pickme.passenger", notifText)

        assertNotNull(tx)
        assertEquals(850.0, tx!!.amountLkr, 0.001)
        assertEquals("PickMe", tx.merchantName)
        assertEquals("TRANSPORT", tx.category)
        assertEquals("DISCRETIONARY", tx.commitmentType)
    }

    @Test
    fun testUberEatsAppNotification() {
        val notifText = "Uber Eats: Total of LKR 1,950.00 paid for your order."
        val tx = TransactionParser.parse("com.ubercab.eats", notifText)

        assertNotNull(tx)
        assertEquals(1950.0, tx!!.amountLkr, 0.001)
        assertEquals("Uber Eats", tx.merchantName)
        assertEquals("DINING_OUT", tx.category)
        assertEquals("DISCRETIONARY", tx.commitmentType)
    }

    @Test
    fun testCommercialBankKeellsGroceriesSms() {
        val sms = "COMBANK Alert: Txn of LKR 3,400.00 done on card 4123****4567 at KEELLS SUPER"
        val tx = TransactionParser.parse("COMBANK", sms)

        assertNotNull(tx)
        assertEquals(3400.0, tx!!.amountLkr, 0.001)
        assertEquals("Keells Super", tx.merchantName)
        assertEquals("MISC", tx.category)
        assertEquals("COMMITTED", tx.commitmentType)
    }

    @Test
    fun testDialogUtilityBillSms() {
        val sms = "Combk: Paid LKR 5,800.00 to DIALOG FIBER with Card ending 1234"
        val tx = TransactionParser.parse("COMBANK", sms)

        assertNotNull(tx)
        assertEquals(5800.0, tx!!.amountLkr, 0.001)
        assertEquals("Dialog Broadband/Mobile", tx.merchantName)
        assertEquals("UTILITIES", tx.category)
        assertEquals("COMMITTED", tx.commitmentType)
    }

    @Test
    fun testAtmWithdrawalSms() {
        val sms = "Sampath Bank Alert: ATM Cash withdrawal of LKR 15,000.00 on 02-Nov-2026"
        val tx = TransactionParser.parse("SAMPATH", sms)

        assertNotNull(tx)
        assertEquals(15000.0, tx!!.amountLkr, 0.001)
        assertEquals("ATM_WITHDRAWAL", tx.category)
        assertEquals("TRANSFER", tx.commitmentType)
        assertEquals("CASH", tx.paymentAccount)
    }

    @Test
    fun testOtpAndNonTransactionMessagesIgnored() {
        val otpSms = "Your COMBANK OTP is 849201 for online banking login. Valid for 5 minutes. Do not share."
        val tx1 = TransactionParser.parse("COMBANK", otpSms)
        assertNull(tx1)

        val promoSms = "Enjoy 20% off on all pizzas this weekend at Domino's with your Sampath Bank Card!"
        val tx2 = TransactionParser.parse("SAMPATH", promoSms)
        assertNull(tx2)

        val salaryCreditSms = "Your account has been credited with LKR 140,000.00. Salary deposit."
        val tx3 = TransactionParser.parse("HNB", salaryCreditSms)
        assertNull(tx3)
    }
}

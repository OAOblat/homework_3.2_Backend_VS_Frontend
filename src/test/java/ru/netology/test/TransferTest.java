package ru.netology.test;

import groovy.transform.ToString;
import io.restassured.mapper.ObjectMapper;
import io.restassured.mapper.ObjectMapperDeserializationContext;
import io.restassured.mapper.ObjectMapperSerializationContext;
import org.junit.jupiter.api.Test;
import ru.netology.data.APIHelper;
import ru.netology.data.DataHelper;
import ru.netology.data.SQLHelper;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.netology.data.DataHelper.*;

public class TransferTest {
    @Test
    void shouldTestTransferFromFirstToSecond() {
        var authDemo = getAuthInfoDemo();
        APIHelper.sendQueryForLogin(authDemo, 200);
        VerificationCode verificationCode = SQLHelper.getVerificationCode();
        var code = verificationCode.getCode();
        var login = authDemo.getLogin();
        var verificationInfo = new VerificationInfo(login, code);
        var token = APIHelper.sendQueryForVerify(verificationInfo, 200);
        var cardsBalances = APIHelper.sendQueryToGetCardBalance(token.getToken(), 200);
        var firstCardBalance = cardsBalances.get(getFirstCardInfo().getTestId());
        var secondCardBalance = cardsBalances.get(getSecondCardInfo().getTestId());
        var amount = generateValidAmount(firstCardBalance);
        var transferInfo = new APIHelper.APITransferInfo(getFirstCardInfo().getCardNumber(), getSecondCardInfo().getCardNumber(), amount);
        APIHelper.sendQueryToTransfer(token.getToken(), transferInfo, 200);
        cardsBalances = APIHelper.sendQueryToGetCardBalance(token.getToken(), 200);
        var actualCardBalanceFirstCard = cardsBalances.get(getFirstCardInfo().getTestId());
        var actualCardBalanceSecondCard = cardsBalances.get(getSecondCardInfo().getTestId());
        var expectedCardBalanceFirstCard = firstCardBalance - amount;
        var expectedCardBalanceSecondCard = secondCardBalance + amount;
        assertAll(() -> assertEquals(actualCardBalanceFirstCard, expectedCardBalanceFirstCard),
                () -> assertEquals(actualCardBalanceSecondCard, expectedCardBalanceSecondCard));
    }
}
package pl.cleankod.exchange

import org.apache.http.HttpResponse
import pl.cleankod.BaseApplicationSpecification
import pl.cleankod.exchange.core.domain.Account
import pl.cleankod.exchange.core.domain.Money

import java.nio.charset.StandardCharsets

class AccountSpecification extends BaseApplicationSpecification {

    def "should return an account by ID"() {
        given:
        def accountId = "fa07c538-8ce4-11ec-9ad5-4f5a625cd744"

        when:
        Account response = get("/accounts/${accountId}", Account)

        then:
        response == new Account(
                UUID.fromString(accountId),
                "65 1090 1665 0000 0001 0373 7343",
                Money.of("123.45", "PLN")
        )
    }

    def "should return an account by ID with different currency"() {
        given:
        def accountId = "fa07c538-8ce4-11ec-9ad5-4f5a625cd744"
        def currency = "EUR"

        when:
        Account response = get("/accounts/${accountId}?currency=${currency}", Account)

        then:
        response.id() == UUID.fromString(accountId)
        response.number() == "65 1090 1665 0000 0001 0373 7343"
        response.balance().currency().toString() == currency
        response.balance().amount() != null
    }

    def "should throw exception when wrong currency"() {
        given:
        def accountId = "fa07c538-8ce4-11ec-9ad5-4f5a625cd744"
        def currency = "EU"

        when:
        HttpResponse response = getResponse("/accounts/${accountId}?currency=${currency}")

        then:
        response.getStatusLine().getStatusCode() == 500
    }

    def "should return an account by number"() {
        given:
        def accountNumberValue = "75 1240 2034 1111 0000 0306 8582"
        def accountNumberUrlEncoded = URLEncoder.encode(accountNumberValue, StandardCharsets.UTF_8)

        when:
        Account response = get("/accounts/number=${accountNumberUrlEncoded}", Account)

        then:
        response == new Account(
                UUID.fromString("78743420-8ce9-11ec-b0d0-57b77255c208"),
                accountNumberValue,
                Money.of("456.78", "EUR")
        )
    }

    def "should throw exception when base currency mismatches"() {
        given:
        def accountNumberValue = "75 1240 2034 1111 0000 0306 8582"
        def accountNumberUrlEncoded = URLEncoder.encode(accountNumberValue, StandardCharsets.UTF_8)

        when:
        HttpResponse response = getResponse("/accounts/number=${accountNumberUrlEncoded}?currency=PLN")

        then:
        response.getStatusLine().getStatusCode() == 500
    }

    def "should not find an account by ID"() {
        given:
        def accountId = "ac270f3a-8d08-11ec-8b91-9bcdf6e2522a"

        when:
        def response = getResponse("/accounts/${accountId}")

        then:
        response.getStatusLine().getStatusCode() == 404
    }

    def "should not find an account by number"() {
        given:
        def accountNumber = URLEncoder.encode("11 1750 0009 0000 0000 2156 6004", StandardCharsets.UTF_8)

        when:
        def response = getResponse("/accounts/number=${accountNumber}")

        then:
        response.getStatusLine().getStatusCode() == 404
    }
}

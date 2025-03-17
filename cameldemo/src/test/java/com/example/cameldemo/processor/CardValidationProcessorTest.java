package com.example.cameldemo.processor;

import com.example.cameldemo.model.Payment;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CardValidationProcessorTest {

    private CardValidationProcessor processor;
    private Exchange exchange;
    private Message message;

    @BeforeEach
    void setup() {
        processor = new CardValidationProcessor();
        exchange = mock(Exchange.class);
        message = mock(Message.class);
        when(exchange.getIn()).thenReturn(message);
    }

    @Test
    void testValidCard() {
        Payment payment = new Payment("4111111111111111", 50.0);
        when(message.getBody(Payment.class)).thenReturn(payment);

        assertDoesNotThrow(() -> processor.process(exchange));
    }

    @Test
    void testInvalidCard() {
        Payment payment = new Payment("123456", 30.0);
        when(message.getBody(Payment.class)).thenReturn(payment);

        assertThrows(IllegalArgumentException.class, () -> processor.process(exchange));
    }

    @Test
    void testNullPayment() {
        when(message.getBody(Payment.class)).thenReturn(null);

        assertThrows(NullPointerException.class, () -> processor.process(exchange));
    }
}

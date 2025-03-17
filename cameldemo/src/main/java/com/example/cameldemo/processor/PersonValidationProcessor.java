package com.example.cameldemo.processor;

import com.example.cameldemo.model.Person;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class PersonValidationProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        Person person = exchange.getIn().getBody(Person.class);

        if (person.getAge() < 18) {
            throw new IllegalArgumentException("Person under 18: " + person.getName());
        }
    }
}

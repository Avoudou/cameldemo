package com.example.cameldemo.route;

import com.example.cameldemo.model.Person;
import com.example.cameldemo.processor.PersonValidationProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.bindy.csv.BindyCsvDataFormat;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

@Component
public class CsvToJsonRoute extends RouteBuilder {

    private int fileCounter = 0;

    @Override
    public void configure() throws Exception {

        onException(Exception.class)
                .log("Error processing record: ${exception.message}")
                .handled(true);



        restConfiguration()
                .component("servlet")
                .contextPath("/camel")
                .port(8088);

        rest("/uploadCamel")
                .post()
                .consumes("text/plain")
                .to("direct:uploadCsv");

        from("direct:uploadCsv")
                .process(exchange -> {
                    String body = exchange.getIn().getBody(String.class);
                    String filename = "upload-" + System.currentTimeMillis() + ".csv";

                    File target = new File(System.getProperty("user.dir") + "/input", filename);
                    if (!target.getParentFile().exists()) target.getParentFile().mkdirs();

                    java.nio.file.Files.write(target.toPath(), body.getBytes());

                    exchange.getMessage().setBody("File saved as " + filename);
                });


        File outputDir = new File("output");
        if (!outputDir.exists()) outputDir.mkdirs();

        int maxIndex = Arrays.stream(Objects.requireNonNull(outputDir.listFiles()))
                .map(File::getName)
                .map(name -> name.split("-")[0])
                .filter(n -> n.matches("\\d+"))
                .mapToInt(Integer::parseInt)
                .max()
                .orElse(-1);

        fileCounter = maxIndex + 1;


        BindyCsvDataFormat bindy = new BindyCsvDataFormat(Person.class);


        from("file:input?noop=false&move=../processed/${file:name}&moveFailed=../error/${file:name}")
                .log("Picked up file: ${header.CamelFileName}")
                .unmarshal(bindy)
                .split(body())
                .process(exchange -> {
                    Person person = exchange.getIn().getBody(Person.class);
                    int currentIndex = fileCounter++;
                    String filename = currentIndex + "-" + person.getName() + ".json";
                    exchange.getIn().setHeader(Exchange.FILE_NAME, filename);
                })
                .process(new PersonValidationProcessor())
                .marshal().json()
                .log("Processed: ${body}")
                .to("file:output");
    }
}
package com.aws.sample.url;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.amazonaws.services.lambda.runtime.Context;

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.aws.sampleImage.url.LambdaFunctionImageURLCrawlerHandler;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
public class LambdaFunctionImageURLCrawlerHandlerTest {

    private static DynamodbEvent input;

    @BeforeClass
    public static void createInput() throws IOException {
        input = TestUtils.parse("dynamodb-update-event.json", DynamodbEvent.class);
    }

    private Context createContext() {
        TestContext ctx = new TestContext();

        // TODO: customize your context here if needed.
        ctx.setFunctionName("Your Function Name");

        return ctx;
    }

    @Test
    public void testLambdaFunctionImageURLCrawlerHandler() {
        LambdaFunctionImageURLCrawlerHandler handler = new LambdaFunctionImageURLCrawlerHandler();
        Context ctx = createContext();

        String output = handler.handleRequest(input, ctx);

        // TODO: validate output here if needed.
        if (output != null) {
            System.out.println(output.toString());
        }
    }
}

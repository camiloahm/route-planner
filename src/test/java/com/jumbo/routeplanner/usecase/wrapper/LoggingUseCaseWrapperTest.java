package com.jumbo.routeplanner.usecase.wrapper;

import com.jumbo.routeplanner.usecase.UseCase;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.rule.OutputCapture;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class LoggingUseCaseWrapperTest {
    @Data
    @AllArgsConstructor
    class SomeQuery {
        private String value;
    }

    @Mock
    private UseCase decorated;

    @InjectMocks
    private LoggingUseCaseWrapper<SomeQuery, Void> loggingQueryHandlerDecorator;

    @Rule
    public OutputCapture outputCapture = new OutputCapture();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldLogSuccessfullyForAGivenInput() {
        // GIVEN the INPUT
        SomeQuery inputQuery = new SomeQuery("Testing the! ? parameter123!! ");

        // WHEN I try yo execute it
        loggingQueryHandlerDecorator.execute(inputQuery);

        // THEN the result show be reported in the console
        String consoleOutput = outputCapture.toString();
        then(consoleOutput).contains("Executing usecase for SomeQuery parameters LoggingUseCaseWrapperTest.SomeQuery(value=Testing the! ? parameter123!! ");
    }

    @Test
    public void shouldLogSuccessfullyEventWhenAnExceptionOccurs() {
        // GIVEN the INPUT
        SomeQuery inputQuery = new SomeQuery("Testing the! ? parameter123!! ");
        loggingQueryHandlerDecorator.execute(inputQuery);

        // AND force an exception to be thrown
        when(decorated.execute(any())).thenThrow(new RuntimeException("Sample InVaLiD Text"));

        // WHEN I try to execute the method
        thrown.expect(RuntimeException.class);
        loggingQueryHandlerDecorator.execute(inputQuery);

        // THEN the log of the query and the exception message should be logged
        String consoleOutput = outputCapture.toString();
        then(consoleOutput).contains("Sample InVaLiD Text");
        then(consoleOutput).contains("Executing query for LoggingQueryHandlerDecoratorTest.SomeQuery(value=Testing the! ? parameter123!! ");
    }

}
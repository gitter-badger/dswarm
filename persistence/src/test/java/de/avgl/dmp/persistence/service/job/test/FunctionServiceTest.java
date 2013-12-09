package de.avgl.dmp.persistence.service.job.test;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.avgl.dmp.persistence.GuicedTest;
import de.avgl.dmp.persistence.model.job.Function;
import de.avgl.dmp.persistence.model.job.FunctionType;
import de.avgl.dmp.persistence.service.job.FunctionService;
import de.avgl.dmp.persistence.service.test.IDBasicJPAServiceTest;

public class FunctionServiceTest extends IDBasicJPAServiceTest<Function, FunctionService, Long> {

	private static final org.apache.log4j.Logger	LOG				= org.apache.log4j.Logger.getLogger(FunctionServiceTest.class);

	private final ObjectMapper						objectMapper	= GuicedTest.injector.getInstance(ObjectMapper.class);

	public FunctionServiceTest() {

		super("function", FunctionService.class);
	}

	@Test
	public void testSimpleFunction() {

		final String functionName = "trim";
		final String functionDescription = "trims leading and trailing whitespaces from a given string";
		final String functionParameter1 = "inputString";
		final String functionParameter2 = "parameter2";

		final Function function = createObject();
		function.setName(functionName);
		function.setDescription(functionDescription);
		function.addParameter(functionParameter1);
		function.addParameter(functionParameter2);

		final Function updatedFunction = updateObjectTransactional(function);

		Assert.assertNotNull("the function name shouldn't be null", function.getName());
		Assert.assertEquals("the function names are not equal", functionName, function.getName());
		Assert.assertNotNull("the function description shouldn't be null", function.getDescription());
		Assert.assertEquals("the function descriptions are not equal", functionDescription, function.getDescription());
		Assert.assertNotNull("the function parameters shouldn't be null", function.getParameters());
		Assert.assertEquals("the function parameters' size are not equal", 2, function.getParameters().size());
		Assert.assertEquals("the function parameter '" + functionParameter1 + "' are not equal", functionParameter1, function.getParameters().get(0));
		Assert.assertEquals("the function type is not '" + FunctionType.Function + "'", FunctionType.Function, function.getFunctionType());

		String json = null;

		try {
			json = objectMapper.writeValueAsString(updatedFunction);
		} catch (final JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		FunctionServiceTest.LOG.debug("function json: " + json);

		// clean up DB
		deletedObject(function.getId());
	}
}
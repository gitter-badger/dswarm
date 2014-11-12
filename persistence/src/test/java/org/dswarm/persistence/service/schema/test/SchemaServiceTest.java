/**
 * Copyright (C) 2013, 2014 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dswarm.persistence.service.schema.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.GuicedTest;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.schema.proxy.ProxySchema;
import org.dswarm.persistence.service.schema.SchemaService;
import org.dswarm.persistence.service.schema.test.utils.SchemaServiceTestUtils;
import org.dswarm.persistence.service.test.IDBasicJPAServiceTest;

public class SchemaServiceTest extends IDBasicJPAServiceTest<ProxySchema, Schema, SchemaService> {

	private static final Logger LOG = LoggerFactory.getLogger(SchemaServiceTest.class);

	private final ObjectMapper objectMapper = GuicedTest.injector.getInstance(ObjectMapper.class);

	private SchemaServiceTestUtils sstUtils;

	public SchemaServiceTest() {
		super("schema", SchemaService.class);
	}

	@Override protected void initObjects() {
		super.initObjects();

		sstUtils = new SchemaServiceTestUtils();
	}

	@Test
	public void testSimpleSchema() throws Exception {

		final Schema schema = sstUtils.createDefaultCompleteObject();
		final Schema updatedSchema = sstUtils.updateAndCompareObject(schema, schema);

		Assert.assertNotNull("the schema's attribute paths of the updated schema shouldn't be null", updatedSchema.getUniqueAttributePaths());
		Assert.assertEquals("the schema's attribute paths size are not equal", schema.getUniqueAttributePaths().size(),
				updatedSchema.getUniqueAttributePaths().size());

		String json = null;
		try {
			json = objectMapper.writeValueAsString(schema);
		} catch (final JsonProcessingException e) {
			LOG.error(e.getMessage(), e);
		}

		SchemaServiceTest.LOG.debug("schema json: " + json);
	}
}

/**
 * Copyright (C) 2013 – 2015 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
package org.dswarm.persistence.service.schema.test.internalmodel;

import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.schema.utils.NameSpacePrefixRegistry;
import org.dswarm.persistence.model.schema.utils.SchemaUtils;

public class FoafPersonSchemaBuilder extends SchemaBuilder {

	@Override
	public Schema buildSchema() throws Exception {

		final AttributePathBuilder builder = new AttributePathBuilder();

		// we should take a static identifier here
		final Schema tempSchema = new Schema(SchemaUtils.FOAF_SCHEMA_UUID);

		final Clasz clasz = claszServiceTestUtils.createObject(NameSpacePrefixRegistry.FOAF + "Person", "Person");

		// basic properties as an example:
		tempSchema.addAttributePath(builder.parseAsAttributePathInstance("rdf:type"));
		tempSchema.addAttributePath(builder.parseAsAttributePathInstance("foaf:familyName"));
		tempSchema.addAttributePath(builder.parseAsAttributePathInstance("foaf:givenName"));

		// store all parsed paths as an overview
		prefixPaths = builder.getPrefixPaths();

		return schemaServiceTestUtils
				.createAndPersistSchema(tempSchema.getUuid(), "foaf:Person-Schema", tempSchema.getUniqueAttributePaths(), clasz);
	}

}

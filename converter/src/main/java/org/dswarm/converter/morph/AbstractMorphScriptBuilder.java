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
package org.dswarm.converter.morph;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.common.io.CharSource;
import com.google.common.io.Resources;
import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import org.dswarm.converter.DMPConverterException;
import org.dswarm.init.util.DMPStatics;
import org.dswarm.persistence.model.job.Filter;
import org.dswarm.persistence.model.job.Task;

/**
 * @author tgaengler
 */
public abstract class AbstractMorphScriptBuilder<MORPHSCRIPTBUILDERIMPL extends AbstractMorphScriptBuilder> {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractMorphScriptBuilder.class);

	private static final DocumentBuilderFactory DOC_FACTORY = DocumentBuilderFactory.newInstance();

	private static final TransformerFactory TRANSFORMER_FACTORY;

	private static final String TRANSFORMER_FACTORY_CLASS = "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl";

	private static final String SCHEMA_PATH                                   = "schemata/metamorph.xsd";

	protected Document doc;

	protected Element metaName;

	protected Element rules;

	protected Element maps;

	static {

		System.setProperty("javax.xml.transform.TransformerFactory", AbstractMorphScriptBuilder.TRANSFORMER_FACTORY_CLASS);
		TRANSFORMER_FACTORY = TransformerFactory.newInstance();
		AbstractMorphScriptBuilder.TRANSFORMER_FACTORY.setAttribute("indent-number", 4);

		final URL resource = Resources.getResource(AbstractMorphScriptBuilder.SCHEMA_PATH);
		final CharSource inputStreamInputSupplier = Resources.asCharSource(resource, Charsets.UTF_8);

		try (final Reader schemaStream = inputStreamInputSupplier.openStream()) {

			// final StreamSource SCHEMA_SOURCE = new StreamSource(schemaStream);
			final SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = null;

			try {

				// TODO: dummy schema right now, since it couldn't parse the metamorph schema for some reason
				schema = sf.newSchema();
			} catch (final SAXException e) {

				LOG.error("couldn't read schema", e);
			}

			if (schema == null) {

				AbstractMorphScriptBuilder.LOG.error("couldn't parse schema");
			}

			AbstractMorphScriptBuilder.DOC_FACTORY.setSchema(schema);

		} catch (final IOException e1) {

			AbstractMorphScriptBuilder.LOG.error("couldn't read schema resource", e1);
		}
	}

	protected static final String METAMORPH_IDENTIFIER = "metamorph";

	protected static final String METAMORPH_ELEMENT_META_INFORMATION = "meta";

	protected static final String METAMORPH_ELEMENT_RULESET = "rules";

	protected static final String METAMORPH_ELEMENT_MAP_CONTAINER = "maps";

	protected static final String METAMORPH_ELEMENT_DATA = "data";

	protected static final String METAMORPH_FUNCTION_COMBINE = "combine";

	protected static final String METAMORPH_FUNCTION_ALL = "all";

	protected static final String METAMORPH_FUNCTION_IF = "if";

	protected static final String METAMORPH_FUNCTION_REGEXP = "regexp";

	protected static final String METAMORPH_DATA_SOURCE = "source";

	protected static final    String MF_ELEMENT_NAME_ATTRIBUTE_IDENTIFIER = "name";

	protected static final String METAMORPH_DATA_TARGET                = MF_ELEMENT_NAME_ATTRIBUTE_IDENTIFIER;

	protected static final String FILTER_VARIABLE_POSTFIX = ".filtered";

	protected static final String METAFACTURE_RECORD_IDENTIFIER = "record";

	protected static final String MF_COLLECTOR_INCLUDE_SUB_ENTITIES_ATTRIBUTE_IDENTIFIER = "includeSubEntities";

	protected static final String MF_COLLECTOR_SAME_ENTITY_ATTRIBUTE_IDENTIFIER = "sameEntity";

	protected static final String MF_COLLECTOR_RESET_ATTRIBUTE_IDENTIFIER = "reset";

	protected static final String MF_ELEMENT_VALUE_ATTRIBUTE_IDENTIFIER = "value";

	protected static final String MF_FLUSH_WITH_ATTRIBUTE_IDENTIFIER = "flushWith";

	protected static final String BOOLEAN_VALUE_TRUE = "true";

	protected static final String MF_REGEXP_FUNCTION_MATCH_ATTRIBUTE_IDENTIFIER = "match";

	protected static final String FILTER_ALL_COLLECTOR_NAME = "CONDITION_ALL";

	final protected ObjectMapper objectMapper = new ObjectMapper();

	public String render(final boolean indent, final Charset encoding) {

		if (doc == null) {

			// don't render, when there's no document

			LOG.debug("no document available for morph script rendering");

			return null;
		}

		final String defaultEncoding = encoding.name();
		final Transformer transformer;
		try {

			transformer = AbstractMorphScriptBuilder.TRANSFORMER_FACTORY.newTransformer();
		} catch (final TransformerConfigurationException e) {

			LOG.error("couldn't create transformer for morph script builder");

			return null;
		}

		transformer.setOutputProperty(OutputKeys.INDENT, indent ? "yes" : "no");

		transformer.setOutputProperty(OutputKeys.ENCODING, defaultEncoding);

		final ByteArrayOutputStream stream = new ByteArrayOutputStream();

		final StreamResult result;

		try {

			result = new StreamResult(new OutputStreamWriter(stream, defaultEncoding));
		} catch (final UnsupportedEncodingException e) {

			LOG.error("couldn't render morph script, because the encoding is not supported", e);

			return null;
		}

		try {

			transformer.transform(new DOMSource(doc), result);
		} catch (final TransformerException e) {

			LOG.error("couldn't render morph script", e);

			return null;
		}

		try {

			return stream.toString(defaultEncoding);
		} catch (final UnsupportedEncodingException e) {

			LOG.error("couldn't render morph script, because the encoding is not supported", e);

			return null;
		}
	}

	public String render(final boolean indent) {

		return render(indent, Charsets.UTF_8);
	}

	@Override
	public String toString() {

		return render(true);
	}

	public File toFile() throws IOException {

		final String str = render(false);

		final File file = File.createTempFile("avgl_dmp", ".tmp");

		final BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		bw.write(str);
		bw.close();

		return file;
	}

	public MORPHSCRIPTBUILDERIMPL apply(final Task task) throws DMPConverterException {

		final DocumentBuilder docBuilder;
		try {
			docBuilder = AbstractMorphScriptBuilder.DOC_FACTORY.newDocumentBuilder();
		} catch (final ParserConfigurationException e) {
			throw new DMPConverterException(e.getMessage());
		}

		doc = docBuilder.newDocument();
		doc.setXmlVersion("1.1");

		final Element rootElement = doc.createElement(METAMORPH_IDENTIFIER);
		rootElement.setAttribute("xmlns", "http://www.culturegraph.org/metamorph");
		rootElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		rootElement.setAttribute("xsi:schemaLocation", "http://www.culturegraph.org/metamorph metamorph.xsd");
		rootElement.setAttribute("entityMarker", DMPStatics.ATTRIBUTE_DELIMITER.toString());
		rootElement.setAttribute("version", "1");
		doc.appendChild(rootElement);

		final Element meta = doc.createElement(METAMORPH_ELEMENT_META_INFORMATION);
		rootElement.appendChild(meta);

		metaName = doc.createElement(MF_ELEMENT_NAME_ATTRIBUTE_IDENTIFIER);
		meta.appendChild(metaName);

		rules = doc.createElement(METAMORPH_ELEMENT_RULESET);
		rootElement.appendChild(rules);

		maps = doc.createElement(METAMORPH_ELEMENT_MAP_CONTAINER);
		rootElement.appendChild(maps);

		return (MORPHSCRIPTBUILDERIMPL) this;
	}

	protected void addFilter(final String inputAttributePathStringXMLEscaped, final String variable, final Map<String, String> filterExpressionMap,
			final Element rules, final boolean resultNameAsVariable) {

		final String combineValueVariable = variable + MorphScriptBuilder.FILTER_VARIABLE_POSTFIX;

		final Element combineAsFilter = doc.createElement(METAMORPH_FUNCTION_COMBINE);
		combineAsFilter.setAttribute(MF_COLLECTOR_RESET_ATTRIBUTE_IDENTIFIER, BOOLEAN_VALUE_TRUE);
		combineAsFilter.setAttribute(MF_COLLECTOR_SAME_ENTITY_ATTRIBUTE_IDENTIFIER, BOOLEAN_VALUE_TRUE);
		combineAsFilter.setAttribute(MF_COLLECTOR_INCLUDE_SUB_ENTITIES_ATTRIBUTE_IDENTIFIER, BOOLEAN_VALUE_TRUE);

		final String resultName;

		if(resultNameAsVariable) {

			resultName = "@" + variable;
		} else {

			resultName = variable;
		}

		combineAsFilter.setAttribute(METAMORPH_DATA_TARGET, resultName);
		combineAsFilter.setAttribute(MF_ELEMENT_VALUE_ATTRIBUTE_IDENTIFIER, "${" + combineValueVariable + "}");

		final String commonAttributePath = validateCommonAttributePath(inputAttributePathStringXMLEscaped, filterExpressionMap.keySet());

		combineAsFilter.setAttribute(MF_FLUSH_WITH_ATTRIBUTE_IDENTIFIER, commonAttributePath);

		final Element filterIf = doc.createElement(METAMORPH_FUNCTION_IF);
		final Element filterAll = doc.createElement(METAMORPH_FUNCTION_ALL);
		filterAll.setAttribute(MF_ELEMENT_NAME_ATTRIBUTE_IDENTIFIER, FILTER_ALL_COLLECTOR_NAME);
		filterAll.setAttribute(MF_COLLECTOR_RESET_ATTRIBUTE_IDENTIFIER, BOOLEAN_VALUE_TRUE);
		filterAll.setAttribute(MF_COLLECTOR_INCLUDE_SUB_ENTITIES_ATTRIBUTE_IDENTIFIER, BOOLEAN_VALUE_TRUE);
		filterAll.setAttribute(MF_FLUSH_WITH_ATTRIBUTE_IDENTIFIER,
				StringEscapeUtils.unescapeXml(Iterators.getLast(filterExpressionMap.keySet().iterator())));

		for (final Map.Entry<String, String> filter : filterExpressionMap.entrySet()) {

			final Element combineAsFilterData = doc.createElement(METAMORPH_ELEMENT_DATA);
			combineAsFilterData.setAttribute(METAMORPH_DATA_SOURCE, StringEscapeUtils.unescapeXml(filter.getKey()));

			final Element combineAsFilterDataFunction = doc.createElement(METAMORPH_FUNCTION_REGEXP);
			combineAsFilterDataFunction.setAttribute(MF_REGEXP_FUNCTION_MATCH_ATTRIBUTE_IDENTIFIER, filter.getValue());

			combineAsFilterData.appendChild(combineAsFilterDataFunction);
			filterAll.appendChild(combineAsFilterData);
		}

		filterIf.appendChild(filterAll);
		combineAsFilter.appendChild(filterIf);

		final Element combineAsFilterDataOut = createFilterDataElement(combineValueVariable, inputAttributePathStringXMLEscaped);

		combineAsFilter.appendChild(combineAsFilterDataOut);

		rules.appendChild(combineAsFilter);
	}

	protected abstract Element createFilterDataElement(final String variable, final String attributePathString);

	protected String getFilterExpression(final Filter filter) {

		if (filter != null) {

			final String filterExpressionString = filter.getExpression();

			if (filterExpressionString != null && !filterExpressionString.isEmpty()) {

				return StringEscapeUtils.unescapeXml(filterExpressionString);
			}
		}

		return null;
	}

	protected Map<String, String> extractFilterExpressions(final String filterExpressionString) {

		final Map<String, String> filterExpressionMap = Maps.newLinkedHashMap();

		if (filterExpressionString != null && !filterExpressionString.isEmpty()) {

			ArrayNode filterExpressionArray = null;

			try {

				filterExpressionArray = objectMapper.readValue(filterExpressionString, ArrayNode.class);

			} catch (final IOException e) {

				AbstractMorphScriptBuilder.LOG.debug("something went wrong while deserializing filter expression", e);
			}

			if (filterExpressionArray != null) {

				for (final JsonNode filterExpressionNode : filterExpressionArray) {

					final Iterator<Map.Entry<String, JsonNode>> filterExpressionIter = filterExpressionNode.fields();

					while (filterExpressionIter.hasNext()) {

						final Map.Entry<String, JsonNode> filterExpressionEntry = filterExpressionIter.next();
						filterExpressionMap.put(filterExpressionEntry.getKey(), filterExpressionEntry.getValue().asText());
					}
				}
			}
		}

		return filterExpressionMap;
	}

	protected String validateCommonAttributePath(final String valueAttributePath, final Set<String> filterAttributePaths) {

		final String commonAttributePath = determineCommonAttributePath(valueAttributePath, filterAttributePaths);

		if (commonAttributePath == null || commonAttributePath.isEmpty()) {

			// to flush at record level
			return METAFACTURE_RECORD_IDENTIFIER;
		}

		final String[] commonAttributePathAttributes = determineAttributes(commonAttributePath);
		final String[] valueAttributePathAttributes = determineAttributes(valueAttributePath);

		boolean isValid = true;

		for (int i = 0; i < commonAttributePathAttributes.length; i++) {

			final String commonAttributePathAttribute = commonAttributePathAttributes[i];
			final String valueAttributePathAttribute = valueAttributePathAttributes[i];

			if (!commonAttributePathAttribute.equals(valueAttributePathAttribute)) {

				isValid = false;

				break;
			}
		}

		if (isValid) {

			return commonAttributePath;
		} else {

			// to flush at record level
			return METAFACTURE_RECORD_IDENTIFIER;
		}
	}

	protected String determineCommonAttributePath(final String valueAttributePath, final Set<String> filterAttributePaths) {

		final String[] attributePaths = new String[filterAttributePaths.size() + 1];

		attributePaths[0] = valueAttributePath;

		int i = 1;

		for (final String filterAttributePath : filterAttributePaths) {

			attributePaths[i] = StringEscapeUtils.unescapeXml(filterAttributePath);

			i++;
		}

		final String commonPrefix = StringUtils.getCommonPrefix(attributePaths);

		if (!commonPrefix.endsWith(DMPStatics.ATTRIBUTE_DELIMITER.toString())) {

			if (!commonPrefix.contains(DMPStatics.ATTRIBUTE_DELIMITER.toString())) {

				return commonPrefix;
			}

			return commonPrefix.substring(0, commonPrefix.lastIndexOf(DMPStatics.ATTRIBUTE_DELIMITER));
		}

		return commonPrefix.substring(0, commonPrefix.length() - 1);
	}

	protected String[] determineAttributes(final String attributePath) {

		final String[] attributes;

		if (attributePath.contains(DMPStatics.ATTRIBUTE_DELIMITER.toString())) {

			attributes = attributePath.split(DMPStatics.ATTRIBUTE_DELIMITER.toString());
		} else {

			attributes = new String[] { attributePath };
		}

		return attributes;
	}
}

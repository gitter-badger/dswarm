package de.avgl.dmp.converter.flow.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.StringWriter;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.culturegraph.mf.stream.sink.ObjectJavaIoWriter;
import org.culturegraph.mf.stream.source.FileOpener;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import de.avgl.dmp.converter.flow.CSVSourceResourcePreviewFlow;
import de.avgl.dmp.converter.mf.stream.reader.CsvReader;
import de.avgl.dmp.converter.mf.stream.source.CSVEncoder;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

public class CSVSourceResourcePreviewFlowTest {

	@Test
	public void testEndToEnd() throws Exception {
		
		final String expected = DMPPersistenceUtil.getResourceAsString("test_csv.csv");

		final CSVSourceResourcePreviewFlow flow = CSVSourceResourcePreviewFlow.fromConfigurationParameters(Charsets.UTF_8.name(), '\\', '"', ';', "\n");

		final URL url = Resources.getResource("test_csv.csv");
		final File file = FileUtils.toFile(url);
		
		final String actual = flow.applyFile(file.getAbsolutePath());

		assertEquals(expected, actual);
	}

	@Test
	public void readCSVTest() throws Exception {

		final FileOpener opener = new FileOpener();
		
		// set encoding
		opener.setEncoding(Charsets.UTF_8.name());
		
		final URL url = Resources.getResource("test_csv.csv");
		final File file = FileUtils.toFile(url);

		// set column separator and line separator
		final CsvReader reader = new CsvReader('\\', '"', ';', "\n");
		
		// set number of header lines (if header lines = 1, then schema header line = 1)
		reader.setHeader(true);
		final CSVEncoder converter = new CSVEncoder();
		converter.withHeader();
		final StringWriter stringWriter = new StringWriter();
		final ObjectJavaIoWriter<String> writer = new ObjectJavaIoWriter<String>(stringWriter);

		opener.setReceiver(reader).setReceiver(converter).setReceiver(writer);

		opener.process(file.getAbsolutePath());

		final String resultOutput = stringWriter.toString();

		Assert.assertNotNull("the result output shoudln't be null", resultOutput);
		
		final String expectedResult = DMPPersistenceUtil.getResourceAsString("test_csv.csv");
		
		Assert.assertEquals("the processing outputs are not equal", expectedResult, resultOutput);
	}
}

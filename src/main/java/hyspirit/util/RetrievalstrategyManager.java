/**
 * @author Ingo Frommholz &lt;ingo@frommholz.org&gt;
 */
package hyspirit.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A retrieval strategy is actually a sequence of files, specified in a
 * configuration file, that is executed by the respective HyInferenceEngine in
 * the sequence provided. The idea is that the combination of different files
 * (e.g. PD or PRA files) constitutes a retrieval strategy. This class is a
 * convenience class to handle different such strategies and assign them to a
 * sequence of files to be executed.
 * 
 * Retrieval strategies are controlled by an XML file similar to
 * 
 * {@code
 * <?xml version="1.0" encoding="UTF-8"?>
<retrievalstrategies>
	<basedir>pd</basedir>
	<retrievalstrategy name="basic">
		<file sequence="1">test1.pd</file>
		<file sequence="2">test2.pd</file>
	</retrievalstrategy>
	<retrievalstrategy name="advanced">
		<file sequence="1">test1_adv.pd</file>
		<file sequence="2">test2_adv.pd</file>
		<file sequence="3">test3_adv.pd</file>
	</retrievalstrategy>
</retrievalstrategies>
}
 * 
 * 
 * @author Ingo Frommholz &lt;ingo@frommholz.org&gt;
 *
 */
public class RetrievalstrategyManager {

    /**
     * The main data structure containing the mapping between retrieval strategy
     * names and the list of files.
     */
    private final Map<String, List<String>> retrievalstrategies = new HashMap<String, List<String>>();

    /**
     * @param configurationXMLFile
     *            the XML configuration filename (absolute file name)
     */
    public RetrievalstrategyManager(String configurationXMLFile)
	    throws ParserConfigurationException, SAXException, IOException {
	this(new File(configurationXMLFile));
    }

    /**
     * @param configurationXMLFile
     *            the XML configuration file (absolute file name)
     */
    public RetrievalstrategyManager(File configurationXMLFile)
	    throws ParserConfigurationException, SAXException, IOException {
	SAXParserFactory factory = SAXParserFactory.newInstance();

	/*
	 * Uncomment for XML validation (enter schema URL URL)
	 *
	 * factory.setValidating(true); factory.setFeature(
	 * "http://apache.org/xml/features/validation/schema", true);
	 */

	SAXParser saxParser = factory.newSAXParser();
	DefaultHandler handler = new DefaultHandler() {

	    /*
	     * Helper variables
	     */
	    private boolean inRetrievalStrategies = false;
	    private boolean inRetrievalStrategy = false;
	    private boolean inFile = false;
	    private boolean inBasedir = false;
	    private String currentStrategyname = null;
	    private List<String> currentFileSequence = null;
	    private int currentSeq = -1;
	    private String basedir = "";
	    private String currentFilename = null;

	    /* (non-Javadoc)
	     * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	     */
	    @Override
	    public void startElement(String uri, String localName,
		    String qName, Attributes attributes)
			    throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		switch (qName) {
		case "retrievalstrategies":
		    inRetrievalStrategies = true;
		    break;
		case "retrievalstrategy":
		    if (!inRetrievalStrategies)
			throw new SAXParseException(
				"<retrievalstrategies> not found!", null);
		    inRetrievalStrategy = true;
		    currentStrategyname = attributes.getValue("name");
		    if (currentStrategyname == null) {
			throw new SAXParseException(
				"No strategy name provided!", null);
		    }
		    currentFileSequence = new ArrayList<String>();
		    break;
		case "basedir":
		    if (!inRetrievalStrategies)
			throw new SAXParseException(
				"<retrievalstrategies> not found!", null);
		    inBasedir = true;
		    break;
		case "file":
		    if (!inRetrievalStrategy)
			throw new SAXParseException(
				"<file> found outside <retrievalstrategy!",
				null);
		    String seqS = attributes.getValue("sequence");
		    inFile = true;
		    if (seqS == null) {
			throw new SAXParseException(
				"No sequence number provided!", null);
		    }
		    try {
			currentSeq = Integer.parseInt(seqS);
		    } catch (NumberFormatException ne) {
			throw new SAXParseException(
				"Sequence number not positive int!", null);
		    }
		    if (currentSeq < 1)
			throw new SAXParseException("Sequence number < 1!",
				null);
		}
	    }

	    /* (non-Javadoc)
	     * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	     */
	    @Override
	    public void endElement(String uri, String localName,
		    String qName) throws SAXException {
		super.endElement(uri, localName, qName);
		switch (qName) {
		case "retrievalstrategies":
		    inRetrievalStrategies = false;
		    break;
		case "retrievalstrategy":
		    inRetrievalStrategy = false;
		    retrievalstrategies.put(currentStrategyname,
			    currentFileSequence);
		    break;
		case "basedir":
		    inBasedir = false;
		    break;
		case "file":
		    inFile = false;
		    currentFileSequence.add(currentSeq - 1,
			    basedir + File.separator + currentFilename);
		    break;
		}
	    }

	    /* (non-Javadoc)
	     * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	     */
	    @Override
	    public void characters(char[] ch, int start, int length)
		    throws SAXException {
		super.characters(ch, start, length);
		if (inBasedir) {
		    String s = new String(ch, start, length);
		    if (s != null) {
			basedir = s.trim();
		    }
		} else if (inFile) {
		    String s = new String(ch, start, length);
		    if (s != null) {
			currentFilename = s.trim();
		    }
		} else {
		    String s = new String(ch, start, length);
		    if (s != null && !s.trim().equals(""))
			throw new SAXParseException(
				"Found unexpected text: " + s,
				null);
		}
	    }

	};

	saxParser.parse(configurationXMLFile, handler);

    }

    /**
     * Returns the sequence of file names for a given retrieval strategy as a
     * list for further processing
     * 
     * @param retrievalStrategy
     *            the retrieval strategy
     * @return the list of file names, to be executed in the sequence provided
     *         in this list
     */
    public List<String> getSequence(String retrievalStrategy) {
	return this.retrievalstrategies.get(retrievalStrategy);
    }
}

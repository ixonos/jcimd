package com.googlecode.jcimd.charset;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.spi.CharsetProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A {@link CharsetProvider} for the
 * <a href="http://en.wikipedia.org/wiki/GSM_03.38">GSM 3.38</a>
 * character set.
 * <p>
 * The encoding and decoding are based on the mapping at
 * <a href="http://www.unicode.org/Public/MAPPINGS/ETSI/GSM0338.TXT">
 * http://www.unicode.org/Public/MAPPINGS/ETSI/GSM0338.TXT</a>.
 *
 * @author Lorenzo Dee
 */
public class GsmCharsetProvider extends CharsetProvider {

	private static final Log logger = LogFactory.getLog(GsmCharsetProvider.class);

	static final char ESCAPE = 0x1B;
	static char[] BYTE_TO_CHAR_SMALL_C_CEDILLA = new char[128];
	static char[] BYTE_TO_CHAR_ESCAPED_DEFAULT = new char[128];
	static int[] CHAR_TO_BYTE_SMALL_C_CEDILLA = new int[0x7FFF]; // 32k
	static final char NO_GSM_BYTE = 0xFF;

	static {
		try {
			Arrays.fill(BYTE_TO_CHAR_ESCAPED_DEFAULT, NO_GSM_BYTE);
			Arrays.fill(CHAR_TO_BYTE_SMALL_C_CEDILLA, NO_GSM_BYTE);
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(
							Gsm7BitPackedCharset.class.getResourceAsStream("GSM0338.TXT"),
							Charset.forName("US-ASCII")));
			try {
				init(reader);
			} finally {
				reader.close();
			}
		} catch (IOException e) {
			throw new RuntimeException(
					"Error initializing GSM charset look-up table", e);
		}
	}

	private static int init(BufferedReader reader) throws IOException {
		String line = null;
		int count = 0;
		while ((line = reader.readLine()) != null) {
			if (line.startsWith("#")) {
				if (logger.isTraceEnabled()) {
					logger.trace("Skipping line starting with '#': " + line);
				}
				continue;
			}
			StringTokenizer tokenizer = new StringTokenizer(line, "\t");
			String hex1 = null;
			String hex2 = null;
			if (tokenizer.hasMoreTokens()) {
				hex1 = tokenizer.nextToken();
			}
			if (tokenizer.hasMoreTokens()) {
				hex2 = tokenizer.nextToken();
			}
			if (hex1 == null || hex2 == null) {
				continue;
			}
			int bite = Integer.parseInt(hex1.substring(2), 16);
			byte index = (byte) (bite & 0xFF);
			char ch = (char) Integer.parseInt(hex2.substring(2), 16);
			CHAR_TO_BYTE_SMALL_C_CEDILLA[ch] = bite;
			if ((bite & 0xFF00) >> 8 == ESCAPE) {
				// escape to extension table
				BYTE_TO_CHAR_ESCAPED_DEFAULT[index] = ch;
				if (logger.isTraceEnabled()) {
					logger.trace(String.format("(escaped) %d == %s", index,
							(ch != 10 && ch != 12 && ch != 13) ? ch :
								(ch == 10 ? "\\n" : (ch == 12 ? "0x0C (form feed)" : "\\r"))));
				}
			} else {
				BYTE_TO_CHAR_SMALL_C_CEDILLA[index] = ch;
				if (logger.isTraceEnabled()) {
					logger.trace(String.format("%d == %s", index,
							(ch != 10 && ch != 13) ? ch : (ch == 10 ? "\\n" : "\\r")));
				}
			}
			count++;
		}
		if (count < 128 && logger.isWarnEnabled()) {
			logger.warn("Character look-up initialized with only "
					+ count + " value(s) (expecting 128 values)");
		}
		return count;
	}

	private static final List<Charset> charsets = new ArrayList<Charset>();
	private static final Map<String, Charset> charsetsMap = new HashMap<String, Charset>();

	static {
		Charset[] charsets = new Charset[] {
				new Gsm7BitPackedCharset("GSM", new String[] {
						"GSM-DEFAULT-ALPHABET", "GSM-0338", "GSM-DEFAULT", "GSM7", "GSM-7BIT"
					},
					BYTE_TO_CHAR_SMALL_C_CEDILLA,
					CHAR_TO_BYTE_SMALL_C_CEDILLA,
					BYTE_TO_CHAR_ESCAPED_DEFAULT),
				new Gsm8BitUnpackedCharset("GSM-8BIT", new String[] {
					},
					BYTE_TO_CHAR_SMALL_C_CEDILLA,
					CHAR_TO_BYTE_SMALL_C_CEDILLA,
					BYTE_TO_CHAR_ESCAPED_DEFAULT)
		};
		GsmCharsetProvider.charsets.addAll(Arrays.asList(charsets));
		for (Charset charset : charsets) {
			GsmCharsetProvider.charsetsMap.put(
					charset.name().toLowerCase(), charset);
			for (String alias : charset.aliases()) {
				GsmCharsetProvider.charsetsMap.put(
						alias.toLowerCase(), charset);
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.nio.charset.spi.CharsetProvider#charsetForName(java.lang.String)
	 */
	@Override
	public Charset charsetForName(String charsetName) {
		return GsmCharsetProvider.charsetsMap.get(
				charsetName.toLowerCase());
	}

	/* (non-Javadoc)
	 * @see java.nio.charset.spi.CharsetProvider#charsets()
	 */
	@Override
	public Iterator<Charset> charsets() {
		return GsmCharsetProvider.charsets.iterator();
	}

}

package za.co.kirenpillay.eclipse.datatools.sqlinsert;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Types;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.datatools.sqltools.result.IResultSetObject;
import org.eclipse.datatools.sqltools.result.IResultSetRow;
import org.eclipse.datatools.sqltools.result.export.AbstractOutputter;
import org.eclipse.datatools.sqltools.result.export.IResultConstants;
import org.eclipse.datatools.sqltools.result.internal.utils.HexHelper;
import org.eclipse.datatools.sqltools.result.model.IResultInstance;
import org.eclipse.datatools.sqltools.result.model.ResultItem;

/**
 * 
 * 
 * EPL
 * 
 * 
 * 
 * @author kiren
 *
 */
public class SqlInsertOutPutter extends AbstractOutputter {

	private static Map _columnLen = new HashMap();
	private String tableName;

	public void output(IResultSetObject rs, Properties props, String path)
			throws IOException {
		PrintWriter writer = createPrintWriter(path,
				props.getProperty(IResultConstants.ENCODING));

		output(rs, props, writer);
		writer.close();
	}

	public void output(IResultInstance rs, Properties props, String path)
			throws IOException {
		PrintWriter writer = createPrintWriter(path,
				props.getProperty(IResultConstants.ENCODING));
		extractTableName(rs);
		output(rs, props, writer);
		// "select * from EXPORT_ME\n"

		writer.close();
	}

	Pattern nonQualifiedpattern = Pattern.compile(".*from\\s+(\\w+).*",
			Pattern.UNIX_LINES | Pattern.MULTILINE | Pattern.CASE_INSENSITIVE
					| Pattern.DOTALL);
	Pattern qualifiedPattern = Pattern.compile(".*from\\s+\\w+[.](\\w+).*",
			Pattern.UNIX_LINES | Pattern.MULTILINE | Pattern.CASE_INSENSITIVE
					| Pattern.DOTALL);

	private void extractTableName(IResultInstance rs) {
		parseNonQualified(rs, qualifiedPattern);
		if ("TABLE_NAME".equals(tableName)) {
			parseNonQualified(rs, nonQualifiedpattern);
		}
	}

	private void parseNonQualified(IResultInstance rs, Pattern pattern) {
		String displayString = rs.getOperationCommand().getDisplayString();
		Matcher matcher = pattern.matcher(displayString);
		if (matcher.matches()) {
			tableName = matcher.group(1);
		} else {
			tableName = "TABLE_NAME";
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.datatools.sqltools.result.internal.export.AbstractOutputter
	 * #output(org.eclipse.datatools.sqltools.result.IResultSetObject,
	 * java.util.Properties, java.io.OutputStream)
	 */
	public void output(IResultSetObject resultset, Properties options,
			OutputStream stream) throws IOException {
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(stream,
				options.getProperty(IResultConstants.ENCODING)));
		output(resultset, options, writer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.datatools.sqltools.result.internal.export.AbstractOutputter
	 * #output(org.eclipse.datatools.sqltools.result.IResultSetObject,
	 * java.util.Properties, java.io.PrintWriter)
	 */
	public void output(IResultSetObject resultset, Properties options,
			PrintWriter writer) throws IOException {

		String nullValue = "NULL";

		String sb = generateInserts(resultset, nullValue);

		writer.write(sb);
		writer.flush();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.datatools.sqltools.result.internal.export.AbstractOutputter
	 * #output
	 * (org.eclipse.datatools.sqltools.result.internal.model.IResultInstance,
	 * java.util.Properties, java.io.OutputStream)
	 */
	public void output(IResultInstance rs, Properties props, OutputStream os)
			throws IOException {
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(os,
				props.getProperty(IResultConstants.ENCODING)));
		output(rs, props, writer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.datatools.sqltools.result.internal.export.AbstractOutputter
	 * #output
	 * (org.eclipse.datatools.sqltools.result.internal.model.IResultInstance,
	 * java.util.Properties, java.io.PrintWriter)
	 */
	public void output(IResultInstance rs, Properties props, PrintWriter pw)
			throws IOException {
		for (int i = 0; i < rs.getItemCount(); i++) {
			ResultItem item = rs.getItem(i);
			if (item != null) {
				if (item.getResultObject() instanceof IResultSetObject) {
					IResultSetObject result = (IResultSetObject) item
							.getResultObject();
					output(result, props, pw);

					// An empty line between result sets
					pw.println();
					pw.flush();
				}
			}
		}
	}

	// Comma separated-list of HEADINGS
	private String getColumnHeadings(IResultSetObject result) {

		_columnLen.clear();
		int columnCount = result.getColumnCount();

		StringBuilder cn = new StringBuilder();
		for (int i = 1; i < columnCount + 1; i++) {
			cn.append(result.getColumnName(i));
			cn.append(',');

		}
		removeTrailingComma(cn);

		return cn.toString();
	}

	private void removeTrailingComma(StringBuilder cn) {
		if (cn.toString().charAt(cn.length() - 1) == ',') {
			cn.deleteCharAt(cn.length() - 1);
		}
	}

	private String generateInserts(IResultSetObject result, String nullValue) {
		StringBuilder data = new StringBuilder("");
		String sql = "INSERT INTO %s (%s) VALUES (%s);\n";
		String columnHeadings = getColumnHeadings(result);
		StringBuilder finalString = new StringBuilder();

		// get the records to display, maybe not all records, rely on the
		// options
		Iterator iter = result.getAllRecords();
		while (iter.hasNext()) {
			IResultSetRow row = (IResultSetRow) iter.next();
			int columnCount = result.getColumnCount();
			for (int i = 1; i < columnCount + 1; i++) {
				Object columnValue = row.getData(i - 1);
				String outValue;
				if (columnValue == null) {
					outValue = null;
				} else {

					// consider the image type
					if (columnValue instanceof byte[]) {
						byte[] os = (byte[]) columnValue;
						outValue = HexHelper.toHexString(os);
					} else {
						outValue = columnValue.toString();
					}
				}

				int columnSQLType = result.getColumnSQLType(i);

				boolean requiresQuotes = (columnSQLType == Types.VARCHAR
						|| columnSQLType == Types.CHAR
						|| columnSQLType == Types.CLOB
						|| columnSQLType == Types.DATE
						|| columnSQLType == Types.TIMESTAMP
						|| columnSQLType == Types.TIME
						|| columnSQLType == Types.LONGNVARCHAR
						|| columnSQLType == Types.NCHAR
						|| columnSQLType == Types.NCLOB
						|| columnSQLType == Types.VARCHAR
						|| columnSQLType == Types.TIMESTAMP_WITH_TIMEZONE || columnSQLType == Types.SQLXML); // Thumbsuck,
																												// should
																												// cover
																												// all?

				data.append(getDisplayString(outValue, requiresQuotes,
						(i == columnCount)));

			}
			removeTrailingComma(data);
			finalString.append(String.format(sql, tableName, columnHeadings,
					data.toString()));
			data.setLength(0);
		}
		return finalString.toString();

	}

	/**
	 * Escape the '"' included in string
	 * 
	 * @param s
	 * @return escaped string
	 */
	private String escape(String s) {
		StringBuffer sb = new StringBuffer("");

		if (s == null || s.trim().equals("")) {
			return "null";
		}
		sb.append('\'');
		for (int i = 0, size = s.length(); i < size; i++) {
			char c = s.charAt(i);
			if (c == '\"') {
				sb.append("\"\"");
			} else {
				sb.append(c);
			}
		}
		sb.append('\'');

		return sb.toString();
	}

	/**
	 * Gets the display value with specified delimiter (without filling spaces)
	 * 
	 * @param str
	 *            the value of the column
	 * @param isString
	 *            the delimiter
	 * @param lastColumn
	 *            <true> if this column is the last column of the row
	 * @return the display string of this column
	 */
	private String getDisplayString(String str, boolean isString,
			boolean lastColumn) {
		StringBuffer sb = new StringBuffer("");

		if (isString) {
			sb.append(escape(str));
		} else {
			sb.append(str);
		}

		sb.append(",");

		return sb.toString();
	}

}

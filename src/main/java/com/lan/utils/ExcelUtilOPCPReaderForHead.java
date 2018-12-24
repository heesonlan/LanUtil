package com.lan.utils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.SAXHelper;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler.SheetContentsHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.springframework.util.StringUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * 采用OPCPackage读取excel,可防止内存溢出，当单个excel很大时采用这个类。
 * 这个类不是线程安全的，不可多次使用，每次使用时请new一个对象再使用。
 * @author LAN
 * @date 2018年12月24日
 */
public class ExcelUtilOPCPReaderForHead implements SheetContentsHandler{
	
	private Class toclazz;
	private int rowStart;
	private List data = new ArrayList<>();
	private Map<String, String> methodNameMap = new HashMap<>();
	private Map<String, Method> methodMap = new HashMap<>();
	private Map<String, Class> parameterTypeMap = new HashMap<>();
	private Object currentObj = null;
	
	/**
	 * excel转List。
	 * 要求：
	 * 1、excel首行是为对象的set方法；
	 * 2、对象设置属性的方法必须是set方法，并且set方法参数只能有1个，set方法参数类型可以为[String,Date,BigDecimal,Double,Integer]
	 * 3、每次调用此方法的对象必须是新创建出来的，不可用同一个对象调用此方法多次。
	 * @author LAN
	 * @date 2018年12月24日
	 * @param file
	 * @param clazz
	 * @param rowStart
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> xlsxToList(String file, Class<T> clazz, int rowStart) {
		this.toclazz = clazz;
		this.rowStart = rowStart;
		Method[] declaredMethods = clazz.getDeclaredMethods();
		methodMap = new HashMap<>();
		parameterTypeMap = new HashMap<>();
		for(Method m:declaredMethods){
			String methodName = m.getName();
			if(methodName.startsWith("set")){
				methodMap.put(m.getName(), m);
				Class<?> class1 = m.getParameterTypes()[0];
				parameterTypeMap.put(m.getName(), class1);
			}
		}
		process(file);
		return (List<T>)data;
	}

	private void process(String filename){
		OPCPackage pkg;
		try {
			pkg = OPCPackage.open(filename);
			XSSFReader xssfReader = new XSSFReader(pkg);
			StylesTable stylesTable = xssfReader.getStylesTable();
			XMLReader parser = SAXHelper.newXMLReader();
			ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(pkg); 
			DataFormatter formatter = new DataFormatter();
			ContentHandler handler = new XSSFSheetXMLHandler(stylesTable, strings, this, formatter, false);
			parser.setContentHandler(handler);
			InputStream sheet1 = xssfReader.getSheetsData().next();
			InputSource sheetSource = new InputSource(sheet1);
			parser.parse(sheetSource); // 解析excel的每条记录，在这个过程中startElement()、characters()、endElement()这三个函数会依次执行
			sheet1.close();
		} catch (IOException | OpenXML4JException | SAXException | ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void startRow(int rowNum) {
		if(rowNum>=rowStart){
			try {
				currentObj = toclazz.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void endRow(int rowNum) {
		if(rowNum>=rowStart){
			data.add(currentObj);
		}
	}

	@Override
	public void cell(String cellReference, String formattedValue, XSSFComment comment) {
		CellReference cellReference1 = new CellReference(cellReference);
		int thisCol = cellReference1.getCol();
		int thisRow = cellReference1.getRow();
		
		if(thisRow==0){
			String method = formattedValue;
			methodNameMap.put(""+thisCol, method);
		}
		if(thisRow>=this.rowStart){
			String methodName = methodNameMap.get(""+thisCol);
			if(StringUtils.isEmpty(methodName)){
				return;
			}
			Method method = methodMap.get(methodName);
			if(method==null){
				return;
			}
			Class parameterClass = parameterTypeMap.get(methodName);
			try {
				if(parameterClass==String.class)
					method.invoke(currentObj, formattedValue);
				else if(parameterClass==Date.class){
					System.out.println(thisRow + "cell:"+thisCol+",cellReference:"+cellReference+",formattedValue:"+formattedValue+",comment:"+comment);
					method.invoke(currentObj, getDateValue(formattedValue));
				}else if(parameterClass==BigDecimal.class)
					method.invoke(currentObj, getDecimalValue(formattedValue));
				else if(parameterClass==Double.class)
					method.invoke(currentObj, getDoubleValue(formattedValue));
				else if(parameterClass==Integer.class)
					method.invoke(currentObj, new Integer((int)(double)getDoubleValue(formattedValue)));
			} catch (IllegalAccessException | InvocationTargetException | RuntimeException e) {
				System.out.println(thisRow + "cell:"+thisCol+",cellReference:"+cellReference+",formattedValue:"+formattedValue+",comment:"+comment);
				e.printStackTrace();
			}
		}
	}

	@Override
	public void headerFooter(String text, boolean isHeader, String tagName) {
	}
	
	//字符串转日期
	private Date getDateValue(String str) {
		if(StringUtils.isEmpty(str)){
			return null;
		}
		if(str.contains("-")){
			if(str.contains(":")){
				return DateUtil.parseDate(str, "yyyy-MM-dd HH:mm");
			}
			return DateUtil.parseDate(str, "yyyy-MM-dd");
		}
		if(str.contains("/")){
			if(str.indexOf("/")<3){//MM/dd/yy
				if(str.contains(":")){
					return DateUtil.parseDate(str, "MM/dd/yy HH:mm");
				}
				return DateUtil.parseDate(str, "MM/dd/yy");
			}
			if(str.contains(":")){
				return DateUtil.parseDate(str, "yyyy/MM/dd HH:mm");
			}
			return DateUtil.parseDate(str, "yyyy/MM/dd");
		}
		if(str.contains("年")){
			return DateUtil.parseDate(str, "yyyy年MM月dd日");
		}
		return DateUtil.parseDate(str, "yyyyMMdd");
	}
	
	private BigDecimal getDecimalValue(String str) {
		if(StringUtils.isEmpty(str)){
			return null;
		}
		if("-".equals(str.trim())){
			return null;
		}
		str = str.replaceAll(",", "").trim();
		return new BigDecimal(str);
	}
	
	private Double getDoubleValue(String str) {
		if(StringUtils.isEmpty(str)){
			return null;
		}
		return Double.valueOf(str);
	}
}

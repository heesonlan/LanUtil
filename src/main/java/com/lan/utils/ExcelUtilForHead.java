package com.lan.utils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class ExcelUtilForHead {
	
	private static Logger logger = LoggerFactory.getLogger(ExcelUtilForHead.class);
	
	public static String getValue(XSSFCell hssfCell) {
		
		if (hssfCell.getCellType() == XSSFCell.CELL_TYPE_BOOLEAN) {
			
			return String.valueOf(hssfCell.getBooleanCellValue());
			
		} else if (hssfCell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC) {
			
			return String.valueOf(hssfCell.getNumericCellValue());
		} else {
			
			return String.valueOf(hssfCell.getStringCellValue());
		}
	}
	
	public static BigDecimal getDecimalValue(XSSFCell xssfCell) {
		try {
			if (xssfCell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
				return new BigDecimal(xssfCell.getNumericCellValue());
			} 
			if (xssfCell.getCellType() == HSSFCell.CELL_TYPE_BLANK) {
				return null;
			}
			if(!StringUtils.isEmpty(xssfCell.getStringCellValue())){
				String str = String.valueOf(xssfCell.getStringCellValue()).replaceAll(",", "").trim();
				return new BigDecimal(str);
			}
		} catch (Exception e) {}
		return null;
	}
	
	public static double getNumberValue(XSSFCell hssfCell) {
		try {
			if (hssfCell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC) {

				return hssfCell.getNumericCellValue();
			}
			if (!StringUtils.isEmpty(hssfCell.getStringCellValue())) {

				String str = String.valueOf(hssfCell.getStringCellValue()).trim();
				return getDoubleValue(str);
			}
		} catch (Exception e) {}
		return 0;
	}
	//
	public static Date getDateValue(XSSFCell hssfCell) {
		
		if (hssfCell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC) {
			
			return hssfCell.getDateCellValue();
		} 
		try {
			if(!StringUtils.isEmpty(hssfCell.getStringCellValue())){
				String str = String.valueOf(hssfCell.getStringCellValue()).trim();
				return getDateValue(str);
			}
		} catch (Exception e) {
			logger.error("excel读取日期失败,返回空");
		}
		
		return null;
	}
	
	private static Double getDoubleValue(String str) {
		if(StringUtils.isEmpty(str)){
			return null;
		}
		return Double.valueOf(str);
	}
	
	//字符串转日期
	private static Date getDateValue(String str) {
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
	
	public static <T> List<T> xlsxToList(String file, Class<T> clazz, int rowStart) {
		if(file == null) 
			return null;
		XSSFWorkbook xssfWorkbook;
		try {
			xssfWorkbook = new XSSFWorkbook(file);
		
			XSSFSheet xssfSheet = xssfWorkbook.getSheetAt(0);
			XSSFRow row0 = xssfSheet.getRow(0);
			
			List<Integer> colList = new ArrayList<>();
			List<String> methodList = new ArrayList<>();
			for(int i=0; i<50; i++){
				XSSFCell cell = row0.getCell(i);
				if (null == cell) {
					continue;
				}
				String headMethod = cell.getStringCellValue();
				if(StringUtils.isEmpty(headMethod) || StringUtils.isEmpty(headMethod.trim())){
					continue;
				}
				colList.add(i);
				methodList.add(headMethod);
			}
			Method[] declaredMethods = clazz.getDeclaredMethods();
			Map<String, Method> methodMap = new HashMap<>();
			Map<String, Class> parameterTypeMap = new HashMap<>();
			for(Method m:declaredMethods){
				String methodName = m.getName();
				if(methodName.startsWith("set")){
					methodMap.put(m.getName(), m);
					Class<?> class1 = m.getParameterTypes()[0];
					parameterTypeMap.put(m.getName(), class1);
				}
			}
			
			List<T> list = new ArrayList<T>();
			int rowEnd = xssfSheet.getLastRowNum();
			for (int i=rowStart; i<=rowEnd; i++) {
				XSSFRow row = xssfSheet.getRow(i);
				if (null == row) {
					continue;
				}	
				T t = clazz.newInstance();
				for (int k = 0; k < methodList.size(); k++) {
					String methodName = methodList.get(k);
					int colindex = colList.get(k);
					XSSFCell cell = row.getCell(colindex);
					if (null == cell) {
						continue;
					}
					Method method = methodMap.get(methodName);
					if(method==null){
						continue;
					}
					Class paramType = parameterTypeMap.get(methodName);
					if(paramType==null){
						continue;
					}
					if(paramType==String.class)
						method.invoke(t, getValue(cell));
					else if(paramType==Date.class)
						method.invoke(t, getDateValue(cell));
					else if(paramType==BigDecimal.class)
						method.invoke(t, getDecimalValue(cell));
					else if(paramType==Double.class)
						method.invoke(t, getNumberValue(cell));
					else if(paramType==Integer.class){
						method.invoke(t, new Integer((int)(double)getNumberValue(cell)));
					}
				}
				list.add(t);
			}
			xssfWorkbook.close();
			return list;
		} catch (IOException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e) {
			logger.error("excel转list失败", e);
		}
		return null;
	}
	
}

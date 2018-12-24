package com.lan.utils;


import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.util.StringUtils;

/**
 * 
 * @author LAN
 * @date 2016年12月6日
 */
public class ExcelUtilLan {

	private static Logger logger = Logger.getLogger(ExcelUtilLan.class);
	
	public static String getValue(HSSFCell hssfCell) {
		
		if (hssfCell.getCellType() == HSSFCell.CELL_TYPE_BOOLEAN) {
			
			return String.valueOf(hssfCell.getBooleanCellValue());
			
		} else if (hssfCell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
			
			return String.valueOf(hssfCell.getNumericCellValue());
		} else {
			
			return String.valueOf(hssfCell.getStringCellValue());
		}
	}
	//
	public static String getValue(XSSFCell hssfCell) {
		
		if (hssfCell.getCellType() == XSSFCell.CELL_TYPE_BOOLEAN) {
			
			return String.valueOf(hssfCell.getBooleanCellValue());
			
		} else if (hssfCell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC) {
			
			return String.valueOf(hssfCell.getNumericCellValue());
		} else {
			
			return String.valueOf(hssfCell.getStringCellValue());
		}
	}
	//
	public static Double getNumberValue(HSSFCell hssfCell) {
		try {
			if (hssfCell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
				return hssfCell.getNumericCellValue();
			} 
			if(!StringUtils.isEmpty(hssfCell.getStringCellValue())){
				String str = String.valueOf(hssfCell.getStringCellValue()).replaceAll(",", "").trim();
				return getDoubleValue(str);
			}
		} catch (Exception e) {}
		return null;
	}
	/**
	 * 之前通过double转换的方式可能出空指针异常
	 * @param hssfCell
	 * @return
	 */
	public static BigDecimal getDecimalValue(HSSFCell hssfCell) {
		try {
			if (hssfCell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
				return new BigDecimal(hssfCell.getNumericCellValue());
			} 
			if (hssfCell.getCellType() == HSSFCell.CELL_TYPE_BLANK) {
				return null;
			}
			if(!StringUtils.isEmpty(hssfCell.getStringCellValue())){
				String str = String.valueOf(hssfCell.getStringCellValue()).replaceAll(",", "").trim();
				return new BigDecimal(str);
			}
		} catch (Exception e) {}
		return null;
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
	//
	public static Date getDateValue(HSSFCell hssfCell) {
		
		if (hssfCell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
			
			return hssfCell.getDateCellValue();
		}
		if(!StringUtils.isEmpty(hssfCell.getStringCellValue())){
			
			String str = String.valueOf(hssfCell.getStringCellValue()).trim();
			return getDateValue(str);
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
	//
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
	

	/**
	 * excel转成List<Object>， 只读第一个sheet
	 * @author Lan
	 * @date 2016年6月12日
	 * @param file   文件路径
	 * @param clazz    要转成的类
	 * @param methodsName    字段，第一个字母大写，与set对应，对应的bean类必须有set方法
	 * @param colIndex     字段个列的对应，与fieldName对应使用
	 * @param startRow		从哪行开始读
	 * @return
	 */
	public static <T> List<T> excelToList(String file, Class<T> clazz, String[] methodsName, Class[] paramTypes, int[] colIndexs, int rowStart) {
		if(file == null) 
			return null;
		if(file.endsWith(".xls")){
			return xlsToList(file, clazz, methodsName, paramTypes, colIndexs, rowStart);
		}else if(file.endsWith(".xlsx")){
			return xlsxToList(file, clazz, methodsName, paramTypes, colIndexs, rowStart);
		}else{
			logger.error("ExcelUtil只能转.xls和.xlsx文件，不能处理"+file);
			return null;
		}
	}
	
	/**
	 * xlsx文件类型转list
	 * @author Lan
	 * @date 2016年6月13日
	 * @param file
	 * @param clazz
	 * @param methodsName
	 * @param paramTypes
	 * @param colIndexs
	 * @param rowStart
	 * @return
	 */
	public static <T> List<T> xlsxToList(String file, Class<T> clazz, String[] methodsName, Class[] paramTypes, int[] colIndexs, int rowStart) {
		try{
			if(file == null) 
				return null;
			XSSFWorkbook xssfWorkbook = new XSSFWorkbook(file);
			XSSFSheet xssfSheet = xssfWorkbook.getSheetAt(0);
			List<T> list = new ArrayList<T>();
			int rowEnd = xssfSheet.getLastRowNum();
			for (int i = rowStart; i <= rowEnd; i++) {
				XSSFRow row = xssfSheet.getRow(i);
				if (null == row) {
					continue;
				}	
				T t = clazz.newInstance();
				for (int k = 0; k < methodsName.length; k++) {
					String methodName = methodsName[k];
					int colindex = colIndexs[k];
					Class paramType = paramTypes[k];
					XSSFCell cell = row.getCell(colindex);
					if (null == cell) {
						continue;
					}
					Method method = clazz.getDeclaredMethod(methodName, paramType);
					if(paramType==String.class)
						method.invoke(t, getValue(cell));
					else if(paramType==Date.class)
						method.invoke(t, getDateValue(cell));
					else if(paramType==BigDecimal.class)
						method.invoke(t, new BigDecimal(getNumberValue(cell)));
				}
				list.add(t);
			}
			xssfWorkbook.close();
			return list;
		}catch(Exception e){
			logger.error("error", e);
		}
		return null;
	}
	
	public static <T> List<T> xlsxToList(InputStream file, Class<T> clazz, String[] methodsName, Class[] paramTypes, int[] colIndexs, int rowStart) {
		try{
			if(file == null) 
				return null;
			XSSFWorkbook xssfWorkbook = new XSSFWorkbook(file);
			XSSFSheet xssfSheet = xssfWorkbook.getSheetAt(0);
			List<T> list = new ArrayList<T>();
			int rowEnd = xssfSheet.getLastRowNum();
			for (int i = rowStart; i <= rowEnd; i++) {
				XSSFRow row = xssfSheet.getRow(i);
				if (null == row) {
					continue;
				}	
				T t = clazz.newInstance();
				for (int k = 0; k < methodsName.length; k++) {
					String methodName = methodsName[k];
					int colindex = colIndexs[k];
					Class paramType = paramTypes[k];
					XSSFCell cell = row.getCell(colindex);
					if (null == cell) {
						continue;
					}
					Method method = clazz.getDeclaredMethod(methodName, paramType);
					if(paramType==String.class)
						method.invoke(t, getValue(cell));
					else if(paramType==Date.class)
						method.invoke(t, getDateValue(cell));
					else if(paramType==BigDecimal.class)
						method.invoke(t, getDecimalValue(cell));
					else if(paramType==Double.class)
						method.invoke(t, getNumberValue(cell));
					else if(paramType==Integer.class){
						method.invoke(t, new Integer((int)getNumberValue(cell)));
					}
				}
				list.add(t);
			}
			xssfWorkbook.close();
			return list;
		}catch(Exception e){
			logger.error("error", e);
		}
		return null;
	}
	
	/**
	 * xls文件类型转list
	 * @author Lan
	 * @date 2016年6月13日
	 * @param file
	 * @param clazz
	 * @param methodsName
	 * @param paramTypes
	 * @param colIndexs
	 * @param rowStart
	 * @return
	 */
	public static <T> List<T> xlsToList(String file, Class<T> clazz, String[] methodsName, Class[] paramTypes, int[] colIndexs, int rowStart) {
		try{
			if(file == null) 
				return null;
			InputStream is = new FileInputStream(file);
			return xlsToList(is, clazz, methodsName, paramTypes, colIndexs, rowStart);
		}catch(Exception e){
			logger.error("error", e);
		}
		return null;
	}
	
	public static <T> List<T> xlsToList(InputStream is, Class<T> clazz, String[] methodsName, Class[] paramTypes, int[] colIndexs, int rowStart) {
		try{
			POIFSFileSystem poifsFileSystem = new POIFSFileSystem(is);
			HSSFWorkbook hssfWorkbook = new HSSFWorkbook(poifsFileSystem);
			HSSFSheet hssfSheet = hssfWorkbook.getSheetAt(0);
			List<T> list = new ArrayList<T>();
			int rowEnd = hssfSheet.getLastRowNum();
			for (int i = rowStart; i <= rowEnd; i++) {
				HSSFRow row = hssfSheet.getRow(i);
				if (null == row) {
					continue;
				}	
				T t = clazz.newInstance();
				for (int k = 0; k < methodsName.length; k++) {
					String methodName = methodsName[k];
					int colindex = colIndexs[k];
					Class paramType = paramTypes[k];
					HSSFCell cell = row.getCell(colindex);
					if (null == cell) {
						continue;
					}
					Method method = clazz.getDeclaredMethod(methodName, paramType);
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
			hssfWorkbook.close();
			return list;
		}catch(Exception e){
			e.printStackTrace();
			logger.error("error", e);
		}
		return null;
	}
	
	/**
	 * excel转成List<Object>， 只读第一个sheet
	 * @author Lan
	 * @date 2016年6月12日
	 * @param fileName   文件名
	 * @param file     文件流
	 * @param clazz    要转成的类
	 * @param methodsName    字段，第一个字母大写，与set对应，对应的bean类必须有set方法
	 * @param colIndex     字段个列的对应，与fieldName对应使用
	 * @param startRow		从哪行开始读
	 * @return
	 */
	public static <T> List<T> excelToList(String fileName, InputStream file, Class<T> clazz,
			String[] methodsName, Class[] paramTypes, int[] colIndexs, int rowStart) {
		if(file == null || StringUtils.isEmpty(fileName)) 
			return null;
		if(fileName.endsWith(".xls")){
			return xlsToList(file, clazz, methodsName, paramTypes, colIndexs, rowStart);
		}else if(fileName.endsWith(".xlsx")){
			return xlsxToList(file, clazz, methodsName, paramTypes, colIndexs, rowStart);
		}else{
			logger.error("ExcelUtil只能转.xls和.xlsx文件，不能处理"+file);
			return null;
		}
	}

}

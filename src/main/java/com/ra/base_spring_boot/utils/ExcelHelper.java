package com.ra.base_spring_boot.utils;

import com.ra.base_spring_boot.dto.questions.QuestionRequestDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExcelHelper {
    public static String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    public static boolean hasExcelFormat(MultipartFile file) {
        return TYPE.equals(file.getContentType());
    }

    public static List<QuestionRequestDTO> excelToQuestions(InputStream is) {
        try {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            List<QuestionRequestDTO> questions = new ArrayList<>();

            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();

                // Skip header
                if (rowNumber == 0) {
                    rowNumber++;
                    continue;
                }

                Iterator<Cell> cellsInRow = currentRow.iterator();

                QuestionRequestDTO question = new QuestionRequestDTO();
                List<String> options = new ArrayList<>();

                int cellIdx = 0;
                while (cellsInRow.hasNext()) {
                    Cell currentCell = cellsInRow.next();
                    String cellValue = getCellValueAsString(currentCell);

                    switch (cellIdx) {
                        case 0: // Category
                            question.setCategory(cellValue);
                            break;
                        case 1: // Question Text
                            question.setQuestionText(cellValue);
                            break;
                        case 2: // Option 1
                        case 3: // Option 2
                        case 4: // Option 3
                        case 5: // Option 4
                            if (cellValue != null && !cellValue.isEmpty()) {
                                options.add(cellValue);
                            }
                            break;
                        case 6: // Correct Answer
                            question.setCorrectAnswer(cellValue);
                            break;
                        case 7: // Explanation
                            question.setExplanation(cellValue);
                            break;
                        default:
                            break;
                    }

                    cellIdx++;
                }
                question.setOptions(options);
                questions.add(question);
            }

            workbook.close();
            return questions;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }

    private static String getCellValueAsString(Cell cell) {
        if (cell == null)
            return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // For numbers, handle potential ".0" for integers
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == (long) numericValue) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return "";
            default:
                return "";
        }
    }
}

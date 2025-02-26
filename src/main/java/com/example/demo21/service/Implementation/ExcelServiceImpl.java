package com.example.demo21.service.Implementation;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.demo21.dto.CustomMultipartFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.List;
import java.util.Map;

@Service
public class ExcelServiceImpl {

    @Autowired
    private Cloudinary cloudinary;

    public byte[] processAndDownloadExcel(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            // Open the Excel workbook from the uploaded file
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0); // Assuming the first sheet

            // Process each row
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                Cell filePathCell = row.getCell(1); // Column with image paths
                Cell cloudinaryUrlCell = row.createCell(2); // New column for Cloudinary URLs

                if (filePathCell != null) {
                    String filePath = filePathCell.getStringCellValue();

                    // Upload image to Cloudinary
                    File tempFile = new File(filePath); // Local path or downloaded file
                    Map uploadResult = cloudinary.uploader().upload(tempFile, Map.of());
                    String cloudinaryUrl = (String) uploadResult.get("secure_url");

                    // Write Cloudinary URL to the Excel file
                    cloudinaryUrlCell.setCellValue(cloudinaryUrl);
                }
            }

            // Autosize columns
            for (int i = 0; i < 3; i++) {
                sheet.autoSizeColumn(i); // Adjust column size
            }

            // Create a ByteArrayOutputStream to write the updated Excel file to memory
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                workbook.write(outputStream); // Write to output stream
                workbook.close();
                return outputStream.toByteArray(); // Return the byte array containing the Excel file
            } catch (IOException e) {
                throw new RuntimeException("Error writing the updated Excel file", e);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error processing the Excel file", e);
        }
    }

    public List<Map> getImagesInFolder(String folderName) throws Exception {
        // Use the `resource_type` and `type` parameters to fetch images
        Map<String, Object> options = ObjectUtils.asMap(
                "type", "upload", // Fetch uploaded resources
                "asset_folder", folderName + "/", // Specify the folder
                "max_results", 500 // Maximum number of results to return
        );
        Object cld = cloudinary.api().resources(ObjectUtils.asMap("max_results", 500)).get("resources");
        // Fetch resources (images) in the folder
        Map<?, ?> result = cloudinary.api().resources(options);

        // Extract the list of images from the result
        return (List<Map>) result.get("resources");
    }

}

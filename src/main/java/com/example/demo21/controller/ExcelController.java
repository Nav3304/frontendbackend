package com.example.demo21.controller;

import com.example.demo21.service.Implementation.ExcelServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/excel")
public class ExcelController {

    @Autowired
    private ExcelServiceImpl excelService;

    @PostMapping("/uploadAndDownload")
    public ResponseEntity<byte[]> uploadAndDownloadExcel(@RequestParam("file") MultipartFile file) {
        try {
            byte[] updatedFile = excelService.processAndDownloadExcel(file); // Process the Excel and return as byte array

            // Set headers to download the file as Excel
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=updated_file.xlsx"); // Specify the file name
            headers.add("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"); // Set Excel MIME type

            // Return the updated Excel file
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(updatedFile);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/folder")
    public List<Map> getImagesInFolder(@RequestParam String folderName) throws Exception {
        return excelService.getImagesInFolder(folderName);
    }

}

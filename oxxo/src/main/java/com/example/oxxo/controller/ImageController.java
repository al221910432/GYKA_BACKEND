package com.example.oxxo.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.UUID;

import com.example.oxxo.model.response.MessageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.oxxo.entity.Image;
import com.example.oxxo.exception.BadRequestException;
import com.example.oxxo.exception.InternalServerException;
import com.example.oxxo.service.ImageService;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/image")
@CrossOrigin(origins = "*",maxAge = 3600)
public class ImageController {
    private static String UPLOAD_DIR  = System.getProperty("user.dir") + "/src/main/resources/static/photos/";

    @Autowired
    private ImageService imageService;


    @GetMapping("/")
    public ResponseEntity<?> getList(){
        List<Image> listImage = imageService.getListImage();

        return  ResponseEntity.ok(listImage);
    }

    @GetMapping("/user/{id}")
    @Operation(summary="Obtenga una lista de imágenes de usuario usando user_id")
    public ResponseEntity<?> getListByUser(@PathVariable long userId){
        List<Image> listImage = imageService.getListByUser(userId);

        return ResponseEntity.ok(listImage);
    }

    @PostMapping("/upload-file")
    @Operation(summary="Subir archivos a la base de datos")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file){
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);;
        if (originalFilename != null && originalFilename.length() > 0) {
            if (!extension.equals("png") && !extension.equals("jpg") && !extension.equals("gif") && !extension.equals("svg") && !extension.equals("jpeg")) {
                throw new BadRequestException("Este formato de archivo no es compatible");
            }
            try {
                Image img = new Image();
                img.setName(file.getName());
                img.setSize(file.getSize());
                img.setType(extension);
                img.setData(file.getBytes());
                String uid = UUID.randomUUID().toString();
                String link = UPLOAD_DIR + uid + "." + extension;
                // Create file
                File serverFile = new File(link);
                BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile));
                stream.write(file.getBytes());
                stream.close();

                imageService.save(img);
                return ResponseEntity.ok(img);
            } catch (Exception e) {
                throw new InternalServerException("Error al subir archivo");
            }
        }

        throw new BadRequestException("Archivo inválido");

    }
}

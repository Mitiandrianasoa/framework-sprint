package com.sprint.util;

import com.sprint.model.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * SPRINT 10 - Extraction des fichiers et paramètres d'une requête multipart.
 *
 * S'appuie sur l'API Servlet (request.getParts()) pour séparer :
 *   - les fichiers uploadés  -> MultipartFile,
 *   - les champs textuels    -> paramètres classiques.
 */
public class MultipartRequestHandler {

    /** Vrai si la requête est de type multipart/form-data. */
    public static boolean isMultipartRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase().startsWith("multipart/form-data");
    }

    /** Extrait tous les fichiers uploadés (clé = nom du champ). */
    public static Map<String, MultipartFile> extractMultipartFiles(HttpServletRequest request)
            throws IOException, jakarta.servlet.ServletException {

        Map<String, MultipartFile> files = new HashMap<>();
        if (!isMultipartRequest(request)) {
            return files;
        }
        for (Part part : request.getParts()) {
            if (part.getSize() > 0 && part.getSubmittedFileName() != null) {
                files.put(part.getName(), convertPartToMultipartFile(part));
            }
        }
        return files;
    }

    /** Extrait les paramètres textuels d'une requête multipart. */
    public static Map<String, String> extractMultipartParameters(HttpServletRequest request)
            throws IOException, jakarta.servlet.ServletException {

        Map<String, String> parameters = new HashMap<>();
        if (!isMultipartRequest(request)) {
            Map<String, String[]> paramMap = request.getParameterMap();
            for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
                if (entry.getValue() != null && entry.getValue().length > 0) {
                    parameters.put(entry.getKey(), entry.getValue()[0]);
                }
            }
            return parameters;
        }
        for (Part part : request.getParts()) {
            if (part.getSubmittedFileName() == null) {
                parameters.put(part.getName(), readPartAsString(part));
            }
        }
        return parameters;
    }

    /** Récupère un fichier précis par le nom de son champ. */
    public static MultipartFile getMultipartFile(HttpServletRequest request, String name)
            throws IOException, jakarta.servlet.ServletException {
        if (!isMultipartRequest(request)) {
            return null;
        }
        Part part = request.getPart(name);
        if (part != null && part.getSize() > 0) {
            return convertPartToMultipartFile(part);
        }
        return null;
    }

    private static MultipartFile convertPartToMultipartFile(Part part) throws IOException {
        MultipartFile file = new MultipartFile();
        file.setName(part.getName());
        file.setOriginalFilename(part.getSubmittedFileName());
        file.setContentType(part.getContentType());
        file.setSize(part.getSize());
        file.setBytes(readPartAsBytes(part));
        file.setEmpty(part.getSize() == 0);
        return file;
    }

    private static byte[] readPartAsBytes(Part part) throws IOException {
        try (InputStream in = part.getInputStream()) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[16384];
            int n;
            while ((n = in.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, n);
            }
            return buffer.toByteArray();
        }
    }

    private static String readPartAsString(Part part) throws IOException {
        return new String(readPartAsBytes(part), "UTF-8");
    }
}

package com.sprint.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * SPRINT 10 - Représente un fichier uploadé via un formulaire (multipart/form-data).
 *
 * Le contrôleur reçoit un MultipartFile en paramètre et peut l'enregistrer
 * (transferTo), lire ses octets, son nom d'origine, sa taille, etc.
 */
public class MultipartFile {

    private String name;              // nom du champ de formulaire
    private String originalFilename;  // nom du fichier côté client
    private String contentType;
    private long size;
    private byte[] bytes;
    private boolean empty;

    public MultipartFile() {
    }

    public MultipartFile(String name, String originalFilename, String contentType,
                         long size, byte[] bytes) {
        this.name = name;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.size = size;
        this.bytes = bytes;
        this.empty = (bytes == null || bytes.length == 0);
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }

    public byte[] getBytes() { return bytes; }
    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
        this.empty = (bytes == null || bytes.length == 0);
    }

    public boolean isEmpty() { return empty; }
    public void setEmpty(boolean empty) { this.empty = empty; }

    public String getExtension() {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
    }

    /** Enregistre le fichier sur le disque. */
    public void transferTo(File dest) throws IOException {
        if (bytes != null) {
            Files.write(dest.toPath(), bytes);
        } else {
            throw new IOException("Aucune donnée disponible pour le transfert");
        }
    }

    @Override
    public String toString() {
        return "MultipartFile{name='" + name + "', originalFilename='" + originalFilename
                + "', contentType='" + contentType + "', size=" + size + ", empty=" + empty + '}';
    }
}

package ru.rutmiit.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class SubmitAssignmentDto {
    @NotBlank(message = "Решение не может быть пустым!")
    private String solutionText;

    private String comment;
    private String attachmentUrl;

    public String getAttachmentUrl() {
        return attachmentUrl;
    }

    public void setAttachmentUrl(String attachmentUrl) {
        this.attachmentUrl = attachmentUrl;
    }
}
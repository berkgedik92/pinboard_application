package project.pinboard.Controllers;

import org.apache.tomcat.util.http.fileupload.FileUploadBase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MultipartException;

@ControllerAdvice
public class ExceptionController {

    @ExceptionHandler(value = MultipartException.class)
    public ResponseEntity<Object> handleException(MultipartException e) {
        String message = "Invalid form data";
        if (e.getCause() instanceof IllegalStateException){
            if (e.getRootCause() instanceof FileUploadBase.SizeException) {
                FileUploadBase.SizeException sizeException = (FileUploadBase.SizeException) (e.getRootCause());
                if (sizeException instanceof FileUploadBase.FileSizeLimitExceededException){
                    FileUploadBase.FileSizeLimitExceededException cause = (FileUploadBase.FileSizeLimitExceededException) sizeException;
                    message  = "File Size should be less than: " + cause.getPermittedSize() + " bytes";
                }
                else if (sizeException instanceof FileUploadBase.SizeLimitExceededException){
                    FileUploadBase.SizeLimitExceededException cause = (FileUploadBase.SizeLimitExceededException) sizeException;
                    message  = "Request Size should be less than: " + cause.getPermittedSize() + " bytes";
                }
            }
        }
        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }
}
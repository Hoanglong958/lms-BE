package com.ra.base_spring_boot.advice;

import com.ra.base_spring_boot.exception.*;
import com.ra.base_spring_boot.dto.ResponseWrapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalHandleException {
        /**
         * @param ex MethodArgumentNotValidException
         * @apiNote handle valid exception for validation (400)
         */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<?> handleValidException(MethodArgumentNotValidException ex) {
                Map<String, String> errors = new HashMap<>();
                ex.getFieldErrors().forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                                ResponseWrapper.builder()
                                                .data(errors)
                                                .code(HttpStatus.BAD_REQUEST.value())
                                                .status(HttpStatus.BAD_REQUEST)
                                                .build());
        }

        /**
         * @param ex MaxUploadSizeExceededException
         * @apiNote handle exception max upload file (400)
         */
        @ExceptionHandler(MaxUploadSizeExceededException.class)
        public ResponseEntity<?> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                                ResponseWrapper.builder()
                                                .data(ex.getMessage())
                                                .code(HttpStatus.BAD_REQUEST.value())
                                                .status(HttpStatus.BAD_REQUEST)
                                                .build());
        }

        /**
         * @param ex NoResourceFoundException
         * @apiNote handle exception not found resource (404)
         */
        @ExceptionHandler(NoResourceFoundException.class)
        public ResponseEntity<?> handleNoResourceFoundException(NoResourceFoundException ex) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                                ResponseWrapper.builder()
                                                .data(ex.getMessage())
                                                .code(HttpStatus.NOT_FOUND.value())
                                                .status(HttpStatus.NOT_FOUND)
                                                .build());
        }

        /**
         * @param ex UsernameNotFoundException
         * @apiNote handle username not found exception
         */
        @ExceptionHandler(UsernameNotFoundException.class)
        public ResponseEntity<?> handleUsernameNotFoundException(UsernameNotFoundException ex) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                                ResponseWrapper.builder()
                                                .data(ex.getMessage())
                                                .code(HttpStatus.NOT_FOUND.value())
                                                .status(HttpStatus.NOT_FOUND)
                                                .build());
        }

        /**
         * @param ex HttpBadRequest
         * @apiNote handle exception bad request (400)
         */
        @ExceptionHandler(HttpBadRequest.class)
        public ResponseEntity<?> handleHttpBadReqeust(HttpBadRequest ex) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                                ResponseWrapper.builder()
                                                .data(ex.getMessage())
                                                .code(HttpStatus.BAD_REQUEST.value())
                                                .status(HttpStatus.BAD_REQUEST)
                                                .build());
        }

        /**
         * @param ex HttpUnAuthorized
         * @apiNote handle exception unauthorized (401)
         */
        @ExceptionHandler(HttpUnAuthorized.class)
        public ResponseEntity<?> handleHttpUnAuthorized(HttpUnAuthorized ex) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                                ResponseWrapper.builder()
                                                .data(ex.getMessage())
                                                .code(HttpStatus.UNAUTHORIZED.value())
                                                .status(HttpStatus.UNAUTHORIZED)
                                                .build());
        }

        /**
         * @param ex HttpForbiden
         * @apiNote handle exception forbiden (403)
         */
        @ExceptionHandler(HttpForbiden.class)
        public ResponseEntity<?> handleHttpForbiden(HttpForbiden ex) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                                ResponseWrapper.builder()
                                                .data(ex.getMessage())
                                                .code(HttpStatus.FORBIDDEN.value())
                                                .status(HttpStatus.FORBIDDEN)
                                                .build());
        }

        /**
         * @param ex AccessDeniedException
         * @apiNote handle security access denied exception (403)
         */
        @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
        public ResponseEntity<?> handleAccessDeniedException(
                        org.springframework.security.access.AccessDeniedException ex) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                                ResponseWrapper.builder()
                                                .data("Bạn không có quyền truy cập tài nguyên này!")
                                                .code(HttpStatus.FORBIDDEN.value())
                                                .status(HttpStatus.FORBIDDEN)
                                                .build());
        }

        /**
         * @param ex HttpNotFound
         * @apiNote handle exception not found (404)
         */
        @ExceptionHandler(HttpNotFound.class)
        public ResponseEntity<?> handleHttpNotFound(HttpNotFound ex) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                                ResponseWrapper.builder()
                                                .data(ex.getMessage())
                                                .code(HttpStatus.NOT_FOUND.value())
                                                .status(HttpStatus.NOT_FOUND)
                                                .build());
        }

        /**
         * @param ex HttpConflict
         * @apiNote handle exception conflict (409)
         */
        @ExceptionHandler(HttpConflict.class)
        public ResponseEntity<?> handleHttpConflict(HttpConflict ex) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(
                                ResponseWrapper.builder()
                                                .data(ex.getMessage())
                                                .code(HttpStatus.CONFLICT.value())
                                                .status(HttpStatus.CONFLICT)
                                                .build());
        }

        /**
         * @apiNote map lỗi vi phạm ràng buộc DB về 400 thay vì 500
         */
        @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
        public ResponseEntity<?> handleDataIntegrityViolation(
                        org.springframework.dao.DataIntegrityViolationException ex) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                                ResponseWrapper.builder()
                                                .data("Dữ liệu không hợp lệ hoặc vi phạm ràng buộc!")
                                                .code(HttpStatus.BAD_REQUEST.value())
                                                .status(HttpStatus.BAD_REQUEST)
                                                .build());
        }

        @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
        public ResponseEntity<?> handleConstraintViolation(jakarta.validation.ConstraintViolationException ex) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                                ResponseWrapper.builder()
                                                .data("Dữ liệu không hợp lệ!")
                                                .code(HttpStatus.BAD_REQUEST.value())
                                                .status(HttpStatus.BAD_REQUEST)
                                                .build());
        }

        /**
         * Handle IllegalArgumentException
         * Map to 400 Bad Request
         */
        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                                ResponseWrapper.builder()
                                                .data(ex.getMessage())
                                                .code(HttpStatus.BAD_REQUEST.value())
                                                .status(HttpStatus.BAD_REQUEST)
                                                .build());
        }

        /**
         * Handle generic Exception
         * Map to 500 Internal Server Error but with error message for debugging
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<?> handleGenericException(Exception ex) {
                ex.printStackTrace(); // Log for server logs
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                                ResponseWrapper.builder()
                                                .data("Internal Server Error: " + ex.getMessage())
                                                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                .build());
        }
}

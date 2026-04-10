package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.config.SepayProperties;
import com.ra.base_spring_boot.dto.Registration.RegistrationRequestDTO;
import com.ra.base_spring_boot.dto.Registration.RegistrationResponseDTO;
import com.ra.base_spring_boot.dto.Registration.SepayWebhookDTO;
import com.ra.base_spring_boot.dto.ResponseWrapper;
import com.ra.base_spring_boot.model.User;
import com.ra.base_spring_boot.security.principle.MyUserDetails;
import com.ra.base_spring_boot.services.registration.IRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/registrations")
@RequiredArgsConstructor
@Tag(name = "09 - Registrations", description = "Quản lý đăng ký khóa học và thanh toán học phí")
public class RegistrationController {

        private final IRegistrationService registrationService;
        private final SepayProperties sepayProperties;

        @PostMapping
        @PreAuthorize("hasAuthority('ROLE_USER')")
        @Operation(summary = "Sinh viên đăng ký khóa học", description = "Tạo bản ghi đăng ký với trạng thái PENDING")
        public ResponseEntity<?> register(
                        @AuthenticationPrincipal MyUserDetails userDetails,
                        @RequestBody RegistrationRequestDTO dto) {
                User user = userDetails.getUser();
                RegistrationResponseDTO response = registrationService.register(user, dto);
                return ResponseEntity.status(HttpStatus.CREATED).body(
                                ResponseWrapper.builder()
                                                .status(HttpStatus.CREATED)
                                                .code(201)
                                                .data(response)
                                                .build());
        }

        @GetMapping("/my")
        @PreAuthorize("hasAuthority('ROLE_USER')")
        @Operation(summary = "Xem danh sách đăng ký của tôi", description = "Danh sách khóa học đã đăng ký và trạng thái thanh toán")
        public ResponseEntity<?> getMyRegistrations(@AuthenticationPrincipal MyUserDetails userDetails) {
                List<RegistrationResponseDTO> response = registrationService
                                .getMyRegistrations(userDetails.getUser().getId());
                return ResponseEntity.ok(
                                ResponseWrapper.builder()
                                                .status(HttpStatus.OK)
                                                .code(200)
                                                .data(response)
                                                .build());
        }

        @GetMapping("/all")
        @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TEACHER')")
        @Operation(summary = "Admin/Teacher xem tất cả đăng ký", description = "Danh sách toàn bộ lượt đăng ký trong hệ thống")
        public ResponseEntity<?> getAllRegistrations() {
                List<RegistrationResponseDTO> response = registrationService.getAllRegistrations();
                return ResponseEntity.ok(
                                ResponseWrapper.builder()
                                                .status(HttpStatus.OK)
                                                .code(200)
                                                .data(response)
                                                .build());
        }

        @PatchMapping("/{id}/cancel")
        @PreAuthorize("hasAuthority('ROLE_USER')")
        @Operation(summary = "Sinh viên hủy đăng ký", description = "Chuyển trạng thái sang CANCELLED nếu đang ở PENDING")
        public ResponseEntity<?> cancelRegistration(
                        @AuthenticationPrincipal MyUserDetails userDetails,
                        @PathVariable Long id) {
                RegistrationResponseDTO response = registrationService.cancelRegistration(id, userDetails.getUser());
                return ResponseEntity.ok(
                                ResponseWrapper.builder()
                                                .status(HttpStatus.OK)
                                                .code(200)
                                                .data(response)
                                                .build());
        }

        @GetMapping("/bank-info")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Lấy thông tin SePay/VietQR để thanh toán")
        public ResponseEntity<?> getBankInfo() {
                return ResponseEntity.ok(
                                ResponseWrapper.builder()
                                                .status(HttpStatus.OK)
                                                .code(200)
                                                .data(Map.of(
                                                                "qrAcc", sepayProperties.getQrAcc(),
                                                                "qrBank", sepayProperties.getQrBank()))
                                                .build());
        }

        @GetMapping("/webhook/sepay")
        @Operation(summary = "Kiểm tra trạng thái Webhook SePay")
        public ResponseEntity<?> checkWebhookStatus() {
                return ResponseEntity.ok(Map.of("status", "online", "message", "SePay Webhook endpoint is ready for POST requests"));
        }

        @PostMapping("/webhook/sepay")
        @Operation(summary = "Webhook SePay xác nhận thanh toán", description = "Public endpoint để SePay gửi giao dịch và tự động xác nhận học phí")
        public ResponseEntity<?> handleSepayWebhook(
                        @RequestHeader(value = "Authorization", required = false) String token,
                        @RequestBody SepayWebhookDTO payload) {
                log.info("🔔 [SePay Webhook] Received request with Authorization header: {}", 
                    (token != null) ? (token.length() > 10 ? token.substring(0, 10) + "..." : "present") : "null");
                
                if (!isValidSepayToken(token)) {
                        log.warn("❌ [SePay Webhook] Validation failed for Authorization header");
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                                        ResponseWrapper.builder()
                                                        .status(HttpStatus.UNAUTHORIZED)
                                                        .code(401)
                                                        .data("Invalid SePay webhook token")
                                                        .build());
                }

                RegistrationResponseDTO response = registrationService.processSepayWebhook(payload);
                return ResponseEntity.ok(
                                ResponseWrapper.builder()
                                                .status(HttpStatus.OK)
                                                .code(200)
                                                .data(Map.of(
                                                                "registrationId", response.getId(),
                                                                "paymentStatus", response.getPaymentStatus(),
                                                                "paymentDate", response.getPaymentDate(),
                                                                "enrolledClassName", response.getEnrolledClassName(),
                                                                "transferRef", response.getTransferRef()))
                                                .build());
        }

        @GetMapping("/export/excel")
        @PreAuthorize("hasAuthority('ROLE_ADMIN')")
        @Operation(summary = "Admin xuất danh sách đăng ký ra file Excel")
        public ResponseEntity<byte[]> exportExcel() {
                byte[] data = registrationService.exportToExcel();
                return ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=registrations.xls")
                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                .body(data);
        }

        @GetMapping("/{id}/export/pdf")
        @PreAuthorize("hasAuthority('ROLE_ADMIN')")
        @Operation(summary = "Admin xuất hóa đơn ra file PDF")
        public ResponseEntity<byte[]> exportPdf(@PathVariable Long id) {
                byte[] data = registrationService.generateInvoicePdf(id);
                return ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice_" + id + ".pdf")
                                .contentType(MediaType.APPLICATION_PDF)
                                .body(data);
        }

        private boolean isValidSepayToken(String token) {
                String secret = sepayProperties.getWebhookSecret();
                
                if (secret == null || secret.isBlank()) {
                        log.error("🛑 [SePay Webhook] No webhook secret configured in application.yml!");
                        return false;
                }
                secret = secret.trim();
                
                if (token == null || token.isBlank()) {
                        log.warn("⚠️ [SePay Webhook] No Authorization token provided in request header");
                        return false;
                }
 
                String normalizedToken = token.trim();

                // Accept common auth schemes:
                // - Authorization: Bearer <secret>
                // - Authorization: Apikey <secret>
                // - Authorization: <secret>
                String[] parts = normalizedToken.split("\\s+", 2);
                if (parts.length == 2) {
                        String scheme = parts[0].replace(":", "");
                        if ("Bearer".equalsIgnoreCase(scheme)
                                        || "Apikey".equalsIgnoreCase(scheme)
                                        || "ApiKey".equalsIgnoreCase(scheme)) {
                                normalizedToken = parts[1].trim();
                        }
                }

                // Defensive: some proxies concatenate duplicate headers using commas.
                int commaIndex = normalizedToken.indexOf(',');
                if (commaIndex > 0) {
                        normalizedToken = normalizedToken.substring(0, commaIndex).trim();
                }
                
                boolean match = secret.equals(normalizedToken);
                if (!match) {
                    log.warn("🚫 [SePay Webhook] Token mismatch");
                }
                
                return match;
        }
}

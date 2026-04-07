package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.Registration.RegistrationRequestDTO;
import com.ra.base_spring_boot.dto.Registration.RegistrationResponseDTO;
import com.ra.base_spring_boot.dto.ResponseWrapper;
import com.ra.base_spring_boot.model.User;
import com.ra.base_spring_boot.security.principle.MyUserDetails;
import com.ra.base_spring_boot.services.registration.IRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/registrations")
@RequiredArgsConstructor
@Tag(name = "09 - Registrations", description = "Quản lý đăng ký khóa học và thanh toán học phí")
public class RegistrationController {

        private final IRegistrationService registrationService;

        @Value("${payment.bank.id:970422}")
        private String bankId;

        @Value("${payment.bank.account-no:8888888888}")
        private String accountNo;

        @Value("${payment.bank.account-name:TRUONG HOC ONLINE}")
        private String accountName;

        @Value("${payment.bank.name:MB Bank}")
        private String bankName;

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

        @PatchMapping("/{id}/confirm-payment")
        @PreAuthorize("hasAuthority('ROLE_ADMIN')")
        @Operation(summary = "Admin xác nhận thanh toán", description = "Chuyển trạng thái sang PAID và tự động thêm sinh viên vào lớp")
        public ResponseEntity<?> confirmPayment(@PathVariable Long id) {
                RegistrationResponseDTO response = registrationService.confirmPayment(id);
                return ResponseEntity.ok(
                                ResponseWrapper.builder()
                                                .status(HttpStatus.OK)
                                                .code(200)
                                                .data(response)
                                                .build());
        }

        @PatchMapping("/{id}/confirm-refund")
        @PreAuthorize("hasAuthority('ROLE_ADMIN')")
        @Operation(summary = "Admin xác nhận hoàn tiền", description = "Xác nhận hoàn tiền sau khi kiểm tra ngân hàng")
        public ResponseEntity<?> confirmRefund(@PathVariable Long id) {
                RegistrationResponseDTO response = registrationService.confirmRefund(id);
                return ResponseEntity.ok(
                                ResponseWrapper.builder()
                                                .status(HttpStatus.OK)
                                                .code(200)
                                                .data(response)
                                                .build());
        }

        @PatchMapping("/{id}/payment-submitted")
        @PreAuthorize("hasAuthority('ROLE_USER')")
        @Operation(summary = "Sinh viên báo đã chuyển khoản", description = "Cho phép sinh viên ghi nhận thanh toán trước khi admin xác nhận")
        public ResponseEntity<?> markPaymentSubmitted(@AuthenticationPrincipal MyUserDetails userDetails,
                                                      @PathVariable Long id) {
                RegistrationResponseDTO response = registrationService.markPaymentSubmitted(id, userDetails.getUser());
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

        @PatchMapping("/{id}/request-refund")
        @PreAuthorize("hasAuthority('ROLE_USER')")
        @Operation(summary = "Sinh viên yêu cầu hoàn tiền", description = "Cho phép yêu cầu hoàn tiền sau 3 ngày kể từ ngày thanh toán")
        public ResponseEntity<?> requestRefund(
                        @AuthenticationPrincipal MyUserDetails userDetails,
                        @PathVariable Long id) {
                RegistrationResponseDTO response = registrationService.requestRefund(id, userDetails.getUser());
                return ResponseEntity.ok(
                                ResponseWrapper.builder()
                                                .status(HttpStatus.OK)
                                                .code(200)
                                                .data(response)
                                                .build());
        }

        @GetMapping("/transfer-ref/{code}")
        @PreAuthorize("hasAuthority('ROLE_ADMIN')")
        @Operation(summary = "Tìm đăng ký theo mã chuyển khoản", description = "Dùng khi admin đối soát mã chuyển tiền")
        public ResponseEntity<?> getByTransferRef(@PathVariable("code") String code) {
                RegistrationResponseDTO response = registrationService.getByTransferRef(code);
                return ResponseEntity.ok(
                                ResponseWrapper.builder()
                                                .status(HttpStatus.OK)
                                                .code(200)
                                                .data(response)
                                                .build());
        }

        @PatchMapping("/bulk-confirm")
        @PreAuthorize("hasAuthority('ROLE_ADMIN')")
        @Operation(summary = "Admin xác nhận thanh toán hàng loạt", description = "Chuyển trạng thái sang PAID cho nhiều bản ghi")
        public ResponseEntity<?> confirmBulkPayment(@RequestBody List<Long> ids) {
                List<RegistrationResponseDTO> response = registrationService.confirmBulkPayment(ids);
                return ResponseEntity.ok(
                                ResponseWrapper.builder()
                                                .status(HttpStatus.OK)
                                                .code(200)
                                                .data(response)
                                                .build());
        }

        @GetMapping("/bank-info")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Lấy thông tin ngân hàng để thanh toán")
        public ResponseEntity<?> getBankInfo() {
                return ResponseEntity.ok(
                                ResponseWrapper.builder()
                                                .status(HttpStatus.OK)
                                                .code(200)
                                                .data(Map.of(
                                                                "bankId", bankId,
                                                                "accountNo", accountNo,
                                                                "accountName", accountName,
                                                                "bankName", bankName))
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
}

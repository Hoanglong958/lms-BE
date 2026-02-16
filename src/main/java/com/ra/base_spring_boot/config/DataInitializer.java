// package com.ra.base_spring_boot.config;

// import com.ra.base_spring_boot.model.User;
// import com.ra.base_spring_boot.model.constants.RoleName;
// import com.ra.base_spring_boot.repository.IUserRepository;
// import lombok.RequiredArgsConstructor;
// import org.springframework.boot.CommandLineRunner;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.stereotype.Component;

// import java.time.LocalDateTime;

// @Component
// @RequiredArgsConstructor
// public class DataInitializer implements CommandLineRunner {

//     private final IUserRepository userRepository;
//     private final PasswordEncoder passwordEncoder;

//     @Override
//     public void run(String... args) throws Exception {
//         // 1. Tạo tài khoản ADMIN
//         if (!userRepository.existsByGmail("admin@gmail.com")) {
//             User admin = User.builder()
//                     .firstName("System")
//                     .lastName("Admin")
//                     .fullName("System Admin")
//                     .gmail("admin@gmail.com")
//                     .password(passwordEncoder.encode("admin123"))
//                     .role(RoleName.ROLE_ADMIN)
//                     .isActive(true)
//                     .createdAt(LocalDateTime.now())
//                     .build();
//             userRepository.save(admin);
//             System.out.println("✅ Trình khởi tạo: Đã tạo tài khoản ADMIN (admin@gmail.com / admin123)");
//         }

//         // 2. Tạo tài khoản TEACHER
//         if (!userRepository.existsByGmail("teacher@gmail.com")) {
//             User teacher = User.builder()
//                     .firstName("Main")
//                     .lastName("Teacher")
//                     .fullName("Main Teacher")
//                     .gmail("teacher@gmail.com")
//                     .password(passwordEncoder.encode("teacher123"))
//                     .role(RoleName.ROLE_TEACHER)
//                     .isActive(true)
//                     .createdAt(LocalDateTime.now())
//                     .build();
//             userRepository.save(teacher);
//             System.out.println("✅ Trình khởi tạo: Đã tạo tài khoản TEACHER (teacher@gmail.com / teacher123)");
//         }

//         // 3. Tạo tài khoản USER
//         if (!userRepository.existsByGmail("user@gmail.com")) {
//             User user = User.builder()
//                     .firstName("Basic")
//                     .lastName("User")
//                     .fullName("Basic User")
//                     .gmail("user@gmail.com")
//                     .password(passwordEncoder.encode("user123"))
//                     .role(RoleName.ROLE_USER)
//                     .isActive(true)
//                     .createdAt(LocalDateTime.now())
//                     .build();
//             userRepository.save(user);
//             System.out.println("✅ Trình khởi tạo: Đã tạo tài khoản USER (user@gmail.com / user123)");
//         }
//     }
// }

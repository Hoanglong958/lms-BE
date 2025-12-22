package com.ra.base_spring_boot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import com.ra.base_spring_boot.config.controller.AuthController;
import com.ra.base_spring_boot.services.IAuthService;
import com.ra.base_spring_boot.services.IPasswordResetTokenService;
import com.ra.base_spring_boot.security.principle.MyUserDetailsService;
import com.ra.base_spring_boot.security.jwt.JwtProvider;
import com.ra.base_spring_boot.security.jwt.JwtTokenFilter;

@WebMvcTest(
        controllers = AuthController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings("removal")
class BaseSpringBootApplicationTests
{

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IAuthService authService;
    @MockBean
    private IPasswordResetTokenService passwordResetTokenService;
    @MockBean
    private MyUserDetailsService myUserDetailsService;
    @MockBean
    private JwtProvider jwtProvider;
    @MockBean
    private JwtTokenFilter jwtTokenFilter;
    @MockBean
    private org.springframework.data.jpa.mapping.JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void contextLoads()
    {
    }

}

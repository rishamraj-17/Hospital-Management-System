package com.portfolio.hms.clinical;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.hms.auth.entity.User;
import com.portfolio.hms.auth.enums.Role;
import com.portfolio.hms.auth.repository.UserRepository;
import com.portfolio.hms.clinical.dto.DepartmentRequest;
import com.portfolio.hms.clinical.dto.DoctorRequest;
import com.portfolio.hms.clinical.entity.Department;
import com.portfolio.hms.clinical.repository.DepartmentRepository;
import com.portfolio.hms.clinical.repository.DoctorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ClinicalIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM prescriptions");
        jdbcTemplate.execute("DELETE FROM prescription_items");
        jdbcTemplate.execute("DELETE FROM appointments");
        jdbcTemplate.execute("DELETE FROM doctor_availability");
        jdbcTemplate.execute("DELETE FROM doctors");
        jdbcTemplate.execute("DELETE FROM departments");
        jdbcTemplate.execute("DELETE FROM users");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateAndGetDepartment() throws Exception {
        DepartmentRequest request = new DepartmentRequest("Cardiology", "Heart diseases");
        String response = mockMvc.perform(post("/api/v1/departments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Cardiology"))
                .andReturn().getResponse().getContentAsString();

        String departmentId = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(get("/api/v1/departments/" + departmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cardiology"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRejectDuplicateDepartment() throws Exception {
        DepartmentRequest request = new DepartmentRequest("Neurology", "Brain diseases");
        mockMvc.perform(post("/api/v1/departments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/departments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateAndSearchDoctor() throws Exception {
        // Create User
        User user = User.builder().username("drsmith").email("smith@example.com").password("pass").role(Role.DOCTOR).build();
        userRepository.save(user);

        // Create Department
        Department dept = Department.builder().name("Orthopedics").description("Bones").build();
        departmentRepository.save(dept);

        // Create Doctor
        DoctorRequest doctorRequest = new DoctorRequest(
                user.getId(),
                dept.getId(),
                "LIC123",
                "smith@ortho.com",
                "123456",
                "Orthopedics",
                10
        );

        mockMvc.perform(post("/api/v1/doctors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(doctorRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.licenseNumber").value("LIC123"));

        // Test Search
        mockMvc.perform(get("/api/v1/doctors?specialization=Orthopedics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].licenseNumber").value("LIC123"))
                .andExpect(jsonPath("$.content[0].departmentName").value("Orthopedics"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRejectDuplicateLicense() throws Exception {
        User user1 = User.builder().username("dr1").email("dr1@example.com").password("pass").role(Role.DOCTOR).build();
        User user2 = User.builder().username("dr2").email("dr2@example.com").password("pass").role(Role.DOCTOR).build();
        userRepository.save(user1);
        userRepository.save(user2);

        Department dept = Department.builder().name("General").description("General").build();
        departmentRepository.save(dept);

        DoctorRequest doctorRequest1 = new DoctorRequest(
                user1.getId(), dept.getId(), "LIC-DUP", "dr1@clinic.com", "123", "General", 5
        );
        mockMvc.perform(post("/api/v1/doctors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(doctorRequest1)))
                .andExpect(status().isCreated());

        DoctorRequest doctorRequest2 = new DoctorRequest(
                user2.getId(), dept.getId(), "LIC-DUP", "dr2@clinic.com", "123", "General", 5
        );
        mockMvc.perform(post("/api/v1/doctors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(doctorRequest2)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAggregateDashboardData() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDoctors").isNumber())
                .andExpect(jsonPath("$.activeDepartments").isNumber());
    }
}

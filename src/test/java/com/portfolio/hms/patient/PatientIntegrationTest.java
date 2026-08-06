package com.portfolio.hms.patient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.hms.patient.dto.PatientRequest;
import com.portfolio.hms.patient.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class PatientIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM prescriptions");
        jdbcTemplate.execute("DELETE FROM prescription_items");
        jdbcTemplate.execute("DELETE FROM appointments");
        jdbcTemplate.execute("CREATE SEQUENCE IF NOT EXISTS patient_mrn_seq START WITH 1000 INCREMENT BY 1");
        jdbcTemplate.execute("DELETE FROM patients");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateAndSearchPatient() throws Exception {
        PatientRequest request = new PatientRequest(
                "John",
                "Doe",
                LocalDate.of(1990, 5, 15),
                "Male",
                "O+",
                "1234567890",
                "john.doe@example.com",
                "123 Main St",
                "Jane Doe",
                "0987654321",
                "Wife"
        );

        mockMvc.perform(post("/api/v1/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mrn").exists())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        mockMvc.perform(get("/api/v1/patients")
                .param("name", "john")
                .param("bloodGroup", "O+"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].firstName").value("John"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}

package com.portfolio.hms.appointment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.hms.appointment.dto.AppointmentRequest;
import com.portfolio.hms.appointment.repository.AppointmentRepository;
import com.portfolio.hms.clinical.entity.Department;
import com.portfolio.hms.clinical.entity.Doctor;
import com.portfolio.hms.clinical.repository.DepartmentRepository;
import com.portfolio.hms.clinical.repository.DoctorRepository;
import com.portfolio.hms.patient.entity.Patient;
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
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AppointmentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private DoctorRepository doctorRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Patient testPatient;
    private Doctor testDoctor;

    @Autowired
    private com.portfolio.hms.auth.repository.UserRepository userRepository;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM prescriptions");
        jdbcTemplate.execute("DELETE FROM prescription_items");
        jdbcTemplate.execute("DELETE FROM appointments");
        jdbcTemplate.execute("DELETE FROM patients");
        jdbcTemplate.execute("DELETE FROM doctors");
        jdbcTemplate.execute("DELETE FROM departments");
        jdbcTemplate.execute("DELETE FROM users");
        jdbcTemplate.execute("CREATE SEQUENCE IF NOT EXISTS patient_mrn_seq START WITH 1000 INCREMENT BY 1");

        com.portfolio.hms.auth.entity.User user = userRepository.save(com.portfolio.hms.auth.entity.User.builder()
                .username("doctor_alice")
                .email("alice@test.com")
                .password("password")
                .role(com.portfolio.hms.auth.enums.Role.DOCTOR)
                .build());

        Department dept = departmentRepository.save(Department.builder()
                .name("Cardiology").description("Heart").build());

        testDoctor = doctorRepository.save(Doctor.builder()
                .user(user)
                .specialization("Cardiologist")
                .licenseNumber("LIC-123")
                .experienceYears(10)
                .phone("111").email("alice@test.com")
                .department(dept).build());

        testPatient = patientRepository.save(Patient.builder()
                .mrn("MRN-TEST-123")
                .firstName("Bob").lastName("Jones")
                .dateOfBirth(LocalDate.of(1990, 1, 1)).gender("Male")
                .bloodGroup("O+").phone("222").email("bob@test.com")
                .emergencyContactName("Wife").emergencyContactPhone("333").build());
    }

    @Test
    @WithMockUser(roles = "RECEPTIONIST")
    void shouldScheduleAppointmentAndTransitionStates() throws Exception {
        LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);

        AppointmentRequest req1 = new AppointmentRequest(testPatient.getId(), testDoctor.getId(), startTime, 30);

        String response = mockMvc.perform(post("/api/v1/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(response).get("id").asText();

        // Test Overlap Booking
        AppointmentRequest req2 = new AppointmentRequest(testPatient.getId(), testDoctor.getId(), startTime.plusMinutes(15), 30);
        mockMvc.perform(post("/api/v1/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isBadRequest()); // IllegalStateException mapped to 400

        // Test Confirm Transition
        mockMvc.perform(patch("/api/v1/appointments/" + id + "/confirm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        // Test Complete Transition
        mockMvc.perform(patch("/api/v1/appointments/" + id + "/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
}

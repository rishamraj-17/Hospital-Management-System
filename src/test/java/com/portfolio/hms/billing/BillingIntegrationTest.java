package com.portfolio.hms.billing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.hms.appointment.entity.Appointment;
import com.portfolio.hms.appointment.enums.AppointmentStatus;
import com.portfolio.hms.appointment.repository.AppointmentRepository;
import com.portfolio.hms.auth.entity.User;
import com.portfolio.hms.auth.enums.Role;
import com.portfolio.hms.auth.repository.UserRepository;
import com.portfolio.hms.billing.dto.BillRequest;
import com.portfolio.hms.billing.dto.PaymentRequest;
import com.portfolio.hms.billing.enums.PaymentMethod;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class BillingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    private Appointment completedAppointment;
    private Appointment scheduledAppointment;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM payments");
        jdbcTemplate.execute("DELETE FROM bills");
        jdbcTemplate.execute("DELETE FROM prescriptions");
        jdbcTemplate.execute("DELETE FROM prescription_items");
        jdbcTemplate.execute("DELETE FROM appointments");
        jdbcTemplate.execute("DELETE FROM patients");
        jdbcTemplate.execute("DELETE FROM doctor_availability");
        jdbcTemplate.execute("DELETE FROM doctors");
        jdbcTemplate.execute("DELETE FROM departments");
        jdbcTemplate.execute("DELETE FROM users");
        jdbcTemplate.execute("CREATE SEQUENCE IF NOT EXISTS patient_mrn_seq START WITH 1000 INCREMENT BY 1");

        User user = userRepository.save(User.builder()
                .username("doctor_bill")
                .email("bill@test.com")
                .password("password")
                .role(Role.DOCTOR)
                .build());

        Department dept = departmentRepository.save(Department.builder()
                .name("Cardiology").description("Heart").build());

        Doctor testDoctor = doctorRepository.save(Doctor.builder()
                .user(user)
                .specialization("Cardiologist")
                .licenseNumber("LIC-BILL")
                .experienceYears(10)
                .phone("111").email("bill@test.com")
                .department(dept).build());

        Patient testPatient = patientRepository.save(Patient.builder()
                .mrn("MRN-BILL")
                .firstName("Bob").lastName("Jones")
                .dateOfBirth(LocalDate.of(1990, 1, 1)).gender("Male")
                .bloodGroup("O+").phone("222").email("bob@test.com")
                .emergencyContactName("Wife").emergencyContactPhone("333").build());

        completedAppointment = appointmentRepository.save(Appointment.builder()
                .patient(testPatient)
                .doctor(testDoctor)
                .startTime(LocalDateTime.now().minusDays(1))
                .durationMinutes(30)
                .status(AppointmentStatus.COMPLETED)
                .build());

        scheduledAppointment = appointmentRepository.save(Appointment.builder()
                .patient(testPatient)
                .doctor(testDoctor)
                .startTime(LocalDateTime.now().plusDays(1))
                .durationMinutes(30)
                .status(AppointmentStatus.SCHEDULED)
                .build());
    }

    @Test
    @WithMockUser(roles = "RECEPTIONIST")
    void shouldGenerateBillForCompletedAppointment() throws Exception {
        BillRequest request = new BillRequest(
                completedAppointment.getId(),
                new BigDecimal("100.00"),
                new BigDecimal("50.00"),
                new BigDecimal("0.00")
        );

        mockMvc.perform(post("/api/v1/billing/bills")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalAmount").value(150.00))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = "RECEPTIONIST")
    void shouldRejectBillGenerationForScheduledAppointment() throws Exception {
        BillRequest request = new BillRequest(
                scheduledAppointment.getId(),
                new BigDecimal("100.00"),
                new BigDecimal("50.00"),
                new BigDecimal("0.00")
        );

        mockMvc.perform(post("/api/v1/billing/bills")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // IllegalStateException mapped to 400
    }

    @Test
    @WithMockUser(roles = "RECEPTIONIST")
    void shouldProcessPaymentAndRefundFlow() throws Exception {
        // 1. Generate Bill
        BillRequest request = new BillRequest(
                completedAppointment.getId(),
                new BigDecimal("100.00"),
                new BigDecimal("50.00"),
                new BigDecimal("20.00")
        );

        String response = mockMvc.perform(post("/api/v1/billing/bills")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String billId = objectMapper.readTree(response).get("id").asText();

        // 2. Pay Bill
        PaymentRequest paymentRequest = new PaymentRequest(
                java.util.UUID.fromString(billId),
                new BigDecimal("170.00"),
                PaymentMethod.CREDIT_CARD,
                "TXN123"
        );

        mockMvc.perform(post("/api/v1/billing/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(170.00));

        // 3. Refund Bill
        mockMvc.perform(post("/api/v1/billing/bills/" + billId + "/refund"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REFUNDED"));
    }
}

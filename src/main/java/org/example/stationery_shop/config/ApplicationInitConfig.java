package org.example.stationery_shop.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.stationery_shop.entity.auth.Role;
import org.example.stationery_shop.entity.auth.User;
import org.example.stationery_shop.entity.shipping.Carrier;
import org.example.stationery_shop.enums.UserStatus;
import org.example.stationery_shop.exception.AppException;
import org.example.stationery_shop.exception.ErrorCode;
import org.example.stationery_shop.repository.CarrierRepository;
import org.example.stationery_shop.repository.RoleRepository;
import org.example.stationery_shop.repository.UserRepository;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
@RequiredArgsConstructor
@Slf4j
@Configuration
public class ApplicationInitConfig {

    private final PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner runner(UserRepository userAccountRepository, RoleRepository roleRepository, CarrierRepository carrierRepository) {
        return args -> {
            log.info("===== START INITIALIZING DATABASE =====");

            // 1. INIT ROLES
            Role roleUser = null;
            Role roleAdmin = null;
            Role roleStaff = null;
            Role roleCarrier = null;

            if (!roleRepository.existsByCode("ROLE_USER")) {
                roleUser = new Role();
                roleUser.setCode("ROLE_USER");
                roleUser.setName("User");
                roleUser.setDescription("Default user role");
                roleRepository.save(roleUser);
                log.info("Created ROLE_USER");
            } else {
                roleUser = roleRepository.findByCode("ROLE_USER")
                        .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXIST));
            }

            if (!roleRepository.existsByCode("ROLE_ADMIN")) {
                roleAdmin = new Role();
                roleAdmin.setCode("ROLE_ADMIN");
                roleAdmin.setName("Admin");
                roleAdmin.setDescription("Administrator");
                roleRepository.save(roleAdmin);
                log.info("Created ROLE_ADMIN");
            } else {
                roleAdmin = roleRepository.findByCode("ROLE_ADMIN")
                        .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXIST));
            }
            if (!roleRepository.existsByCode("ROLE_STAFF")) {
                roleStaff = new Role();
                roleStaff.setCode("ROLE_STAFF");
                roleStaff.setName("Staff");
                roleStaff.setDescription("Staff");
                roleRepository.save(roleStaff);
                log.info("Created ROLE_STAFF");
            } else {
                roleStaff = roleRepository.findByCode("ROLE_STAFF")
                        .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXIST));
            }
            if (!roleRepository.existsByCode("ROLE_CARRIER")) {
                roleCarrier = new Role();
                roleCarrier.setCode("ROLE_CARRIER");
                roleCarrier.setName("Carrier");
                roleCarrier.setDescription("Internal delivery carrier");
                roleRepository.save(roleCarrier);
                log.info("Created ROLE_CARRIER");
            } else {
                roleCarrier = roleRepository.findByCode("ROLE_CARRIER")
                        .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXIST));
            }

            // 2. INIT ADMIN USER
            if (!userAccountRepository.existsByEmail("admin@shop.com")) {
                User admin = new User();
                admin.setEmail("admin@shop.com");
                admin.setPassword(passwordEncoder.encode("admin"));
                admin.setName("Admin");
                admin.setStatus(UserStatus.ACTIVE);
                admin.setPhoneVerified(true);

                // Set roles
                HashSet<Role> roles = new HashSet<>();
                roles.add(roleAdmin);
                roles.add(roleUser);
                roles.add(roleStaff);// Admin có cả 2 role (tùy chọn)
                admin.setRoles(roles);

                userAccountRepository.save(admin);
                log.info("Created admin user with email: admin@shop.com, password: admin");
            } else {
                log.info("Admin user already exists");
            }

            // 3. INIT TEST USER (Optional)
            if (!userAccountRepository.existsByEmail("staff@shop.com")) {
                User staff = new User();
                staff.setEmail("staff@shop.com");
                staff.setPassword(passwordEncoder.encode("staff"));
                staff.setName("Staff");
                staff.setStatus(UserStatus.ACTIVE);
                staff.setPhoneVerified(true);

                // Set roles
                HashSet<Role> roles = new HashSet<>();
                roles.add(roleUser);
                roles.add(roleStaff);// Admin có cả 2 role (tùy chọn)
                staff.setRoles(roles);

                userAccountRepository.save(staff);
                log.info("Created staff user with email: staff@shop.com, password: staff");
            } else {
                log.info("Staff user already exists");
            }

            User carrierUser;
            if (!userAccountRepository.existsByEmail("carrier@shop.com")) {
                carrierUser = new User();
                carrierUser.setEmail("carrier@shop.com");
                carrierUser.setPassword(passwordEncoder.encode("carrier"));
                carrierUser.setName("Carrier");
                carrierUser.setPhone("0900000202");
                carrierUser.setStatus(UserStatus.ACTIVE);
                carrierUser.setPhoneVerified(true);

                HashSet<Role> roles = new HashSet<>();
                roles.add(roleUser);
                roles.add(roleCarrier);
                carrierUser.setRoles(roles);

                userAccountRepository.save(carrierUser);
                log.info("Created carrier user with email: carrier@shop.com, password: carrier");
            } else {
                carrierUser = userAccountRepository.findByEmail("carrier@shop.com")
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));
            }

            // 4. INIT DEMO INTERNAL CARRIER
            if (!carrierRepository.existsByPhoneAndProvinceId("0900000202", 202)) {
                Carrier carrier = Carrier.builder()
                        .user(carrierUser)
                        .name("Da Nang Internal Carrier")
                        .phone("0900000202")
                        .provinceId(202)
                        .provinceName("Da Nang")
                        .districtId(1524)
                        .districtName("Hai Chau")
                        .currentAssignedOrders(0)
                        .maxActiveOrders(20)
                        .active(true)
                        .build();
                carrierRepository.save(carrier);
                log.info("Created demo internal carrier for Da Nang");
            } else {
                carrierRepository.findByPhoneAndProvinceId("0900000202", 202)
                        .filter(carrier -> carrier.getUser() == null)
                        .ifPresent(carrier -> {
                            carrier.setUser(carrierUser);
                            carrierRepository.save(carrier);
                            log.info("Linked demo internal carrier to carrier@shop.com");
                        });
                log.info("Demo internal carrier already exists");
            }

            log.info("===== DATABASE INITIALIZATION COMPLETED =====");
        };
    }
}

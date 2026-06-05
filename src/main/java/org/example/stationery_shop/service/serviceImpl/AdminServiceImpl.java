package org.example.stationery_shop.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.example.stationery_shop.dto.response.UserResponse;
import org.example.stationery_shop.dto.response.admin.AdminDashboardResponse;
import org.example.stationery_shop.entity.auth.Role;
import org.example.stationery_shop.entity.auth.User;
import org.example.stationery_shop.enums.OrderStatus;
import org.example.stationery_shop.enums.UserStatus;
import org.example.stationery_shop.exception.AppException;
import org.example.stationery_shop.exception.ErrorCode;
import org.example.stationery_shop.mapper.UserMapper;
import org.example.stationery_shop.repository.BrandRepository;
import org.example.stationery_shop.repository.CategoryRepository;
import org.example.stationery_shop.repository.OrderRepository;
import org.example.stationery_shop.repository.PaymentRepository;
import org.example.stationery_shop.repository.ProductRepository;
import org.example.stationery_shop.repository.ProductVariantRepository;
import org.example.stationery_shop.repository.RoleRepository;
import org.example.stationery_shop.repository.UserRepository;
import org.example.stationery_shop.service.AdminService;
import org.example.stationery_shop.service.serviceImpl.auth.RedisServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_STAFF = "ROLE_STAFF";
    private static final String ROLE_USER = "ROLE_USER";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final UserMapper userMapper;
    private final RedisServiceImpl redisService;

    @Override
    public AdminDashboardResponse getDashboard() {
        var startOfToday = LocalDate.now(VIETNAM_ZONE).atStartOfDay(VIETNAM_ZONE).toInstant();
        var startOfTomorrow = LocalDate.now(VIETNAM_ZONE).plusDays(1).atStartOfDay(VIETNAM_ZONE).toInstant();

        return AdminDashboardResponse.builder()
                .totalRevenue(defaultMoney(paymentRepository.sumSuccessfulAmount()))
                .todayRevenue(defaultMoney(paymentRepository.sumSuccessfulAmountBetween(startOfToday, startOfTomorrow)))
                .totalOrders(orderRepository.count())
                .pendingPaymentOrders(orderRepository.countByStatus(OrderStatus.PENDING_PAYMENT))
                .processingOrders(orderRepository.countByStatus(OrderStatus.PROCESSING))
                .completedOrders(orderRepository.countByStatus(OrderStatus.COMPLETED))
                .totalProducts(productRepository.count())
                .activeProducts(productRepository.countByActiveTrue())
                .totalVariants(productVariantRepository.count())
                .activeVariants(productVariantRepository.countByActiveTrue())
                .totalBrands(brandRepository.count())
                .activeBrands(brandRepository.countByActiveTrue())
                .totalCategories(categoryRepository.count())
                .activeCategories(categoryRepository.countByActiveTrue())
                .totalUsers(userRepository.count())
                .usersByStatus(countUsersByStatus())
                .build();
    }

    @Override
    public List<UserResponse> getUsers(UserStatus status) {
        List<User> users = status == null
                ? userRepository.findAllByOrderByCreatedAtDesc()
                : userRepository.findByStatusOrderByCreatedAtDesc(status);
        return users.stream().map(userMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public UserResponse updateUserStatus(String userId, UserStatus status) {
        User user = userRepository.findWithRolesById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));
        if (user.getRoles().stream().anyMatch(role -> ROLE_ADMIN.equals(role.getCode()))) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }
        user.setStatus(status);
        User savedUser = userRepository.save(user);
        if (status != UserStatus.ACTIVE) {
            redisService.logoutAllDevices(userId);
        }
        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponse updateUserRole(String userId, String roleCode, boolean enabled) {
        if (ROLE_ADMIN.equals(roleCode) || !ROLE_STAFF.equals(roleCode)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        User user = userRepository.findWithRolesById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));

        Role userRole = roleRepository.findByCode(ROLE_USER)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXIST));
        user.getRoles().add(userRole);

        Role staffRole = roleRepository.findByCode(ROLE_STAFF)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXIST));

        if (enabled) {
            user.getRoles().add(staffRole);
        } else {
            user.getRoles().removeIf(role -> ROLE_STAFF.equals(role.getCode()));
        }

        User savedUser = userRepository.save(user);
        redisService.logoutAllDevices(userId);
        return userMapper.toResponse(savedUser);
    }

    @Override
    public void logoutAllUserDevices(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new AppException(ErrorCode.USER_NOT_EXIST);
        }
        redisService.logoutAllDevices(userId);
    }

    @Override
    public void logoutAllUsers() {
        userRepository.findAll()
                .forEach(user -> redisService.logoutAllDevices(user.getId()));
    }

    private Map<UserStatus, Long> countUsersByStatus() {
        Map<UserStatus, Long> result = new EnumMap<>(UserStatus.class);
        Arrays.stream(UserStatus.values())
                .forEach(status -> result.put(status, userRepository.countByStatus(status)));
        return result;
    }

    private BigDecimal defaultMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}

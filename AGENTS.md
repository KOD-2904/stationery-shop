# AGENTS.md

## 1. Project Overview

This project is an e-commerce jewelry system inspired by a PNJ-style business model.

The system sells fixed jewelry SKUs such as rings, earrings, necklaces, bracelets, gold bars, 24K gold rings, diamond jewelry, and gemstone products.

Core business characteristics:

- Products are sold as fixed SKUs, not custom-made items.
- A product can have multiple variants by size/ni.
- Each variant has its own SKU, price, gold weight, and inventory.
- Customers cannot checkout anonymously.
- Email is used for login and OTP delivery.
- Phone number is used as an important customer identity field and must be unique when provided.
- Orders must keep snapshot data because jewelry prices and gold prices can change later.
- Inventory must be handled carefully to avoid overselling.
- Payment webhook handling must be idempotent to avoid duplicate payment processing.

The current backend already has authentication and authorization tables:

- `user_account`
- `role`
- `permission`
- `user_role`
- `role_permission`
- `email_verify_token`
- `user_provider`

Do not remove or rewrite these existing concepts unless explicitly asked.

---

## 2. Tech Stack

Use the following stack unless the user explicitly changes it:

### Backend

- Java 17.0.12
- Spring Boot 4.x
- Spring Security
- Spring Data JPA / Hibernate
- Bean Validation
- MySQL
- Lombok
- JWT authentication
- OAuth2 Google Login
- Email OTP using Java Mail Sender or external email provider

### Frontend

- React.js or Next.js
- TailwindCSS
- Mobile-first UI
- API-based communication with backend

### Database

- MySQL or PostgreSQL
- Use relational design.
- Prefer clear foreign keys.
- Prefer transaction-safe design for inventory, payment, and order workflow.

### Optional Infrastructure

- Redis for caching:
  - Gold price cache
  - OTP rate limiting
  - Refresh token/session tracking
  - Cart or checkout temporary data if needed

---

## 3. General Codex Rules

When modifying this repository:

1. Read the existing code structure before editing.
2. Do not rewrite the whole project unless explicitly requested.
3. Prefer small, focused changes.
4. Keep naming consistent with the existing codebase.
5. Do not introduce unnecessary frameworks.
6. Do not delete existing files unless explicitly requested.
7. Do not change public API response formats unless required.
8. Do not hardcode secrets, tokens, passwords, private keys, or credentials.
9. Do not add fake production credentials.
10. Do not silently ignore exceptions.
11. After changing backend code, check whether DTOs, services, repositories, controllers, and exception handlers still match.
12. If adding database entities, include proper relationships and constraints.
13. If adding business logic, place it in service classes, not controllers.
14. Controllers should be thin.
15. Avoid circular dependencies.
16. Keep code understandable for a student project report.

---

## 4. Backend Architecture Rules

Use a layered architecture.

Recommended package structure:

```text
org.example.stationery_shop
├── config
├── controller
├── dto
│   ├── request
│   └── response
├── entity
├── enums
├── exception
├── mapper
├── repository
├── security
├── service
│   └── impl
└── utils
```

Rules:

- `controller`: receive HTTP requests, validate input, return API response.
- `service`: contain business logic.
- `repository`: database access only.
- `entity`: JPA entities only.
- `dto.request`: request DTOs.
- `dto.response`: response DTOs.
- `mapper`: convert entity to DTO and DTO to entity.
- `exception`: centralized exception handling.
- `security`: JWT, OAuth2, filters, user details, security config.

Controllers must not directly access repositories.

Bad:

```java
@RestController
public class ProductController {
    private final ProductRepository productRepository;
}
```

Good:

```java
@RestController
public class ProductController {
    private final ProductService productService;
}
```

---

## 5. API Response Rule

If the project already uses an `ApiResponse<T>` wrapper, continue using it.

Recommended format:

```java
{
  "code": 200,
  "message": "Success",
  "result": {}
}
```

Do not randomly return raw entities from controllers.

Bad:

```java
return productRepository.findAll();
```

Good:

```java
return ApiResponse.<List<ProductResponse>>builder()
        .code(200)
        .message("Success")
        .result(productService.getAllProducts())
        .build();
```

---

## 6. Authentication & Authorization Rules

The current auth model must be respected:

- `user_account.email` is unique and required.
- `user_account.phone` is unique when provided.
- `user_account.google_id` is unique when provided.
- `user_account.status` can be `ACTIVE`, `INACTIVE`, or `BANNED`.
- `role` and `permission` are many-to-many through `role_permission`.
- `user_account` and `role` are many-to-many through `user_role`.

Rules:

1. Do not store raw passwords.
2. Use `PasswordEncoder` for password hashing.
3. Do not expose password in response DTOs.
4. Do not expose refresh tokens unnecessarily.
5. Do not log access tokens or refresh tokens.
6. Do not allow banned users to authenticate.
7. Keep Google OAuth2 login compatible with the existing auth flow.
8. Use Spring Security authorization rules for protected APIs.
9. Admin-only APIs must require proper roles/permissions.
10. Customer APIs must only access the current authenticated user's data unless admin permission is present.

---

## 7. JWT Rules

When editing JWT-related code:

1. Keep access token and refresh token responsibilities separate.
2. Access token should be short-lived.
3. Refresh token should be longer-lived and revocable.
4. Prefer storing refresh token session metadata in Redis or database.
5. Include stable user identity in token claims.
6. If `userId` is already used as subject/claim, keep it consistent.
7. Validate token signature and expiration.
8. Do not trust client-provided user IDs when authenticated user can be read from SecurityContext.

Recommended approach:

- Use `userId` internally for database lookup.
- Use email for login credential.
- Use phone as customer identity/business identity after verification.

---

## 8. OTP & Email Rules

The system uses Email as the main OTP channel.

OTP purposes may include:

- Register account
- Verify email
- Forgot password
- Checkout phone verification
- Pickup verification
- Delivery verification

Rules:

1. OTP must expire.
2. OTP must be single-use.
3. Do not store OTP in plain text if security is being improved.
4. Apply rate limiting:
   - Resend locked for 60 seconds.
   - Maximum 3 OTP requests per 15 minutes.
   - Temporary lock for 1 hour if exceeded.
5. Do not reveal whether an email exists in sensitive flows unless the project already allows it.
6. Email sending logic should be in a dedicated service.

---

## 9. ERD / Database Design Rules

The temporary ERD should be treated as expandable.

Important modules:

### Auth

- `user_account`
- `role`
- `permission`
- `user_role`
- `role_permission`
- `email_verify_token`
- `user_provider`

### Customer

- `customer_profile`
- `customer_address`
- `vip_tier`

### Catalog

- `category`
- `collection`
- `product`
- `product_variant`
- `product_image`
- `product_certificate`

### Inventory

- `store`
- `inventory`
- `inventory_transaction`

### Cart & Wishlist

- `cart`
- `cart_item`
- `wishlist`

### Voucher

- `voucher`
- `voucher_category`
- `voucher_usage`

### Order

- `orders`
- `order_item`
- `order_status_history`

### Payment

- `payment`
- `payment_webhook_log`

### Shipping & Click And Collect

- `shipment`
- `otp_token`

### Return / Refund

- `return_request`
- `refund`

### Loyalty

- `vip_tier`
- `loyalty_transaction`

### Admin / Audit

- `notification_log`
- `audit_log`

Do not merge everything into one huge table.

---

## 10. Product & SKU Rules

A parent product is not the same as a sellable SKU.

Use this concept:

- `product`: parent product.
- `product_variant`: sellable SKU by size/ni.

Example:

- Product: "Nhẫn vàng 18K đính đá ABC"
- Variants:
  - SKU ABC-SIZE-12
  - SKU ABC-SIZE-13
  - SKU ABC-SIZE-14

Rules:

1. Customer must select a variant before adding to cart.
2. Inventory must be checked by variant, not only by product.
3. Product images can belong to product or variant.
4. Certificate can belong to product or variant.
5. Product detail should show material, gold age, stone type, gold weight, labor cost, and certificate info when available.

---

## 11. Pricing Rules

Jewelry pricing can change depending on product type.

Expected pricing types:

- `DYNAMIC_GOLD`
- `DAILY_FIXED`
- `ABSOLUTE_FIXED`

Rules:

1. Gold bar / 24K plain gold ring may use dynamic gold price.
2. Fashion jewelry may use fixed daily price.
3. Diamond or gemstone jewelry may use absolute fixed price.
4. Cart price is not permanently frozen.
5. Price must be recalculated before checkout.
6. Order price must be frozen only after order creation.
7. `order_item` must store price snapshot fields.

Never calculate final accounting data only from current product price, because product price can change after order creation.

---

## 12. Inventory Rules

Inventory is critical.

Rules:

1. Inventory must be tracked by `product_variant` and `store`.
2. Avoid overselling.
3. Use transaction-safe logic when locking or deducting inventory.
4. Consider pessimistic locking or high isolation transaction for checkout.
5. Adding to cart must not deduct inventory.
6. Creating order may temporarily lock inventory.
7. Successful payment deducts inventory permanently.
8. Expired payment releases locked inventory.
9. Return can increase inventory depending on business rule.
10. Every inventory change should be recorded in `inventory_transaction`.

Inventory transaction types:

- `IMPORT`
- `LOCK`
- `RELEASE`
- `DEDUCT`
- `RETURN`
- `ADJUST`

---

## 13. Cart Rules

Rules:

1. One user should have one active cart.
2. Cart item references `product_variant`.
3. Cart does not freeze price.
4. Cart total must be recalculated when opening cart or checkout.
5. If variant is out of stock, checkout must be blocked.
6. Quantity must be validated against available inventory.

---

## 14. Order Rules

Order state machine is important.

Possible order statuses:

- `PENDING_PAYMENT`
- `PAYMENT_FAILED`
- `PAID`
- `CONFIRMED`
- `PREPARING`
- `READY_FOR_PICKUP`
- `SHIPPING`
- `DELIVERED`
- `PICKED_UP`
- `COMPLETED`
- `CANCELLED`
- `EXPIRED`
- `RETURN_REQUESTED`
- `RETURNED`
- `REFUNDED`

Rules:

1. Do not update order status randomly.
2. Add status history when order status changes.
3. Use service methods for status transitions.
4. Keep order snapshot data immutable after creation unless there is a clear business process.
5. Do not delete orders in normal business flow.
6. Use cancellation or expiration statuses instead.

---

## 15. Payment Rules

Payment handling must be idempotent.

Payment methods may include:

- `VNPAY`
- `COD`

Payment statuses:

- `PENDING`
- `SUCCESS`
- `FAILED`
- `REFUNDED`

Rules:

1. `idempotency_key` must be unique.
2. Webhook/callback must not process the same transaction twice.
3. Store provider transaction ID.
4. Store raw webhook payload in `payment_webhook_log` if needed for debugging.
5. Do not store raw card data, CVV, or sensitive payment information.
6. Payment success should update order status through a service method.
7. Payment failure should not corrupt inventory state.

---

## 16. Shipping & Click And Collect Rules

Receiving methods:

- `HOME_DELIVERY`
- `CLICK_AND_COLLECT`

Click & Collect types:

- `RESERVE_NOW_PAY_LATER`
- `PAY_ONLINE_PICK_UP`

Rules:

1. Reserve Now Pay Later locks inventory temporarily.
2. Pay Online Pick Up deducts or reserves inventory after payment success.
3. Pickup requires Secure OTP verification by email.
4. Delivery may require Secure OTP verification by phone.
5. Shipment should reference the source store if applicable.
6. If shipping API fails, fallback strategy can be used if implemented.

---

## 17. Voucher & Promotion Rules

Rules:

1. Validate voucher before applying.
2. Check voucher active status.
3. Check start and end time.
4. Check minimum order amount.
5. Check usage limit.
6. Check applicable categories if configured.
7. Record voucher usage after order is successfully created or paid depending on business rule.
8. Avoid applying the same voucher multiple times incorrectly.

---

## 18. Loyalty Rules

Loyalty is based on completed orders.

Rules:

1. Add loyalty points only when order becomes `COMPLETED`.
2. Do not add points for `CANCELLED` or `EXPIRED` orders.
3. Refund or return should adjust loyalty.
4. VIP tier can be calculated from total spending in the last 12 months.
5. Phone number is important for customer identity, but the database may still reference `user_id`.

---

## 19. Audit Log Rules

Admin actions that should be audited:

- Product price update
- Inventory adjustment
- Refund approval
- Role or permission update
- Order manual status update
- Voucher configuration update

Rules:

1. Store actor user ID.
2. Store action name.
3. Store target table and target ID.
4. Store old value and new value when useful.
5. Do not store sensitive secrets in audit log.

---

## 20. Validation Rules

Use Bean Validation where appropriate.

Examples:

```java
@NotBlank
private String email;

@Email
private String email;

@NotNull
private BigDecimal price;

@Min(1)
private Integer quantity;
```

Rules:

1. Validate request DTOs.
2. Do not validate only in frontend.
3. Return consistent error responses.
4. Keep validation messages understandable.

---

## 21. Exception Handling Rules

Use centralized exception handling.

Recommended classes:

- `AppException`
- `ErrorCode`
- `GlobalExceptionHandler` or existing exception handler

Rules:

1. Do not throw generic `RuntimeException` for expected business errors.
2. Use meaningful error codes.
3. Do not expose stack traces to users.
4. Log server-side errors.
5. Keep API error response consistent.

---

## 22. Entity Rules

When creating JPA entities:

1. Use `@Entity`.
2. Use `@Table`.
3. Use `@Id`.
4. Use clear column names.
5. Use enum fields carefully.
6. Prefer `@Enumerated(EnumType.STRING)` for enums.
7. Avoid exposing entities directly in API responses.
8. Avoid lazy loading issues in response mapping.
9. Be careful with bidirectional relationships.
10. Do not use Lombok `@Data` blindly on JPA entities if it causes recursive `toString`, `equals`, or `hashCode`.

Preferred Lombok pattern for entities:

```java
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "example")
public class Example {
}
```

Be careful with:

```java
@Data
@Entity
public class Example {
}
```

---

## 23. DTO & Mapper Rules

Rules:

1. Request DTOs should not contain fields controlled by server such as `id`, `createdAt`, `updatedAt`, unless needed.
2. Response DTOs should not expose password, token secrets, or internal sensitive fields.
3. Use mapper classes or MapStruct if the project already uses it.
4. Do not map complex business logic inside controllers.

---

## 24. Testing Rules

When asked to write tests:

1. Prefer service-level unit tests for business logic.
2. Prefer controller tests for API behavior.
3. For inventory, include concurrency-related test scenarios when possible.
4. For payment webhook, test duplicate callback/idempotency.
5. For order state machine, test invalid transitions.
6. For voucher, test expired voucher, minimum order, usage limit, and invalid category.

Do not create meaningless tests that only check object construction.

---

## 25. Security Rules

1. Never commit secrets.
2. Never log JWT tokens.
3. Never log OTP values in production-level code.
4. Never expose password hash in API response.
5. Enforce authorization on admin endpoints.
6. Validate ownership for customer resources.
7. Use HTTPS in deployment assumptions.
8. Do not store raw payment card data.
9. Sanitize file uploads if image upload is added.
10. Restrict CORS properly.

---

## 26. Frontend Rules

If editing frontend code:

1. Use component-based structure.
2. Use TailwindCSS.
3. Keep mobile-first layout.
4. Product listing should support filters.
5. Product detail must require size selection before add to cart.
6. Cart must refresh price before checkout.
7. Checkout must require login.
8. If user has no phone number, require phone update/verification before payment.
9. Admin UI should not be mixed with customer UI without route protection.
10. Keep API error messages user-friendly.

---

## 27. Naming Rules

Use English names in code.

Recommended naming examples:

- `UserAccount`
- `Product`
- `ProductVariant`
- `Inventory`
- `InventoryTransaction`
- `Order`
- `OrderItem`
- `Payment`
- `Shipment`
- `Voucher`
- `LoyaltyTransaction`

Database table names can use snake_case:

- `user_account`
- `product_variant`
- `inventory_transaction`
- `order_item`
- `payment_webhook_log`

Enums should use uppercase snake case:

```java
ACTIVE
INACTIVE
BANNED
PENDING_PAYMENT
READY_FOR_PICKUP
PAY_ONLINE_PICK_UP
```

---

## 28. Things Codex Must Not Do

Do not:

1. Rewrite the whole codebase without instruction.
2. Replace Spring Security with another auth framework.
3. Remove existing JWT/OAuth2 flow without asking.
4. Return JPA entities directly from controllers.
5. Store passwords in plain text.
6. Store card/CVV data.
7. Skip inventory locking in checkout.
8. Process payment webhook without idempotency.
9. Add guest checkout.
10. Assume product price is stable forever.
11. Remove order snapshot fields.
12. Merge product and variant into one concept if variants are required.
13. Ignore phone uniqueness.
14. Ignore email uniqueness.
15. Add excessive abstractions that make the student project hard to explain.
16. Add dependencies without a clear reason.
17. Change package names globally unless requested.
18. Break existing tests or API contracts silently.

---

## 29. Preferred Workflow For Codex

For every task:

1. Inspect relevant files first.
2. Identify existing naming and style.
3. Make the smallest safe change.
4. Update related DTOs/services/repositories/controllers if needed.
5. Add or update tests if the task affects logic.
6. Run available tests or at least mention which tests should be run.
7. Summarize:
   - What changed
   - Files changed
   - Any risk or follow-up needed

---

## 30. Current Project Notes

The project is still evolving.

Current completed/known area:

- Authentication schema already exists.
- Role and permission schema already exists.
- Email verification token exists.
- Google provider support exists.
- Future modules should integrate with this auth model instead of replacing it.

Near-future modules likely to be implemented:

1. Product catalog
2. Product variants by size/ni
3. Inventory by store
4. Cart
5. Order
6. Payment
7. Voucher
8. Shipping / Click & Collect
9. Loyalty
10. Audit log

When generating new code, prefer creating code that can be extended module by module.

---

## 31. Language Preference

The user usually discusses requirements in Vietnamese.

When explaining changes to the user, use Vietnamese unless the user asks otherwise.

Code, class names, method names, database names, and comments may remain in English for consistency.

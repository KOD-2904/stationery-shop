# FE.md

## Muc tieu

Xay dung frontend React cho project jewelry e-commerce nay bang Vite.

Frontend can uu tien lam duoc luong MVP:

- Dang ky, dang nhap, dang xuat.
- Xem danh sach san pham, chi tiet san pham.
- Chon variant/size truoc khi them vao gio hang.
- Quan ly gio hang.
- Quan ly dia chi giao hang.
- Tinh phi ship, chon dich vu ship neu can.
- Checkout tu gio hang hoac mua ngay.
- Ap dung voucher.
- Thanh toan VNPAY hoac COD.
- Xem lich su don hang va chi tiet don.
- Wishlist.
- Admin co ban: catalog, store, inventory, voucher, order.

## Stack bat buoc

Dung cac package nay:

```json
{
  "react": "latest",
  "react-dom": "latest",
  "vite": "latest",
  "@vitejs/plugin-react": "latest",
  "lucide-react": "latest"
}
```

Nen hoi chu project truoc khi them package khac. Neu can them, de xuat:

- `react-router-dom`: routing nhieu page.
- `tailwindcss`: styling nhanh, mobile-first.
- `axios`: API client. Neu khong them thi dung `fetch`.
- `clsx`: xu ly class conditional.

## Backend

Backend chay mac dinh:

```txt
http://localhost:8080
```

Tat ca response thuong theo wrapper:

```json
{
  "code": 200,
  "message": "Success",
  "result": {}
}
```

Khi goi API, frontend can doc `data.result`.

Auth hien tra token va set cookie `accessToken`, `refreshToken`.
Frontend nen goi API voi:

```js
fetch(url, {
  credentials: "include"
})
```

Neu dung bearer token tu response thi luu y khong log token, khong hien token tren UI.

## GHN mock

Dang test GHN nen backend co che do mock.

Trong `.env` backend co the set:

```env
GHN_MOCK_ENABLED=true
```

Khi bat mock:

- Tinh phi ship tra ve phi gia `30000`.
- Tao don GHN thanh cong voi ma `MOCK-GHN-{orderId}`.
- Lay thong tin GHN tra status gia `created`.
- Khong ton luot GHN that.

FE khong can xu ly khac, cu goi API checkout/shipping nhu binh thuong.

## Cau truc frontend de xuat

```txt
src
|-- api
|   |-- client.js
|   |-- authApi.js
|   |-- catalogApi.js
|   |-- cartApi.js
|   |-- checkoutApi.js
|   |-- orderApi.js
|   |-- addressApi.js
|   |-- wishlistApi.js
|   |-- voucherApi.js
|   `-- adminApi.js
|-- components
|   |-- layout
|   |-- product
|   |-- cart
|   |-- checkout
|   `-- common
|-- pages
|   |-- HomePage.jsx
|   |-- LoginPage.jsx
|   |-- RegisterPage.jsx
|   |-- ProductListPage.jsx
|   |-- ProductDetailPage.jsx
|   |-- CartPage.jsx
|   |-- CheckoutPage.jsx
|   |-- OrdersPage.jsx
|   |-- OrderDetailPage.jsx
|   |-- WishlistPage.jsx
|   `-- admin
|-- state
|   |-- authState.js
|   `-- cartState.js
|-- utils
|   |-- money.js
|   `-- apiError.js
`-- App.jsx
```

## UI guideline

- Mobile-first.
- Dung `lucide-react` cho icon: cart, search, heart, user, menu, plus, minus, trash, edit, package, truck.
- Khong lam landing page marketing qua dai. Trang dau nen la product/catalog experience.
- Product card can hien: anh, ten, gia min/variant dau tien, brand/category neu co, nut wishlist.
- Product detail bat buoc chon variant/size moi cho add to cart/buy now.
- Cart phai hien item unavailable neu backend tra `availableForCheckout=false`.
- Checkout phai bat login.
- Neu order co `paymentUrl`, redirect user sang VNPAY.

## Visual direction cho web trang suc

Tham khao tinh than tu cac site trang suc nhu Tiffany, Cartier, Mejuri, PNJ: giao dien can sang, thoang, anh san pham lon, nhieu khoang trang, filter ro rang, mua hang nhanh nhung khong lam cam giac "sale app" qua re tien.

Huong thiet ke nen theo:

- Nen chinh: `#FFFFFF`, `#F7F5F0`, `#111111`.
- Mau nhan: vang kim nhe `#C8A24A`, champagne `#E8D8B8`, xanh ngoc rat tiet che `#7DBDB4`.
- Text: den gan muc, xam dam, han che gradient.
- Border: xam nhat `#E7E2D8`.
- Radius: 6-8px cho card/input/button; khong dung bo tron qua lon.
- Shadow: rat nhe, uu tien border va spacing hon shadow.
- Font: neu duoc dung font system truoc. Neu them font thi chon serif thanh lich cho heading va sans-serif de doc cho body. Hoi chu project truoc khi them Google Fonts.

Phong cach:

- Product image phai la trung tam, card it text.
- Gia, material, size, stock status phai scan nhanh.
- Dung filter sidebar/drawer cho category, material, stone type, price, brand.
- Tren mobile, filter la bottom sheet/drawer.
- Trang admin khong lam theo style luxury qua da; admin can dense, ro, de thao tac.

## Animation va micro-interactions

Dung animation de tao cam giac cao cap, khong lam cham checkout.

Nguyen tac:

- Moi animation 120-240ms, easing `cubic-bezier(0.2, 0.8, 0.2, 1)`.
- Ton trong `prefers-reduced-motion`.
- Khong animation layout lon khi user dang checkout hoac admin dang thao tac.
- Khong dung orb/blob/gradient nen trang tri.

Nen co:

- Product card hover: anh zoom nhe `scale(1.03)`, hien nut wishlist/add quick action.
- Heart wishlist: transition fill/scale nhe khi add/remove.
- Add to cart: button loading state, sau do cart icon pulse 1 lan.
- Drawer cart/mobile menu/filter: slide 180-220ms, backdrop fade.
- Toast: slide up/fade, auto close, co icon lucide.
- Skeleton loading: shimmer nhe cho product grid, cart, order list.
- Image gallery PDP: fade/crossfade khi doi anh; thumbnail active border vang.
- Checkout stepper: active step transition mau/border, khong animate chieu cao qua manh.
- Admin row action: hover background xam rat nhe, icon button tooltip.

Khong nen:

- Parallax nang.
- Animation xoay trang suc 3D neu khong co asset that.
- Auto carousel hero chay lien tuc.
- Motion lam nut checkout doi vi tri.

CSS motion mau:

```css
:root {
  --ease-luxury: cubic-bezier(0.2, 0.8, 0.2, 1);
}

@media (prefers-reduced-motion: reduce) {
  * {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    scroll-behavior: auto !important;
    transition-duration: 0.01ms !important;
  }
}

.product-card-image {
  transition: transform 180ms var(--ease-luxury);
}

.product-card:hover .product-card-image {
  transform: scale(1.03);
}
```

## Trang Home / Product Listing

Trang dau khong can hero marketing dai. Nen la "shopfront" co:

- Top nav sticky: logo, search, category, wishlist, cart, account.
- Mot hero ngan 40-55vh co anh trang suc that/anh import, text khong nam trong card.
- Ben duoi hero phai lo mot phan product grid tren desktop/mobile.
- Section category chips: rings, earrings, necklaces, bracelets, gold bars, diamonds.
- Product grid 2 cot mobile, 3-4 cot desktop.
- Filter + sort:
  - category
  - brand
  - material
  - gold age
  - stone type
  - price range
  - in stock
- Search bar nen co debounce 250-350ms neu FE filter local; neu backend chua co search API thi filter client tren list hien tai.

Product card:

- Anh vuong hoac 4:5, nen neutral.
- Ten san pham toi da 2 dong.
- Gia format VND.
- Hien material/gold age nho.
- Neu product co nhieu variant, hien "Tu {price}" hoac "Nhieu size".
- Icon heart goc tren phai.
- Khong dat qua nhieu badge.

## Product Detail Page

PDP nen uu tien:

- Gallery lon ben trai desktop, sticky purchase panel ben phai.
- Mobile: gallery truoc, thong tin mua ngay ben duoi.
- Ten san pham, SKU variant da chon, gia, material, gold age, stone type.
- Variant/size selector bat buoc.
- Store availability/pickup candidates sau khi chon variant.
- CTA ro:
  - `Add to cart`
  - `Buy now`
  - wishlist icon
- Disable CTA neu chua chon variant hoac het hang.
- Accordion thong tin:
  - Product details
  - Material & care
  - Shipping / pickup
  - Warranty / return policy placeholder

Microcopy:

- Neu chua chon size: "Chon size de them vao gio hang".
- Neu het hang: "Tam het tai cua hang da chon".
- Neu dynamic gold/fixed price: "Gia co the duoc tinh lai tai checkout".

## Cart UX

Cart nen co:

- List item ro anh, ten, size, store, quantity, unit price, line total.
- Quantity stepper dung icon `Minus`, `Plus`.
- Remove dung icon `Trash2`.
- Hien warning neu `availableForCheckout=false`.
- Subtotal o sticky summary tren desktop, bottom summary tren mobile.
- Nut `Checkout` disabled neu co item unavailable, tru khi user chon remove unavailable theo flow backend.

Animation:

- Khi update quantity: row loading opacity 60%, khong xoa row ngay.
- Khi remove: collapse 160ms roi refetch cart.

## Checkout UX

Checkout nen chia thanh step:

1. Dia chi / pickup method.
2. Shipping service / fee.
3. Voucher.
4. Payment.
5. Review.

Luu y:

- Luon goi tinh ship truoc khi checkout home delivery.
- Neu GHN mock dang bat, FE van hien nhu ship that.
- Voucher validate rieng truoc hoac khi submit checkout.
- Total summary phai hien:
  - subtotal
  - shipping fee
  - voucher code
  - discount amount
  - total amount
- Neu response co `paymentUrl`, redirect ngay.
- Neu COD va success, dan user ve order detail.

Checkout animation:

- Step transition fade/slide nhe.
- Summary sticky khong nhay layout.
- Loading overlay chi trong panel dang submit, khong block toan page tru khi dang redirect payment.

## Order UX

My orders:

- Tabs/filter theo status: All, Pending payment, Processing, Shipping, Completed, Cancelled.
- Order card hien id ngan, ngay tao, status chip, total, item count.
- Status chip mau:
  - pending: amber
  - processing/shipping: blue
  - completed: green
  - failed/cancelled: red/gray
  - manual: purple/amber

Order detail:

- Timeline status history neu co API admin thi admin xem duoc; customer hien status hien tai + shipping info.
- Nut cancel chi hien khi backend cho phep, xu ly loi transition bang message than thien.
- GHN info nen co block shipping status, mock hay real khong can phan biet trong UI customer.

## Admin UI direction

Admin la operational tool, khong dung hero/luxury layout.

Nen co:

- Sidebar trai: Dashboard placeholder, Products, Categories, Brands, Stores, Inventory, Vouchers, Orders.
- Top bar: search, user menu.
- Tables dense nhung thoang:
  - sticky header
  - filter row
  - status chip
  - action icon buttons
- Form drawer hoac modal cho create/update.
- Upload image dung dropzone nho, preview anh.
- Admin order detail co panel status transition va note.

Admin animation:

- Drawer slide 200ms.
- Modal fade/scale 120ms.
- Table row hover nhe.
- Toast sau khi save.

Khong nen:

- Card long nhau.
- Dashboard decorative cards qua nhieu mau.
- Animation gay delay khi thao tac inventory/order.

## Accessibility va polish

- Button/input phai co focus ring ro.
- Icon-only button phai co `aria-label`.
- Anh product phai co alt text theo product name.
- Color contrast dat muc de doc tren nen sang.
- Form error hien gan input.
- Loading state khong lam mat context.
- Empty state can co action tiep theo: "Tiep tuc mua sam", "Them dia chi", "Tao voucher".
- Money format dung VND:

```js
export function formatVnd(value) {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
    maximumFractionDigits: 0
  }).format(value || 0);
}
```

## API chinh

### Auth

Base: `/api/auth`

- `POST /api/auth/register`
- `GET /api/auth/verify-user?token=...`
- `GET /api/auth/resend-verify-user?email=...`
- `POST /api/auth/login`
- `POST /api/auth/refreshToken`
- `POST /api/auth/logout`
- `POST /api/auth/logout-all`

Login payload:

```json
{
  "email": "user@example.com",
  "password": "Password123!"
}
```

### Catalog public

- `GET /api/brands`
- `GET /api/categories`
- `GET /api/products`
- `GET /api/products/{id}`
- `GET /api/stores`
- `GET /api/stores/pickup-candidates?productVariantId=...&quantity=1`
- `GET /api/inventory/variants/{productVariantId}`

Product response co:

- `id`
- `name`
- `slug`
- `thumbnailUrl`
- `pricingType`
- `brand`
- `category`
- `variants`
- `images`

Variant co:

- `id`
- `sku`
- `size`
- `price`
- `goldWeight`
- `laborCost`
- `imageUrl`
- `active`

### Wishlist

Can login.

- `GET /api/wishlist`
- `POST /api/wishlist/items`
- `DELETE /api/wishlist/items/{productId}`

Add wishlist payload:

```json
{
  "productId": "product-id"
}
```

### Cart

Can login.

- `GET /api/cart`
- `POST /api/cart/items`
- `PUT /api/cart/items/{id}`
- `DELETE /api/cart/items/{id}`
- `DELETE /api/cart`

Add cart payload:

```json
{
  "productVariantId": "variant-id",
  "storeId": "store-id",
  "quantity": 1
}
```

Update cart item payload:

```json
{
  "quantity": 2
}
```

Cart item co:

- `id`
- `productVariantId`
- `sku`
- `productName`
- `variantSize`
- `storeId`
- `storeCode`
- `quantity`
- `availableQuantity`
- `availableForCheckout`
- `unitPrice`
- `lineTotal`

### Address

Can login.

- `GET /api/addresses`
- `POST /api/addresses`
- `PUT /api/addresses/{id}`
- `DELETE /api/addresses/{id}`
- `PATCH /api/addresses/{id}/default`

Address payload can co cac field tinh/huyen/xa GHN:

```json
{
  "receiverName": "Nguyen Van A",
  "receiverPhone": "0900000000",
  "provinceId": 202,
  "provinceName": "Ho Chi Minh",
  "districtId": 1442,
  "districtName": "Quan 1",
  "wardCode": "20101",
  "wardName": "Phuong Ben Nghe",
  "detailAddress": "1 Le Loi",
  "default": true
}
```

Neu field boolean response/request khac ten do Java Bean mapping, kiem tra bang API that te.

### Voucher

Validate can login.

- `POST /api/vouchers/validate`

Payload:

```json
{
  "code": "SALE10",
  "subtotal": 1000000,
  "productIds": ["product-id-1", "product-id-2"]
}
```

Response:

```json
{
  "code": "SALE10",
  "valid": true,
  "message": "Voucher hop le",
  "subtotal": 1000000,
  "discountAmount": 100000,
  "totalAfterDiscount": 900000
}
```

### Checkout

Can login.

- `POST /api/checkout/shipping-services/from-cart`
- `POST /api/checkout/shipping-fee`
- `POST /api/checkout/shipping-fee/from-cart`
- `POST /api/checkout`
- `POST /api/checkout/buy-now`
- `POST /api/checkout/from-cart`

Tinh ship tu cart:

```json
{
  "deliveryMethod": "SHIP_TO_HOME",
  "addressId": "address-id",
  "cartItemIds": ["cart-item-id"],
  "weight": 300,
  "length": 10,
  "width": 10,
  "height": 5,
  "serviceId": 53320,
  "serviceTypeId": 2
}
```

Checkout tu cart:

```json
{
  "deliveryMethod": "SHIP_TO_HOME",
  "paymentMethod": "VNPAY",
  "addressId": "address-id",
  "shippingFeeSnapshotId": "snapshot-id",
  "voucherCode": "SALE10",
  "note": "Giao gio hanh chinh",
  "cartItemIds": ["cart-item-id"],
  "removeUnavailableItems": false
}
```

Buy now / checkout direct:

```json
{
  "deliveryMethod": "SHIP_TO_HOME",
  "paymentMethod": "VNPAY",
  "addressId": "address-id",
  "shippingFeeSnapshotId": "snapshot-id",
  "voucherCode": "SALE10",
  "note": "",
  "items": [
    {
      "productVariantId": "variant-id",
      "storeId": "store-id",
      "quantity": 1
    }
  ]
}
```

Payment method:

- `VNPAY`
- `COD`

Delivery method:

- `SHIP_TO_HOME`
- `PICKUP_AT_STORE`

Checkout response:

```json
{
  "orderId": "order-id",
  "paymentId": "payment-id",
  "paymentMethod": "VNPAY",
  "voucherCode": "SALE10",
  "discountAmount": 100000,
  "totalAmount": 930000,
  "paymentUrl": "https://..."
}
```

Neu `paymentUrl` khac null, redirect browser sang URL do.

### Orders

Can login.

- `GET /api/orders/my-orders`
- `GET /api/orders/{id}`
- `PATCH /api/orders/{id}/cancel`
- `GET /api/orders/{id}/ghn-info`

Order response co:

- `id`
- `status`
- `deliveryMethod`
- `subtotal`
- `shippingFee`
- `voucherCode`
- `discountAmount`
- `totalAmount`
- `receiverName`
- `receiverPhone`
- `shippingAddress`
- `items`

Status hien co:

- `PENDING_PAYMENT`
- `PAYMENT_FAILED`
- `PAID`
- `PROCESSING`
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
- `NEED_MANUAL_PROCESSING`

## Admin APIs

Can role `ROLE_ADMIN` hoac `ROLE_STAFF`.

### Catalog admin

- `POST /api/brands`
- `PUT /api/brands/{id}`
- `POST /api/brands/{id}/logo`
- `POST /api/categories`
- `PUT /api/categories/{id}`
- `POST /api/categories/{id}/image`
- `POST /api/products`
- `PUT /api/products/{id}`
- `POST /api/products/{id}/thumbnail`
- `POST /api/products/{id}/images`
- `POST /api/products/{productId}/variants`
- `PUT /api/product-variants/{id}`
- `POST /api/product-variants/{id}/main-image`
- `POST /api/product-variants/{id}/images`
- `POST /api/catalog/import`

### Store / inventory admin

- `POST /api/stores`
- `PUT /api/stores/{id}`
- `POST /api/inventory/import`
- `POST /api/inventory/adjust`
- `POST /api/inventory/lock`
- `POST /api/inventory/release`
- `POST /api/inventory/deduct`

### Voucher admin

- `GET /api/vouchers?includeInactive=true`
- `POST /api/vouchers`
- `PUT /api/vouchers/{id}`

Voucher payload:

```json
{
  "code": "SALE10",
  "name": "Sale 10%",
  "discountType": "PERCENT",
  "discountValue": 10,
  "maxDiscountAmount": 100000,
  "minOrderAmount": 500000,
  "usageLimit": 100,
  "startsAt": "2026-06-01T00:00:00Z",
  "endsAt": "2026-06-30T23:59:59Z",
  "active": true,
  "categoryIds": []
}
```

Discount type:

- `FIXED_AMOUNT`
- `PERCENT`

### Order admin

- `GET /api/orders/admin`
- `GET /api/orders/admin?status=PROCESSING`
- `GET /api/orders/admin/{id}`
- `GET /api/orders/admin/{id}/status-history`
- `PATCH /api/orders/admin/{id}/status`

Update status payload:

```json
{
  "status": "SHIPPING",
  "note": "Da ban giao don cho shipper"
}
```

## Man hinh can lam

### Customer

1. Login / register
2. Product list
3. Product detail
4. Wishlist
5. Cart
6. Address book
7. Checkout
8. Payment result page
9. My orders
10. Order detail

### Admin

1. Admin layout rieng
2. Product management
3. Category management
4. Brand management
5. Store management
6. Inventory management
7. Voucher management
8. Order management

## Nhung phan backend chua nen lam FE sau

Chua can lam sau ngay:

- Return/refund day du.
- Loyalty/VIP.
- OTP pickup/delivery.
- Forgot password.
- Audit log.
- Dashboard bao cao nang cao.

## Luu y quan trong

- Khong checkout anonymous. User phai login.
- Add cart phai gui `productVariantId`, khong gui product id.
- Product detail phai bat user chon size/variant.
- Cart khong freeze price. Gia co the thay doi khi checkout.
- Order da tao moi la snapshot gia/discount.
- Neu checkout tra `unavailableItems`, FE phai hien danh sach item het hang va cho user xoa item unavailable.
- FE khong tu tinh tong tien cuoi cung lam du lieu chinh. Chi hien preview; backend la nguon dung.
- Khong hardcode token/API secret trong FE.

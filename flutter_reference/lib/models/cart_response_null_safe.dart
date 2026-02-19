// Add/update these models so scan and getCart work without "Null is not a subtype of String".
// Backend returns: { "items": [ { "productName", "price", "quantity" }, ... ], "totalAmount": number }

class CartItemResponse {
  final String productName;
  final double price;
  final int quantity;

  CartItemResponse({
    required this.productName,
    required this.price,
    required this.quantity,
  });

  factory CartItemResponse.fromJson(Map<String, dynamic> json) {
    return CartItemResponse(
      productName: (json['productName'] as String?) ?? '',
      price: (json['price'] as num?)?.toDouble() ?? 0.0,
      quantity: (json['quantity'] as int?) ?? 0,
    );
  }
}

class CartResponse {
  final List<CartItemResponse> items;
  final double totalAmount;

  CartResponse({required this.items, required this.totalAmount});

  factory CartResponse.fromJson(dynamic response) {
    final Map<String, dynamic> json = response is Map<String, dynamic>
        ? response
        : (response is String ? {} : {});
    final itemsList = json['items'];
    final List<CartItemResponse> items = itemsList is List
        ? itemsList
            .map((e) => CartItemResponse.fromJson(
                e is Map<String, dynamic> ? e : <String, dynamic>{}))
            .toList()
        : [];
    final totalAmount = (json['totalAmount'] as num?)?.toDouble() ?? 0.0;
    return CartResponse(items: items, totalAmount: totalAmount);
  }
}

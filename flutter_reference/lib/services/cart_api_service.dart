// Reference implementation for shopzy/services/cart_api_service.dart
// Copy or adapt this into your Flutter project. Uses null-safe parsing to avoid
// "type 'Null' is not a subtype of type 'String'" when the backend returns nulls.

import 'dart:convert';
import 'package:http/http.dart' as http;

/// Base URL of your SelfCheckout backend.
/// - Android emulator: http://10.0.2.2:8087
/// - iOS simulator: http://127.0.0.1:8087
/// - Real device: http://<your-pc-ip>:8087
const String kCartBaseUrl = 'http://10.0.2.2:8087';

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

  factory CartResponse.fromJson(Map<String, dynamic> json) {
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

class CartService {
  final String baseUrl;

  CartService({String? baseUrl}) : baseUrl = baseUrl ?? kCartBaseUrl;

  /// Call this when user scans a barcode. Adds product to cart and returns updated cart.
  /// Throws on network/API errors. Use null-safe barcode and userId (your screen already does).
  Future<CartResponse> scanProduct(String barcode, String userId,
      {int quantity = 1}) async {
    final uri = Uri.parse('$baseUrl/api/cart/scan');
    final body = jsonEncode({
      'barcode': barcode,
      'userId': userId,
      'quantity': quantity,
    });

    final response = await http.post(
      uri,
      headers: {'Content-Type': 'application/json'},
      body: body,
    );

    if (response.statusCode != 200) {
      final message = response.body.isNotEmpty
          ? response.body
          : 'Scan failed (${response.statusCode})';
      throw Exception(message);
    }

    final decoded = jsonDecode(response.body);
    if (decoded is! Map<String, dynamic>) {
      throw Exception('Invalid response from server');
    }
    return CartResponse.fromJson(decoded);
  }

  Future<CartResponse> getCart(String userId) async {
    final uri = Uri.parse('$baseUrl/api/cart/$userId');
    final response = await http.get(uri);
    if (response.statusCode != 200) {
      final message = response.body.isNotEmpty
          ? response.body
          : 'Failed to load cart (${response.statusCode})';
      throw Exception(message);
    }
    final decoded = jsonDecode(response.body);
    if (decoded is! Map<String, dynamic>) throw Exception('Invalid response');
    return CartResponse.fromJson(decoded);
  }
}

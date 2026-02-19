// Updated cart_api_service.dart — fix scan response type and checkout URL
// Replace your shopzy cart_api_service with this (and add/update models below).

import '../models/cart_response.dart'; // use CartResponse for scan result
import '../models/scan_request.dart';
import 'api_client.dart';
import 'api_config.dart';

class CartService {
  final ApiClient _apiClient;

  CartService({ApiClient? apiClient})
      : _apiClient = apiClient ?? ApiClient();

  /// Scan product → add to cart. Backend returns CartResponse (items + totalAmount).
  Future<CartResponse> scanProduct(String barcode, String userId,
      {int quantity = 1}) async {
    try {
      final scanRequest = ScanRequest(
        barcode: barcode,
        userId: userId,
        quantity: quantity,
      );

      final response = await _apiClient.post(
        ApiConfig.scanEndpoint,
        body: scanRequest.toJson(),
      );

      // Backend returns { "items": [...], "totalAmount": number } — use CartResponse
      return CartResponse.fromJson(response);
    } catch (e) {
      throw Exception("Scan product failed: $e");
    }
  }

  Future<CartResponse> getActiveCart(String userId) async {
    try {
      final response = await _apiClient.get(
        '${ApiConfig.cartEndpoint}/$userId',
      );
      return CartResponse.fromJson(response);
    } catch (e) {
      throw Exception("Fetch cart failed: $e");
    }
  }

  /// Checkout: backend expects POST /api/cart/{userId}/checkout (userId before checkout)
  Future<void> checkoutCart(String userId) async {
    try {
      await _apiClient.post(
        '${ApiConfig.cartEndpoint}/$userId/checkout',
      );
    } catch (e) {
      throw Exception("Checkout failed: $e");
    }
  }

  void dispose() {
    _apiClient.dispose();
  }
}

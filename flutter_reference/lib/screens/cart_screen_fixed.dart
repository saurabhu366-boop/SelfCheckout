// lib/screens/cart_screen.dart - FIXED
// - Use totalQuantity (sum of item.quantity) so My Cart matches scanner cart badge.
// - Refetch when screen is shown so data is fresh after scanning.

import 'package:flutter/material.dart';
import 'package:shopzy/services/cart_api_service.dart';
import 'package:shopzy/models/cart_response.dart';

class CartScreen extends StatefulWidget {
  const CartScreen({super.key});

  @override
  State<CartScreen> createState() => _CartScreenState();
}

class _CartScreenState extends State<CartScreen> {
  final CartService _apiService = CartService();

  List<CartItemResponse> cartItems = [];
  double totalAmount = 0;
  bool loading = true;

  final String userId = "user1"; // Must match barcode_scanner_screen userId

  /// Total number of units (same as scanner cart badge). Use this everywhere for "cart count".
  int get totalQuantity =>
      cartItems.fold<int>(0, (sum, item) => sum + item.quantity);

  @override
  void initState() {
    super.initState();
    fetchCart();
  }

  Future<void> fetchCart() async {
    try {
      final data = await _apiService.getActiveCart(userId);
      setState(() {
        cartItems = data.items;
        totalAmount = data.totalAmount;
        loading = false;
      });
    } catch (e) {
      debugPrint("Cart fetch error: $e");
      setState(() => loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(cartItems.isEmpty ? 'My Cart' : 'My Cart ($totalQuantity items)'),
        centerTitle: true,
      ),
      body: RefreshIndicator(
        onRefresh: fetchCart,
        child: loading
            ? const Center(child: CircularProgressIndicator())
            : cartItems.isEmpty
                ? ListView(
                    physics: const AlwaysScrollableScrollPhysics(),
                    children: const [
                      SizedBox(height: 100),
                      Center(
                        child: Text(
                          "Your cart is empty",
                          style: TextStyle(fontSize: 18),
                        ),
                      ),
                    ],
                  )
                : ListView(
                    physics: const AlwaysScrollableScrollPhysics(),
                    children: [
                      ...List.generate(cartItems.length, (index) {
                        final item = cartItems[index];
                        return Card(
                          margin: const EdgeInsets.symmetric(
                              horizontal: 16, vertical: 6),
                          child: ListTile(
                            title: Text(
                              item.productName,
                              style: const TextStyle(
                                  fontWeight: FontWeight.bold),
                            ),
                            subtitle: Text("Quantity: ${item.quantity}"),
                            trailing: Text(
                              "₹${item.price}",
                              style: const TextStyle(
                                  fontWeight: FontWeight.bold),
                            ),
                          ),
                        );
                      }),
                      Container(
                        padding: const EdgeInsets.all(20),
                        width: double.infinity,
                        color: Colors.black,
                        child: Text(
                          "Total Amount: ₹$totalAmount",
                          style: const TextStyle(
                            color: Colors.white,
                            fontSize: 20,
                            fontWeight: FontWeight.bold,
                          ),
                          textAlign: TextAlign.center,
                        ),
                      ),
                    ],
                  ),
      ),
    );
  }
}

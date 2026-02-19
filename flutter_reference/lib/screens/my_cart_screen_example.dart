// Example: My Cart screen — use PROPERTY ACCESS, not map access.
// CartItemResponse is a class, so use item.productName, item.price, item.quantity.

import 'package:flutter/material.dart';
// import your CartResponse / CartItemResponse and cart service

// WRONG (causes NoSuchMethodError: has no instance method '[]'):
//   Text(item['productName'])
//   Text('${item['price']}')
//   Text('${item['quantity']}')

// CORRECT:
//   Text(item.productName)
//   Text('${item.price}')
//   Text('${item.quantity}')

class MyCartScreenExample extends StatelessWidget {
  final List<CartItemResponse> items;
  final double totalAmount;

  const MyCartScreenExample({
    super.key,
    required this.items,
    required this.totalAmount,
  });

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('My Cart')),
      body: ListView.builder(
        itemCount: items.length,
        itemBuilder: (context, index) {
          final item = items[index];
          return ListTile(
            title: Text(item.productName),
            subtitle: Text('Qty: ${item.quantity}'),
            trailing: Text('₹${item.price.toStringAsFixed(1)}'),
          );
        },
      ),
      bottomNavigationBar: Container(
        color: Colors.black,
        padding: const EdgeInsets.all(16),
        child: Text(
          'Total Amount: ₹$totalAmount',
          style: const TextStyle(color: Colors.white, fontSize: 18),
        ),
      ),
    );
  }
}

// Placeholder so the file is valid; use your real CartItemResponse from models.
class CartItemResponse {
  final String productName;
  final double price;
  final int quantity;
  CartItemResponse({
    required this.productName,
    required this.price,
    required this.quantity,
  });
}

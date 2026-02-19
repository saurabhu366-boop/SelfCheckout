# Cart quantity: My Cart vs Scanner cart badge

## Why numbers differed (3 vs 8)

- **3** = number of **line items** (different products): `cartItems.length`
- **8** = **total quantity** (all units): sum of each line’s `item.quantity` (e.g. 2 + 3 + 3 = 8)

Use **one** definition everywhere so the badge and My Cart match.

## Use "total quantity" everywhere

Compute it the same way in both places:

```dart
// Total number of units (what the scanner cart badge should show)
int totalQuantity = cartItems.fold<int>(0, (sum, item) => sum + item.quantity);
```

- **My Cart screen:** Use `totalQuantity` for the title, e.g. "My Cart (8 items)".
- **Cart icon badge (scanner/cart icon):** Use the same `totalQuantity`, not `cartItems.length`.

## Where the cart icon gets its count

- If the icon uses **local state** updated after each scan: keep that state as **total quantity** (sum of `item.quantity` from the last `CartResponse`).
- If the icon **fetches cart** when the screen loads: use `data.items.fold<int>(0, (s, i) => s + i.quantity)` and show that.
- Use the same `userId` as the scanner and My Cart (e.g. `"user1"`).

## Refreshing My Cart

- My Cart fetches in `initState`, so opening the screen shows the latest cart.
- Pull-to-refresh is added so the user can refresh after scanning.
- If the cart icon uses its own fetch, it should refetch when the scanner returns (e.g. after `scanProduct` you update the same cart state the icon reads).
